package novice;
import static edu.mines.jtk.util.ArrayMath.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;


import edu.mines.jtk.dsp.ButterworthFilter;
import edu.mines.jtk.dsp.RecursiveExponentialFilter;
import edu.mines.jtk.io.ArrayInputStream;

/**
 * The Class Segd.
 * 
 * <p> Tools for working with collections of Segdata objects.
 * Contains static methods for working with .segd files.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class Segd {

  /**
   * Instantiates a new segd. Private. Contains only static public methods.
   */
  private Segd() {}

  /**
   * Reads all .segd files from a directory.
   *
   * @param segdDir the directory to read .segd files from
   * @return an array list of Segdata from the directory.
   */
  public static ArrayList<Segdata> readLineSegd(String segdDir) {
    ArrayList<Segdata> _segd = new ArrayList<Segdata>(0);
    try {
      File[] segdList = (new File(segdDir)).listFiles();
      int nshot = segdList.length;
      for (int i = 0; i < nshot; ++i) {
        System.out.println(segdList[i].getName());
        Segdata seg = readSegd(segdList[i]);
        // min:max range for shots to not add empty shots.
        if (!(seg.getSP() < 0) && notEmpty(seg) && !(seg.getSP() > 10000)) 
          _segd.add(seg);
      }
      Collections.sort(_segd, new SegdataComp()); // sort by shot point number
    } catch (IOException e) {
      System.out.println(e);
    }
    return _segd;
  }

  /**
   * Read Segdata from a .segd file.
   *
   * @param segdFile the segd file
   * @return the segdata
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Segdata readSegd(File segdFile) throws IOException { 
    byte[] gh = zerobyte(32); // general header
    byte[] th = zerobyte(20); // trace header
    byte[] the = zerobyte(32); // trace header extension
    byte[] csh = zerobyte(32); // channel set header
    ArrayInputStream ais = new ArrayInputStream(segdFile,
        ByteOrder.BIG_ENDIAN);
    ais.readBytes(gh); // general header 1
    int fn = bcd2(gh, 0); // file number
    ais.readBytes(gh); // general header 2
    ais.readBytes(gh); // general header 3
    int sln, spn;
    sln = bin5(gh, 3); // source line number
    spn = bin5(gh, 8); // source point number
    System.out.println("fn=" + fn + ", sln=" + sln + ", spn=" + spn);
    int cns = 0; // channel set number for seismic trace
    int nct = 0; // total number of channels, including aux channels
    int ncs = 0; // number of seismic channels
    int cn, ct, nc, ic, rln = 0, rpn;
    for (int i = 0; i < 16; ++i) { // for each channel set header, ...
      ais.readBytes(csh); // read channel set header
      cn = csh[1]; // channel set number
      ct = (csh[10] >> 4) & 0xf; // channel type (high 4 bits)
      nc = bcd2(csh, 8); // number of channels
      if (nc > 0) { // if we have channels of this type, ...
        System.out.println("cn =" + cn + " nc =" + nc + " ct =" + ct);
        if (ct == 1) { // if seismic, ...
          cns = cn; // remember channel set number for seismic
          ncs = nc; // remember number of seismic channels
        }

        nct += nc; // count total number of channels
      }
    }
    System.out.println("nct =" + nct + " cns =" + cns + " ncs =" + ncs);
    ais.skipBytes(1024); // skip extended header
    ais.skipBytes(1024); // skip external header
    int rpf = 1;
    int rpl = 1;
    int n1 = 0; // # samples
    int n2 = ncs; // #traces
    float[][] f = null;
    for (int j = 0; j < nct; ++j) { // for all channels (including aux
                    // channels)
      ais.readBytes(th); // trace header
      cn = th[3]; // channel set number
      ic = bcd2(th, 4); // channel (trace) number
      ais.readBytes(the); // trace header extension 1
      rln = bin3(the, 0); // receiver line number
      rpn = bin3(the, 3); // receiver point number
      n1 = bin3(the, 7); // number of samples
      // System.out.println("n1 = "+n1 + " the[7-9]: " + the[7] +" "+ the[8] +" "+the[9]);
      // System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn + " n1 =" + n1);
      if (ic == 1) {
        rpf = rpn;
      } else if (ic == n2) {
        rpl = rpn;
      }
      ais.skipBytes(6 * the.length); // skip trace header extensions 2-7
      if (cn == cns) { // if seismic channel, ...
        if (f == null)
          f = new float[n2][n1];
        // System.out.println("ic =" + ic + " rln =" + rln + " rpn =" +
        // rpn);
        ais.readFloats(f[ic - 1]); // get the seismic trace
      } else {
        ais.skipBytes(4 * n1); // skip the aux trace
      }
    }
    ais.close();
    f = mul(1.0e-14f, f); // scale values to approx. range [-10,10]
    return new Segdata(sln, spn, rln, rpf, rpl, f);
  }

  /**
   * Not empty. Checks if the data in a Segdata is non-zero.
   *
   * @param seg the seg to check.
   * @return true, if the Segdata contains data
   */
  public static boolean notEmpty(Segdata seg){
    float[][] f = seg.getF();
    int n1 = f[0].length;
    int n2 = f.length;
    for(int i=0;i<n2;++i)
      for(int j=0;j<n1;++j)
        if(f[i][j]!=0)
          return true;
    return false;
  }

  /**
   * Maximum shot in an ArrayList of Segdata.
   *
   * @param s the ArrayList to search.
   * @return the maximum shot number in list s
   */
  public static int maxShot(ArrayList<Segdata> s){
    int max = s.get(0).getSP();
    for(Segdata tmp:s){
      if(tmp.getSP() > max){
        max = tmp.getSP();
      }
    }
    return max;
  }

  /**
   * Minimum shot in an ArrayList of Segdata.
   *
   * @param s the ArrayList to search.
   * @return the minimum shot number in list s
   */
  public static int minShot(ArrayList<Segdata> s){
    int min = s.get(0).getSP();
    for(Segdata tmp:s){
      if(tmp.getSP() < min){
        min = tmp.getSP();
      }
    }
    return min;
  }


  /**
   * Minimum shot in an ArrayList of Segdata.
   *
   * @param s the ArrayList to search.
   * @return the minimum shot in list s
   */
  public static Segdata firstShot(ArrayList<Segdata> s){
    Segdata min = s.get(0);
    for(Segdata tmp:s){
      if(tmp.getSP() < min.getSP()){
        min = tmp;
      }
    }
    return min;
  }
 
  /**
   * Tpow2. Tpow of a 2D array of data.
   *
   * @param f the data
   * @param p the power to take time to.
   * @return the tpow data
   */
  public static float[][] tpow2(float[][] f, float p) {
    int n1 = f[0].length;
    int n2 = f.length;
    float[][] t = pow(rampfloat(0.0f, 0.002f, 0.0f, n1, n2),p);
    float[][] g = mul(t, f);
    return g;
  }

  /**
   * Gain2. Gaining of a 2D array of data.
   *
   * @param f the data to gain.
   * @param sigma the sigma for the RecursiveExponentialFilter
   * @return the gained data
   */
  public static float[][] gain2(float[][] f, double sigma) {
    int n1 = f[0].length;
    int n2 = f.length;
    float[][] a = new float[n2][n1];
    RecursiveExponentialFilter ref = new RecursiveExponentialFilter(sigma); //gain control
    if (max(abs(f)) > 0.0f) {
      float[][] g = mul(f, f);
      ref.apply1(g, g);
      div(f, sqrt(g), a);
    }
    return a;
  }

   /**
   * Lowpass2 filtering of the data.
   *
   * @param f the data array to filter
   * @param lowpassNum the lowpass number (cycles/sample)
   * @return the filtered float[][]
   */
  public static float[][] lowpass2(float[][] f, double lowpassNum) {
    int n1 = f[0].length;
    int n2 = f.length;
    float[][] g = new float[n2][n1];
    double f3db = lowpassNum * 0.002; // cycles/sample
    ButterworthFilter bf = new ButterworthFilter(f3db, 6,
        ButterworthFilter.Type.LOW_PASS);
    bf.apply1ForwardReverse(f, g);
    return g;
  }

  /**
   * Gets the max number of channels from an ArrayList of Segdata.
   *
   * @param seg the ArrayList of Segdata to search
   * @return the max number of channels
   */
  public static int getMaxNumChan(ArrayList<Segdata> seg){
    int max = 0;
    for(Segdata s:seg){
      int rf = s.getRPF();
      int rl = s.getRPL();
      if((rl-rf)>max)
        max = rl-rf;
    }
    return max;
  }

  /**
   * Bcd2.
   *
   * @param b the byte array
   * @param k the index to start reading
   * @return the converted int
   */
  private static int bcd2(byte[] b, int k) {
    return (1000 * ((b[k] >> 4) & 0xf) + 100 * (b[k] & 0xf) + 10
        * ((b[k + 1] >> 4) & 0xf) + 1 * (b[k + 1] & 0xf));
  }

  // Returns binary integer from bytes k,k+1,k+2 in b.
  /**
   * Bin3. Converts 3 byte integer to integer type. 
   *
   * @param b the byte array
   * @param k the index to start reading
   * @return the converted int
   */
  private static int bin3(byte[] b, int k) {
    byte b0 = b[k];
    byte b1 = b[k + 1];
    byte b2 = b[k + 2];
    return (b2 & 0xFF) | ((b1 & 0xFF) << 8) | ((b0 & 0x0F) << 16);
  }

  // Returns binary integer from bytes k,k+1,...,k+4 in b.
  /**
   * Bin5. Converts 5 byte integer to integer type. 
   *
   * @param b the byte array
   * @param k the index to start reading
   * @return the converted int
   */
  private static int bin5(byte[] b, int k) {
    int b0 = b[k] & 0xFF;
    int b1 = b[k + 1] & 0xFF;
    int b2 = b[k + 2] & 0xFF;
    int b3 = b[k + 3] & 0xFF;
    int b4 = b[k + 4] & 0xFF;
    return (int)(b0 * 65536.0 + b1 * 256.0 + b2 + b3 / 256.0 + b4 / 65536.0);
  }

}
