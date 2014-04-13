package novice;
import java.util.*;

/**
 * Map-Point object.
 * 
 * <p> Various attributes of a mapped point of a survey.
 *
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class MPoint {
  /**
   * Instantiates a new map point.
   *
   * @param x the Easting in UTM meters
   * @param y the Northing in UTM meters.
   * @param UTM a boolean to distinguish constructors.
   */
  MPoint(double x, double y, boolean UTM) {
    this.x = x;
    this.y = y;
  }

  /**
   * Instantiates a new map point.
   *
   * @param stationID the station number.
   * @param x the Easting in UTM meters.
   * @param y the Northing in UTM meters.
   * @param z the elevation in meters.
   * @param UTM a boolean to distinguish constructors.
   */
  MPoint(int stationID, double x, double y, double z, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Instantiates a new map point.
   *
   * @param stationID the station number
   * @param x the Easting in UTM meters.
   * @param y the Northing in UTM meters.
   * @param UTMzone the UTM Zone number.
   * @param UTM the boolean to distinguish constructors.
   */
  MPoint(int stationID, double x, double y, int UTMzone, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.UTMzone = UTMzone;
  }

  /**
   * Instantiates a new map point.
   *
   * @param stationID the station number
   * @param x the Easting in UTM meters.
   * @param y the Northing in UTM meters.
   * @param z the Elevation in meters.
   * @param UTMzone the UTM Zone number.
   * @param UTM the boolean to distinguish constructors.
   */
  MPoint(int stationID, double x, double y, double z, int UTMzone, boolean UTM) {
    this.stationID = stationID;
    this.x = x;
    this.y = y;
    this.z = z;
    this.UTMzone = UTMzone;
  }

  /**
   * Instantiates a new map point.
   *
   * @param stationID the station number.
   * @param lat the Latitude in decimal form.
   * @param lon the Longitude in decimal form.
   */
  MPoint(int stationID, double lat, double lon) {
    this.stationID = stationID;
    this.lat = lat;
    this.lon = lon;
  }

  /**
   * Instantiates a new map point.
   *
   * @param stationID the station number.
   * @param lat the Latitude in decimal form.
   * @param lon the Longitude in decimal form.
   * @param z the Elevation in meters.
   */
  MPoint(int stationID, double lat, double lon, double z) {
    this.stationID = stationID;
    this.lat = lat;
    this.lon = lon;
    this.z = z;
  }

  /**
   * XY distance.
   *
   * @param p the map point to be compared.
   * @return the xy distance (in meters) between two map points. 
   */
  public double xyDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
  }

  /**
   * X dist.
   *
   * @param p the map point to be compared.
   * @return the x distance (in meters) between two map points. 
   */
  public double xDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x));
  }

  /**
   * Y distance.
   *
   * @param p the map point to be compared.
   * @return the y distance (in meters) between two map points. 
   */
  public double yDist(MPoint p) {
    return Math.sqrt((y - p.y) * (y - p.y));
  }

  /**
   * Z distance.
   *
   * @param p the map point to be compared.
   * @return the z distance (in meters) between two map points. 
   */
  public double zDist(MPoint p) {
    return Math.sqrt((z - p.z) * (z - p.z));
  }

  /**
   * XYZ distance.
   *
   * @param p the map point to be compared.
   * @return the xyz distance (in meters) between two map points. 
   */
  public double xyzDist(MPoint p) {
    return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
        + (z - p.z) * (z - p.z));
  }

  /**
   * Midpoint.
   *
   * @param p the map point to be compared.
   * @return the Map Point between two map points. 
   */
  public MPoint midpoint(MPoint p){
    return new MPoint(0,0.5*xDist(p),0.5*yDist(p),0.5*zDist(p), true);
  }

  /**
   * Gets the station.
   *
   * @return the station number.
   */
  public int getStation() {
    return stationID;
  }

  /**
   * Gets the UTM easting (m).
   *
   * @return the UTM easting (m)
   */
  public double getUTMX() {
    return x;
  }

  /**
   * Gets the UTM northing (m).
   *
   * @return the UTM northing (m)
   */
  public double getUTMY() {
    return y;
  }

  /**
   * Gets the Elevation (m).
   *
   * @return the elevation (m)
   */
  public double getElev() {
    return z;
  }

  /**
   * Sets the elevation (m).
   *
   * @param d the new elevation.
   */
  public void setElev(double d) {
    z=d;
  }

  /**
   * Gets the latitude (decimal).
   *
   * @return the latitude (decimal)
   */
  public double getLat() {
    return lat;
  }

  /**
   * Gets the longitude (decimal).
   *
   * @return the longitude (decimal)
   */
  public double getLon() {
    return lon;
  }

  /**
   * Gets the UTM zone.
   *
   * @return the UTM zone
   */
  public double getUTMZone() {
    return UTMzone;
  }

  /**
   * Sets the UTM Easting (m).
   *
   * @param x the new UTM Easting (m)
   */
  public void setUTMX(double x) {
    this.x = x;
  }

  /**
   * Sets the UTM Northing (m).
   *
   * @param y the new UTM Northing (m)
   */
  public void setUTMY(double y) {
    this.y = y;
  }
  
  /**
   * Sets the UTM zone.
   *
   * @param UTM the new UTM zone
   */
  public void setZone(int UTM){
    this.UTMzone = UTM;
  }
  
  /** The station number. */
  private int stationID;
  
  /**  The easting, northing and elevation. */
  private double x, y, z;
  
  /** The latitude and longitude. */
  private double lat, lon;
  
  /** The UTM zone. */
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
