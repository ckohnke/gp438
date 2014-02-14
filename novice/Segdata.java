import static edu.mines.jtk.util.ArrayMath.*;
import java.util.*;

public class Segdata{
  public Segdata(int sl,int sp,int rpf,int rpl,float[][] f){
    this.sl = sl; 
    this.sp = sp;
    this.rpf = rpf; 
    this.rpl = rpl;
    this.f = ccopy(f);
  }
  public int sl; // Source line number
  public int sp; // Source point number
  public int rpf; // receiver point first
  public int rpl; // receiver point last
  public float[][] f;
}

class SegdataComp implements Comparator<Segdata>{

  //@Override
  public int compare(Segdata p1, Segdata p2) {
    if(p1.sp > p2.sp){
       return 1;
    } else {
       return -1;
    }
  }
}
