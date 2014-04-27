package novice;

import java.io.*;

/**
 * The Class NedFileHeader.
 * 
 * <p> Tools for reading and defining the Header (.hdr) files of 
 * Nation Elevation Database (NED) File (.ftl).
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 * 
 */
public class NedFileHeader{

    /**
   * Instantiates a new NED File Header. Assumes that the header files (.hdr) 
   * is in the same directory as the GridFloat file (.ftl).
   *
   * @param data of NED file (.ftl)
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public NedFileHeader(File data) throws IOException {
    String path = data.getPath();
    System.out.println(path);
    int i = path.lastIndexOf(".");
    path = path.substring(0,i);
    path = path + ".hdr";
    System.out.println(path);
    BufferedReader br = new BufferedReader(new FileReader(path));

    String line = br.readLine();
    String[] fields = line.split("\\s+");
    String name = fields[0];
    this.ncols = Integer.parseInt(fields[1]);
    System.out.println(name+" "+ this.ncols);    

    line = br.readLine();
    System.out.println(line);
    fields = line.split("\\s+");
    name = fields[0];
    this.nrows = Integer.parseInt(fields[1]);
    System.out.println(name+" "+ this.nrows);

    line = br.readLine();
    System.out.println(line);
    fields = line.split("\\s+");
    name = fields[0];
    this.xllcorner = Double.parseDouble(fields[1]);
    System.out.println(name+" "+ this.xllcorner);
    
    line = br.readLine();
    System.out.println(line);
    fields = line.split("\\s+");
    name = fields[0];
    this.yllcorner = Double.parseDouble(fields[1]);
    System.out.println(name+" "+ this.yllcorner);

    line = br.readLine();
    System.out.println(line);
    fields = line.split("\\s+");
    name = fields[0];
    this.cellsize = Double.parseDouble(fields[1]);
    System.out.println(name+" "+ this.cellsize);

    line = br.readLine();
    System.out.println(line);
    fields = line.split("\\s+");
    name = fields[0];
    this.noDataValue = Double.parseDouble(fields[1]);  

    br.close();

  }

  /**
   * Gets the number of columns in the GridFloat file.
   *
   * @return the number of columns in the GridFloat file
   */
  public int getNcols(){
    return ncols;
  }

  /**
   * Gets the number of rows in the GridFloat file.
   *
   * @return the number of rows in the GridFloat file
   */
  public int getNrows(){
    return nrows;
  }

  /**
   * Gets the lower left latitude of the map.
   *
   * @return the lower left latitude of the GridFloat file.
   */
  public double getXLL(){
    return xllcorner;
  }

  /**
   * Gets the lower left longitude of the map.
   *
   * @return the lower left longitude of the GridFloat file.
   */
  public double getYLL(){
    return yllcorner;
  }

  /**
   * Gets the cell size of the NED GridFloat file.
   *
   * @return the cell size
   */
  public double getCellSize(){
    return cellsize;
  }

  /**
   * Gets the no data value.
   *
   * @return the no data value
   */
  public double getNoDataValue(){
    return noDataValue;
  }

  /** The number of columns. */
  private int ncols;
  
  /** The number of rows. */
  private int nrows;
  
  /** The xllcorner and yllcorner. */
  private double xllcorner, yllcorner;
  
  /** The cellsize. */
  private double cellsize;
  
  /** The no data value. */
  private double noDataValue;
  
}
