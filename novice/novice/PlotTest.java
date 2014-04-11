package novice;

import static edu.mines.jtk.util.ArrayMath.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.*;

import static novice.Segd.*;
import static novice.Waypoints.*;

public class PlotTest {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new PlotTest();
      }
    });
  }

  // TODO
  // Location Plot
  // Display common channel
  // Display shot range (assume same active receiver)

  // Location and size of overlay plot.
  private static final int M_X = 0;
  private static final int M_Y = 0;
  private static final int M_WIDTH = 500;
  private static final int M_HEIGHT = 500;

  // Location and size of response plot.
  // private static final int RP_X = M_X + M_WIDTH;
  // private static final int RP_Y = 0;
  // private static final int RP_WIDTH = 520;
  // private static final int RP_HEIGHT = 550;

  // Plot of source/receivers
  // private ArrayList<MPoint> _shots;
  // private ArrayList<MPoint> _recs;
  public static ArrayList<MPoint> _gps;
  public ArrayList<Segdata> _segd;
  public BasePlot _bp;
  public ResponsePlot _rp;
  public ElevPlot _elev;

  // Sliders
  public JSlider gainSlider;
  public JSlider lowpassSlider;
  public JSlider tpowSlider;

  private PlotTest() {
    // _shots = new ArrayList<MPoint>(0);
    // _gps = new ArrayList<MPoint>(0);
    _segd = new ArrayList<Segdata>(0);
    _bp = new BasePlot();
    _rp = new ResponsePlot();
    _elev = new ElevPlot();
  }

  // /////////////////////////////////////////////////////////////////////////

  private class BasePlot {

    private PlotFrame _plotFrame;
    private PlotPanel _plotPanel;
    private PointsView _baseView;
    private PointsView _redView;
    private PointsView _blueView;
    private PointsView _greenView;

    private BasePlot() {

      // The plot panel.
      _plotPanel = new PlotPanel();
      _plotPanel.setTitle("Base Plot Test");
      _plotPanel.setHLabel("Easting (UTM)");
      _plotPanel.setVLabel("Northing (UTM)");
      _plotPanel.setHLimits(317600, 320600); // TODO: plot displays E+06
                          // for large ints
      _plotPanel.setVLimits(4121800, 4123600); // TODO: plot displays E+06
                            // for large ints

      // A grid view for horizontal and vertical lines (axes).
      _plotPanel.addGrid("H0-V0-");

      // A plot frame has a mode for zooming in tiles or tile axes.
      _plotFrame = new PlotFrame(_plotPanel);
      TileZoomMode tzm = _plotFrame.getTileZoomMode();

      // We add two more modes for editing poles and zeros.
      ModeManager mm = _plotFrame.getModeManager();
      RoamMode rm = new RoamMode(mm); // roam and plot
      CircleMode om = new CircleMode(mm);
      ChannelMode cm = new ChannelMode(mm);
      // PoleZeroMode zm = new PoleZeroMode(mm,false); // for zeros

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

      JMenu toolMenu = new JMenu("Tools");
      toolMenu.setMnemonic('T');
      toolMenu.add(new GetFlagsFromHH()).setMnemonic('f');
      toolMenu.add(new GetDEM(_plotPanel)).setMnemonic('g');
      toolMenu.add(new ExportFlagsToCSV()).setMnemonic('e');
      toolMenu.add(new ImportSegdDir()).setMnemonic('s');
      toolMenu.add(new ImportSegdFile()).setMnemonic('d');

      JMenu testMenu = new JMenu("Test");
      testMenu.setMnemonic('E');
      testMenu.add(new DisplayRange()).setMnemonic('g');
      testMenu.add(new ClearSegdata()).setMnemonic('c');
      testMenu.add(new ShowPlotSettings()).setMnemonic('p');

      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(modeMenu);
      menuBar.add(toolMenu);
      menuBar.add(testMenu);

      _plotFrame.setJMenuBar(menuBar);

      // The tool bar includes toggle buttons for selecting a mode.
      JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
      toolBar.setRollover(true);
      toolBar.add(new ModeToggleButton(tzm));
      toolBar.add(new ModeToggleButton(rm));
      toolBar.add(new ModeToggleButton(om));
      toolBar.add(new ModeToggleButton(cm));
      _plotFrame.add(toolBar, BorderLayout.WEST);

      // Make the plot frame visible.
      _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _plotFrame.setLocation(M_X, M_Y);
      _plotFrame.setSize(M_WIDTH, M_HEIGHT);
      _plotFrame.setFontSizeForPrint(8, 240);
      _plotFrame.setVisible(true);

    }

    // Makes points visible
    private void updateBPView() {
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

    private void drawCurrentGPS(ArrayList<MPoint> p){
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

    private void drawCurrentSeg(ArrayList<Segdata> s){
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

    private MPoint getNearestGPSFromSegdata(Segdata s){
      MPoint p = _gps.get(0);
      for(int i=1; i<_gps.size(); ++i){
        if(abs(p.getStation()-s.getSP())>abs(_gps.get(i).getStation()-s.getSP()))
          p = _gps.get(i);
      }
      return p;
    }

    private void plotActiveReceivers(Segdata s){
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

    private void plotActiveReceivers(ArrayList<Segdata> seg){
      for(Segdata s:seg){
        plotActiveReceivers(s);
      }
    }

    private boolean isActive(float[] f){
      int n1 = f.length;
      for(int i=0; i<n1; ++i){
        if(f[i] != 0){
          return true;
        }
      }
      return false;
    }

    public ArrayList<MPoint> gpsWithinRange(MPoint g, double d){
      ArrayList<MPoint> p = new ArrayList<MPoint>(0);
      for(MPoint m:_gps){
        if(g.xyDist(m) <= d){
          p.add(m);
        }
      }
      return p;
    }

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

  }

  // /////////////////////////////////////////////////////////////////////////

  private class ResponsePlot {

    // private PlotPanel _plotPanel;
    // private PlotFrame _plotFrame;
    public SimplePlot sp;
    private PixelsView pv;
    private double gainNum;
    private float tpowNum;
    private double lowpassNum;
    private JFrame sliderFrame;
    private JPanel sliderPanel;
    // The Shot response
    private ResponsePlot() {

      // Makes Sliders for gain, tpow and lowpass      
      sliderFrame = new JFrame();
      sliderFrame.setTitle("Plot Sliders");
      sliderFrame.setSize(250,300);
      sliderFrame.setLocation(100,500);
      sliderPanel = new JPanel();
      sliderFrame.add(sliderPanel);

      gainSlider = new JSlider(JSlider.HORIZONTAL,0,100,40);
      gainSlider.setMajorTickSpacing(20);
      gainSlider.setMinorTickSpacing(1);
      gainSlider.setPaintTicks(true);
      gainSlider.setPaintLabels(true);
      gainSlider.setSize(200,100);
      sliderPanel.add(gainSlider);
      gainSlider.addChangeListener(cl);
      JLabel gainLabel = new JLabel("Gain Controll", JLabel.CENTER);
      gainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(gainLabel);

      lowpassSlider = new JSlider(JSlider.HORIZONTAL,0,100,40);
      lowpassSlider.setMajorTickSpacing(20);
      lowpassSlider.setMinorTickSpacing(1);
      lowpassSlider.setPaintTicks(true);
      lowpassSlider.setPaintLabels(true);
      lowpassSlider.setSize(200,100);
      sliderPanel.add(lowpassSlider);
      lowpassSlider.addChangeListener(cl);
      JLabel lowpassLabel = new JLabel("Low Pass", JLabel.CENTER);
      lowpassLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(lowpassLabel);

      tpowSlider = new JSlider(JSlider.HORIZONTAL,0,100,40);
      tpowSlider.setMajorTickSpacing(20);
      tpowSlider.setMinorTickSpacing(1);
      tpowSlider.setPaintTicks(true);
      tpowSlider.setPaintLabels(true);
      tpowSlider.setSize(200,100);
      sliderPanel.add(tpowSlider);
      tpowSlider.addChangeListener(cl);
      JLabel tpowLabel = new JLabel("Tpow Number", JLabel.CENTER);
      tpowLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      sliderPanel.add(tpowLabel);

      sliderFrame.setVisible(false);

      sp = new SimplePlot(SimplePlot.Origin.UPPER_LEFT);
      sp.setSize(600, 600);
      sp.setVLabel("Time (s)");
      sp.setLocation(500,0);
      pv = null;

    }

      private ChangeListener cl = new ChangeListener(){
        public void stateChanged(ChangeEvent e) {
          if (!gainSlider.getValueIsAdjusting() && !lowpassSlider.getValueIsAdjusting() && !tpowSlider.getValueIsAdjusting() )
            gainNum = gainSlider.getValue();
            lowpassNum = lowpassSlider.getValue();
            tpowNum = tpowSlider.getValue();
        }
      };

    public void updateRP(Segdata seg) {
      int n1 = seg.getF()[0].length;
      int n2 = seg.getF().length;
      Sampling s1 = new Sampling(n1, 0.001, 0.0);
      Sampling s2 = new Sampling(n2, 1.0, seg.getRPF());
      if (s2.getDelta() == 1.0)
        sp.setHLabel("Station");
      else
        sp.setHLabel("Offset (km)");
      sp.setHLimits(seg.getRPF(), seg.getRPL());
      sp.setTitle("Shot " + seg.getSP());
      pv = sp.addPixels(s1, s2, gain2(seg.getF(),gainNum));
      // pv = sp.addPixels(s1, s2, seg.getF()));
      pv.setPercentiles(1, 99);

    }

    public void updateRP(ArrayList<Segdata> s) {
      int n1 = getN1(s);
      int n2 = getN2(s);
      int rpf = getRPF(s);
      int rpl = rpf+n2;
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
      Sampling s1 = new Sampling(n1, 0.001, 0.0);
      Sampling s2 = new Sampling(n2, 1.0, rpf);
      pv = sp.addPixels(s1, s2, gain2(stot,gainNum));
      sp.setHLimits(rpf, rpl);
      sp.setTitle("Brute Combine");
      sp.setHLabel("Station");
      pv.setPercentiles(1, 99);
    }

    public void updateRP(ArrayList<Segdata> s, int channel) {
      ArrayList<Segdata> seg = new ArrayList<Segdata>(0);
      int min = getMinStationID(_gps);
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
      // Start Trusting this more
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
      Sampling s1 = new Sampling(n1, 0.001, 0.0);
      Sampling s2 = new Sampling(n2, 1.0, fsp);
      pv = sp.addPixels(s1, s2, gain2(chan, gainNum));
      // pv = sp.addPixels(s1, s2, (chan));
      sp.setHLimits(fsp, maxShot(seg));
      sp.setTitle("Channel: "+channel);
      sp.setHLabel("Shot");
      pv.setPercentiles(1, 99);
      
      for(Segdata r:seg){
        _bp.plotActiveReceivers(r);
      }
      _bp.drawCurrentSeg(seg);

    }

    private int getN1(ArrayList<Segdata> s){
      int n1 = s.get(0).getF()[0].length;
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t = tmp.getF()[0].length;
        if(t>n1) n1=t;
      }
      return n1;
    }

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

    private int getFirstSP(ArrayList<Segdata> s){
      int s1 = s.get(0).getSP();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getSP();
        if(t1<s1) s1=t1;
      }
      return s1;
    }

    private int getLastSP(ArrayList<Segdata> s){
      int s1 = s.get(0).getSP();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getSP();
        if(t1>s1) s1=t1;
      }
      return s1;
    }

    private int getRPF(ArrayList<Segdata> s){
      int s1 = s.get(0).getRPF();
      for(int i=1; i<s.size(); ++i){
        Segdata tmp = s.get(i);
        int t1 = tmp.getRPF();
        if(t1<s1) s1=t1;
      }
      return s1;
    }

    private boolean isActive(float[] f){
      int n1 = f.length;
      for(int i=0; i<n1; ++i){
        if(f[i] != 0){
          return true;
        }
      }
      return false;
    }

    public void showPlotSlider(){
      sliderFrame.setVisible(true);
    }

  }

  // /////////////////////////////////////////////////////////////////////////

  private class ElevPlot {

    public SimplePlot elev;

    private ElevPlot() {
      elev = new SimplePlot(SimplePlot.Origin.LOWER_LEFT);
      elev.setSize(500, 250);
      elev.setVLabel("meters (m)");
      elev.setHLabel("Station ID");
      elev.setLocation(0,500);
    }

    public void updateElev(ArrayList<MPoint> e) {
      // TODO: Update to make xaxis distance between points instead of
      // stationID
      int n = e.size();
      double[] x = new double[n];
      double[] y = new double[n];
      for (int i = 0; i < n; ++i) {
        MPoint p = e.get(i);
        x[i] = p.getStation();
        y[i] = p.getElev();
      }
      elev.setHLimits(min(x) - 10, max(x) + 10);
      elev.setVLimits(min(y) - 50, max(y) + 50);
      elev.addPoints(x, y);
    }

  }

  // /////////////////////////////////////////////////////////////////////////

  private class RoamMode extends Mode {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoamMode(ModeManager modeManager) {
      super(modeManager);
      setName("Roaming Mode");
      // setIcon(loadIcon(PolesAndZerosDemo.class,"Poles16.png"));
      setMnemonicKey(KeyEvent.VK_R);
      setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
      setShortDescription("Roaming Mode");
    }

    private JFrame sliderFrame;
    private JPanel sliderPanel;
    private JSlider sliderNear;
    private int sumDist = 0;

    // When this mode is activated (or deactivated) for a tile, it simply
    // adds (or removes) its mouse listener to (or from) that tile.
    protected void setActive(Component component, boolean active) {
      if (component instanceof Tile) {
        if (active) {
          component.addMouseListener(_ml);
          if(sliderFrame == null){
            sliderFrame = new JFrame();
            sliderFrame.setTitle("Sum Slider (M)");
            sliderFrame.setSize(250,100);
            sliderFrame.setLocation(100,500);
            sliderPanel = new JPanel();
            sliderFrame.add(sliderPanel);

            sliderNear = new JSlider(JSlider.HORIZONTAL,0,1000,0);
            sliderNear.setMajorTickSpacing(200);
            sliderNear.setMinorTickSpacing(25);
            sliderNear.setPaintTicks(true);
            sliderNear.setPaintLabels(true);
            sliderNear.setSize(250,100);
            sliderPanel.add(sliderNear);
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

    private boolean _moving; // if true, currently moving
    private Tile _tile; // tile in which editing began

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

    private ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        if (!sliderNear.getValueIsAdjusting())
          sumDist = sliderNear.getValue();
      }
    };

    // Handles mouse dragged events.
    private MouseMotionListener _mml = new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (_moving)
          duringMove(e);
      }
    };

    private boolean beginMove(MouseEvent e) {
      _tile = (Tile) e.getSource();
      // int x = e.getX();
      // int y = e.getY();
      // getNearestGPS(x, y);
      return true;
    }

    private void duringMove(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      ArrayList<MPoint> gpsPlot = new ArrayList<MPoint>(0);
      ArrayList<Segdata> segPlot = new ArrayList<Segdata>(0);
      MPoint gpsNear = getNearestGPS(x, y);
      Segdata segNear = getNearestSegdata(gpsNear.getStation());
      
      gpsPlot.add(gpsNear);
      segPlot.add(segNear);
      if(sumDist > 0){
        gpsPlot = _bp.gpsWithinRange(gpsNear, sumDist);
        segPlot = _bp.segWithinRange(gpsPlot);
      }
      _bp.drawCurrentSeg(segPlot);
      _bp.plotActiveReceivers(segPlot);
      //_bp.drawCurrentGPS(gpsPlot);

      _rp.updateRP(segPlot);
    }

    private void endMove(MouseEvent e) {
      duringMove(e);
    }

    private MPoint getNearestGPS(int x, int y) {
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

    private Segdata getNearestSegdata(int stationID) {
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

  }

  // /////////////////////////////////////////////////////////////////////////

  private class ChannelMode extends Mode {
    private static final long serialVersionUID = 1L;

    public ChannelMode(ModeManager modeManager) {
      super(modeManager);
      setName("Channel Mode");
      // setIcon(loadIcon(PolesAndZerosDemo.class,"Poles16.png"));
      // setMnemonicKey(KeyEvent.VK_P);
      // setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
      setShortDescription("Display a Channel Mode");
    }

    private JFrame sliderFrame;
    private JPanel sliderPanel;
    private JSlider sliderChan;

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
        }
        sliderChan.setMaximum(getMaxNumChan(_segd));
        sliderChan.addChangeListener(cl);
        sliderFrame.setVisible(true);
      } else {
        sliderFrame.setVisible(false);
        sliderChan.removeChangeListener(cl);
      }
    }

    private ChangeListener cl = new ChangeListener(){
      public void stateChanged(ChangeEvent e) {
        if (!sliderChan.getValueIsAdjusting())
          adjust(sliderChan.getValue());
      }
};

    private void adjust(int chan){
      _rp.updateRP(_segd,chan);
    }

  }

