package novice;

import static edu.mines.jtk.util.ArrayMath.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import edu.mines.jtk.awt.*;
import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.mosaic.*;

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
	private static final int M_X = 100;
	private static final int M_Y = 0;
	private static final int M_WIDTH = 600;
	private static final int M_HEIGHT = 600;

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

	private PlotTest() {
		// _shots = new ArrayList<MPoint>(0);
		// _gps = new ArrayList<MPoint>(0);
		_segd = new ArrayList<Segdata>(0);
		_bp = new BasePlot();
		_rp = new ResponsePlot();
		_elev = new ElevPlot();
	}

	// private void addMPoint(MPoint p) {
	// _recs.add(p);
	// _bp.updateBPView();
	// }

	// /////////////////////////////////////////////////////////////////////////

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
			PlayMode pm = new PlayMode(mm);
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
			modeMenu.add(new ModeMenuItem(pm));

			JMenu toolMenu = new JMenu("Tools");
			toolMenu.setMnemonic('T');
			toolMenu.add(new GetFlagsFromHH()).setMnemonic('f');
			toolMenu.add(new GetDEM(_plotPanel)).setMnemonic('g');
			toolMenu.add(new ExportFlagsToCSV()).setMnemonic('e');
			toolMenu.add(new ImportSegdDir()).setMnemonic('s');

			JMenu testMenu = new JMenu("Test");
			testMenu.setMnemonic('E');
			testMenu.add(new DisplayRange()).setMnemonic('g');

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
			toolBar.add(new ModeToggleButton(pm));
			_plotFrame.add(toolBar, BorderLayout.WEST);

			// Initially, enable editing of poles.
			// pm.setActive(true);

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

	}

	// /////////////////////////////////////////////////////////////////////////

	private class ResponsePlot {

		// private PlotPanel _plotPanel;
		// private PlotFrame _plotFrame;
		public SimplePlot sp;
		private PixelsView pv;

		// The Shot response
		private ResponsePlot() {

			// One plot panel for the impulse response.
			// _plotPanel = new PlotPanel();
			// _plotPanel.setHLabel("Station");
			// _plotPanel.setVLabel("Time (s)");
			// _plotPanel.setTitle("Shot");
			//
			// // This first update constructs a sequence view for the impulse
			// // response, and a points view for amplitude and phase responses.
			// // updateViews();
			//
			// _plotFrame = new PlotFrame(_plotPanelH);
			//
			// // The menu bar.
			// JMenu fileMenu = new JMenu("File");
			// fileMenu.setMnemonic('F');
			// fileMenu.add(new SaveAsPngAction(_plotFrame)).setMnemonic('a');
			// fileMenu.add(new ExitAction()).setMnemonic('x');
			// JMenuBar menuBar = new JMenuBar();
			// menuBar.add(fileMenu);
			//
			// _plotFrame.setJMenuBar(menuBar);
			//
			// // Make the plot frame visible.
			// _plotFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// _plotFrame.setLocation(RP_X, RP_Y);
			// _plotFrame.setSize(RP_WIDTH, RP_HEIGHT);
			// _plotFrame.setFontSizeForPrint(8, 240);
			// _plotFrame.setVisible(false);
			sp = new SimplePlot(SimplePlot.Origin.UPPER_LEFT);
			sp.setSize(600, 600);
			sp.setVLabel("Time (s)");
			pv = null;

		}

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
			pv = sp.addPixels(s1, s2, seg.getF());
			pv.setPercentiles(1, 99);
		}

		public void updateRP(ArrayList<Segdata> s) {
			//TODO: DBA: First Segdata has what we want in it
			float count = 0.0f;
			Segdata seg = s.get(0);
			int n1 = seg.getF()[0].length;
			int n2 = seg.getF().length;
			float[][] stot = seg.getF();
			for (int i = 1; i < s.size(); ++i) {
				Segdata segnew = s.get(i);
				int n1tmp = segnew.getF()[0].length;
				int n2tmp = segnew.getF().length;
				if (n1tmp == n1 && n2tmp == n2 && segnew.getSP()>0) {
					float[][] ftmp = segnew.getF();
					for (int m = 0; m < n2; ++m) {
						for (int n = 0; n < n1; ++n) {
							stot[m][n] += ftmp[m][n];
						}
					}
					count++;
				}
			}
			if (count > 0) {
				for (int m = 0; m < n2; ++m) {
					for (int n = 0; n < n1; ++n) {
						stot[m][n] = stot[m][n] / count;
					}
				}
				//Segd.gain2(stot);
			}
			Sampling s1 = new Sampling(n1, 0.001, 0.0);
			Sampling s2 = new Sampling(n2, 1.0, s.get(0).getRPF());
			pv = sp.addPixels(s1, s2, stot);
			pv.setPercentiles(1, 99);

		}

	}

	// /////////////////////////////////////////////////////////////////////////

	private class ElevPlot {

		public SimplePlot elev;

		private ElevPlot() {
			elev = new SimplePlot(SimplePlot.Origin.LOWER_LEFT);
			elev.setSize(600, 200);
			elev.setVLabel("meters (m)");
			elev.setHLabel("Station ID");
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
			// System.out.println("x: " + x + " y: " + y);
			MPoint gpsNear = getNearestGPS(x, y);
			// System.out.println(gpsNear.stationID);
			Segdata segNear = getNearestSegdata(gpsNear.getStation());
			// System.out.println(segNear.sp);
			_rp.updateRP(segNear);
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

	private class PlayMode extends Mode {
		private static final long serialVersionUID = 1L;

		public PlayMode(ModeManager modeManager) {
			super(modeManager);
			setName("Play Mode");
			// setIcon(loadIcon(PolesAndZerosDemo.class,"Poles16.png"));
			// setMnemonicKey(KeyEvent.VK_P);
			// setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
			setShortDescription("Playing (Movie) Mode");
		}

		protected void setActive(Component component, boolean active) {
			if (component instanceof Tile) {
				if (active) {
					component.addMouseListener(_ml);
				} else {
					component.removeMouseListener(_ml);
				}
			}
		}

		private MouseListener _ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				System.out.println("I'M ALIVE!!!!");
				Segdata s = null;
				System.out.println(_segd.size());
				for (int i = 0; i < _segd.size(); ++i) {
					s = _segd.get(i);
					_rp.updateRP(s);
					// TODO: Only displays the last one after
					// massive lag...

				}
			}
		};

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
			super("Get USGS Elevation");

		}

		public void actionPerformed(ActionEvent event) {
			// TODO
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
			_segd = Segd.readLineSegd(f.getAbsolutePath());
			System.out.println("SEGD IMPORTED");
		}

	}

	private class DisplayRange extends AbstractAction {
		private static final long serialVersionUID = 1L;

		private DisplayRange() {
			super("Display Range");

		}

		public void actionPerformed(ActionEvent event) {
			_rp.updateRP(_segd); //TODO: Write logic for dynamic shots
		}
	}

	// /////////////////////////////////////////////////////////////////////////

}
