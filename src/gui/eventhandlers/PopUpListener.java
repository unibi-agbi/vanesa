package gui.eventhandlers;import database.brenda.BRENDASearch;import database.kegg.KEGGConnector;import database.kegg.KeggSearch;import edu.uci.ics.jung.algorithms.layout.CircleLayout;import edu.uci.ics.jung.algorithms.layout.FRLayout;import edu.uci.ics.jung.algorithms.layout.ISOMLayout;import edu.uci.ics.jung.algorithms.layout.KKLayout;import edu.uci.ics.jung.algorithms.layout.SpringLayout;//import edu.uci.ics.jung.graph.Edge;//import edu.uci.ics.jung.graph.Vertex;//import edu.uci.ics.jung.visualization.FRLayout;//import edu.uci.ics.jung.visualization.ISOMLayout;//import edu.uci.ics.jung.visualization.SpringLayout;import edu.uci.ics.jung.visualization.VisualizationViewer;import graph.ContainerSingelton;import graph.GraphContainer;import graph.GraphInstance;import graph.jung.classes.MyGraph;import graph.layouts.gemLayout.GEMLayout;import gui.MainWindow;import gui.MainWindowSingleton;import gui.ProgressBar;import io.OpenDialog;import java.awt.Color;import java.awt.Cursor;import java.awt.event.ActionEvent;import java.awt.event.ActionListener;import java.beans.PropertyChangeEvent;import java.beans.PropertyChangeListener;import java.util.HashSet;import java.util.Iterator;import java.util.Vector;import javax.swing.JOptionPane;import Copy.CopySelection;import Copy.CopySelectionSingelton;import biologicalElements.Pathway;import biologicalObjects.edges.BiologicalEdgeAbstract;import biologicalObjects.nodes.BiologicalNodeAbstract;import biologicalObjects.nodes.PathwayMap;import configurations.gui.LayoutConfig;//import edu.uci.ics.jung.visualization.contrib.CircleLayout;//import edu.uci.ics.jung.visualization.contrib.KKLayout;//import graph.layouts.modularLayout.MDForceLayout;public class PopUpListener implements ActionListener, PropertyChangeListener {	Pathway pw;	MainWindow w = MainWindowSingleton.getInstance();	public void actionPerformed(ActionEvent e) {		GraphInstance graphInstance = new GraphInstance();		String event = e.getActionCommand();		w = MainWindowSingleton.getInstance();		pw = graphInstance.getPathway();		if ("center".equals(event)) {			graphInstance.getPathway().getGraph().animatedCentering();		} else if ("springLayout".equals(event)) {			LayoutConfig.changeToLayout(SpringLayout.class);		} else if ("kkLayout".equals(event)) {			LayoutConfig.changeToLayout(KKLayout.class);		} else if ("frLayout".equals(event)) {			LayoutConfig.changeToLayout(FRLayout.class);		} else if ("circleLayout".equals(event)) {			LayoutConfig.changeToLayout(CircleLayout.class);		} else if ("gemLayout".equals(event)) {			LayoutConfig.changeToLayout(GEMLayout.class);		} else if ("isomLayout".equals(event)) {			LayoutConfig.changeToLayout(ISOMLayout.class);		} else if ("MDLayout".equals(event)) {			//LayoutConfig.changeToLayout(MDForceLayout.class);		} else if ("copy".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv = con.getPathway(w.getCurrentPathway())						.getGraph().getVisualizationViewer();				HashSet<BiologicalNodeAbstract> vertices = new HashSet<BiologicalNodeAbstract>();				HashSet<BiologicalEdgeAbstract> edges = new HashSet<BiologicalEdgeAbstract>();								Iterator<BiologicalNodeAbstract> itBna = vv.getPickedVertexState().getPicked().iterator();				while(itBna.hasNext()){					vertices.add(itBna.next());				}				Iterator<BiologicalEdgeAbstract> itBea = vv.getPickedEdgeState().getPicked().iterator(); 				while(itBea.hasNext()){					edges.add(itBea.next());				}				CopySelectionSingelton.setInstance(new CopySelection(vertices, edges));			}		} else if ("cut".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv = con.getPathway(w.getCurrentPathway())						.getGraph().getVisualizationViewer();				HashSet<BiologicalNodeAbstract> vertices = new HashSet<BiologicalNodeAbstract>();				HashSet<BiologicalEdgeAbstract> edges = new HashSet<BiologicalEdgeAbstract>();								Iterator<BiologicalNodeAbstract> itBna = vv.getPickedVertexState().getPicked().iterator();				while(itBna.hasNext()){					vertices.add(itBna.next());				}				Iterator<BiologicalEdgeAbstract> itBea = vv.getPickedEdgeState().getPicked().iterator(); 				while(itBea.hasNext()){					edges.add(itBea.next());				}				CopySelectionSingelton.setInstance(new CopySelection(vertices, edges));				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();				g.removeSelection();				w.updateElementTree();				w.updateFilterView();				w.updatePathwayTree();				w.updateTheoryProperties();			}		} else if ("paste".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				//MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();				CopySelectionSingelton.getInstance().paste();			}		} else if ("delete".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();				g.removeSelection();				w.updateElementTree();				w.updateFilterView();				w.updateTheoryProperties();			}		} else if ("environment".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv = con.getPathway(w.getCurrentPathway())						.getGraph().getVisualizationViewer();								Iterator<BiologicalNodeAbstract> itBna = vv.getPickedVertexState().getPicked().iterator();				while(itBna.hasNext()){					BiologicalNodeAbstract next = itBna.next();					next.markAsEnvironment(!next.isMarkedAsEnvironment());				}				con.getPathway(w.getCurrentPathway()).getGraph().clearPickedElements();			}		} else if ("coarse".equals(event)) {			GraphContainer con = ContainerSingelton.getInstance();			if (con.containsPathway()) {				VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv = con.getPathway(w.getCurrentPathway())						.getGraph().getVisualizationViewer();								Iterator<BiologicalNodeAbstract> itBna = vv.getPickedVertexState().getPicked().iterator();				while(itBna.hasNext()){					BiologicalNodeAbstract next = itBna.next();					BiologicalNodeAbstract.coarse(next);				}				con.getPathway(w.getCurrentPathway()).getGraph().clearPickedElements();			}		} else if ("keggSearch".equals(event) || "brendaSearch".equals(event)) {			String[] input = { "", "", "", "", "" };			Vector<BiologicalNodeAbstract> vertices = pw.getSelectedNodes();			if (vertices.size() == 0) {				JOptionPane.showMessageDialog(								w,								"Please select a node to search after it in a database!",								"Operation not possible...",								JOptionPane.ERROR_MESSAGE);				return;			}			BiologicalNodeAbstract bna = vertices.get(0);//pw.getNodeByVertexID(vertices.get(0)					//.toString());			if (vertices.size() > 1) {				String[] possibilities = new String[vertices.size()];				for (int i = 0; i < vertices.size(); i++)					possibilities[i] = 							vertices.get(i).getLabel();				String answer = (String) JOptionPane.showInputDialog(								w,								"Choose one of the selected nodes, which shall be searched in a database",								"Select a node...",								JOptionPane.QUESTION_MESSAGE, null,								possibilities, possibilities[0]);				if (answer == null)					return;				bna = pw.getNodeByLabel(answer);			}			if ("keggSearch".equals(event)) {				if (bna.getBiologicalElement().equals(						biologicalElements.Elementdeclerations.enzyme))					input[2] = bna.getLabel();				else if (bna.getBiologicalElement().equals(						biologicalElements.Elementdeclerations.gene))					input[3] = bna.getLabel();				else if (bna.getBiologicalElement().equals(						biologicalElements.Elementdeclerations.pathwayMap))					input[0] = bna.getLabel();				else					input[4] = bna.getLabel();				KeggSearch keggSearch = new KeggSearch(input, w,						new ProgressBar(), graphInstance.getPathway());				keggSearch.execute();			} else if ("brendaSearch".equals(event)) {				if (bna.getBiologicalElement().equals(						biologicalElements.Elementdeclerations.enzyme))					input[0] = bna.getLabel();				else {					input[2] = bna.getLabel();					input[3] = bna.getLabel();				}				BRENDASearch brendaSearch = new BRENDASearch(input, w,						new ProgressBar(), pw, false);				brendaSearch.execute();			}		} else if ("openPathway".equals(event)) {			pwName = w.getCurrentPathway();			//for (Iterator<Vertex> it = pw.getSelectedNodes().iterator(); it			BiologicalNodeAbstract bna;			for (Iterator<BiologicalNodeAbstract> it = pw.getSelectedNodes().iterator(); it					.hasNext();) {				bna = it.next();				if (bna != null && bna instanceof PathwayMap) {					map = (PathwayMap) bna;					Pathway newPW = map.getPathwayLink();					if (newPW == null) {						KEGGConnector kc = new KEGGConnector(new ProgressBar(),								new String[] { map.getName(), "", "" }, true);						kc.setSearchMicroRNAs(JOptionPane								.showConfirmDialog(										w,										"Search also after possibly connected microRNAs in mirBase/tarBase?",										"Search paramaters...",										JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);						kc.addPropertyChangeListener(this);						kc.execute();					} else {						w.removeTab(false);						w.returnFrame().setCursor(								new Cursor(Cursor.WAIT_CURSOR));						String newPathwayName = con.addPathway(pwName, newPW);						newPW = con.getPathway(newPathwayName);						w.addTab(newPW.getTab().getTitelTab());						w.returnFrame().setCursor(								new Cursor(Cursor.DEFAULT_CURSOR));					}					w.updateAllGuiElements();					return;				}			}		} else if ("openPathwayTab".equals(event)) {			//for (Iterator<Vertex> it = pw.getSelectedNodes().iterator(); it			BiologicalNodeAbstract bna;			for (Iterator<BiologicalNodeAbstract> it = pw.getSelectedNodes().iterator(); it					.hasNext();) {				bna = it.next();				if (bna != null && bna instanceof PathwayMap) {					map = (PathwayMap) bna;					//Pathway newPW = map.getPathwayLink();											KEGGConnector kc = new KEGGConnector(new ProgressBar(),								new String[] { map.getName(), "", "" }, false);						kc.setSearchMicroRNAs(JOptionPane								.showConfirmDialog(										w,										"Search also after possibly connected microRNAs in mirBase/tarBase?",										"Search paramaters...",										JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);						kc.execute();									}			}		} else if ("addPathway".equals(event)) {			//for (Iterator<Vertex> it = pw.getSelectedNodes().iterator(); it			OpenDialog op = new OpenDialog(pw);			op.execute();					} else if ("returnToParent".equals(event) && pw.getParent() != null) {			pwName = w.getCurrentPathway();			w.removeTab(false);			w.returnFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));			String newPathwayName = con.addPathway(pwName, pw.getParent());			Pathway newPW = con.getPathway(newPathwayName);			w.addTab(newPW.getTab().getTitelTab());			w.returnFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));			w.updateAllGuiElements();		}	}	private GraphContainer con = ContainerSingelton.getInstance();	private String pwName;	private PathwayMap map;	@Override	public void propertyChange(PropertyChangeEvent evt) {		if (evt.getNewValue().equals("finished")) {			Pathway newPW = ((KEGGConnector) evt.getSource()).getPw();			newPW.setParent(pw);			w.removeTab(false);			w.returnFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));			String newPathwayName = con.addPathway(pwName, newPW);			newPW = con.getPathway(newPathwayName);			w.addTab(newPW.getTab().getTitelTab());			w.returnFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));			map.setPathwayLink(newPW);			map.setReference(false);			map.setColor(Color.BLUE);			w.updateAllGuiElements();		}	}}