// /////////////////////////////////////////////////////////////////////////

  private class CircleMode extends Mode {
    private static final long serialVersionUID = 1L;

    public CircleMode(ModeManager modeManager) {
      super(modeManager);
      setName("Circle Mode");
      setShortDescription("Display within a Circle");
    }

    protected void setActive(Component component, boolean active) {
      if (active) {
        count=0;
        component.addMouseListener(_ml);
      } else {
        component.removeMouseListener(_ml);
      }
    }

    private MouseListener _ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        _tile = (Tile) e.getSource();
        addPoint(e);
      }

    };

    private void addPoint(MouseEvent e){
      if(count!=2){
        int x = e.getX();
        int y = e.getY();
        Transcaler ts = _tile.getTranscaler();
        Projector hp = _tile.getHorizontalProjector();
        Projector vp = _tile.getVerticalProjector();
        double xu = ts.x(x);
        double yu = ts.y(y);
        double xv = hp.v(xu);
        double yv = vp.v(yu);
        if(count==0){
          p1 = new MPoint(xv,yv,true); //center of circle
          ++count;
        } else if(count==1){
          p2 = new MPoint(xv,yv,true); //point on radius
          ++count;
        }
      }
      if(count==2){
        ArrayList<Segdata> segPlot = _bp.segWithinRange(_bp.gpsWithinRange(p1, p1.xyDist(p2)));
        _bp.drawCurrentSeg(segPlot);
        _bp.plotActiveReceivers(segPlot);
      //_bp.drawCurrentGPS(gpsPlot);        
        count=0;
        p1=null;
        p2=null;
      }
      
    }

    private MPoint p1, p2;
    private int count;
    private Tile _tile;
  }

  // /////////////////////////////////////////////////////////////////////////

  // Actions common to both plot frames.
  private class ExitAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ExitAction() {
      super("Exit");
    }

    public void actionPerformed(ActionEvent event) {
      System.exit(0);
    }
  }

  private class SaveAsPngAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private PlotFrame _plotFrame;

    private SaveAsPngAction(PlotFrame plotFrame) {
      super("Save as PNG");
      _plotFrame = plotFrame;
    }

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

  private class GetDEM extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private GetDEM(PlotPanel plotPanel) {
      super("Get NED Elevation");

    }

    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showOpenDialog(null);
      File f = fc.getSelectedFile();
      Ned.readElevFromNed(f,_gps);
    }
  }

  private class GetFlagsFromHH extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private GetFlagsFromHH() {
      super("Get HandHeld GPS");
    }

    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showOpenDialog(null);
      File f = fc.getSelectedFile();
      // _gps = Waypoints.readLatLonFromTSV(f);
      String ext = "";
      int i = f.getName().lastIndexOf('.');
      if (i > 0) {
        ext = f.getName().substring(i + 1);
      }
      if (ext.equals("gpx"))
        _gps = Waypoints.readLatLonFromXML(f);
      else if (ext.equals("csv"))
        _gps = Waypoints.readLatLonFromCSV(f);
      else
        _gps = Waypoints.readLatLonFromTSV(f);
      Waypoints.latLonToUTM(_gps);
      // Waypoints.extrapolateGPS(_gps);
      _bp.updateBPView();
      _elev.updateElev(_gps);
    }
  }

  private class ExportFlagsToCSV extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ExportFlagsToCSV() {
      super("Export GPS to CSV");

    }

    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(null);
      File f = fc.getSelectedFile();
      Waypoints.exportToCSV(_gps, f);
    }
  }

  private class ImportSegdDir extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ImportSegdDir() {
      super("Import Segd Directory");

    }

    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fc.showSaveDialog(null);
      File f = fc.getSelectedFile();
      ArrayList<Segdata> tmp = Segd.readLineSegd(f.getAbsolutePath());
      for(int i=0; i<tmp.size(); ++i)
        _segd.add(tmp.get(i));
      System.out.println("SEGD IMPORTED");
    }

  }

  private class ImportSegdFile extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ImportSegdFile() {
      super("Import Segd File(s)");

    }

    public void actionPerformed(ActionEvent event) {
      try{
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(null);
      File[] f = fc.getSelectedFiles();
      ArrayList<Segdata> tmp = new ArrayList<Segdata>(0);
      for(int i=0; i<f.length; ++i){
        Segdata ts = readSegd(f[i]);
        tmp.add(ts);
      }
      for(int i=0; i<tmp.size(); ++i)
        _segd.add(tmp.get(i));
      System.out.println("SEGD IMPORTED");
      } catch(Exception e){
        System.out.println(e);
      }
    }

  }

  private class DisplayRange extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private DisplayRange() {
      super("Display Range");

    }

    public void actionPerformed(ActionEvent event) {
      _rp.updateRP(_segd, 200); //TODO: Write logic for dynamic shots
    }
  }

 private class ClearSegdata extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ClearSegdata() {
      super("Clear Data");

    }

    public void actionPerformed(ActionEvent event) {
      _segd.removeAll(_segd);
      _gps.removeAll(_gps);
    }
  }

 private class ShowPlotSettings extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private ShowPlotSettings() {
      super("Plot Controls");

    }

    public void actionPerformed(ActionEvent event) {
      _rp.showPlotSlider();
    }
  }

  // /////////////////////////////////////////////////////////////////////////

}
