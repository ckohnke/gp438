package novice;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import edu.mines.jtk.io.ArrayInputStream;
import edu.mines.jtk.dsp.Sampling;

// Reads gridFloat files for elevation values

/**
 * The Class NedReader.
 * 
 * <p> Tools for reading values from the National Elevation Database (NED) 
 * GridFloat files (.ftl) and their Header files (.hdr).
 * This process assumes that the GridFloat and Header files are 
 * in the same directory.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class NedReader{

  /**
   * Instantiates a new ned reader. Private.
   */
  private NedReader(){}

  /**
   * Import NedFile from file.
   *
   * @param f the GridFloat file (.ftl)
   * @return the NedFile object
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static NedFile importNed(File f) throws IOException{
    String fn = f.getName();
    fn = fn.replace("float",""); //Parse filename
    String[] s = fn.split("[a-z_]");
    int n = Integer.parseInt(s[1]); // north latitude
    int w = Integer.parseInt(s[2]); // west longitude
    return (new NedFile(f,n,w));
  }

  /**
   * Read NED file and update the elevations of points in a list.
   *
   * @param f the NedFile
   * @param g the ArrayList of MPoint objects
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void readNed(NedFile f, ArrayList<MPoint> g) throws IOException{
    for(MPoint p:g){
      double lat = p.getLat();  // Lat of MPoint
      double lon = p.getLon(); // Lon of MPoint
      Sampling slat = f.getSLat(); // Sampling of NED Lats
      Sampling slon = f.getSLon(); // Sampling of Ned Lons
      int ilat = slat.indexOfNearest(lat); 
      int ilon = slon.indexOfNearest(lon);
      double lati = slat.getValue(ilat); 
      double loni = slon.getValue(ilon);
      int nlat = slat.getCount();
      // If this MPoint is defined in the NEDFile, set this MPoint's elevation to the Elevation in the GridFloat file
      if(!((lati<slat.getFirst() || lati>slat.getLast()) && (loni<slon.getFirst() || loni>slon.getLast()))){
        ArrayInputStream ais = new ArrayInputStream(f.getF());
        ais.skipBytes(4*(ilon+(nlat-1-ilat)*slon.getCount()));
        p.setElev((double) ais.readFloat());
        ais.close();
      }
    }
  
  }

}
