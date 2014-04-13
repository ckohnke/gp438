package novice;

import java.io.*;

public class NedFileHeader{

  private int ncols;
  private int nrows;
  private double xllcorner, yllcorner;
  private double cellsize;
  private double noDataValue;

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

  public int getNcols(){
    return ncols;
  }

  public int getNrows(){
    return nrows;
  }

  public double getXLL(){
    return xllcorner;
  }

  public double getYLL(){
    return yllcorner;
  }

  public double getCellSize(){
    return cellsize;
  }

  public double getNoDataValue(){
    return noDataValue;
  }

}
