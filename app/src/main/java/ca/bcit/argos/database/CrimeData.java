package ca.bcit.argos.database;

public class CrimeData {
    private int ID;
    private int count;
    private String hundredBlock;
    private String neighbourhood;
    private double longitude;
    private double latitude;

    public CrimeData() {

    }
    public CrimeData(int id, int count, String hb, String n, double lon, double lat) {
        this.ID = id;
        this.count = count;
        this.hundredBlock = hb;
        this.neighbourhood = n;
        this.longitude = lon;
        this.latitude = lat;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getHundredBlock() {
        return hundredBlock;
    }

    public void setHundredBlock(String hundredBlock) {
        this.hundredBlock = hundredBlock;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
