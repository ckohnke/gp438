public class MPoint {
  // from xyz coord
  MPoint(int stationID, double x, double y, double z){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.z = z;
  }
   
  MPoint(int stationID, double x, double y, int UTMzone){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.UTMzone = UTMzone;
  }

  MPoint(int stationID, double x, double y, double z, int UTMzone){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.z = z;
    this.UTMzone = UTMzone;
  }

  // from xy coord
  MPoint(int stationID, double x, double y, boolean temp){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
  }
  
  MPoint(int stationID, double lat, double lon){
    this.stationID = stationID;
    this.lat = lat; 
    this.lon = lon;
  }

  public double xyDistance(MPoint p){
    return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
  }

  public double xyzDistance(MPoint p){
    return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y)+(z-p.z)*(z-p.z));
  }
  
  public int stationID;
  public double x, y, z;
  public double lat, lon;
  public int UTMzone;  
  public boolean selected;
}

