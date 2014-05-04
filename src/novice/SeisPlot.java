package novice;

import static edu.mines.jtk.util.ArrayMath.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.*;

import static novice.Segd.*;
import static novice.Waypoints.*;
import static novice.NedReader.*;

/**
 * The Class SeisPlot.
 * 
 * <p> The main container class for the program. Controls the contained class for plot,
 * data imports and other subroutines. Takes calls from contained classes
 * and updates them accordingly.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 * 
 */
public class SeisPlot {

  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new SeisPlot();
      }
    });
  }


  // Location and size of overlay plot.
  /** The Constant M_X. */
  private static final int M_X = 0;
  
  /** The Constant M_Y. */
  private static final int M_Y = 0;
  
  /** The Constant M_WIDTH. */
  private static final int M_WIDTH = 500;
  
  /** The Constant M_HEIGHT. */
  private static final int M_HEIGHT = 500;

  /** The _gps. */
  public static ArrayList<MPoint> _gps;
  
  /** The _segd. */
  public ArrayList<Segdata> _segd;
  
  /** The _ned files. */
  public ArrayList<NedFile> _nedFiles;
  
  /** The _bp. */
  public BasePlot _bp;
  
  /** The _rp. */
  public ResponsePlot _rp;
  
  /** The _elev. */
  public ElevPlot _elev;

  // Sliders
  /** The gain slider. */
  public JSlider gainSlider;
  
  /** The lowpass slider. */
  public JSlider lowpassSlider;
  
  /** The tpow slider. */
  public JSlider tpowSlider;

  /**
   * Instantiates a new plot test.
   * Initiates the plots
   */
  private SeisPlot() {
    // _shots = new ArrayList<MPoint>(0);
    // _gps = new ArrayList<MPoint>(0);
    // _segd = new ArrayList<Segdata>(0);
    _bp = new BasePlot();
    _rp = new ResponsePlot();
    _elev = new ElevPlot();
  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The Class BasePlot.
   * 
   * <p> Class for controlling the base map view.
   * 
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 13, 2014
   * 
   */
  private class BasePlot {

    /** The _plot frame. */
    private PlotFrame _plotFrame;
    
    /** The _plot panel. */
    private PlotPanel _plotPanel;
    
    /** The _base view. */
    private PointsView _baseView;
    
    /** The _red view. */
    private PointsView _redView;
    
    /** The _blue view. */
    private PointsView _blueView;
    
    /** The _green view. */
    private PointsView _greenView;
    
    /** The _circle view. */
    private PointsView _circleView;

    /**
     * Instantiates a new base plot.
     */
    private BasePlot() {

      // The plot panel.
      _plotPanel = new PlotPanel();
      _plotPanel.setTitle("Base Plot");
      _plotPanel.setHLabel("Easting (UTM)");
      _plotPanel.setVLabel("Northing (UTM)");
      _plotPanel.setHLimits(317600, 320600);  
      _plotPanel.setVLimits(4121800, 4123600);
      _plotPanel.setHFormat("%s");
      _plotPanel.setVFormat("%s");
      _plotPanel.addGrid("H-.V-.");

      // A grid view for horizontal and vertical lines (axes).
      _plotPanel.addGrid("H0-V0-");

      // A plot frame has a mode for zooming in tiles or tile axes.
      _plotFrame = new PlotFrame(_plotPanel);
      _plotFrame.setTitle("Base Plot");
      TileZoomMode tzm = _plotFrame.getTileZoomMode();

      // Modes for Base plot
      ModeManager mm = _plotFrame.getModeManager();
      RoamMode rm = new RoamMode(mm); // roam and plot
      CircleMode om = new CircleMode(mm);
      ChannelMode cm = new ChannelMode(mm);
      NoGPSMode gm = new NoGPSMode(mm);

      // The menu bar includes a mode menu for selecting a mode.
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');

      JMenu modeMenu = new JMenu("Mode");
      modeMenu.setMnemonic('M');
      modeMenu.add(new ModeMenuItem(tzm));
      modeMenu.add(new ModeMenuItem(rm));
      modeMenu.add(new ModeMenuItem(om));
      modeMenu.add(new ModeMenuItem(cm));
      modeMenu.add(new ModeMenuItem(gm));

      JMenu gpsMenu = new JMenu("GPS Tools");
      gpsMenu.add(new GetFlagsFromHH()).setMnemonic('f');
      gpsMenu.add(new GetDEM(_plotPanel)).setMnemonic('g');
      gpsMenu.add(new ReadNedElevation(_plotPanel));
      gpsMenu.add(new ExportFlagsToCSV()).setMnemonic('e');

      JMenu segdMenu = new JMenu("SEGD Tools");
      segdMenu.add(new ImportSegdDir()).setMnemonic('s');
      segdMenu.add(new ImportSegdFile()).setMnemonic('d');

      JMenu testMenu = new JMenu("Dev");
      testMenu.setMnemonic('E');
      testMenu.add(new ClearData()).setMnemonic('c');
      testMenu.add(new DownloadNedFile()).setMnemonic('d');

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(modeMenu);
      menuBar.add(gpsMenu);
      menuBar.add(segdMenu);
      menuBar.add(testMenu);

      _plotFrame.setJMenuBar(menuBar);

      // The tool bar includes toggle buttons for selecting a mode.
      JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
      toolBar.setRollover(true);
      toolBar.add(new ModeToggleButton(tzm));
      toolBar.add(new ModeToggleButton(rm));
      toolBar.add(new ModeToggleButton(om));
      toolBar.add(new ModeToggleButton(cm));
      toolBar.add(new ModeToggleButton(gm));
      _plotFrame.add(toolBar, BorderLayout.WEST);

      // Make the plot frame visible.
      _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _plotFrame.setLocation(M_X, M_Y);
      _plotFrame.setSize(M_WIDTH, M_HEIGHT);
      _plotFrame.setFontSizeForPrint(8, 240);
      _plotFrame.setVisible(true);

    }

    // Makes points visible
    /**
     * Update bp view.
     */
    private void updateBPView() {
      if(_gps != null){
        int np = _gps.size();
        float[] xp = new float[np];
        float[] yp = new float[np];
        for (int ip = 0; ip < np; ++ip) {
          MPoint p = _gps.get(ip);
          xp[ip] = (float) p.getUTMX();
          yp[ip] = (float) p.getUTMY();
        }
        _plotPanel.setHLimits(min(xp) - 25, max(xp) + 25);
        _plotPanel.setVLimits(min(yp) - 25, max(yp) + 25);
        if (_baseView == null) {
          _baseView = _plotPanel.addPoints(xp, yp);
          _baseView.setMarkStyle(PointsView.Mark.CROSS);
          _baseView.setLineStyle(PointsView.Line.NONE);
        } else {
          _baseView.set(xp, yp);
        }
      }
    }

    /**
     * Draw current gps.
     *
     * @param p the p
     */
    private void drawCurrentGPS(MPoint p){
      float[] xp = new float[1];
      float[] yp = new float[1];
      xp[0] = (float) p.getUTMX();
      yp[0] = (float) p.getUTMY();
      if(_redView == null){
        _redView = _plotPanel.addPoints(xp, yp);
        _redView.setMarkStyle(PointsView.Mark.CROSS);
        _redView.setLineStyle(PointsView.Line.NONE);
        _redView.setMarkColor(Color.RED);
      } else{
        _redView.set(xp, yp);
      }
    }

    /**
     * Draw current gps.
     *
     * @param p the p
     */
    private void drawCurrentGPS(ArrayList<MPoint> p){
      if(p.size() > 0){
        int np = p.size();
        float[] xp = new float[np];
        float[] yp = new float[np];
        for (int ip = 0; ip < np; ++ip) {
          MPoint m = p.get(ip);
          xp[ip] = (float) m.getUTMX();
          yp[ip] = (float) m.getUTMY();
        }
        if(_redView == null){
          _redView = _plotPanel.addPoints(xp, yp);
          _redView.setMarkStyle(PointsView.Mark.CROSS);
          _redView.setLineStyle(PointsView.Line.NONE);
          _redView.setMarkColor(Color.RED);
        } else{
          _redView.set(xp, yp);
        }
      }
    }

    /**
     * Draw current seg.
     *
     * @param s the s
     */
    private void drawCurrentSeg(Segdata s){
      int g = s.getSP();
      MPoint p = getNearestGPSFromSegdata(s);
      float[] xp = new float[1];
      float[] yp = new float[1];
      xp[0] = (float) p.getUTMX();
      yp[0] = (float) p.getUTMY();
      if(_blueView == null){
        _blueView = _plotPanel.addPoints(xp, yp);
        _blueView.setMarkStyle(PointsView.Mark.HOLLOW_CIRCLE);
        _blueView.setLineStyle(PointsView.Line.NONE);
        _blueView.setMarkColor(Color.BLUE);
      } else{
        _blueView.set(xp, yp);
      }
    }

    /**
     * Draw current seg.
     *
     * @param s the s
     */
    private void drawCurrentSeg(ArrayList<Segdata> s){
      if(s.size() > 0){
        int np = s.size();
        float[] xp = new float[np];
        float[] yp = new float[np];
        for (int ip = 0; ip < np; ++ip) {
          Segdata m = s.get(ip);
          MPoint p = getNearestGPSFromSegdata(m);
          xp[ip] = (float) p.getUTMX();
          yp[ip] = (float) p.getUTMY();
        }
        if(_blueView == null){
          _blueView = _plotPanel.addPoints(xp, yp);
          _blueView.setMarkStyle(PointsView.Mark.HOLLOW_CIRCLE);
          _blueView.setLineStyle(PointsView.Line.NONE);
          _blueView.setMarkColor(Color.BLUE);
        } else{
          _blueView.set(xp, yp);
        }
      }
    }

    /**
     * Gets the nearest gps from segdata.
     *
     * @param s the s
     * @return the nearest gps from segdata
     */
    private MPoint getNearestGPSFromSegdata(Segdata s){
      if(_gps != null && _gps.size() > 0){
        MPoint p = _gps.get(0);
        for(int i=1; i<_gps.size(); ++i){
          if(abs(p.getStation()-s.getSP())>abs(_gps.get(i).getStation()-s.getSP())){
            p = _gps.get(i);
          }
        }
        return p;
      }
      return null;
    }

    /**
     * Plot active receivers.
     *
     * @param s the s
     */
    private void plotActiveReceivers(Segdata s){
      if(_gps != null && _gps.size()>0){
        ArrayList<Integer> recs = new ArrayList<Integer>(0);
        ArrayList<MPoint> g = new ArrayList<MPoint>(0);
        float[][] f = s.getF();
        int n2 = f.length;
        int start = s.getRPF();
        for(int i=0; i<n2; ++i){
          if(isActive(f[i])){
            recs.add(start+i);
          }
        }
        for(int i=0; i<_gps.size(); ++i){
          MPoint p = _gps.get(i);
          if(recs.contains(p.getStation())){
            g.add(p);
          }
        }
        int np = g.size();
        float[] xp = new float[np];
        float[] yp = new float[np];
        for (int ip = 0; ip < np; ++ip) {
          MPoint m = g.get(ip);
          xp[ip] = (float) m.getUTMX();
          yp[ip] = (float) m.getUTMY();
        }
        if(_greenView == null){
          _greenView = _plotPanel.addPoints(xp, yp);
          _greenView.setMarkStyle(PointsView.Mark.CROSS);
          _greenView.setLineStyle(PointsView.Line.NONE);
          _greenView.setMarkColor(Color.GREEN);
        } else{
          _greenView.set(xp, yp);
        }
      }
    }
    /**
     * Plot active receivers.
     *
     * @param seg the seg
     */
    private void plotActiveReceivers(ArrayList<Segdata> seg){
      for(Segdata s:seg){
        plotActiveReceivers(s);
      }
    }

    /**
     * Checks if is active.
     *
     * @param f the f
     * @return true, if is active
     */
    private boolean isActive(float[] f){
      int n1 = f.length;
      for(int i=0; i<n1; ++i){
        if(f[i] != 0){
          return true;
        }
      }
      return false;
    }

    /**
     * Gps within range.
     *
     * @param g the g
     * @param d the d
     * @return the array list
     */
    public ArrayList<MPoint> gpsWithinRange(MPoint g, double d){
      if(_gps!=null && _gps.size()>0){
        ArrayList<MPoint> p = new ArrayList<MPoint>(0);
        for(MPoint m:_gps){
          if(g.xyDist(m) <= d){
            p.add(m);
          }
        }
        return p;
      }
      return null;
    }
    /**
     * Seg within range.
     *
     * @param g the g
     * @return the array list
     */
    public ArrayList<Segdata> segWithinRange(ArrayList<MPoint> g){
      ArrayList<Segdata> p = new ArrayList<Segdata>(0);
      int max = maxStation(g);
      int min = minStation(g);
      for(Segdata s:_segd){
        if(s.getSP() <= max && s.getSP()>=min){
          p.add(s);
        }
      }
      return p;
    }

    /**
     * Draw circle.
     *
     * @param mid the mid
     * @param r the r
     */
    public void drawCircle(MPoint mid, double r){
      float[][] circlePoints = makeCirclePoints(mid, r);
      if(_circleView == null){
        _circleView = _plotPanel.addPoints(circlePoints[0],circlePoints[1]);
        _circleView.setMarkStyle(PointsView.Mark.NONE);
        _circleView.setLineStyle(PointsView.Line.SOLID);
        _circleView.setLineColor(Color.RED);
      } else{
        _circleView.set(circlePoints[0],circlePoints[1]);
      }
    }

    /**
     * Make circle points.
     *
     * @param mid the mid
     * @param r the r
     * @return the float[][]
     */
    public float[][] makeCirclePoints(MPoint mid, double r){
      int nt = 1000;
      double dt = 2.0*DBL_PI/(nt-1);
      float[] x = new float[nt];
      float[] y = new float[nt];
      for (int it=0; it<nt; ++it) {
        float t = (float)(it*dt);
        x[it] = (float)(mid.getUTMX()+r*cos(t));
        y[it] = (float)(mid.getUTMY()-r*sin(t));
      }
      return new float[][]{x,y};
    }

  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The Class ResponsePlot.
   * 
   * Creates and controls the Response plot (shot record, brute stack, etc.)
   * 
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 13, 2014
   */
  private class ResponsePlot {

    // private PlotPanel _plotPanel;

    /** The sp. */
    public SimplePlot sp;
    
    /** The pv. */
    private PixelsView pv;
    
    /** The gain num. */
    private double gainNum = 40.0;
    private JLabel gainLabel;    

    /** The tpow num. */
    private float tpowNum = 0.0f;
    private JLabel tpowLabel;    

    /** The lowpass num. */
    private double lowpassNum = 25.0;
    private JLabel lowpassLabel;
    
    /** The slider frame. */
    private JFrame sliderFrame;
    
    /** The slider panel. */
    private JPanel sliderPanel;


    /** The s1. */
    private Sampling s1;
    
    /** The s2. */
    private Sampling s2;
    
    /** The plot array. */
    private float[][] plotArray;

    // The Shot response
    /**
     * Instantiates a new response plot.
     */
    private ResponsePlot() {

      // Makes Sliders for gain, tpow and lowpass      
      sliderFrame = new JFrame();
      sliderFrame.setTitle("Plot Sliders");
      sliderFrame.setSize(250,300);
      sliderFrame.setLocation(100,500);
      sliderPanel = new JPanel();
      sliderFrame.add(sliderPanel);

      gainSlider = new JSlider(0,100,40);
      gainSlider.setOrientation(JSlider.HORIZONTAL);
      gainSlider.setMajorTickSpacing(20);
      gainSlider.setMinorTickSpacing(5);
      gainSlider.setPaintTicks(true);
      gainSlider.setPaintLabels(true);
      gainSlider.setSize(200,100);
      sliderPanel.add(gainSlider);
      gainSlider.addChangeListener(cl);
      gainLabel = new JLabel("Gain Control", JLabel.CENTER);
      gainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(gainLabel);

      lowpassSlider = new JSlider(0,100,25);
      lowpassSlider.setOrientation(JSlider.HORIZONTAL);
      lowpassSlider.setMajorTickSpacing(20);
      lowpassSlider.setMinorTickSpacing(5);
      lowpassSlider.setPaintTicks(true);
      lowpassSlider.setPaintLabels(true);
      lowpassSlider.setSize(200,100);
      sliderPanel.add(lowpassSlider);
      lowpassSlider.addChangeListener(cl);
      lowpassLabel = new JLabel("Lowpass Freq. (cycles/s)", JLabel.CENTER);
      lowpassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(lowpassLabel);

      tpowSlider = new JSlider(0,50,0);
      tpowSlider.setOrientation(JSlider.HORIZONTAL);
      tpowSlider.setMajorTickSpacing(10);
      tpowSlider.setMinorTickSpacing(1);
      tpowSlider.setPaintTicks(true);
      tpowSlider.setPaintLabels(true);

      Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
      labelTable.put(new Integer(0),  new JLabel("0.0"));
      labelTable.put(new Integer(10), new JLabel("1.0"));
      labelTable.put(new Integer(20), new JLabel("2.0"));
      labelTable.put(new Integer(30), new JLabel("3.0"));
      labelTable.put(new Integer(40), new JLabel("4.0"));
      labelTable.put(new Integer(50), new JLabel("5.0"));
      tpowSlider.setLabelTable(labelTable);

      tpowSlider.setSize(200,100);
      sliderPanel.add(tpowSlider);
      tpowSlider.addChangeListener(cl);
      tpowLabel = new JLabel("tpow Power", JLabel.CENTER);
      tpowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(tpowLabel);

      sliderFrame.setVisible(false);

      sp = new SimplePlot(SimplePlot.Origin.UPPER_LEFT);
      sp.setSize(600, 600);
      sp.setVLabel("Time (s)");
      sp.setLocation(500,0);
      pv = null;

      // Menu for Response Plot
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(sp)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');

      JMenu plotMenu = new JMenu("Plot Tools");
      plotMenu.add(new ShowPlotSettings()).setMnemonic('p'); 

      // Menu bar for Response Plot
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(plotMenu);

      sp.setJMenuBar(menuBar);

    }

      /** The cl. */
      private ChangeListener cl = new ChangeListener(){
        public void stateChanged(ChangeEvent e) {
          gainNum = gainSlider.getValue();
          lowpassNum = lowpassSlider.getValue();
          tpowNum = tpowSlider.getValue()/10.0f;
          updateRP();
          gainLabel.setText("Gain Number: "+gainNum);
          lowpassLabel.setText("Lowpass Freq. (cycles/s): "+lowpassNum);
          tpowLabel.setText("tpow Power: "+tpowNum);  
        }
      };

    /**
     * Update rp.
     */
    public void updateRP(){
      if(plotArray != null){
        pv = sp.addPixels(s1, s2, gain2(lowpass2(tpow2(plotArray, tpowNum), lowpassNum), gainNum));
        pv.setPercentiles(1, 99);
      }
    }

    /**
     * Update rp.
     *
     * @param seg the seg
     */
    public void updateRP(Segdata seg) {
      int n1 = seg.getF()[0].length;
      int n2 = seg.getF().length;
      s1 = new Sampling(n1, 0.001, 0.0);
      s2 = new Sampling(n2, 1.0, seg.getRPF());
      if (s2.getDelta() == 1.0)
        sp.setHLabel("Station");
      else
        sp.setHLabel("Offset (km)");
      sp.setHLimits(seg.getRPF(), seg.getRPL());
      sp.setTitle("Shot " + seg.getSP());
      plotArray = seg.getF();
      updateRP();
    }

    /**
     * Update rp.
     *
     * @param s the s
     */
    public void updateRP(ArrayList<Segdata> s) {
      if(s != null && s.size()>0){
        int n1 = getN1(s);
        int n2 = getN2(s);
        int rpf = getRPF(s);
        int rpl = rpf+n2;
        int minSP = minShot(s);
        int maxSP = maxShot(s);
        float[] count = new float[n2];
        float[][] stot = new float[n2][n1];
        for (int i = 0; i < s.size(); ++i) {
          Segdata seg = s.get(i);
          int rpftmp = seg.getRPF();
          int rpltmp = seg.getRPL();
          float[][] f = seg.getF();
          for(int j=0; j<f.length; ++j){
            int index = j+(rpftmp-rpf);
            if(isActive(f[j])){
              for(int k=0; k<f[0].length; ++k){   
                stot[index][k] += f[j][k];
              }
             ++count[index];
            }
          }
        }
        for(int i=0;i<n2; ++i){
          for(int j=0;j<n1;++j){
            stot[i][j] = stot[i][j]/count[i];
          }
        }
        s1 = new Sampling(n1, 0.001, 0.0);
        s2 = new Sampling(n2, 1.0, rpf);
        plotArray = stot;
        updateRP();
        sp.setHLimits(rpf, rpl);
        if((maxSP-minSP)==0){
          sp.setTitle("Shot: "+minSP);
        } else{
          sp.setTitle("Brute Stack: "+minSP+"-"+maxSP);
        }
        sp.setHLabel("Station");
      }
    }

    /**
     * Update rp.
     *
     * @param s the s
     * @param channel the channel
     */
    public void updateRP(ArrayList<Segdata> s, int channel) {
      if(s!=null && s.size()>0){
        ArrayList<Segdata> seg = new ArrayList<Segdata>(0);
        int min = firstShot(s).getRPF();
        int station = min+channel;
        for(int i=0; i<s.size(); ++i){
          Segdata t = s.get(i);
          if(t.getF().length>=channel){
            seg.add(t);
          }
        }
        int rpf = getRPF(seg);
        int fsp = getFirstSP(seg);
        int lsp = getLastSP(seg);
        int n1 = getN1(seg);
        int n2 = lsp-fsp+1;
        int rpl = rpf+n2;
        float[][] chan = new float[n2][n1];     
        for (int i = 0; i < seg.size(); ++i) {
          Segdata tmp = seg.get(i);
          int stmp = tmp.getSP();
          int rpftmp = tmp.getRPF();
          // float[] c = tmp.getF()[(rpf-rpftmp)+channel-1];
          float[] c = tmp.getF()[channel];
          if(isActive(c)){
            for(int j=0;j<c.length; ++j){
              chan[stmp-fsp][j] += c[j];
            }
          }
        }
        s1 = new Sampling(n1, 0.001, 0.0);
        s2 = new Sampling(n2, 1.0, fsp);
        plotArray = chan;
        updateRP();
        sp.setHLimits(fsp, maxShot(seg));
        sp.setTitle("Channel: "+channel);
        sp.setHLabel("Station Number");     
    
        if(_gps != null && _gps.size()>0){
          for(Segdata r:seg){
            _bp.plotActiveReceivers(r);
          }
          _bp.drawCurrentSeg(seg);
        }
      }
    }

    /**
     * Gets the n1.
     *
     * @param s the s
     * @return the n1
     */
    private int getN1(ArrayList<Segdata> s){
      int n1 = s.get(0).getF()[0].length;
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t = tmp.getF()[0].length;
        if(t>n1) n1=t;
      }
      return n1;
    }

    /**
     * Gets the n2.
     *
     * @param s the s
     * @return the n2
     */
    private int getN2(ArrayList<Segdata> s){
      int s1 = s.get(0).getRPF();
      int s2 = s.get(0).getRPL();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getRPF();
        int t2 = tmp.getRPL();
        if(t1<s1) s1=t1;
        if(t2>s2) s2=t2;
      }
      return s2-s1+1;
    }

    /**
     * Gets the first sp.
     *
     * @param s the s
     * @return the first sp
     */
    private int getFirstSP(ArrayList<Segdata> s){
      int s1 = s.get(0).getSP();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getSP();
        if(t1<s1) s1=t1;
      }
      return s1;
    }

    /**
     * Gets the last sp.
     *
     * @param s the s
     * @return the last sp
     */
    private int getLastSP(ArrayList<Segdata> s){
      int s1 = s.get(0).getSP();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getSP();
        if(t1>s1) s1=t1;
      }
      return s1;
    }

    /**
     * Gets the rpf.
     *
     * @param s the s
     * @return the rpf
     */
    private int getRPF(ArrayList<Segdata> s){
      int s1 = s.get(0).getRPF();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getRPF();
        if(t1<s1) s1=t1;
      }
      return s1;
    }

    /**
     * Checks if a receiver is active.
     *
     * @param f the f
     * @return true, if is active
     */
    private boolean isActive(float[] f){
      int n1 = f.length;
      for(int i=0; i<n1; ++i){
        if(f[i] != 0){
          return true;
        }
      }
      return false;
    }

    /**
     * Show plot slider.
     */
    public void showPlotSlider(){
      sliderFrame.setVisible(true);
    }

  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The Class ElevPlot.
   * 
   * <p> Controls the Elevation plot
   * 
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 13, 2014
   */
  private class ElevPlot {

    /** The elev. */
    private SimplePlot elev;
    
    /** The pv. */
    private PointsView pv;

    /**
     * Instantiates a new elev plot.
     */
    private ElevPlot() {
      elev = new SimplePlot(SimplePlot.Origin.LOWER_LEFT);
      elev.setSize(500, 250);
      elev.setVLabel("meters (m)");
      elev.setHLabel("Station ID");
      elev.setLocation(0,500);

      // Menu for Elev Plot
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(elev)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');

      // Menu bar for Elev Plot
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);

      elev.setJMenuBar(menuBar);

    }

    /**
     * Update elev.
     *
     * @param e the e
     */
    public void updateElev(ArrayList<MPoint> e) {
      // TODO: Update to make xaxis distance between points instead of stationID
      if(e != null && e.size() >0){
        int n = e.size();
        float[] x = new float[n];
        float[] y = new float[n];
        for (int i = 0; i < n; ++i) {
          MPoint p = e.get(i);
          x[i] = p.getStation();
          y[i] = (float) p.getElev();
        }
        elev.setHLimits(min(x) - 10, max(x) + 10);
        elev.setVLimits(min(y) - 50, max(y) + 50);
        if(pv == null){
          pv = elev.addPoints(x,y);
        }
        else{
          pv.set(x, y);
        }
      }
    }
  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The Class RoamMode.
   * 
   * <p> Mode that allows for click and drag exploration of imported seismic data. 
   * 
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 13, 2014
   */
  private class RoamMode extends Mode {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new roam mode.
     *
     * @param modeManager the mode manager
     */
    public RoamMode(ModeManager modeManager) {
      super(modeManager);
      setName("Roaming Mode");
      setIcon(loadIcon(SeisPlot.class,"Roam16.png"));
      setMnemonicKey(KeyEvent.VK_R);
      setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
      setShortDescription("Roaming Mode");
    }

    /** The slider frame. */
    private JFrame sliderFrame;
    
    /** The slider panel. */
    private JPanel sliderPanel;
    
    /** The slider near. */
    private JSlider sliderNear;
    private JLabel  nearLabel;
    
    /** The sum dist. */
    private int sumDist = 0;

    /** The nearest. */
    private MPoint nearest;

    // When this mode is activated (or deactivated) for a tile, it simply
    // adds (or removes) its mouse listener to (or from) that tile.
    /* (non-Javadoc)
     * @see edu.mines.jtk.awt.Mode#setActive(java.awt.Component, boolean)
     */
    protected void setActive(Component component, boolean active) {
      if (component instanceof Tile) {
        if (active) {
          component.addMouseListener(_ml);
          if(sliderFrame == null){
            sliderFrame = new JFrame();
            sliderFrame.setTitle("Sum Slider (m)");
            sliderFrame.setSize(250,100);
            sliderFrame.setLocation(100,500);
            sliderPanel = new JPanel();
            sliderFrame.add(sliderPanel);

            sliderNear = new JSlider(JSlider.HORIZONTAL,0,1000,0);
            sliderNear.setMajorTickSpacing(200);
            sliderNear.setMinorTickSpacing(50);
            sliderNear.setPaintTicks(true);
            sliderNear.setPaintLabels(true);
            sliderNear.setSize(250,100);
            sliderPanel.add(sliderNear);
            nearLabel = new JLabel("Sum Distance", JLabel.CENTER);
            nearLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sliderPanel.add(nearLabel);
          }
          sliderNear.addChangeListener(cl);
          sliderFrame.setVisible(true);
        } else {
          component.removeMouseListener(_ml);
          sliderFrame.setVisible(false);
          sliderNear.removeChangeListener(cl);
        }
      }
    }

    /** The _moving. */
    private boolean _moving; // if true, currently moving
    
    /** The _tile. */
    private Tile _tile; // tile in which editing began

    /** The _ml. */
    private MouseListener _ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (beginMove(e)) {
          _moving = true;
          _tile.addMouseMotionListener(_mml);
        }
      }

      public void mouseReleased(MouseEvent e) {
        _tile.removeMouseMotionListener(_mml);
        endMove(e);
        _moving = false;
      }
    };

    /** The cl. */
    private ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
          sumDist = sliderNear.getValue();
          _bp.drawCircle(nearest, sumDist);
          updatePlot();
          nearLabel.setText("Sum Distance: "+sumDist+"m");
      }
    };

    // Handles mouse dragged events.
    /** The _mml. */
    private MouseMotionListener _mml = new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (_moving)
          duringMove(e);
      }
    };

    /**
     * Begin move.
     *
     * @param e the e
     * @return true, if successful
     */
    private boolean beginMove(MouseEvent e) {
      _tile = (Tile) e.getSource();
      return true;
    }

    /**
     * During move.
     *
     * @param e the e
     */
    private void duringMove(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      ArrayList<MPoint> gpsPlot = new ArrayList<MPoint>(0);
      ArrayList<Segdata> segPlot = new ArrayList<Segdata>(0);
      nearest = getNearestGPS(x, y);
      Segdata segNear = getNearestSegdata(nearest.getStation());
      
      gpsPlot.add(nearest);
      segPlot.add(segNear);
      if(sumDist > 0){
        gpsPlot = _bp.gpsWithinRange(nearest, sumDist);
        segPlot = _bp.segWithinRange(gpsPlot);
      }
      _bp.drawCurrentSeg(segPlot);
      _bp.plotActiveReceivers(segPlot);
      _bp.drawCircle(nearest, sumDist);
      //_bp.drawCurrentGPS(gpsPlot);

      _rp.updateRP(segPlot);
    }

    /**
     * Update plot.
     */
    private void updatePlot() {
      ArrayList<MPoint> gpsPlot = new ArrayList<MPoint>(0);
      ArrayList<Segdata> segPlot = new ArrayList<Segdata>(0);
      Segdata segNear = getNearestSegdata(nearest.getStation());
      
      gpsPlot.add(nearest);
      segPlot.add(segNear);
      if(sumDist > 0){
        gpsPlot = _bp.gpsWithinRange(nearest, sumDist);
        segPlot = _bp.segWithinRange(gpsPlot);
      }
      _bp.drawCurrentSeg(segPlot);
      _bp.plotActiveReceivers(segPlot);
      //_bp.drawCurrentGPS(gpsPlot);

      _rp.updateRP(segPlot);
    }

    /**
     * End move.
     *
     * @param e the e
     */
    private void endMove(MouseEvent e) {
      duringMove(e);
    }

    /**
     * Gets the nearest gps.
     *
     * @param x the x
     * @param y the y
     * @return the nearest gps
     */
    private MPoint getNearestGPS(int x, int y) {
      if(_gps!=null && _gps.size()>0){
        Transcaler ts = _tile.getTranscaler();
        Projector hp = _tile.getHorizontalProjector();
        Projector vp = _tile.getVerticalProjector();
        double xu = ts.x(x);
        double yu = ts.y(y);
        double xv = hp.v(xu);
        double yv = vp.v(yu);
        MPoint test = new MPoint(xv, yv, true);
        MPoint near = _gps.get(0);
        MPoint fin = _gps.get(0);
        double d = near.xyDist(test);
        for (int i = 1; i < _gps.size(); ++i) {
          near = _gps.get(i);
          if (near.xyDist(test) < d) {
            fin = _gps.get(i);
            d = fin.xyDist(test);
          }
        }
        return fin;
      }
      return null;  
    }

    /**
     * Gets the nearest segdata.
     *
     * @param stationID the station id
     * @return the nearest segdata
     */
    private Segdata getNearestSegdata(int stationID) {
      if(_segd!=null && _segd.size()>0){
        Segdata seg1 = _segd.get(0);
        Segdata seg2 = _segd.get(0);
        int d1 = abs(seg1.getSP() - stationID);
        for (int i = 1; i < _segd.size(); ++i) {
          seg2 = _segd.get(i);
          int d2 = abs(seg2.getSP() - stationID);
          if (d2 < d1) {
            seg1 = seg2;
            d1 = abs(seg1.getSP() - stationID);
          }
        }
        return seg1;
      }
      return null;
    }

  }

  // /////////////////////////////////////////////////////////////////////////

  /**
   * The Class ChannelMode.
   * 
   * <p> Mode that allows for dynamic view of channels using a slider
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 13, 2014
   */
  private class ChannelMode extends Mode {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new channel mode.
     *
     * @param modeManager the mode manager
     */
    public ChannelMode(ModeManager modeManager) {
      super(modeManager);
      setName("Channel Mode");
      setIcon(loadIcon(SeisPlot.class,"Chan16.png"));
      setShortDescription("Display a Channel Mode");
    }

    /** The slider frame. */
    private JFrame sliderFrame;
    
    /** The slider panel. */
    private JPanel sliderPanel;
    
    /** The slider chan. */
    private JSlider sliderChan;
    private JLabel  chanLabel;

    /* (non-Javadoc)
     * @see edu.mines.jtk.awt.Mode#setActive(java.awt.Component, boolean)
     */
    protected void setActive(Component component, boolean active) {
      if (active) {
        if(sliderFrame == null){
          sliderFrame = new JFrame();
          sliderFrame.setTitle("Channel Slider");
          sliderFrame.setSize(250,100);
          sliderFrame.setLocation(100,500);
          sliderPanel = new JPanel();
          sliderFrame.add(sliderPanel);

          sliderChan = new JSlider(JSlider.HORIZONTAL,0,getMaxNumChan(_segd),0);
          sliderChan.setMajorTickSpacing(50);
          sliderChan.setPaintTicks(true);
          sliderChan.setPaintLabels(true);
          sliderChan.setSize(200,100);
          sliderPanel.add(sliderChan);
          chanLabel = new JLabel("Channel Number", JLabel.CENTER);
          chanLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
          sliderPanel.add(chanLabel);
        }
        sliderChan.setMaximum(getMaxNumChan(_segd));
        sliderChan.addChangeListener(cl);
        sliderFrame.setVisible(true);
      } else {
        sliderFrame.setVisible(false);
        sliderChan.removeChangeListener(cl);
      }
    }

    /** The cl. */
    private ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
          adjust(sliderChan.getValue());
          chanLabel.setText("Channel Number: "+sliderChan.getValue());
      }
    };

    /**
     * Adjust.
     *
     * @param chan the chan
     */
    private void adjust(int chan){
      if(_segd != null && _segd.size()>0){
        _rp.updateRP(_segd,chan);
      }
    }

  }

