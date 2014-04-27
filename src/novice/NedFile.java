package novice;

import java.io.File;
import java.io.IOException;
import edu.mines.jtk.dsp.Sampling;

/**
 * The NedFile Object.
 * 
 * <p> Tools for defining the properties of a 
 * National Elevation Database (NED) file. 
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class NedFile{

    /**
   * Instantiates a new NED File.
   *
   * @param f the GridFloat (.ftl) file.
   * @param n the north latitude.
   * @param w the west longitude
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public NedFile(File f, int n, int w) throws IOException {
    this.f = f;
    this.n = n;
    this.w = w;
    this.header = new NedFileHeader(f);
    this.slat = new Sampling(header.getNrows(), header.getCellSize(), header.getYLL());
    this.slon = new Sampling(header.getNcols(), header.getCellSize(), header.getXLL());
  }

  /**
   * Gets the NED GridFloat (.ftl) file.
   *
   * @return the NED GridFloat (.ftl) file
   */
  public File getF(){
    return f;
  }

  /**
   * Gets the north latitude of the NED file.
   *
   * @return the latitude of the NED file
   */
  public int getN(){
    return n;
  }

  /**
   * Gets the west longitude of the NED file.
   *
   * @return the longitude of the NED file
   */
  public int getW(){
    return w;
  }

  /**
   * Gets the Sampling of the latitudes.
   *
   * @return the Sampling of latitudes.
   */
  public Sampling getSLat(){
    return slat;
  }

  /**
   * Gets the Sampling of the longitudes.
   *
   * @return the Sampling of longitudes.
   */
  public Sampling getSLon(){
    return slon;
  }
  
  /**
   * Gets the header (.hdr) file of the GridFloat (.ftl) file.
   *
   * @return the NedFileHeader 
   */
  public NedFileHeader getHeader(){
    return header;
  }

  /** The NED (.ftl) file. */
  private File f;
  
  /** The north latitude. */
  private int n;
  
  /** The west longitude. */
  private int w;
  
  /** The header file. */
  private NedFileHeader header;
  
  /** The Sampling of latitudes and longitudes. */
  private Sampling slat, slon;
  
}
