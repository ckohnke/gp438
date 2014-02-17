import static edu.mines.jtk.util.ArrayMath.*;
import java.util.*;

public class Segdata{
  public Segdata(int sl,int sp,int rln,int rpf,int rpl,float[][] f){
    this.sl = sl; 
    this.sp = sp;
    this.rln = rln;
    this.rpf = rpf; 
    this.rpl = rpl;
    this.f = ccopy(f);
  }
  
  public int getSL(){
	  return sl;
  }
  
  public int getSP(){
	  return sp;
  }

  public int getRLN(){
	  return rpf;
  }
  
  public int getRPF(){
	  return rln;
  }
  
  public int getRPL(){
	  return rpl;
  }
  
  public float[][] getF(){
	  return f;
  }
  
  private int sl; // Source line number
  private int sp; // Source point number
  private int rln; // Receiver line number
  private int rpf; // Receiver point first
  private int rpl; // Receiver point last
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
