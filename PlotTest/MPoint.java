public class MPoint {
  // from xyz coord
  MPoint(int stationID, double x, double y, double z){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
    this.z = z;
  }
    
  // from xy coord
  MPoint(int stationID, double x, double y){
    this.stationID = stationID;
    this.x = x; 
    this.y = y;
  }
    
  public int stationID;
  public double x, y, z;
  public double lat, lon;
  public boolean selected;
}

