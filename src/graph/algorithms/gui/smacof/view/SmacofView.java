package graph.algorithms.gui.smacof.view;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jfree.ui.RefineryUtilities;

import biologicalElements.GraphElementAbstract;
import biologicalElements.Pathway;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import biologicalObjects.nodes.BiologicalNodeAbstract.NodeAttribute;
import cluster.clientimpl.ClusterComputeThread;
import cluster.clientimpl.ComputeCallback;
import cluster.slave.JobTypes;
import cluster.slave.LayoutPoint2D;
import graph.GraphContainer;
import graph.GraphInstance;
import graph.algorithms.NodeAttributeNames;
import graph.algorithms.NodeAttributeTypes;
import graph.algorithms.gui.smacof.DoSmacof;
import graph.algorithms.gui.smacof.algorithms.Weighting;
import graph.jung.classes.MyGraph;
import gui.MainWindow;
import net.miginfocom.swing.MigLayout;

public class SmacofView extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = -1342672281173544345L;
	private final int X = 600, Y = 800;
	
	private MyGraph graph;
	
	JPanel panelparam = new JPanel();
	GraphElementAbstract ab;
	GraphInstance graphInstance;
	// Thread that executes the SMACOF algorithm
	DoSmacof dosmacof = null;

	private JComboBox<String> choosedismeasure;
	private JButton startcomputationbutton;
	private JButton stopcomputationbutton;
	private JCheckBox remotecomputation;
	private JSlider slidermaxiter;
	private JSlider sliderepsilon;
	private JSlider sliderp;
	private JSlider sliderresultdim;
	private JLabel labelmaxiter;
	private JLabel labelepsilon;
	private JLabel labelp;
	private JLabel labelresultdim;
	private static JLabel labelcuriteration = null;
	private static JLabel labelcurepsilon = null;
	private static JLabel labelcurtime = null;
	private final String[] dismeasure = { 
			"EUCLIDEAN",
			"MINKOWSKI",
// no support	"NONE",
//			"MAHALANOBIS",
		    "CANBERRA",
//		    "DIVERGENCE",
//		    "BRAY_CURTIS",
//		    "SOERGEL",
//		    "BAHATTACHARYYA",
//		    "WAVE_HEDGES",
		    "ANGULAR_SEPERATION",
		    "CORRELATION" };
	private final String[] dismeasure_remote = {
            "euclidean",
            "cityblock",
            "minkowski",
            "correlation",
            "angularseperatoin",
            "wavehedges",
            "bahattacharyya",
            "soergel",
            "braycurtis",
            "divergence",
            "canberra"};
	private HashMap<Integer, double[]> smacof_data_map = new HashMap<>();
	private HashMap<BiologicalNodeAbstract, Integer> mapped_nodes = new HashMap<>();
	private HashMap<Integer, BiologicalNodeAbstract> mapped_nodes_backwards = new HashMap<>();
	private HashMap<String, Integer> attributes = new HashMap<>();
	private Object[][] tabledata;
	private String[] tableColumnNames = { "Type", "Name", "Count", "Evaluate" };
	final int TYPE_STR = 0, NAME_STR = 1, COUNT_STR = 2, UPLOAD_BOOL = 3;
	
	private ComputeCallback helper;   

	public SmacofView() {
		super("SMACOF: "
				+ MainWindow.getInstance().getCurrentPathway());
		setPreferredSize(new Dimension(X, Y));

		graph = GraphInstance.getMyGraph();

		if (graph != null) {
			try {

				// Hier werden die Daten aus dem Graphen gelesen
				for (BiologicalNodeAbstract bna : graph.getAllVertices()) {
					for (NodeAttribute na : bna
							.getNodeAttributesByType(NodeAttributeTypes.EXPERIMENT)) {
						// count mapped nodes with given attribute
						if (!attributes.containsKey(na.getName())) {
							attributes.put(na.getName(), 1);
						} else {
							attributes.put(na.getName(),
									attributes.get(na.getName()) + 1);
						}
					}
					// Martin
					for (NodeAttribute na : bna
							.getNodeAttributesByType(NodeAttributeTypes.GRAPH_PROPERTY)) {
						// count mapped nodes with given attribute
						if (!attributes.containsKey(na.getName())) {
							attributes.put(na.getName(), 1);
						} else {
							attributes.put(na.getName(),
									attributes.get(na.getName()) + 1);
						}
					}

				}
				// tabledata fuer die Datentabelle anlegen und befuellen
				tabledata = new Object[attributes.keySet().size()][tableColumnNames.length];
				TreeSet<String> displayset = new TreeSet<>(attributes.keySet());
				for (int line = 0; line < attributes.keySet().size(); line++) {
					// hard coded for now, can be dynamically chosen in future
					tabledata[line][TYPE_STR] = "Experiment";
					tabledata[line][NAME_STR] = displayset.pollFirst();
					tabledata[line][COUNT_STR] = attributes
							.get(tabledata[line][NAME_STR])
							+ "/"
							+ graph.getAllVertices().size();
					tabledata[line][UPLOAD_BOOL] = true;

				}

				// hier wird das Fenster fertiggemacht
				initPanel();
				this.setLayout(new BorderLayout(5, 5));
				this.add(panelparam, BorderLayout.NORTH);
				SmacofTablePanel tablepanel = new SmacofTablePanel();
				tablepanel.setOpaque(true);
				this.add(tablepanel, BorderLayout.SOUTH);
				pack();
				RefineryUtilities.centerFrameOnScreen(this);
				setVisible(true);
				setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				// setResizable(false);
			} catch (IndexOutOfBoundsException ex) {
				JOptionPane.showMessageDialog(null,
						"Network contains no additional attributes.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please open a network first.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void initPanel() {
		choosedismeasure = new JComboBox<String>(dismeasure);
		choosedismeasure.setActionCommand("dismeasure");
		choosedismeasure.addActionListener(this);
		
		slidermaxiter = new JSlider(0, 10000, 1000);
		slidermaxiter.setPaintTicks(true);
		slidermaxiter.setMinorTickSpacing(10);
		labelmaxiter = new JLabel("max. iterations: "+slidermaxiter.getValue()); 
		slidermaxiter.addChangeListener( new ChangeListener() {
			  @Override public void stateChanged( ChangeEvent e ) {
				     labelmaxiter.setText("max. iterations: "+((JSlider) e.getSource()).getValue() );
				  }
				} );

		
		sliderepsilon = new JSlider(-15, 10, -5);
		sliderepsilon.setPaintTicks(true);
		sliderepsilon.setMinorTickSpacing(1);
		labelepsilon = new JLabel("epsilon: "+Math.pow(10, sliderepsilon.getValue()));
		sliderepsilon.addChangeListener( new ChangeListener() {
			  @Override public void stateChanged( ChangeEvent e ) {
				  	double myepsilon;
					double tmp = ((JSlider) e.getSource()).getValue();
					if(tmp >= 0) {
						myepsilon = tmp;
					} else {
						myepsilon = Math.pow(10, tmp);
					}
				     labelepsilon.setText("epsilon: "+myepsilon);
				  }
				} );
		
		sliderp = new JSlider(1, 10, 2);
		sliderp.setPaintTicks(true);
		sliderp.setMinorTickSpacing(1);
		labelp = new JLabel("p: "+sliderp.getValue());
		sliderp.addChangeListener( new ChangeListener() {
			@Override public void stateChanged( ChangeEvent e ) {
			     labelp.setText("p: "+((JSlider) e.getSource()).getValue() );
			  }
			} );
		
		sliderresultdim = new JSlider(1, 2, 2);
		sliderresultdim.setPaintTicks(true);
		sliderresultdim.setMinorTickSpacing(1);
		labelresultdim = new JLabel("result dimension: "+sliderresultdim.getValue());
		sliderresultdim.addChangeListener( new ChangeListener() {
			@Override public void stateChanged( ChangeEvent e ) {
			     labelresultdim.setText("result dimension: "+((JSlider) e.getSource()).getValue() );
			  }
			} );
		
		startcomputationbutton = new JButton("start computation");
		startcomputationbutton.setActionCommand("start computation");
		startcomputationbutton.addActionListener(this);
				
		stopcomputationbutton = new JButton("stop computation");
		stopcomputationbutton.setActionCommand("stop computation");
		stopcomputationbutton.addActionListener(this);
		stopcomputationbutton.setEnabled(false);
		
		remotecomputation = new JCheckBox("over remote");
		remotecomputation.setEnabled(true);
		remotecomputation.addActionListener(this);
		remotecomputation.setActionCommand("remotecomputation");
		
		
		labelcuriteration = new JLabel("iteration");
		labelcurepsilon = new JLabel("current epsilon");
		labelcurtime = new JLabel("last iteration time");

		MigLayout layout = new MigLayout("", "[][grow]", "");
		panelparam.setLayout(layout);
		panelparam.add(new JLabel("dissimilarity measure                "), "");
		panelparam.add(choosedismeasure, "wrap");
		
		panelparam.add(labelmaxiter, "");
		panelparam.add(slidermaxiter, "wrap");
		
		panelparam.add(labelepsilon, "");
		panelparam.add(sliderepsilon, "wrap");
		
		panelparam.add(labelp, "");
		panelparam.add(sliderp, "wrap");
		
		panelparam.add(labelresultdim, "");
		panelparam.add(sliderresultdim, "wrap");
		
		//enable remote computation only on dev mode
		if(MainWindow.developer){
			panelparam.add(startcomputationbutton, "");
			panelparam.add(stopcomputationbutton, "");
			panelparam.add(remotecomputation, "wrap");
		}else{
			panelparam.add(startcomputationbutton, "");
			panelparam.add(stopcomputationbutton, "wrap");			
		}
		
		
		panelparam.add(new JLabel("computation status"), "wrap");
		panelparam.add(labelcuriteration, "wrap");
		panelparam.add(labelcurepsilon, "wrap");
		panelparam.add(labelcurtime, "wrap");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			String command = e.getActionCommand();
			switch(command) {
			
			// Bei auswahl des remote Aufrufs gelten andere Distanzma�e
			// Diese werden dann beim ausw�hlen gesetzt
			case "remotecomputation":
				
				if(remotecomputation.isSelected()){
					choosedismeasure.removeAllItems();
					for(String rd : dismeasure_remote)
						choosedismeasure.addItem(rd);
				}else{
					choosedismeasure.removeAllItems();
					for(String ld : dismeasure)
						choosedismeasure.addItem(ld);
				}				
				break;
			
			
			// Start der Berechnungen
			case "start computation":
				// Bevor der Algorithmus gestartet wird, muessen die angewaehlten Daten verpackt werden:
				// in mapped_nodes halten wir fest welcher Knoten welche ID besitzt, fuer die Zuordnung der Ergebnisse
				// in smacof_data_map befinden sich alle Daten, die zur Berechnung des Smacof Algorithmus benoetigt werden
				NodeAttribute nat;
				int size_used_attributes = 0;
				for (int line = 0; line < attributes.keySet().size(); line++) {
					if ((Boolean) tabledata[line][UPLOAD_BOOL]) {
						size_used_attributes++;
					}
				}
				int node_id = 0;
				for (BiologicalNodeAbstract bna : graph.getAllVertices()) {
					mapped_nodes.put(bna, node_id);
					mapped_nodes_backwards.put(node_id, bna);
					smacof_data_map.put(node_id, new double[size_used_attributes]);
					for (int line = 0; line < attributes.keySet().size(); line++) {
						if ((Boolean) tabledata[line][UPLOAD_BOOL]) {
							nat = bna.getNodeAttributeByName((String) tabledata[line][NAME_STR]);
							if (nat != null) {
								smacof_data_map.get(node_id)[line] = nat.getDoublevalue();
							}
						}
					}
					node_id++;
				}
				//printSmacofMap(smacof_data_map);
				
				// hier wird der Thread fuer den SMACOF Algorithmus angestossen
				double myepsilon = sliderepsilon.getValue() >= 0 ? sliderepsilon.getValue() : Math.pow(10, sliderepsilon.getValue());
//				//APSP statt werte
//				NetworkProperties np = new NetworkProperties();
//				short[][] apsp = np.AllPairShortestPaths(false);
//				smacof_data_map.clear();
//				for (int i = 0; i < apsp.length; i++) {
//					double[] dline = new double[apsp[i].length];
//					for(int j = 0; j< apsp[i].length; j++){
//						dline[j] = apsp[i][j];
//					}
//					smacof_data_map.put(i, dline);
//					
//				}
				
				if(!remotecomputation.isSelected()){

					//get weights:
//					Weighting W = new Weighting(mapped_nodes, mapped_nodes_backwards);
//					W.getWeightsByAdjacency();
//			    	W.getWeightsByCellularComponent();
		
					
					dosmacof = new DoSmacof(smacof_data_map, mapped_nodes,
							(String) choosedismeasure.getSelectedItem(),
							slidermaxiter.getValue(), myepsilon,
							sliderp.getValue(), sliderresultdim.getValue(),
							this);
					// start SMACOF algorithm as a thread

					startcomputationbutton.setEnabled(false);
					dosmacof.start();
			}else {
					//Do remote call
					startRemoteCall();
				}				
				
				MainWindow w = MainWindow.getInstance();
				w.showProgressBar("computing new coordinates via SMACOF ...");
				stopcomputationbutton.setEnabled(true);
				break;
			// Abbruch der Berechnungen
			case "stop computation":
				try {
					if(dosmacof != null && dosmacof.isAlive()){
						dosmacof.stopSmacof();
						dosmacof.interrupt();
						stopcomputationbutton.setEnabled(false);
						startcomputationbutton.setEnabled(true);
						MainWindow mw = MainWindow.getInstance();
						mw.closeProgressBar();
					} 
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Die statischen set()-Funktionen sind zum Setzen der Statuslabels
	 */
	public static void setLabelcuriteration(int i) {
		if(labelcuriteration != null) {
			labelcuriteration.setText("iteration: "+i);
		}
	}
	
	public static void setLabelcurepsilon(double e) {
		if(labelcurepsilon != null) {
			labelcurepsilon.setText("current epsilon: "+shortenDouble(e));
		}
	}
	
	public static void setLabelcurtime(double s) {
		if(labelcurtime != null) {
			labelcurtime.setText("last iteration time: "+s+" sec");
		}
	}
	
    private static String shortenDouble(double d) {
        String sd = String.valueOf(d);
        String shortend = "";
        if (sd.contains("E") && sd.length() >= 5)  {
            shortend = sd.substring(0, sd.indexOf(".") + 4) + sd.substring(sd.indexOf("E"), sd.length());
        } else if (sd.length() >= 5) {
            shortend = sd.substring(0, sd.indexOf(".")) + sd.substring(sd.indexOf("."), sd.indexOf(".") + 4);
        } else if (Double.isNaN(d)){
            shortend = "0";
        } else {
            shortend = sd;
        }
        return shortend;
    }
	
    /**
     * invoke the remote call over DaNIeL and get results
     * @throws IOException 
     */
    private void startRemoteCall() throws IOException{
    	
		Weighting W = new Weighting(mapped_nodes, mapped_nodes_backwards);
//		W.getWeightsByAdjacency();
    	float weightings[][];
    	weightings = W.getWeightsByCellularComponent();
//    	weightings = W.getWeightsByAdjacency();
    	
    	

    	
    	double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
    	
    	
    	//convert Smacof data map to Double variant
    	HashMap<Integer, Double[]> tcpmap = new HashMap<>(); 
    	Double[] tcparray;
    	for(Entry<Integer,double[]> entry : smacof_data_map.entrySet()){
    		Integer key = entry.getKey();
    		double[] value = entry.getValue();
    		
    		tcparray = new Double[value.length];
    		for(int i = 0; i<value.length; i++){
    			tcparray[i] = value[i];
    			if(tcparray[i] > max)
    				max = tcparray[i];
    			if(tcparray[i] < min)
    				min = tcparray[i];
    		}
    		
    		tcpmap.put(key, tcparray);   		
    	}
    	//normalize expression to -1 .. +1:
    	Double[] value;
    	System.out.println(min +"   "+max);
    	min = min*-1.0d;
    	for(Entry<Integer,Double[]> entry : tcpmap.entrySet()){
    		value = entry.getValue();
    		for(int i = 0; i<value.length; i++){
    			if(value[i]<0.0d)
    				value[i]/=min;
    			if(value[i]>0.0d)
    				value[i]/=max;
    		}
    	}
    	
    	
    	// cellular component smacof daten
///*
    	BiologicalNodeAbstract bna;
		Double[] cells;

		for (int i = 0; i < smacof_data_map.size(); i++) {
			cells = new Double[11];
			for (int b = 0; b < cells.length; b++) {
				cells[b] = 0.0;
			}
			bna = mapped_nodes_backwards.get(i);
			for (NodeAttribute na : bna.getNodeAttributes()) {
				if (na.getName().equals(NodeAttributeNames.GO_CELLULAR_COMPONENT)) {

					if (na.getStringvalue().equals("Nucleus"))
						cells[0] = 1.0;
					else if (na.getStringvalue().equals("Cytoplasm"))
						cells[1] = 1.0;
					else if (na.getStringvalue().equals("Plasma membrane"))
						cells[2] = 1.0;
					else if (na.getStringvalue().equals("Extracellular"))
						cells[3] = 1.0;
					else if (na.getStringvalue().equals("Mitochondrion"))
						cells[4] = 1.0;
					else if (na.getStringvalue().equals("Nucleolus"))
						cells[5] = 1.0;
					else if (na.getStringvalue().equals("Endoplasmic reticulum"))
						cells[6] = 1.0;
					else if (na.getStringvalue().equals("Golgi apparatus"))
						cells[7] = 1.0;
					else if (na.getStringvalue().equals("Endosome"))
						cells[8] =1.0;
					else if (na.getStringvalue().equals("Cytosol"))
						cells[9] = 1.0;
					else if (na.getStringvalue().equals("Integral to membrane"))
						cells[10] = 1.0;
				}
			}
			
			Double[] current = tcpmap.get(i);
			Double[] newcurrent = new Double[11+current.length];
			
			for(int j = 0; j<current.length; j++){
				newcurrent[j] = current[j];
			}
			
			for(int j = 0; j<cells.length; j++){
				newcurrent[j+current.length] = cells[j];
			}
			tcpmap.put(i, newcurrent);		
			
		}   
		
// */
			
/* Parameters for remote access:
       disfunc - dissimilarity measure: ...
		               0 - euclidean
		               1 - cityblock
		               2 - minkowski
		               3 - correlation
		               4 - angularseperatin
		               5 - wavehedges
		               6 - bahattacharyya
		               7 - soergel
		               8 - braycurtis
		               9 - divergence
		               10 - canberra
		maxiter - maximum number of iterations to use: a positive integer
		epsilon - the maximum error value which is allowed: a positive real_t
		metric_p - a special value to adjust some dissimilarity functions: a positive integer
*/
		double myepsilon = sliderepsilon.getValue() >= 0 ? sliderepsilon.getValue() : Math.pow(10, sliderepsilon.getValue());
		// Set parameters
		HashMap<String,String>parameters = new HashMap<>();
		parameters.put("disfunc", choosedismeasure.getSelectedIndex()+"");
		parameters.put("maxiter", slidermaxiter.getValue()+"");
		parameters.put("epsilon", myepsilon+"");
		parameters.put("metric_p", sliderp.getValue()+"");

		// open objectstream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		// Lock UI and initiate Progress Bar
//		MainWindowSingleton.getInstance().showProgressBar("attempting to queue job.");

//		oos.writeObject(smacof_data_map); //does not work fine with remote 
		oos.writeObject(tcpmap);
		oos.writeObject(parameters);
		oos.writeObject(weightings);

		// close objectstream and transform to bytearray
		oos.close();
		byte[] jobinformation = baos.toByteArray();

		// compute values over RMI
		try {
			helper = new ComputeCallback(this);
			ClusterComputeThread smacof = new ClusterComputeThread(
					JobTypes.LAYOUT_SMACOF_JOB, jobinformation, helper);
			smacof.start();
		} catch (RemoteException e) {
			e.printStackTrace();
			MainWindow.getInstance().closeProgressBar();
		}
    }

    @SuppressWarnings("static-access")
	public void realignNetwork(HashMap<Integer, LayoutPoint2D> coords) {
		MainWindow w = MainWindow.getInstance();
		GraphContainer con = GraphContainer.getInstance();
		Pathway pw = con.getPathway(w.getCurrentPathway());
		float scaling = 0.0f, desired_diameter = 1000.0f;
		
		//set smaller diameter for large networks
		if(pw.getVertices().size()>=1000)
			desired_diameter = 3000.0f;

		//determine optimal result scaling by formula
		//first get the networks real width and height
		int key;
		float minX = Float.MAX_VALUE, maxX = Float.MIN_NORMAL, minY = Float.MAX_VALUE, maxY = Float.MIN_NORMAL, xdiff, ydiff;
		float x,y;
		LayoutPoint2D value;
		for(Entry<Integer,LayoutPoint2D> entry : coords.entrySet()){
			 key = entry.getKey();
			 value = entry.getValue();
			 x = value.getX();
			 y = value.getY();
			 
			 if(x < minX)
				 minX = x;
			 if(x > maxX)
				 maxX = x;
			 
			 if(y < minY)
				 minY = y;
			 if(y>maxY)
				 maxY = y;	 
		}
		
		//check if coordinates are as thought (may be improved, what if min is positive?)
		if(minY < 0 && maxY > 0)
			System.out.println("Y IS OK!");
		else
			System.out.println("Y IS BROKEN!!!");
		if(minX < 0 && maxX > 0)
			System.out.println("X IS OK!");
		else
			System.out.println("X IS BROKEN!!!");
		
		//get width and height of current network coordinates
		xdiff = Math.abs(minX)+maxX;
		ydiff = Math.abs(minY)+maxY;
		
		//set scaling width and height to at least to ~1000
		// scaling has to be proportional, so take the larger value from X and Y as scaling
		scaling = desired_diameter/xdiff;
		if(desired_diameter/ydiff > scaling)
			scaling = desired_diameter/ydiff;
				
		GraphInstance graphInstance = new GraphInstance();
		

		
		System.out.println("scaling was set to:"+scaling);
		for(Entry<Integer,LayoutPoint2D> entry : coords.entrySet()){
			 key = entry.getKey();
			 value = entry.getValue();
			 x = value.getX();
			 y = value.getY();
			 Point2D point = new Point2D.Double(x*scaling, y*scaling);
			 
				graphInstance.getPathway().getGraph()
				.getVisualizationViewer().getModel()
				.getGraphLayout().setLocation(mapped_nodes_backwards.get(key), point);
			 
//			 pw.getGraph().getVertexLocation(mapped_nodes_backwards.get(key)).setLocation(x*scaling,y*scaling);
//			 getVertices()
//				.get(mapped_nodes_backwards.get(key))
//				.setLocation(x*scaling,y*scaling);	 
		}
		 

		
//		System.out.printf("X:%f ~ %f\tY:%f ~ %f\t xdiff:%f\t ydiff:%f\n",minX,maxX,minY,maxY,Math.abs(minX)+maxX,Math.abs(minY)+maxY);
		
		MainWindow.getInstance().showProgressBar("updating graph structure");
		pw.saveVertexLocations();
		
		//center automatically

//		graphInstance.getPathway().getGraph().normalCentering();
		pw.getGraph().normalCentering();
		
		w.closeProgressBar();
		returned();
	}
	
	class SmacofTablePanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2344221533928376628L;

		public SmacofTablePanel() {
			super(new MigLayout("", "[right]"));
			
			SmacofTableModel tablemodel = new SmacofTableModel(tableColumnNames, tabledata);
			JTable table = new JXTable(tablemodel);
			table.setPreferredScrollableViewportSize(new Dimension(600, 250));
			table.setFillsViewportHeight(true);
			table.setFont(new Font("Arial", Font.PLAIN, 14));
			table.setRowHeight(table.getRowHeight() + 5);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// Create the scroll pane and add the table to it.
			JScrollPane scrollPane = new JScrollPane(table);

			// Add the scroll pane to this panel.
			this.add(scrollPane);
			this.setVisible(true);
		}
	}
		
	class SmacofTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = -1996177080571000673L;
		
		private String[] columnNames;
		private Object[][] data;

		public SmacofTableModel(String[] columnNames, Object[][] data) {
			this.columnNames = columnNames;
			this.data = data;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			if (col == 3)
				return true;
			else
				return false;
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			this.fireTableCellUpdated(row, col);
		}
	}	
	
	
	/*
	 * My DEBUG functions
	 */
	void printSmacofMap(HashMap<Integer, double[]> smacof_map) {
		double[] tmp;
		for (Integer key : smacof_map.keySet()) {
			System.out.println("KEY: "+key);
			tmp = smacof_map.get(key);
			for (int i = 0; i < tmp.length; i++) {
				System.out.println(tmp[i]);
			}
			System.out.println("------------------");
		}
	}

	/*
	 * Smacof thread returns and is done, reenable buttons
	 */
	public void returned() {

		startcomputationbutton.setEnabled(true);
		stopcomputationbutton.setEnabled(false);
		
	}


	
}
