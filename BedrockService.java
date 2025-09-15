// BedrockService.java
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;

public class BedrockService {
    private BedrockRuntimeClient bedrockClient;
    private final Gson gson;
    private boolean available;
    
    public BedrockService() {
        this.gson = new Gson();
        initializeBedrock();
    }
    
    private void initializeBedrock() {
        try {
            bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)  // Change to your region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
            available = true;
        } catch (Exception e) {
            System.err.println("Failed to initialize Bedrock: " + e.getMessage());
            available = false;
        }
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public String analyzeData(WorldBankData data, String question, String indicatorName) 
            throws Exception {
        if (!available) {
            throw new RuntimeException("Bedrock service is not available");
        }
        
        // Prepare data summary
        StringBuilder dataSummary = new StringBuilder();
        dataSummary.append("Country: ").append(data.getCountryName()).append("\n");
        dataSummary.append("Indicator: ").append(indicatorName).append("\n\n");
        
        for (WorldBankData.DataEntry entry : data.getEntries()) {
            if (entry.getValue() != null) {
                dataSummary.append(entry.getYear()).append(": ")
                    .append(entry.getValue()).append("\n");
            }
        }
        
        // Create prompt
        String prompt = String.format(
            "\n\nHuman: I have the following World Bank data:\n\n%s\n\n" +
            "User Question: %s\n\n" +
            "Please provide a clear, insightful analysis based on this data.\n\n" +
            "Assistant:",
            dataSummary.toString(), question
        );
        
        // Create request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("max_tokens_to_sample", 500);
        requestBody.addProperty("temperature", 0.7);
        
        // Create invoke request
        InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
            .modelId("anthropic.claude-v2")
            .body(SdkBytes.fromString(gson.toJson(requestBody), StandardCharsets.UTF_8))
            .build();
        
        // Invoke model
        InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
        
        // Parse response
        String responseBody = response.body().asUtf8String();
        JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
        
        return responseJson.get("completion").getAsString().trim();
    }
}
