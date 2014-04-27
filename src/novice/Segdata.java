package novice;

import static edu.mines.jtk.util.ArrayMath.*;
import java.util.*;

/**
 * The Class Segdata.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class Segdata{
  
  /**
   * Instantiates a new segdata.
   *
   * @param sl the source line
   * @param sp the source point number
   * @param rln the receiver line number
   * @param rpf the receiver point first
   * @param rpl the receiver point last
   * @param f the f
   */
  public Segdata(int sl,int sp,int rln,int rpf,int rpl,float[][] f){
    this.sl = sl; 
    this.sp = sp;
    this.rln = rln;
    this.rpf = rpf; 
    this.rpl = rpl;
    this.f = copy(f);
  }
  
  /**
   * Gets the source line number.
   *
   * @return the source line number
   */
  public int getSL(){
    return sl;
  }
  
  /**
   * Gets the source point number.
   *
   * @return the source point number
   */
  public int getSP(){
    return sp;
  }

  /**
   * Gets the receiver line number.
   *
   * @return the receiver line number
   */
  public int getRLN(){
    return rln;
  }
  
  /**
   * Gets the receiver point first.
   *
   * @return the receiver point first
   */
  public int getRPF(){
    return rpf;
  }
  
  /**
   * Gets the receiver point last.
   *
   * @return the receiver point last
   */
  public int getRPL(){
    return rpl;
  }
  
  /**
   * Gets the shot record.
   *
   * @return the shot record
   */
  public float[][] getF(){
    return f;
  }
  
  /** The source line. */
  private int sl; // Source line number
  
  /** The source point. */
  private int sp; // Source point number
  
  /** The receiver line number. */
  private int rln; // Receiver line number
  
  /** The receiver point first. */
  private int rpf; // Receiver point first
  
  /** The receiver point last. */
  private int rpl; // Receiver point last
  
  /** The shot record. */
  private float[][] f;
}

class SegdataComp implements Comparator<Segdata>{

  //@Override
  public int compare(Segdata p1, Segdata p2) {
    if(p1.getSP() > p2.getSP()){
       return 1;
    } else {
       return -1;
    }
  }
}
