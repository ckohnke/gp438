package novice;

import com.github.axet.wget.*;
import java.io.*;
import java.net.*;

/**
 * The Class NedDownloader.
 * 
 * <p> Tools for downloading files from the National Elevation Database (NED) 
 * This code is similar to running:
 * > wget -O ./n46w122.zip "http://gisdata.usgs.gov/TDDS/DownloadFile.php?TYPE=ned3f_zip&FNAME=n46w122.zip&ORIG=RVS"
 * in a Unix environment
 *
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 26, 2014
 */

public class NedDownloader{

  public static void get(int n, int w, String dir) throws IOException {
    try {
      String u = "http://gisdata.usgs.gov/TDDS/DownloadFile.php?TYPE=ned3f_zip&FNAME=n"+n+"w"+w+".zip&ORIG=RVS";
      // String u = "http://tdds3.cr.usgs.gov/Ortho9/ned/ned_13/float/n"+n+"w"+w+".zip";
      URL url = new URL(u);
      String f = dir+"/n"+n+"w"+w+".zip";
      File target = new File(f);
      WGet wg = new WGet(url, target);
      wg.download();
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }

/** The main method.
     * 
     * @param args 
     */
    public static void main(String[] args) {
      try{
        get(46,122,"./");
      }catch (Exception ex) {
        ex.printStackTrace();
      }

    }

}