// /////////////////////////////////////////////////////////////////////////

  /**
   * The Class NoGPSMode.
   * 
   * <p> Mode that allows for dynamic view of shots without GPS data imported.
   *
   * @author Colton Kohnke, Colorado School of Mines
   * @version 1.0
   * @since April 26, 2014
   */
  private class NoGPSMode extends Mode {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new no GPS mode.
     *
     * @param modeManager the mode manager
     */
    public NoGPSMode(ModeManager modeManager) {
      super(modeManager);
      setName("No GPS Mode");
      setIcon(loadIcon(SeisPlot.class,"NoGPS16.png"));
      setShortDescription("Explore Without GPS");
    }

    /** The slider frame. */
    private JFrame sliderFrame;
    
    /** The slider panel. */
    private JPanel sliderPanel;
    
    /** The slider chan. */
    private JSlider sliderShot;
    private JSlider sliderNum;
    private JLabel  sumLabel;
    private JLabel  shotLabel;

    /* (non-Javadoc)
     * @see edu.mines.jtk.awt.Mode#setActive(java.awt.Component, boolean)
     */
    protected void setActive(Component component, boolean active) {
      if (active) {
        if(sliderFrame == null){
          sliderFrame = new JFrame();
          sliderFrame.setTitle("Shot Slider");
          sliderFrame.setSize(250,180);
          sliderFrame.setLocation(100,500);
          sliderPanel = new JPanel();
          sliderFrame.add(sliderPanel);

          sliderShot = new JSlider(JSlider.HORIZONTAL,0,1,0);
          sliderShot.setMajorTickSpacing(50);
          sliderShot.setPaintTicks(true);
          sliderShot.setPaintLabels(true);
          sliderShot.setSize(200,100);
          sliderPanel.add(sliderShot);
          shotLabel = new JLabel("Shot Number", JLabel.CENTER);
          shotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
          sliderPanel.add(shotLabel);

          sliderNum = new JSlider(JSlider.HORIZONTAL,0,100,1);
          sliderNum.setMajorTickSpacing(_segd.size()/4);
          sliderNum.setPaintTicks(true);
          sliderNum.setPaintLabels(true);
          sliderNum.setSize(200,100);
          sliderPanel.add(sliderNum);
          sumLabel = new JLabel("Sum Nearest", JLabel.CENTER);
          sumLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
          sliderPanel.add(sumLabel);
        }
        sliderShot.setMinimum(minShot(_segd));
        sliderShot.setMaximum(maxShot(_segd));
        sliderShot.addChangeListener(cl);

        sliderNum.setMinimum(1);
        sliderNum.setMaximum(_segd.size());
        sliderNum.addChangeListener(cl);
        sliderFrame.setVisible(true);
      } else {
        sliderFrame.setVisible(false);
        sliderShot.removeChangeListener(cl);
      }
    }

    /** The cl. */
    private ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
          adjust(sliderShot.getValue(), sliderNum.getValue());
          shotLabel.setText("Shot Number: "+sliderShot.getValue());
          sumLabel.setText("Sum Nearest: "+sliderNum.getValue());
      }
    };

    /**
     * Adjust.
     *
     * @param chan the chan
     */
    private void adjust(int shot, int sum){
      if(_segd != null && _segd.size()>0){
        ArrayList<Segdata> tmp = new ArrayList<Segdata>(0);
        for(Segdata s:_segd){
          if(abs(s.getSP()-shot)<=(sum-1)){
            tmp.add(s);
          }
        }
        _rp.updateRP(tmp);
      }
    }

  }

