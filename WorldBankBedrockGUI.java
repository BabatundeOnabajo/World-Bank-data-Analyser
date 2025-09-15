import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class WorldBankBedrockGUI extends JFrame {
    private JComboBox<String> countryCombo;
    private JComboBox<String> indicatorCombo;
    private JSpinner startYearSpinner;
    private JSpinner endYearSpinner;
    private JTextField aiQuestionField;
    private JTextArea dataTextArea;
    private JTextArea aiAnalysisTextArea;
    private JButton fetchButton;
    private JButton clearButton;
    private JLabel statusLabel;
    
    private WorldBankAPI worldBankAPI;
    private BedrockService bedrockService;
    
    private Map<String, String> countryCodes;
    private Map<String, String> indicators;
    
    public WorldBankBedrockGUI() {
        initializeServices();
        initializeData();
        setupUI();
    }
    
    private void initializeServices() {
        worldBankAPI = new WorldBankAPI();
        bedrockService = new BedrockService();
    }
    
    private void initializeData() {
        // Country codes
        countryCodes = new HashMap<>();
        countryCodes.put("United Kingdom", "GBR");
        countryCodes.put("United States", "USA");
        countryCodes.put("Germany", "DEU");
        countryCodes.put("France", "FRA");
        countryCodes.put("Japan", "JPN");
        countryCodes.put("China", "CHN");
        countryCodes.put("India", "IND");
        countryCodes.put("Brazil", "BRA");
        countryCodes.put("Canada", "CAN");
        countryCodes.put("Australia", "AUS");
        countryCodes.put("Italy", "ITA");
        countryCodes.put("Spain", "ESP");
        countryCodes.put("South Korea", "KOR");
        countryCodes.put("Mexico", "MEX");
        countryCodes.put("Netherlands", "NLD");
        
        // Indicators
        indicators = new HashMap<>();
        indicators.put("GDP (current US$)", "NY.GDP.MKTP.CD");
        indicators.put("GDP growth (annual %)", "NY.GDP.MKTP.KD.ZG");
        indicators.put("GDP per capita (current US$)", "NY.GDP.PCAP.CD");
        indicators.put("Inflation, consumer prices (annual %)", "FP.CPI.TOTL.ZG");
        indicators.put("Population, total", "SP.POP.TOTL");
        indicators.put("Unemployment, total (% of labor force)", "SL.UEM.TOTL.ZS");
    }
    
    private void setupUI() {
        setTitle("World Bank Data Analyzer with AWS Bedrock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("World Bank Data Analyzer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Input Panel
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        
        // Results Panel
        JPanel resultsPanel = createResultsPanel();
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Status Bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusLabel, BorderLayout.SOUTH);
        
        // Set size and center
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Query Parameters"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Country selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Country:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        countryCombo = new JComboBox<>(countryCodes.keySet().toArray(new String[0]));
        countryCombo.setSelectedItem("United Kingdom");
        panel.add(countryCombo, gbc);
        
        // Indicator selection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Indicator:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        indicatorCombo = new JComboBox<>(indicators.keySet().toArray(new String[0]));
        indicatorCombo.setSelectedItem("GDP (current US$)");
        panel.add(indicatorCombo, gbc);
        
        // Year range
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Start Year:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        SpinnerModel startYearModel = new SpinnerNumberModel(2020, 1960, 2023, 1);
        startYearSpinner = new JSpinner(startYearModel);
        panel.add(startYearSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("End Year:"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        SpinnerModel endYearModel = new SpinnerNumberModel(2023, 1960, 2023, 1);
        endYearSpinner = new JSpinner(endYearModel);
        panel.add(endYearSpinner, gbc);
        
        // AI Question
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("AI Question (optional):"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 4;
        aiQuestionField = new JTextField(30);
        panel.add(aiQuestionField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        
        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this::fetchData);
        buttonPanel.add(fetchButton);
        
        clearButton = new JButton("Clear Results");
        clearButton.addActionListener(e -> clearResults());
        buttonPanel.add(clearButton);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        // Data Display
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(new JLabel("Data:"), BorderLayout.NORTH);
        
        dataTextArea = new JTextArea();
        dataTextArea.setEditable(false);
        dataTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane dataScrollPane = new JScrollPane(dataTextArea);
        dataPanel.add(dataScrollPane, BorderLayout.CENTER);
        
        // AI Analysis Display
        JPanel aiPanel = new JPanel(new BorderLayout());
        aiPanel.add(new JLabel("AI Analysis:"), BorderLayout.NORTH);
        
        aiAnalysisTextArea = new JTextArea();
        aiAnalysisTextArea.setEditable(false);
        aiAnalysisTextArea.setLineWrap(true);
        aiAnalysisTextArea.setWrapStyleWord(true);
        JScrollPane aiScrollPane = new JScrollPane(aiAnalysisTextArea);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);
        
        panel.add(dataPanel);
        panel.add(aiPanel);
        
        return panel;
    }
    
    private void fetchData(ActionEvent e) {
        // Get parameters
        String country = countryCodes.get(countryCombo.getSelectedItem());
        String indicator = indicators.get(indicatorCombo.getSelectedItem());
        int startYear = (int) startYearSpinner.getValue();
        int endYear = (int) endYearSpinner.getValue();
        String aiQuestion = aiQuestionField.getText();
        
        // Validate years
        if (startYear > endYear) {
            JOptionPane.showMessageDialog(this, 
                "Start year must be before end year", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Disable button during fetch
        fetchButton.setEnabled(false);
        statusLabel.setText("Fetching data...");
        
        // Use SwingWorker for background processing
        SwingWorker<WorldBankData, Void> worker = new SwingWorker<>() {
            @Override
            protected WorldBankData doInBackground() throws Exception {
                return worldBankAPI.fetchData(country, indicator, startYear, endYear);
            }
            
            @Override
            protected void done() {
                try {
                    WorldBankData data = get();
                    displayData(data);
                    
                    // Analyze with AI if question provided
                    if (!aiQuestion.isEmpty() && bedrockService.isAvailable()) {
                        analyzeWithBedrock(data, aiQuestion);
                    } else if (!aiQuestion.isEmpty()) {
                        aiAnalysisTextArea.setText("AWS Bedrock is not configured. " +
                            "Please set up AWS credentials to use AI analysis.");
                    }
                    
                    statusLabel.setText("Data fetched successfully");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(WorldBankBedrockGUI.this,
                        "Error fetching data: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Error fetching data");
                } finally {
                    fetchButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayData(WorldBankData data) {
        dataTextArea.setText("");
        
        if (data == null || data.getEntries().isEmpty()) {
            dataTextArea.setText("No data found for the specified parameters.");
            return;
        }
        
        // Display header
        dataTextArea.append("Country: " + data.getCountryName() + "\n");
        dataTextArea.append("Indicator: " + indicatorCombo.getSelectedItem() + "\n");
        dataTextArea.append("-".repeat(50) + "\n\n");
        
        // Display data entries
        for (WorldBankData.DataEntry entry : data.getEntries()) {
            String formattedValue = formatValue(entry.getValue(), 
                (String) indicatorCombo.getSelectedItem());
            dataTextArea.append(entry.getYear() + ": " + formattedValue + "\n");
        }
    }
    
    private String formatValue(Double value, String indicatorName) {
        if (value == null) return "No data";
        
        if (indicatorName.contains("GDP") && !indicatorName.contains("%")) {
            if (value >= 1e12) {
                return String.format("$%.2f trillion", value / 1e12);
            } else if (value >= 1e9) {
                return String.format("$%.2f billion", value / 1e9);
            } else {
                return String.format("$%,.0f", value);
            }
        } else if (indicatorName.contains("%")) {
            return String.format("%.2f%%", value);
        } else if (indicatorName.contains("Population")) {
            return String.format("%,.0f", value);
        } else {
            return String.format("%,.2f", value);
        }
    }
    
    private void analyzeWithBedrock(WorldBankData data, String question) {
        statusLabel.setText("Analyzing with AI...");
        aiAnalysisTextArea.setText("Analyzing data with AI...");
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return bedrockService.analyzeData(data, question, 
                    (String) indicatorCombo.getSelectedItem());
            }
            
            @Override
            protected void done() {
                try {
                    String analysis = get();
                    aiAnalysisTextArea.setText(analysis);
                    statusLabel.setText("Analysis complete");
                } catch (Exception ex) {
                    aiAnalysisTextArea.setText("Error during AI analysis: " + 
                        ex.getMessage());
                    statusLabel.setText("AI analysis failed");
                }
            }
        };
        
        worker.execute();
    }
    
    private void clearResults() {
        dataTextArea.setText("");
        aiAnalysisTextArea.setText("");
        aiQuestionField.setText("");
        statusLabel.setText("Ready");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WorldBankBedrockGUI gui = new WorldBankBedrockGUI();
            gui.setVisible(true);
        });
    }
}
