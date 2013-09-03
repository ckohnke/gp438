import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

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
  private static final int O_X = 100;
  private static final int O_Y = 0;
  private static final int O_WIDTH = 520;
  private static final int O_HEIGHT = 550;

  // Location and size of response plot.
  private static final int RP_X = O_X+O_WIDTH;
  private static final int RP_Y = 0;
  private static final int RP_WIDTH = 520;
  private static final int RP_HEIGHT = 550;
  
  // Plot of source/receivers
  private ArrayList<Cdouble> _shots;
  private ArrayList<Cdouble> _recs;
  private BasePlot _bp;
  private ResponsePlot _rp;

  private PlotTest(){
    _shots = new ArrayList<Cdouble>(0);
    _recs = new ArrayList<Cdouble>(0);
    _bp = new BasePlot();
    _rp = new ResponsePlot();
  }


///////////////////////////////////////////////////////////////////////////

  private class BasePlot {

    private PlotFrame _plotFrame;
    private PlotPanel _plotPanel;
    
    private BasePlot() {

      // The plot panel.
      _plotPanel = new PlotPanel();
      _plotPanel.setTitle("Base Plot Test");
      _plotPanel.setHLabel("Easting (UTM)");
      _plotPanel.setVLabel("Northing (UTM)");
      _plotPanel.setHLimits(-2.0,2.0);
      _plotPanel.setVLimits(-2.0,2.0);

      // A grid view for horizontal and vertical lines (axes).
      _plotPanel.addGrid("H0-V0-");

      // A plot frame has a mode for zooming in tiles or tile axes.
      _plotFrame = new PlotFrame(_plotPanel);
      TileZoomMode tzm = _plotFrame.getTileZoomMode();

      // We add two more modes for editing poles and zeros.
      ModeManager mm = _plotFrame.getModeManager();
      // PoleZeroMode pm = new PoleZeroMode(mm,true); // for poles
      // PoleZeroMode zm = new PoleZeroMode(mm,false);  // for zeros

      // The menu bar includes a mode menu for selecting a mode.
      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic('F');
      fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
      fileMenu.add(new ExitAction()).setMnemonic('x');
      JMenu modeMenu = new JMenu("Mode");
      modeMenu.setMnemonic('M');
      // modeMenu.add(new ModeMenuItem(tzm));
      // modeMenu.add(new ModeMenuItem(pm));
      // modeMenu.add(new ModeMenuItem(zm));
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(fileMenu);
      menuBar.add(modeMenu);
      _plotFrame.setJMenuBar(menuBar);

      // The tool bar includes toggle buttons for selecting a mode.
      JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
      toolBar.setRollover(true);
      // toolBar.add(new ModeToggleButton(tzm));
      // toolBar.add(new ModeToggleButton(pm));
      /// toolBar.add(new ModeToggleButton(zm));
      _plotFrame.add(toolBar,BorderLayout.WEST);

      // Initially, enable editing of poles.
      // pm.setActive(true);

      // Make the plot frame visible.
      _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _plotFrame.setLocation(O_X,O_Y);
      _plotFrame.setSize(O_WIDTH,O_HEIGHT);
      _plotFrame.setFontSizeForPrint(8,240);
      _plotFrame.setVisible(true);
    }
  }

  ///////////////////////////////////////////////////////////////////////////

private class ResponsePlot {

    private PlotPanel _plotPanelH;
    private PlotPanel _plotPanelAP;
    private PlotFrame _plotFrame;
    private SequenceView _hView;
    private PointsView _aView;
    private PointsView _pView;

    // The amplitude response can be in decibels (db).
    private ResponsePlot() {

      // One plot panel for the impulse response.
      _plotPanelH = new PlotPanel();
      _plotPanelH.setHLabel("Easting (UTM)");
      _plotPanelH.setVLabel("Time (ms)");
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

}

