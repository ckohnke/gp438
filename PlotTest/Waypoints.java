/*
public class Main{
  public static void main(String[] args){
    
  }
} 
*/

///////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.util.ArrayList;
import java.util.*;

public class Waypoints{
  public ArrayList<MPoint> _gps;
  private int UTMzone;

  public static void main(String[] args){
    System.out.println("GPS TEST START");
    
    System.out.println("GPS TEST FINISH");    
  }

  public Waypoints(File f){
    _gps = new ArrayList<MPoint>(0);
    readUTMFromTSV(f);
  }

  public void readUTMFromTSV(File f){
    try{
      Scanner s = new Scanner(f);
      s.nextLine(); // header skip = 1
      while(s.hasNext()){
        int stationID = s.nextInt();
        double x = s.nextDouble();
        double y = s.nextDouble();
        double z = s.nextDouble();
        System.out.println("ID: " + stationID + " x: " + x + " y: " + y + " z: " + z);
        MPoint p = new MPoint(stationID, x, y, z);
        _gps.add(p);
        System.out.println(_gps.get(_gps.size()-1).stationID);
      }
      s.close();
    } catch(IOException ex){
      System.out.println(ex);  
    }

  }

  public void exportToCSV(File f){
    try{
      if (f!=null) {
        String filename = f.getAbsolutePath();
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write("Station,Easting,Northing,Elevation");
        w.newLine();
        for(int i=0; i<_gps.size(); ++i){
          MPoint p = _gps.get(i);
          w.write(p.stationID + "," + p.x + "," + p.y + "," + p.z);
          w.newLine();
        }
        w.close();
      }
    } catch(IOException ex){
      System.out.println(ex);  
    }
  }

}
