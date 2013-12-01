import java.util.*;

public class MPoint {
  // from xyz coord
  MPoint(int stationID, double x, double y, double z, boolean UTM){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.z = z;
  }
   
  MPoint(int stationID, double x, double y, int UTMzone, boolean UTM){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.UTMzone = UTMzone;
  }

  MPoint(int stationID, double x, double y, double z, int UTMzone, boolean UTM){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.z = z;
    this.UTMzone = UTMzone;
  }

  // from xy coord
  MPoint(int stationID, double x, double y, boolean temp, boolean UTM){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
  }
  
  MPoint(int stationID, double lat, double lon){
    this.stationID = stationID;
    this.lat = lat; 
    this.lon = lon;
  }

  MPoint(int stationID, double lat, double lon, double z){
    this.stationID = stationID;
    this.lat = lat; 
    this.lon = lon;
    this.z = z;
  }


  public double xyDist(MPoint p){
    return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
  }

  public double xDist(MPoint p){
    return Math.sqrt((x-p.x)*(x-p.x));
  }

  public double yDist(MPoint p){
    return Math.sqrt((y-p.y)*(y-p.y));
  }

  public double zDist(MPoint p){
    return Math.sqrt((z-p.z)*(z-p.z));
  }

  public double xyzDist(MPoint p){
    return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y)+(z-p.z)*(z-p.z));
  }

  public int stationID;
  public double x, y, z;
  public double lat, lon;
  public int UTMzone;  
  public boolean selected;
}

class MPointComp implements Comparator<MPoint>{

  //@Override
  public int compare(MPoint p1, MPoint p2) {
    if(p1.stationID > p2.stationID){
       return 1;
    } else {
       return -1;
    }
  }
}
