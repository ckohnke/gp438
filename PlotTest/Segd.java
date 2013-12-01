import java.awt.*;
import java.io.*;
import java.lang.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.*;
import edu.mines.jtk.interp.*;
import edu.mines.jtk.io.*;
import edu.mines.jtk.mosaic.*;
import edu.mines.jtk.ogl.Gl.*;
import edu.mines.jtk.util.*;
import static edu.mines.jtk.util.ArrayMath.*;

public class Segd{

  public static void main(String[] args){
    Segd s = new Segd();
    s.readLine141Segd();
    
  }

  public Segd(){
    
  }

  public void readLine141Segd(){
    try{
    File[] segdList = (new File(segdDir)).listFiles();
    //File[] segdList = new File[1];
    //segdList[0] = new File(segdDir+"/00000001.00000293.segd");
    int nshot = segdList.length; 
    //float[][][] g = ArrayMath.zerofloat(s1.getCount(), s2.getCount(), s3.getCount());
    for(int i=0; i<segdList.length; ++i){
      System.out.println(segdList[i].getName());
      float[][] f = readSegd(segdList[i]);
      System.out.println("sln ="+sln+" spn ="+spn+" rpf ="+rpf+" rpl ="+rpl);
      int n1 = f[0].length;
      int n2 = f.length;
      Sampling s1 = new Sampling(n1, 0.001, 0.0);
      Sampling s2 = new Sampling(n2, 1.0, 1.0);
      lowpass2(f);
      //tpow2(f);
      gain2(f);
      plot(s1,s2,f,"Shot "+spn);
    }
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public float[][] readSegd(File segdFile) throws IOException{ //return tiltdata-esque
    byte[] gh = zerobyte(32); // general header
    byte[] th = zerobyte(20); // trace header
    byte[] the = zerobyte(32); // trace header extension
    byte[] csh = zerobyte(32); // channel set header
    ArrayInputStream ais = new ArrayInputStream(segdFile,ByteOrder.BIG_ENDIAN);
    ais.readBytes(gh); // general header 1
    int fn = bcd2(gh,0); // file number
    ais.readBytes(gh); // general header 2
    ais.readBytes(gh); // general header 3
    sln = bin5(gh,3); // source line number
    spn = bin5(gh,8); // source point number
    System.out.println("fn=" + fn + ", sln=" + sln + ", spn=" + spn);
    int cns = 0; // channel set number for seismic trace
    int nct = 0; // total number of channels, including aux channels
    int ncs = 0; // number of seismic channels 
    int cn, ct, nc, ic, rln, rpn;
    for(int i=0; i<16; ++i){ // for each channel set header, ...
      ais.readBytes(csh); // read channel set header 
      cn = csh[1]; // channel set number
      ct = (csh[10]>>4)&0xf; // channel type (high 4 bits)
      nc = bcd2(csh,8); // number of channels
      if(nc>0){ // if we have channels of this type, ...
        System.out.println("cn =" + cn + " nc =" + nc + " ct =" + ct);
        if(ct==1){ // if seismic, ...
          cns = cn; // remember channel set number for seismic
          ncs = nc; // remember number of seismic channels
        }
       
      nct += nc; // count total number of channels
      }
    }
    System.out.println("nct =" + nct + " cns =" + cns + " ncs =" + ncs);
    ais.skipBytes(1024); // skip extended header
    ais.skipBytes(1024); // skip external header
    rpf = 1;
    rpl = 1;
    int n1 = 0; // # samples
    int n2 = ncs; // #traces 
    float[][] f = null; 
    for(int j=0; j<nct; ++j){ // for all channels (including aux channels)
      ais.readBytes(th); // trace header
      cn = th[3]; // channel set number
      ic = bcd2(th,4); // channel (trace) number
      ais.readBytes(the); // trace header extension 1
      rln = bin3(the,0); // receiver line number
      rpn = bin3(the,3); // receiver point number
      n1 = bin3(the,7); // number of samples
      //System.out.println("n1 = "+n1 + " the[7-9]: " + the[7] +" "+ the[8] +" "+the[9]); 
      System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn + " n1 =" + n1);
      if(ic==1){
        rpf = rpn;
      } else if(ic == n2){
        rpl = rpn;
      }
      ais.skipBytes(6*the.length); // skip trace header extensions 2-7
      if(cn==cns){ // if seismic channel, ...
        if(f == null)
          f = new float[n2][n1];
        //System.out.println("ic =" + ic + " rln =" + rln + " rpn =" + rpn);
        ais.readFloats(f[ic-1]); // get the seismic trace
      } else{
        ais.skipBytes(4*n1); // skip the aux trace
      }
    }
    ais.close();
    f = mul(1.0e-14f,f); // scale values to approx. range [-10,10]
    return f;
  }

  public void plot(Sampling s1, Sampling s2, float[][] f, String title){
    SimplePlot sp = new SimplePlot(SimplePlot.Origin.UPPER_LEFT);
    sp.setSize(900,900);
    sp.setVLabel("Time (s)");
    if(s2.getDelta() ==1.0)
      sp.setHLabel("Station");
    else
      sp.setHLabel("Offset (km)");
    sp.setTitle(title);
    PixelsView pv = sp.addPixels(s1,s2,f);
    pv.setPercentiles(1,99);
  }

  public void tpow2(float[][] f){
    int n1 = f[0].length;
    int n2 = f.length;
    float[][] t = rampfloat(0.0f,0.002f,0.0f,n1,n2);
    mul(t,t,t);
    mul(t,f);
  }

  public void gain2(float[][] f){
    RecursiveExponentialFilter ref = new RecursiveExponentialFilter(40.0);
    for(int m = 0; m<f.length; ++m){
      if(max(abs(f))>0.0f){
        float[][] g = mul(f,f);
        ref.apply1(g,g);
        div(f,sqrt(g),f);
      }
    }
  }

  public void lowpass2(float[][] f){
    double f3db = 25.0*0.002;
    ButterworthFilter bf = new ButterworthFilter(f3db,6,ButterworthFilter.Type.LOW_PASS);
    bf.apply1ForwardReverse(f,f);
  }

  public int bcd2(byte[] b, int k){
    return (1000*((b[k  ]>>4)&0xf)+100*(b[k  ]&0xf)+
	      10*((b[k+1]>>4)&0xf)+  1*(b[k+1]&0xf));
  }

  public int bin3(byte[] b, int k){
    byte b0 = b[k  ];
    byte b1 = b[k+1];
    byte b2 = b[k+2]; 
    return (b2 & 0xFF) | ((b1 & 0xFF) << 8) | ((b0 & 0x0F) << 16);
  }

  public int bin5(byte[] b, int k){
    byte b0 = b[k  ];
    byte b1 = b[k+1];
    byte b2 = b[k+2];
    byte b3 = b[k+3];
    byte b4 = b[k+4];
    //ByteBuffer bb = ByteBuffer.wrap(new byte[] {b0, b1, b2, b3, b4});
    //int num = bb.getInt();
    //if(b0<0) b0 += 256;
    //if(b1<0) b1 += 256;
    //if(b2<0) b2 += 256;
    //if(b3<0) b3 += 256;
    //if(b4<0) b4 += 256;
    return (int)(256.0+b0*65536.0+b1*256.0+b2+b3/256.0+b4/65536.0);
    //return (((b0 & 0x0F) << 32) | ((b1 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8) | (b4 & 0xFF));
    //return (b4 & 0xFF)|((b3 & 0xFF) << 8)|((b2 & 0xFF) << 16)|((b1 & 0xFF) << 24)|((b0 & 0x0F) << 32);
    //return num;
  }
  
  //public String segdDir = "/gpfc/ckohnke/fc2013/segd/141/"; // Linux Lab
  public String segdDir = "/home/colton/Documents/School/SrDesign/fc2013/segd/141/"; // Laptop
  public Sampling s1 = new Sampling(4001,0.002,0.000);
  public Sampling s2 = new Sampling(342,1,954);
  public Sampling s3 = new Sampling(215,1.0,1003);
  public float[][] f = null;
  public int sln,spn,rpf,rpl;


}