// /////////////////////////////////////////////////////////////////////////

  /**
 * The Class CircleMode.
 * 
 * <p> Mode that allows for the dynamic exploration of shot records based 
 * on a circle that is created by clicking and dragging on the base map.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
private class CircleMode extends Mode {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new circle mode.
     *
     * @param modeManager the mode manager
     */
    public CircleMode(ModeManager modeManager) {
      super(modeManager);
      setName("Circle Mode");
      setIcon(loadIcon(SeisPlot.class,"Circle16.png"));
      setShortDescription("Display within a Circle");
    }

    /* (non-Javadoc)
     * @see edu.mines.jtk.awt.Mode#setActive(java.awt.Component, boolean)
     */
    protected void setActive(Component component, boolean active) {
      if (active) {
        component.addMouseListener(_ml);
      } else {
        component.removeMouseListener(_ml);
      }
    }

    /** The _ml. */
    private MouseListener _ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(beginMove(e)){
          _tile.addMouseMotionListener(_mml);
        }
      }

      public void mouseReleased(MouseEvent e) {
        endMove(e);
        _tile.removeMouseMotionListener(_mml);
      }
    };

    /** The _mml. */
    private MouseMotionListener _mml = new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Transcaler ts = _tile.getTranscaler();
        Projector hp = _tile.getHorizontalProjector();
        Projector vp = _tile.getVerticalProjector();
        double xu = ts.x(x);
        double yu = ts.y(y);
        double xv = hp.v(xu);
        double yv = vp.v(yu);
        MPoint tmp = new MPoint(xv,yv,true);
        plotCircle(p1, tmp);
      }
    };

    /**
     * Begin move.
     *
     * @param e the e
     * @return true, if successful
     */
    private boolean beginMove(MouseEvent e){
      _tile = (Tile) e.getSource();
      int x = e.getX();
      int y = e.getY();
      Transcaler ts = _tile.getTranscaler();
      Projector hp = _tile.getHorizontalProjector();
      Projector vp = _tile.getVerticalProjector();
      double xu = ts.x(x);
      double yu = ts.y(y);
      double xv = hp.v(xu);
      double yv = vp.v(yu);
      p1 = new MPoint(xv,yv,true);
      return true;
    }

    /**
     * End move.
     *
     * @param e the e
     */
    private void endMove(MouseEvent e){
      int x = e.getX();
      int y = e.getY();
      Transcaler ts = _tile.getTranscaler();
      Projector hp = _tile.getHorizontalProjector();
      Projector vp = _tile.getVerticalProjector();
      double xu = ts.x(x);
      double yu = ts.y(y);
      double xv = hp.v(xu);
      double yv = vp.v(yu);
      p2 = new MPoint(xv,yv,true);
      plotCircle(p1,p2);
    }

    /**
     * Plot circle.
     *
     * @param m1 the m1
     * @param m2 the m2
     */
    private void plotCircle(MPoint m1, MPoint m2){
      _bp.drawCircle(m1,m1.xyDist(m2));
      ArrayList<Segdata> segPlot = _bp.segWithinRange(_bp.gpsWithinRange(m1, m1.xyDist(m2)));
      if(segPlot.size()>0){
        _bp.drawCurrentSeg(segPlot);
        _bp.plotActiveReceivers(segPlot);
        _rp.updateRP(segPlot);
      }
    }

    /** The p2. */
    private MPoint p1, p2;
    
    /** The _tile. */
    private Tile _tile;
  }

  // /////////////////////////////////////////////////////////////////////////

  // Actions common to both plot frames.
  /**
   * The Class ExitAction.
   */
  private class ExitAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new exit action.
     */
    private ExitAction() {
      super("Exit");
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      System.exit(0);
    }
  }

  /**
   * The Class SaveAsPngAction.
   */
  private class SaveAsPngAction extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** The _plot frame. */
    private PlotFrame _plotFrame;

    /**
     * Instantiates a new save as png action.
     *
     * @param plotFrame the plot frame
     */
    private SaveAsPngAction(PlotFrame plotFrame) {
      super("Save as PNG");
      _plotFrame = plotFrame;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(_plotFrame);
      File file = fc.getSelectedFile();
      if (file != null) {
        String filename = file.getAbsolutePath();
        _plotFrame.paintToPng(300, 6, filename);
      }
    }
  }

  /**
   * The Class GetDEM.
   */
  private class GetDEM extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new gets the dem.
     *
     * @param plotPanel the plot panel
     */
    private GetDEM(PlotPanel plotPanel) {
      super("Import NED Files");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      try{
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setFileFilter(new FileNameExtensionFilter("GridFloat File", "flt"));
        fc.showOpenDialog(null);
        File f = fc.getSelectedFile();
        if(f!=null){
          if(_nedFiles==null)
            _nedFiles = new ArrayList<NedFile>(0);
          _nedFiles.add(importNed(f));
        } 
      } catch(Exception e){
        System.out.println(e);
      }
    }
  }
  
  /**
   * The Class ReadNedElevation.
   */
  private class ReadNedElevation extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new read ned elevation.
     *
     * @param plotPanel the plot panel
     */
    private ReadNedElevation(PlotPanel plotPanel) {
      super("Read Elevation from NED");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event){
      try{
        if(_nedFiles.size() > 0){
          for(NedFile f:_nedFiles){
            readNed(f,_gps);
          }
          _elev.updateElev(_gps);
        }
      } catch (Exception e){
        System.out.println(e);
      }
    }
  }


  /**
   * The Class GetFlagsFromHH.
   */
  private class GetFlagsFromHH extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new gets the flags from hh.
     */
    private GetFlagsFromHH() {
      super("Get HandHeld GPS");
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showOpenDialog(null);
      fc.setFileFilter(new FileNameExtensionFilter("TSV,CSV, or GPX File", "txt", "csv", "asc", "gpx"));
      File f = fc.getSelectedFile();
      if(f != null){
        String ext = "";
        int i = f.getName().lastIndexOf('.');
        if (i > 0) {
          ext = f.getName().substring(i + 1);
        }
        if (ext.equals("gpx")){
          _gps = readLatLonFromXML(f);
        } else if (ext.equals("csv")){
          _gps = readLatLonFromCSV(f);
        } else {
          _gps = readLatLonFromTSV(f);
        }
        Waypoints.latLonToUTM(_gps);
        // Waypoints.extrapolateGPS(_gps);
        _bp.updateBPView();
        _elev.updateElev(_gps);
      }
    }
  }

  /**
   * The Class ExportFlagsToCSV.
   */
  private class ExportFlagsToCSV extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new export flags to csv.
     */
    private ExportFlagsToCSV() {
      super("Export GPS to CSV");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(null);
      File f = fc.getSelectedFile();
      if(f!=null){
        Waypoints.exportToCSV(_gps, f);
      }
    }
  }

  /**
   * The Class ImportSegdDir.
   */
  private class ImportSegdDir extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new import segd dir.
     */
    private ImportSegdDir() {
      super("Import SEGD Directory");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.showSaveDialog(null);
      File f = fc.getSelectedFile();
      if(f!=null){
        ArrayList<Segdata> tmp = Segd.readLineSegd(f.getAbsolutePath());
        if(_segd == null){
          _segd = new ArrayList<Segdata>(0);
        }
        for(Segdata s:tmp){
          _segd.add(s);
        }
        System.out.println("SEGD IMPORTED");
      }
    }

  }

  /**
   * The Class ImportSegdFile.
   */
  private class ImportSegdFile extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new import segd file.
     */
    private ImportSegdFile() {
      super("Import SEGD File(s)");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      try{
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new FileNameExtensionFilter("SEGD Files", "segd"));
        fc.showSaveDialog(null);
        File[] f = fc.getSelectedFiles();
        if(f!=null){
          ArrayList<Segdata> tmp = new ArrayList<Segdata>(0);
          for(int i=0; i<f.length; ++i){
            Segdata ts = readSegd(f[i]);
            tmp.add(ts);
          }
          if(_segd == null){
            _segd = new ArrayList<Segdata>(0);
          }
          for(Segdata s:tmp){
            _segd.add(s);
          }
          System.out.println("SEGD IMPORTED");
        }
      } catch(Exception e){
        System.out.println(e);
      }
    }
  }


 /**
  * The Class ClearData.
  */
 private class ClearData extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new clear data.
     */
    private ClearData() {
      super("Clear Data");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      if(_segd != null && _segd.size()>0){
        _segd.removeAll(_segd);
      } if(_gps != null && _gps.size()>0){
        _gps.removeAll(_gps);
      } if(_nedFiles != null && _nedFiles.size()>0){
        _nedFiles.removeAll(_nedFiles);
      }
      System.out.println("Cleared Data from GPS, NED and SEGD files");
    }
  }

 /**
  * The Class ShowPlotSettings.
  */
 private class ShowPlotSettings extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new show plot settings.
     */
    private ShowPlotSettings() {
      super("Plot Controls");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      _rp.showPlotSlider();
    }
  }

 /**
  * The Class ShowPlotSettings.
  */
 private class DownloadNedFile extends AbstractAction {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new show plot settings.
     */
    private DownloadNedFile() {
      super("Download NED Zip Archive");

    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
      Object[] opt = {"Continue","Cancel"};
      int k = JOptionPane.showOptionDialog(null,
        "Warning, this may take a long time.",
        "Warning: Time Conflict",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE,
        null,
        opt,
        opt[1]);
      if(k==0){
        String ns = JOptionPane.showInputDialog(
          "Enter North Latitude (Integer)"
          );
        int n = Integer.parseInt(ns);
        String ws = JOptionPane.showInputDialog(
          "Enter West Longitude (Integer)"
          );
        int w = Integer.parseInt(ws);
        try{
          JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
          fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fc.showSaveDialog(null);
          File f = fc.getSelectedFile();
          if(f!=null){
            NedFileDownloader.get(n,w,f.getAbsolutePath());
          }
        }catch(IOException e){
          e.printStackTrace();
        }
      }
    }
  }

  // /////////////////////////////////////////////////////////////////////////

}
