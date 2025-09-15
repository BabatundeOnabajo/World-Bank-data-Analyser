// WorldBankData.java
import java.util.ArrayList;
import java.util.List;

public class WorldBankData {
    private String countryName;
    private List<DataEntry> entries;
    
    public WorldBankData() {
        this.entries = new ArrayList<>();
    }
    
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public void addEntry(String year, Double value) {
        entries.add(new DataEntry(year, value));
    }
    
    public List<DataEntry> getEntries() {
        return entries;
    }
    
    public static class DataEntry {
        private final String year;
        private final Double value;
        
        public DataEntry(String year, Double value) {
            this.year = year;
            this.value = value;
        }
        
        public String getYear() {
            return year;
        }
        
        public Double getValue() {
            return value;
        }
    }
}
