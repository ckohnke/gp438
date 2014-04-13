package novice;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import edu.mines.jtk.io.ArrayInputStream;
import edu.mines.jtk.dsp.Sampling;

// Reads gridFloat files for elevation values

public class NedReader{

  private NedReader(){}

  public static NedFile importNed(File f) throws IOException{
    String fn = f.getName();
    fn = fn.replace("float","");
    String[] s = fn.split("[a-z_]");
    int n = Integer.parseInt(s[1]);
    int w = Integer.parseInt(s[2]);
    return (new NedFile(f,n,w));
  }

  public static void readNed(NedFile f, ArrayList<MPoint> g) throws IOException{
    for(MPoint p:g){
      double lat = p.getLat();
      double lon = p.getLon();
      Sampling slat = f.getSLat();
      Sampling slon = f.getSLon();
      int ilat = slat.indexOfNearest(lat);
      int ilon = slon.indexOfNearest(lon);
      double lati = slat.getValue(ilat);
      double loni = slon.getValue(ilon);
      int nlat = slat.getCount();
      if(!((lati<slat.getFirst() || lati>slat.getLast()) && (loni<slon.getFirst() || loni>slon.getLast()))){
        ArrayInputStream ais = new ArrayInputStream(f.getF());
        ais.skipBytes(4*(ilon+(nlat-1-ilat)*slon.getCount()));
        p.setElev((double) ais.readFloat());
        ais.close();
      }
    }
  
  }

}
