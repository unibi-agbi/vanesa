package gui.eventhandlers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import biologicalElements.Elementdeclerations;
import biologicalElements.Pathway;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import graph.CreatePathway;
import graph.GraphContainer;
import graph.GraphInstance;
import graph.algorithms.gui.CompareGraphsGUI;
import graph.hierarchies.AutoCoarse;
import graph.jung.classes.MyGraph;
import gui.InfoWindow;
import gui.MainWindow;
import gui.MyPopUp;
import gui.ParallelChooseGraphsWindow;
import petriNet.OpenModelicaResult;
import petriNet.PNTableDialog;
import petriNet.ReachController;

public class ToolBarListener implements ActionListener {

	private static ToolBarListener instance;

	private ToolBarListener() {

	}

	public static synchronized ToolBarListener getInstance() {
		if (ToolBarListener.instance == null) {
			ToolBarListener.instance = new ToolBarListener();
		}
		return ToolBarListener.instance;
	}

	public void showCreateBeforeMessage() {
		MyPopUp.getInstance().show("Error", "Please create a network first!");
	}

	public void showCreate2NetworksMessage() {
		MyPopUp.getInstance().show("Error", "Please create a network first!");
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String event = e.getActionCommand();
		MainWindow w = MainWindow.getInstance();
		GraphContainer con = GraphContainer.getInstance();
		GraphInstance graphInstance = new GraphInstance();

		if ("new Network".equals(event)) {

			int option = JOptionPane.showOptionDialog(MainWindow.getInstance(), "Which type of modeling do you prefer?",
					"Choose Network Type...", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { "Biological Graph", "Petri Net" }, JOptionPane.CANCEL_OPTION);
			if (option != -1) {
				new CreatePathway();
				graphInstance.getPathway().setPetriNet(option == JOptionPane.NO_OPTION);
				w.getBar().paintToolbar(option == JOptionPane.NO_OPTION);
				w.updateAllGuiElements();
			}

		} else if ("move".equals(event)) {
			if (con.containsPathway()) {
				con.changeMouseFunction("move");
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.disableGraphTheory();
				// g.getVisualizationViewer().resize(20, 20);
				Dimension d = g.getVisualizationViewer().getPreferredSize();
				d.setSize(d.width * 2, d.height * 2);
				g.getVisualizationViewer().setPreferredSize(d);
				g.getVisualizationViewer().repaint();
			}
		} else if ("pick".equals(event)) {
			if (con.containsPathway()) {
				con.changeMouseFunction("pick");
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.disableGraphTheory();
			}
		} else if ("center".equals(event)) {
			if (con.containsPathway()) {
				// CENTERING WITH SCALING
				graphInstance.getPathway().getGraph().normalCentering();
				// ONLY FOR CENTERING, NOT SCALING
//				graphInstance.getPathway().getGraph().animatedCentering();
			}
		} else if ("zoom in".equals(event)) {
			if (con.containsPathway()) {
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.zoomIn();
			}
		} else if ("full screen".equals(event)) {

			w.setFullScreen();

		} else if ("zoom out".equals(event)) {
			if (con.containsPathway()) {
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.zoomOut();
			}
		} else if ("compressEdges".equals(event)) {
			if (con.containsPathway()) {
				con.getPathway(w.getCurrentPathway()).stretchGraph(0.9);
				con.getPathway(w.getCurrentPathway()).updateMyGraph();
			}

		} else if ("stretchEdges".equals(event)) {
			if (con.containsPathway()) {
				con.getPathway(w.getCurrentPathway()).stretchGraph(1.1);
				con.getPathway(w.getCurrentPathway()).updateMyGraph();
			}
		} else if ("edit".equals(event)) {
			if (con.containsPathway()) {
				con.changeMouseFunction("edit");
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.disableGraphTheory();
			}
		} else if ("merge".equals(event)) {
			if (con.getPathwayNumbers() > 1) {
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.disableGraphTheory();
				new CompareGraphsGUI();
			} else {
				this.showCreate2NetworksMessage();
			}
		} else if ("del".equals(event)) {
			if (con.containsPathway()) {
				Pathway pw = con.getPathway(w.getCurrentPathway());
				// g.stopVisualizationModel();
				pw.removeSelection();
				w.updateElementTree();
				w.updatePathwayTree();
				// w.updateTheoryProperties();
				// g.restartVisualizationModel();
			}
		} else if ("info".equals(event)) {
			if (con.containsPathway()) {
				if (con.getPathway(w.getCurrentPathway()).hasGotAtLeastOneElement()) {
					new InfoWindow(false);
				}
			}
		} else if ("infoextended".equals(event)) {
			if (con.containsPathway()) {
				if (con.getPathway(w.getCurrentPathway()).hasGotAtLeastOneElement()) {
					new InfoWindow(true);
				}
			}
		} else if ("modelling".equals(event)) {

			con.setPetriView(false);

			Component[] c = MainWindow.getInstance().getContentPane().getComponents();
			for (int i = 0; i < c.length; i++) {
				if (c[i].getClass().getName().equals("javax.swing.JPanel")) {
					MainWindow.getInstance().getBar().paintToolbar(false);
					break;
				}

			}
		} else if ("discretePlace".equals(event)) {
			con.changeMouseFunction("edit");
			con.setPetriView(true);
			con.setPetriNetEditingMode(Elementdeclerations.discretePlace);

		} else if ("continuousPlace".equals(event)) {

			con.changeMouseFunction("edit");
			con.setPetriView(true);
			con.setPetriNetEditingMode(Elementdeclerations.continuousPlace);

		} else if ("discreteTransition".equals(event)) {

			con.changeMouseFunction("edit");
			con.setPetriView(true);
			con.setPetriNetEditingMode(Elementdeclerations.discreteTransition);

		} else if ("continiousTransition".equals(event)) {

			con.changeMouseFunction("edit");
			con.setPetriView(true);
			con.setPetriNetEditingMode(Elementdeclerations.continuousTransition);

		} else if ("stochasticTransition".equals(event)) {

			con.changeMouseFunction("edit");
			con.setPetriView(true);
			con.setPetriNetEditingMode(Elementdeclerations.stochasticTransition);

		} else if ("createPetriNet".equals(event)) {
			con.setPetriView(true);

			Component[] c = MainWindow.getInstance().getContentPane().getComponents();
			for (int i = 0; i < c.length; i++) {
				if (c[i].getClass().getName().equals("javax.swing.JPanel")) {
					MainWindow.getInstance().getBar().paintToolbar(true);
					break;
				}
			}

			/*
			 * if (con.getPathwayNumbers() > 0) { MyGraph g =
			 * con.getPathway(w.getCurrentPathway()).getGraph(); g.disableGraphTheory(); //
			 * new CompareGraphsGUI(); new ConvertToPetriNet(); }
			 */

			// if (con.getPathwayNumbers() > 0) {
			// MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
			// g.disableGraphTheory();
			// //new CompareGraphsGUI();
			// new ConvertToPetriNet();
			// }

		} else if ("createCov".equals(event)) {
			// System.out.println("cov erstellen");
			// MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
			// Cov cov = new Cov();
			if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
					"The calculation of the reach graph could take long time, especially if you have many places in your network. Do you want to perform the calculation anyway?",
					"Please Conform your action...", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				new ReachController();
			GraphInstance.getMyGraph().changeToGEMLayout();

		} else if ("editElements".equals(event)) {
			new PNTableDialog().setVisible(true);
		} else if ("parallelview".equals(event)) {
			// create a graph choosing popup and calculate network properties
			new ParallelChooseGraphsWindow();
		} else if ("loadModResult".equals(event)) {
			new OpenModelicaResult().execute();
		} else if ("group".equals(event)) {
			if (con.containsPathway()) {
				con.getPathway(w.getCurrentPathway()).groupSelectedNodes();
				con.getPathway(w.getCurrentPathway()).updateMyGraph();
			}
		} else if ("deleteGroup".equals(event)) {
			if (con.containsPathway()) {
				con.getPathway(w.getCurrentPathway()).deleteGroup();
				con.getPathway(w.getCurrentPathway()).updateMyGraph();
			}
		} else if ("coarseSelectedNodes".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				// System.out.println("coarse");
				Set<BiologicalNodeAbstract> selectedNodes = new HashSet<BiologicalNodeAbstract>();
				selectedNodes.addAll(graphInstance.getPathway().getSelectedNodes());
				BiologicalNodeAbstract.coarse(selectedNodes);
				graphInstance.getPathway().updateMyGraph();
				graphInstance.getPathway().getGraph().getVisualizationViewer().repaint();
			} else {
				System.out.println("No Graph exists!");
			}
		} else if ("flatSelectedNodes".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				for (BiologicalNodeAbstract node : con.getPathway((w.getCurrentPathway())).getGraph()
						.getVisualizationViewer().getPickedVertexState().getPicked()) {
					node.flat();
					graphInstance.getPathway().updateMyGraph();
					MainWindow.getInstance().removeTab(false, node.getTab().getTitelTab(), node);
				}
				new GraphInstance().getPathway().getGraph().getVisualizationViewer().repaint();
			} else {
				System.out.println("No Graph exists!");
			}
		} else if ("enterNode".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				for (BiologicalNodeAbstract node : graphInstance.getPathway().getGraph().getVisualizationViewer()
						.getPickedVertexState().getPicked()) {
					if (!node.isCoarseNode() && !node.isMarkedAsCoarseNode()) {
						continue;
					}
					w.returnFrame().setCursor(new Cursor(Cursor.WAIT_CURSOR));
					// Pathway newPW = null;
					for (BiologicalNodeAbstract n : node.getVertices().keySet()) {
						node.getVertices().put(n, graphInstance.getPathway().getVertices().get(n));
					}
					String newPathwayName = con.addPathway(node.getLabel(), node);
					Pathway pw = con.getPathway(newPathwayName);
					w.addTab(pw.getTab().getTitelTab());
					w.returnFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					graphInstance.getPathway().setPetriNet(node.isPetriNet());
					w.getBar().paintToolbar(node.isPetriNet());
					w.updateAllGuiElements();
					graphInstance.getPathway().updateMyGraph();
					graphInstance.getPathway().getGraph().normalCentering();
				}
			} else {
				System.out.println("No Graph exists!");
			}
		} else if ("autocoarse".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				AutoCoarse.coarseSeperatedSubgraphs(graphInstance.getPathway());
				new GraphInstance().getPathway().getGraph().getVisualizationViewer().repaint();
			} else {
				System.out.println("No Graph exists!");
			}
		} else if ("newWindow".equals(event)) {
			MainWindow.getInstance().addView();
		} else if ("hierarchy".equals(event)) {
			if (con.containsPathway()) {
				con.changeMouseFunction("hierarchy");
				MyGraph g = con.getPathway(w.getCurrentPathway()).getGraph();
				g.disableGraphTheory();
			}
		} else if ("mergeSelectedNodes".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				// System.out.println("merge");
				graphInstance.getPathway().mergeNodes(graphInstance.getPathway().getGraph().getVisualizationViewer()
						.getPickedVertexState().getPicked());
			} else {
				System.out.println("No Graph exists!");
			}
		} else if ("splitNode".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				graphInstance.getPathway().splitNode(graphInstance.getPathway().getGraph().getVisualizationViewer()
						.getPickedVertexState().getPicked());
			}
		} else if ("adjustDown".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				Set<BiologicalNodeAbstract> nodes = graphInstance.getPathway().getSelectedNodes();

				double maxy = Double.MIN_VALUE;
				Point2D point;
				if (nodes.size() > 1) {
					Iterator<BiologicalNodeAbstract> it = nodes.iterator();
					BiologicalNodeAbstract bna;
					while (it.hasNext()) {
						bna = it.next();
						// r = bna.getShape().getBounds();
						/*
						 * VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv =
						 * graphInstance .getPathway().getGraph() .getVisualizationViewer(); double
						 * scaleV = vv.getRenderContext() .getMultiLayerTransformer()
						 * .getTransformer(Layer.VIEW).getScale(); double scaleL = vv.getRenderContext()
						 * .getMultiLayerTransformer() .getTransformer(Layer.LAYOUT).getScale(); double
						 * scale; if (scaleV < 1) { scale = scaleV; } else { scale = scaleL; }
						 * 
						 * System.out.println((r.getMaxX()-r.getMinX())/scale);
						 */
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						if (point.getY() > maxy) {
							maxy = point.getY();
						}
					}
					it = nodes.iterator();
					while (it.hasNext()) {
						bna = it.next();
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						point.setLocation(point.getX(), maxy);
						graphInstance.getPathway().getGraph().getVisualizationViewer().getModel().getGraphLayout()
								.setLocation(bna, point);

					}
				}
				graphInstance.getPathway().saveVertexLocations();
			}

		} else if ("adjustLeft".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				Set<BiologicalNodeAbstract> nodes = graphInstance.getPathway().getSelectedNodes();

				double minx = Double.MAX_VALUE;
				Point2D point;
				if (nodes.size() > 1) {
					Iterator<BiologicalNodeAbstract> it = nodes.iterator();
					BiologicalNodeAbstract bna;
					while (it.hasNext()) {
						bna = it.next();
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						if (point.getX() < minx) {
							minx = point.getX();
						}
					}
					it = nodes.iterator();
					while (it.hasNext()) {
						bna = it.next();
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);

						point.setLocation(minx, point.getY());
						graphInstance.getPathway().getGraph().getVisualizationViewer().getModel().getGraphLayout()
								.setLocation(bna, point);

					}
				}
				graphInstance.getPathway().saveVertexLocations();
			}
		} else if ("adjustHorizontalSpace".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				Set<BiologicalNodeAbstract> nodes = graphInstance.getPathway().getSelectedNodes();

				double minx = Double.MAX_VALUE;
				double maxx = Double.MIN_VALUE;

				HashMap<BiologicalNodeAbstract, Double> map = new HashMap<BiologicalNodeAbstract, Double>();
				Point2D point;
				if (nodes.size() > 2) {
					Iterator<BiologicalNodeAbstract> it = nodes.iterator();
					BiologicalNodeAbstract bna;
					while (it.hasNext()) {
						bna = it.next();
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						if (point.getX() < minx) {
							minx = point.getX();
						}
						if (point.getX() > maxx) {
							maxx = point.getX();
						}
						map.put(bna, point.getX());
					}

					List<Double> c = new ArrayList<Double>(map.values());
					Collections.sort(c);

					it = nodes.iterator();
					while (it.hasNext()) {
						bna = it.next();
						int d = c.indexOf(map.get(bna));
						double newx;
						if (d == 0) {
							newx = minx;
						} else if (d == nodes.size() - 1) {
							newx = maxx;
						} else {
							newx = minx + d * ((Math.abs(maxx - minx)) / (nodes.size() - 1));
						}
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						point.setLocation(newx, point.getY());
						graphInstance.getPathway().getGraph().getVisualizationViewer().getModel().getGraphLayout()
								.setLocation(bna, point);

					}
					graphInstance.getPathway().saveVertexLocations();
				}
			}
		} else if ("adjustVerticalSpace".equals(event)) {
			if (GraphInstance.getMyGraph() != null) {
				Set<BiologicalNodeAbstract> nodes = graphInstance.getPathway().getSelectedNodes();

				double miny = Double.MAX_VALUE;
				double maxy = Double.MIN_VALUE;

				HashMap<BiologicalNodeAbstract, Double> map = new HashMap<BiologicalNodeAbstract, Double>();
				Point2D point;
				if (nodes.size() > 2) {
					Iterator<BiologicalNodeAbstract> it = nodes.iterator();
					BiologicalNodeAbstract bna;
					while (it.hasNext()) {
						bna = it.next();
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						if (point.getY() < miny) {
							miny = point.getY();
						}
						if (point.getY() > maxy) {
							maxy = point.getY();
						}
						map.put(bna, point.getY());
					}

					List<Double> c = new ArrayList<Double>(map.values());
					Collections.sort(c);

					it = nodes.iterator();
					while (it.hasNext()) {
						bna = it.next();
						int d = c.indexOf(map.get(bna));
						double newy;
						if (d == 0) {
							newy = miny;
						} else if (d == nodes.size() - 1) {
							newy = maxy;
						} else {
							newy = miny + d * ((Math.abs(maxy - miny)) / (nodes.size() - 1));
						}
						point = graphInstance.getPathway().getGraph().getVertexLocation(bna);
						point.setLocation(point.getX(), newy);
						graphInstance.getPathway().getGraph().getVisualizationViewer().getModel().getGraphLayout()
								.setLocation(bna, point);

					}
					graphInstance.getPathway().saveVertexLocations();
				}
			}
		}
	}
}
