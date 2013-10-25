import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import java.util.Scanner;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.*;
import edu.mines.jtk.util.Cdouble;
import edu.mines.jtk.mosaic.*;
import static edu.mines.jtk.util.ArrayMath.*;

public class PlotTest{

  public static void main(String[] args){
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new PlotTest();
      }
    });
  }

  // Location and size of overlay plot.
  private static final int M_X = 100;
  private static final int M_Y = 0;
  private static final int M_WIDTH = 520;
  private static final int M_HEIGHT = 550;

  // Location and size of response plot.
  private static final int RP_X = M_X+M_WIDTH;
  private static final int RP_Y = 0;
  private static final int RP_WIDTH = 520;
  private static final int RP_HEIGHT = 550;
  
  // Plot of source/receivers
  // private ArrayList<MPoint> _shots;
  private ArrayList<MPoint> _recs;
  public ArrayList<MPoint> _gps;
  private BasePlot _bp;
  private ResponsePlot _rp;

  private PlotTest(){
    // _shots = new ArrayList<MPoint>(0);
    _gps = new ArrayList<MPoint>(0);
    _bp = new BasePlot();
    _rp = new ResponsePlot();
  }

  private void addMPoint(MPoint p) {
    _recs.add(p);
    _bp.updateBPView();
  }


