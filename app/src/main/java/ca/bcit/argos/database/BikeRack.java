package ca.bcit.argos.database;

public class BikeRack {
    private int ID;
    private int streetNumber;
    private String streetName;
    private String streetSide;
    private String skytrainStationName;
    private String BIA;
    private int numberOfRacks;
    private String yearInstalled;

    public BikeRack() {

    }
    public BikeRack(int id, int snu, String sna, String ss, String ssn, String bia, int nor, String yi) {
        this.ID = id;
        this.streetNumber = snu;
        this.streetName = sna;
        this.streetSide = ss;
        this.skytrainStationName = ssn;
        this.BIA = bia;
        this.numberOfRacks = nor;
        this.yearInstalled = yi;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetName(String streetName){
        this.streetName = streetName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetSide(String streetSide) {
        this.streetSide = streetSide;
    }

    public String getStreetSide() {
        return streetSide;
    }

    public String getSkytrainStationName() {
        return skytrainStationName;
    }

    public void setSkytrainStationName(String skytrainStationName) {
        this.skytrainStationName = skytrainStationName;
    }

    public String getBIA() {
        return BIA;
    }

    public void setBIA(String BIA) {
        this.BIA = BIA;
    }

    public int getNumberOfRacks() {
        return numberOfRacks;
    }

    public void setNumberOfRacks(int numberOfRacks) {
        this.numberOfRacks = numberOfRacks;
    }

    public String getYearInstalled() {
        return yearInstalled;
    }

    public void setYearInstalled(String yearInstalled) {
        this.yearInstalled = yearInstalled;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
