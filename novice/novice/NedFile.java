package novice;

import java.io.File;
import java.io.IOException;
import edu.mines.jtk.dsp.Sampling;

public class NedFile{

  private File f;
  private int n;
  private int w;
  private NedFileHeader header;
  private Sampling slat, slon;

  public NedFile(File f, int n, int w) throws IOException {
    this.f = f;
    this.n = n;
    this.w = w;
    this.header = new NedFileHeader(f);
    this.slat = new Sampling(header.getNrows(), header.getCellSize(), header.getYLL());
    this.slon = new Sampling(header.getNcols(), header.getCellSize(), header.getXLL());
  }

  public File getF(){
    return f;
  }

  public int getN(){
    return n;
  }

  public int getW(){
    return w;
  }

  public Sampling getSLat(){
    return slat;
  }

  public Sampling getSLon(){
    return slon;
  }
  public NedFileHeader getHeader(){
    return header;
  }

}