///////////////////////////////////////////////////////////////////////////

  private class BasePlot {

    private PlotFrame _plotFrame;
    private PlotPanel _plotPanel;
    private PointsView _baseView;
    
    private BasePlot() {

      // The plot panel.
      _plotPanel = new PlotPanel();
      _plotPanel.setTitle("Base Plot Test");
      _plotPanel.setHLabel("Easting (UTM)");
      _plotPanel.setVLabel("Northing (UTM)");
      _plotPanel.setHLimits(100,200); //TODO: plot displays E+06 for large ints
      _plotPanel.setVLimits(100,200);   //TODO: plot displays E+06 for large ints

      // A grid view for horizontal and vertical lines (axes).
      _plotPanel.addGrid("H0-V0-");

      // A plot frame has a mode for zooming in tiles or tile axes.
      _plotFrame = new PlotFrame(_plotPanel);
      TileZoomMode tzm = _plotFrame.getTileZoomMode();

      // We add two more modes for editing poles and zeros.
      ModeManager mm = _plotFrame.getModeManager();
      AddMode am = new AddMode(mm); // for test points
      // PoleZeroMode zm = new PoleZeroMode(mm,false);  // for zeros

      // The menu bar includes a mode menu for selecting a mode.
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');
      
      JMenu modeMenu = new JMenu("Mode");
      modeMenu.setMnemonic('M');
      modeMenu.add(new ModeMenuItem(tzm));
      modeMenu.add(new ModeMenuItem(am));
      
      JMenu toolMenu = new JMenu("Tools");
      toolMenu.setMnemonic('T');
      toolMenu.add(new GetFlagsFromHH()).setMnemonic('f');
      toolMenu.add(new GetDEM(_plotPanel)).setMnemonic('g');
      toolMenu.add(new ExportFlagsToCSV()).setMnemonic('e');
      
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(modeMenu);
      menuBar.add(toolMenu);
    
      _plotFrame.setJMenuBar(menuBar);

      // The tool bar includes toggle buttons for selecting a mode.
      JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
      toolBar.setRollover(true);
      toolBar.add(new ModeToggleButton(tzm));
      toolBar.add(new ModeToggleButton(am));
      _plotFrame.add(toolBar,BorderLayout.WEST);

      // Initially, enable editing of poles.
      // pm.setActive(true);

      // Make the plot frame visible.
      _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _plotFrame.setLocation(M_X,M_Y);
      _plotFrame.setSize(M_WIDTH,M_HEIGHT);
      _plotFrame.setFontSizeForPrint(8,240);
      _plotFrame.setVisible(true);

    }

    // Makes poles view consistent with the list of poles.
    private void updateBPView() {
      int np = _gps.size();
      float[] xp = new float[np];
      float[] yp = new float[np];
      for (int ip=0; ip<np; ++ip) {
        MPoint p = _gps.get(ip);
        xp[ip] = (float)p.x;
        yp[ip] = (float)p.y;
      }
      if (_baseView==null) {
        _baseView = _plotPanel.addPoints(xp,yp);
        _baseView.setMarkStyle(PointsView.Mark.CROSS);
        _baseView.setLineStyle(PointsView.Line.NONE);
      } else {
        _baseView.set(xp,yp);
      }
    }

 }

  ///////////////////////////////////////////////////////////////////////////

  private class ResponsePlot {

    private PlotPanel _plotPanelH;
    private PlotFrame _plotFrame;
    private SequenceView _hView;
    private PointsView _pView;

    // The amplitude response can be in decibels (db).
    private ResponsePlot() {

      // One plot panel for the impulse response.
      _plotPanelH = new PlotPanel();
      _plotPanelH.setHLabel("Easting (UTM)");
      _plotPanelH.setVLabel("Time (s)");
      _plotPanelH.setTitle("Title");

      // This first update constructs a sequence view for the impulse 
      // response, and a points view for amplitude and phase responses.
      // updateViews();

      _plotFrame = new PlotFrame(_plotPanelH);

      // The menu bar.
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      
      _plotFrame.setJMenuBar(menuBar);

      // Make the plot frame visible.
      _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _plotFrame.setLocation(RP_X,RP_Y);
      _plotFrame.setSize(RP_WIDTH,RP_HEIGHT);
      _plotFrame.setFontSizeForPrint(8,240);
      _plotFrame.setVisible(true);
    }

  }

  ///////////////////////////////////////////////////////////////////////////

  private class AddMode extends Mode {
    public AddMode(ModeManager modeManager) {
      super(modeManager);
        setName("Add Receivers");
        // setIcon(loadIcon(PolesAndZerosDemo.class,"Poles16.png"));
        setMnemonicKey(KeyEvent.VK_X);
        setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_X,0));
        setShortDescription("Add (Shift)");
      }
    
    // When this mode is activated (or deactivated) for a tile, it simply 
    // adds (or removes) its mouse listener to (or from) that tile.
    protected void setActive(Component component, boolean active) {
      if (component instanceof Tile) {
        if (active) {
          component.addMouseListener(_ml);
        } else {
          component.removeMouseListener(_ml);
        }
      }
    }
  }

    private boolean _editing; // true, if currently editing
    private Tile _tile; // tile in which editing began

    // Handles mouse pressed and released events.
    private MouseListener _ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.isShiftDown()) {
          add(e);
        } else {
          if (beginEdit(e)) {
            _editing = true;
            _tile.addMouseMotionListener(_mml);
          }
        }
      }
      public void mouseReleased(MouseEvent e) {
        if (_editing) {
          _tile.removeMouseMotionListener(_mml);
          endEdit(e);
          _editing = false;
        }
      }
    };

  // Handles mouse dragged events.
    private MouseMotionListener _mml = new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (_editing)
          duringEdit(e);
      }
    };

    // Adds a pole or zero at mouse coordinates (x,y).
    private void add(MouseEvent e) {
      _tile = (Tile)e.getSource();
      double x = e.getX();
      double y = e.getY();
      MPoint p = new MPoint(1,x,y);
      System.out.println("p.x: " + p.x + " p.y: " + p.y);
      addMPoint(p);
    }
      
    // Begins editing of an existing pole or zero, if close enough.
    // Returns true, if close enough so that we have begun editing; 
    // false, otherwise.
    private boolean beginEdit(MouseEvent e) {
      _tile = (Tile)e.getSource();
      int x = e.getX();
      int y = e.getY();
      return false;
    }

  private void duringEdit(MouseEvent e) {
      int x = e.getX();
      int y = e.getY();
      // Cdouble z = pointToComplex(x,y);
      // if (_poles) {
      //  movePole(_zedit,z);
      // } else {
      //  moveZero(_zedit,z);
      // }
      // _zedit = z;
    }

    // Called when done editing a pole or zero.
    private void endEdit(MouseEvent e) {
      duringEdit(e);
      _editing = false;
    } 
 
  ///////////////////////////////////////////////////////////////////////////
  
  // Actions common to both plot frames.
  private class ExitAction extends AbstractAction {
    private ExitAction() {
      super("Exit");
    }
    public void actionPerformed(ActionEvent event) {
      System.exit(0);
    }
  }
  private class SaveAsPngAction extends AbstractAction {
    private PlotFrame _plotFrame;
    private SaveAsPngAction(PlotFrame plotFrame) {
      super("Save as PNG");
      _plotFrame = plotFrame;
    }
    public void actionPerformed(ActionEvent event) {
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(_plotFrame);
      File file = fc.getSelectedFile();
      if (file!=null) {
        String filename = file.getAbsolutePath();
        _plotFrame.paintToPng(300,6,filename);
      }
    }
  }
  private class GetDEM extends AbstractAction {
    private GetDEM(PlotPanel plotPanel){
      super("Get USGS Elevation");
     
    }
    public void actionPerformed(ActionEvent event){
      //TODO
    }
  }
  private class GetFlagsFromHH extends AbstractAction {
    private GetFlagsFromHH(){
      super("Get HandHeld GPS");
        
    }
    public void actionPerformed(ActionEvent event) {
      //TODO
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showOpenDialog(null);
      try{
        File f = fc.getSelectedFile();
        Scanner s = new Scanner(f);
        s.nextLine(); // header skip = 1
        while(s.hasNext()){
          int stationID = s.nextInt();
          double x = s.nextDouble();
          double y = s.nextDouble();
          double z = s.nextDouble();
          System.out.println("ID: " + stationID + " x: " + x + " y: " + y + " z: " + z);
          MPoint p = new MPoint(stationID, x, y, z);
          _gps.add(p);
          System.out.println(_gps.get(_gps.size()-1).stationID);
        }
        s.close();
        _bp.updateBPView(); 
      } catch(IOException ex){
        System.out.println(ex);  
      }
    }
  }
  private class ExportFlagsToCSV extends AbstractAction {
    private ExportFlagsToCSV(){
      super("Export GPS to CSV");
        
    }
    public void actionPerformed(ActionEvent event) {
      //TODO
      JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
      fc.showSaveDialog(null);
      File file = fc.getSelectedFile();
      if (file!=null) {
        String filename = file.getAbsolutePath();
        
      }

    }
  }


  ///////////////////////////////////////////////////////////////////////////


  public class MPoint {
    // from xyz coord
    MPoint(int stationID, double x, double y, double z){
      this.stationID = stationID;
      this.x = x; 
      this.y = y;
      this.z = z;
    }
    
    // from xy coord
    MPoint(int stationID, double x, double y){
      this.stationID = stationID;
      this.x = x; 
      this.y = y;
    }
    
    public int stationID;
    public double x, y, z;
    public boolean selected;
  }

}

