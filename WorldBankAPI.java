// WorldBankAPI.java
import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldBankAPI {
    private static final String BASE_URL = "https://api.worldbank.org/v2/country";
    private final Gson gson;
    
    public WorldBankAPI() {
        this.gson = new Gson();
    }
    
    public WorldBankData fetchData(String countryCode, String indicator, 
                                   int startYear, int endYear) throws Exception {
        String urlString = String.format("%s/%s/indicator/%s?date=%d:%d&format=json",
            BASE_URL, countryCode, indicator, startYear, endYear);
        
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + 
                conn.getResponseCode());
        }
        
        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String output;
        
        while ((output = br.readLine()) != null) {
            response.append(output);
        }
        conn.disconnect();
        
        return parseResponse(response.toString());
    }
    
    private WorldBankData parseResponse(String jsonResponse) {
        JsonArray jsonArray = JsonParser.parseString(jsonResponse).getAsJsonArray();
        
        if (jsonArray.size() < 2) {
            return null;
        }
        
        JsonArray dataArray = jsonArray.get(1).getAsJsonArray();
        WorldBankData worldBankData = new WorldBankData();
        
        for (JsonElement element : dataArray) {
            JsonObject obj = element.getAsJsonObject();
            
            String countryName = obj.getAsJsonObject("country")
                .get("value").getAsString();
            worldBankData.setCountryName(countryName);
            
            String year = obj.get("date").getAsString();
            JsonElement valueElement = obj.get("value");
            
            Double value = null;
            if (!valueElement.isJsonNull()) {
                value = valueElement.getAsDouble();
            }
            
            worldBankData.addEntry(year, value);
        }
        
        // Sort by year
        Collections.sort(worldBankData.getEntries(), 
            (a, b) -> b.getYear().compareTo(a.getYear()));
        
        return worldBankData;
    }
}

