package novice;
import java.util.*;

public class MPoint {
  // from xyz coord
  MPoint(double x, double y, boolean UTM) {
    this.x = x;
    this.y = y;
  }

  // from xyz coord
  MPoint(int stationID, double x, double y, double z, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  MPoint(int stationID, double x, double y, int UTMzone, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.UTMzone = UTMzone;
  }

  MPoint(int stationID, double x, double y, double z, int UTMzone, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.z = z;
    this.UTMzone = UTMzone;
  }

  // from xy coord
  MPoint(int stationID, double x, double y, boolean temp, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
  }

  MPoint(int stationID, double lat, double lon) {
    this.stationID = stationID;
    this.lat = lat;
    this.lon = lon;
  }

  MPoint(int stationID, double lat, double lon, double z) {
    this.stationID = stationID;
    this.lat = lat;
    this.lon = lon;
    this.z = z;
  }

  public double xyDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
  }

  public double xDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x));
  }

  public double yDist(MPoint p) {
    return Math.sqrt((y - p.y) * (y - p.y));
  }

  public double zDist(MPoint p) {
    return Math.sqrt((z - p.z) * (z - p.z));
  }

  public double xyzDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
        + (z - p.z) * (z - p.z));
  }

  public MPoint midpoint(MPoint p){
    return new MPoint(0,0.5*xDist(p),0.5*yDist(p),0.5*zDist(p), true);
  }

  public int getStation() {
    return stationID;
  }

  public double getUTMX() {
    return x;
  }

  public double getUTMY() {
    return y;
  }

  public double getElev() {
    return z;
  }

  public void setElev(double d) {
    z=d;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public double getUTMZone() {
    return UTMzone;
  }

  public void setUTMX(double x) {
    this.x = x;
  }

  public void setUTMY(double y) {
    this.y = y;
  }
  
  public void setZone(int UTM){
    this.UTMzone = UTM;
  }
  
  private int stationID;
  private double x, y, z;
  private double lat, lon;
  private int UTMzone;

}

class MPointComp implements Comparator<MPoint> {

  // @Override
  public int compare(MPoint p1, MPoint p2) {
    if (p1.getStation() > p2.getStation()) {
      return 1;
    } else {
      return -1;
    }
  }
}
