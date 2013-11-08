///////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.util.*;
import java.xml.*;

public class Waypoints{
  public ArrayList<MPoint> _gps;

  public static void main(String[] args){
    System.out.println("GPS TEST START");
    File input = new File("/home/colton/Documents/School/SrDesign/PlotTest/gps_input_test1.txt");
    Waypoints w = new Waypoints(input);
    for(int i=0; i< w._gps.size(); ++i){
      MPoint p = w._gps.get(i);
      System.out.println("Station: " + p.stationID+ " lat: " + p.lat + " lon: " + p.lon + " x: " + p.x + " y: " + p.y + " z: " + p.z;);
    }
    System.out.println("GPS TEST FINISH");    
  }

  public Waypoints(File f){
    _gps = new ArrayList<MPoint>(0);
    readLatLonFromTSV(f);
    latLonToUTM();
  }

  public double degToRad(double deg){
    return (deg / 180 * PI);
  }

  public double radToDeg(double rad){
    return (rad / PI * 180);
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
        MPoint p = new MPoint(stationID, x, y, z);
        _gps.add(p);
      }
      s.close();
    } catch(IOException ex){
      System.out.println(ex);  
    }
  }

  public void readLatLonFromTSV(File f){
    try{
      Scanner s = new Scanner(f);
      s.nextLine(); //header skip=1;
      while(s.hasNext()){
        int stationID = s.nextInt();
        double lat = s.nextDouble();
        double lon = s.nextDouble();
        System.out.println("ID: " + stationID + " lat: " + lat + " lon: " + lon);
        MPoint p = new MPoint(stationID, lat, lon);
        _gps.add(p);
        System.out.println(_gps.get(_gps.size()-1).stationID);
      }
      s.close();
    } catch(IOException ex){
      System.out.println(ex);  
    }
  }
  
  public void readLatLongFromXML(File f){
    try {

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(f);
      doc.getDocumentElement().normalize();

      System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
      NodeList nList = doc.getElementsByTagName("test");
      System.out.println("-----------------------");

      for (int temp = 0; temp < nList.getLength(); temp++) {

        Node nNode = nList.item(temp);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {

          Element eElement = (Element) nNode;

          System.out.println("First Name : " + getTagValue("id", eElement));
          System.out.println("Last Name : " + getTagValue("result", eElement));

       }
    }
    
  } catch (Exception e) {
    e.printStackTrace();
  }
    
  }

  public void latLonToUTM(){
    for(int i=0; i<_gps.size(); ++i){
      MPoint p = _gps.get(i);
      double lat = p.lat;
      double lon = p.lon;
      int UTMzone = (int)(Math.floor((lon+180.0)/6)+1);
      double a = 6378.137;
      double f = 1.0/298.257223563;
      double n = f/(2.0-f);
      double e0 = 500.0;
      double n0 = 0;
      double k0 = 0.9996;
      double aa = a/(1.0+n)*(1.0+n*n*(1.0/4.0+n*n/64.0));
      double a1 = n*(0.5-n*(2.0/3.0-n*5.0/16.0));
      double a2 = n*n*(13.0/48.0-n*3.0/5.0);
      double a3 = n*n*n*61.0/240.0;
      double st = 2.0*Math.sqrt(n)/(1.0+n);
      double lon0 = -183.0+(UTMzone*6.0); // reference longitude for arbitrary UTM Zone
      lon -= lon0;
      lat *= PI/180.0;
      lon *= PI/180.0;
      double t = Math.sinh(atanh(Math.sin(lat))-st*atanh(st*Math.sin(lat)));
      double ep = Math.atan(t/Math.cos(lon));
      double np = atanh(Math.sin(lon)/Math.sqrt(1.0+t*t));
      double sx  = a1*Math.cos(2.0*ep)*Math.sinh(2.0*np);
      sx += a2*Math.cos(4.0*ep)*Math.sinh(4.0*np);
      sx += a3*Math.cos(6.0*ep)*Math.sinh(6.0*np);
      double sy  = a1*Math.sin(2.0*ep)*Math.cosh(2.0*np);
      sy += a2*Math.sin(4.0*ep)*Math.cosh(4.0*np);
      sy += a3*Math.sin(6.0*ep)*Math.cosh(6.0*np);
      double x = e0+k0*aa*(np+sx);
      double y = n0+k0*aa*(ep+sy);
      x *= 1000.0;
      y *= 1000.0;
      p.x = x; p.y = y; p.UTMzone = UTMzone;
    }
  }

  public void UTMToLatLong(){
    
  }
  
  public void readUTMFromCSV(File f){
    try{
      Scanner s = new Scanner(f);
      s.useDelimeter(",");
      s.nextLine(); // header skip = 1
      while(s.hasNext()){
        int stationID = s.nextInt();
        double x = s.nextDouble();
        double y = s.nextDouble();
        double z = s.nextDouble();
        MPoint p = new MPoint(stationID, x, y, z);
        _gps.add(p);
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

  protected double atanh(double x){
    return 0.5*Math.log((1.0+x)/(1.0-x));
  }

  private final double PI = Math.PI;

}
