package database.unid;

import java.awt.Color;
import java.awt.Point;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import biologicalElements.Pathway;
import biologicalObjects.edges.ReactionEdge;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import biologicalObjects.nodes.Protein;
import cluster.clientimpl.SearchCallback;
import cluster.graphdb.DatabaseEntry;
import cluster.graphdb.GraphDBTransportNode;
import cluster.master.IClusterJobs;
import cluster.slave.JobTypes;
import graph.CreatePathway;
import graph.algorithms.NodeAttributeNames;
import graph.algorithms.NodeAttributeTypes;
import graph.jung.classes.MyGraph;
import gui.MainWindow;

/**
 * 
 * @author mlewinsk June 2014
 */
public class UNIDSearch extends SwingWorker<Object, Object> {

	private MainWindow mw;
	private IClusterJobs server;
	private SearchCallback helper;

	private String graphid;
	private String fullName;
	private String commonName;
	private String type;
	private int depth;
	private int direction;
	private HashSet<String> searchNames;

	private boolean headless;

	private HashMap<GraphDBTransportNode, HashSet<GraphDBTransportNode>> adjacencylist;
/*
	input[0] = (String) choosedatabase.getSelectedItem();
	input[1] = (String) chooseType.getSelectedItem();
	input[2] = fullName.getText();
	input[3] = commonName.getText();
	input[4] = graphID.getText();
	input[5] = depthspinner.getValue() + "";
	input[6] = (String) chooseDirection.getSelectedItem();
*/
	
	
	public UNIDSearch(String[] input, boolean headless) {
		this.type = input[1];
		this.fullName = input[2];
		this.commonName = input[3];
		this.graphid = input[4];
		this.depth = (int) Double.parseDouble(input[5]);
		this.searchNames = new HashSet<>();
		
		switch (input[6]) {
		case "both":
			direction = JobTypes.SEARCH_DIRECTION_BOTH;
			break;
		case "outgoing":
			direction = JobTypes.SEARCH_DIRECTION_OUTGOING;
			break;
		case "incoming":
			direction = JobTypes.SEARCH_DIRECTION_INCOMING;
			break;
		default:
			direction = JobTypes.SEARCH_DIRECTION_BOTH;
			break;
		}		
		
		
		try {
			this.helper = new SearchCallback(this);
		} catch (RemoteException re) {
			re.printStackTrace();
		}
		this.headless = headless;
	}

	protected Object doInBackground() throws Exception {

		// check for multi name input
		boolean multi_id_search = false;
		HashSet<String> commonNames = new HashSet<String>();
		if (commonName.contains(",")) {
			multi_id_search = true;
			String name[] = commonName.split(",");
			for (int i = 0; i < name.length; i++) {
				searchNames.add(name[i]);
				commonNames.add(name[i]);
			}
		} else {
			searchNames.add(commonName);
		}

		try {
			String url = "rmi://cassiopeidae/ClusterJobs";
			server = (IClusterJobs) Naming.lookup(url);
			if (multi_id_search) {
				server.submitSearch(commonNames, depth, "any", helper);
			} else {

				if(type.equals("ppi"))
					server.submitSearch(commonName, depth, helper,type, JobTypes.SEARCH_DIRECTION_BOTH);
				else
					server.submitSearch(fullName, depth, helper, type, direction);
				// DEBUG Dataset Search
				// HashSet<String> datasets = new HashSet<String>();
				// datasets.add("FC_68_S01_GE2_107_Sep09_1_1");
				// server.submitSearch(datasets, 1, helper);
			}
		} catch (Exception e) {
			//MARTIN rmi search debug print
//			e.printStackTrace();
			
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(MainWindow
							.getInstance().returnFrame(),
							"Cluster not reachable.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
			reactivateUI();
		}

		return null;
	}

	public void reactivateUI() {
		// close Progress bar and reactivate UI
		mw = MainWindow.getInstance();
		mw.closeProgressBar();
	}

	/**
	 * Creates a new Network tab with the
	 */
	public void createNetworkFromSearch() {
		new Thread(new Runnable() {
			public void run() {
//				

		//cutoff Name for multi ID searches
		if(commonName.length() > 20){
			commonName = commonName.substring(0, 20);
			commonName+="..";
		}
		
		Pathway pw = new CreatePathway(fullName + commonName + graphid
				+ " depth=" + depth).getPathway();
		MyGraph myGraph = pw.getGraph();

		// DO ADDING
		Protein bna;
		HashSet<GraphDBTransportNode> nodeset = new HashSet<>();
		HashMap<GraphDBTransportNode, BiologicalNodeAbstract> nodes = new HashMap<>();

		// Nodes first
		for (GraphDBTransportNode node : adjacencylist.keySet()) {
			if (!nodeset.contains(node)) {
				nodeset.add(node);
				bna = new Protein(node.commonName, node.fullName);
				//Add Attributes
				addAttributes(bna, node);								
				
				if (searchNames.contains(node.commonName)) {
					bna.setColor(Color.RED);
				}
				pw.addVertex(bna, new Point(150, 100));
				nodes.put(node, bna);
			}
			HashSet<GraphDBTransportNode> partners = adjacencylist.get(node);
			for (GraphDBTransportNode partner : partners) {
				if (!nodeset.contains(partner)) {
					nodeset.add(partner);
					bna = new Protein(partner.commonName, partner.fullName);
					addAttributes(bna, partner);
					if (searchNames.contains(partner.commonName)) {
						bna.setColor(Color.RED);
					}
					pw.addVertex(bna, new Point(150, 100));
					nodes.put(partner, bna);
				}
			}
		}

		// then edges
		ReactionEdge r;
		for (GraphDBTransportNode node : adjacencylist.keySet()) {
			HashSet<GraphDBTransportNode> companions = adjacencylist.get(node);
			for (GraphDBTransportNode companion : companions) {
				r = new ReactionEdge("", "", nodes.get(node),
						nodes.get(companion));

				if(type.equals("ppi"))
					r.setDirected(false);
				else
					r.setDirected(true);
				r.setVisible(true);

				pw.addEdge(r);
			}
		}

		myGraph.restartVisualizationModel();

		MainWindow window = MainWindow.getInstance();
		window.updateOptionPanel();
		window.setEnabled(true);
		if (!headless) {
			pw.getGraph().changeToCircleLayout();
			myGraph.normalCentering();
		}
		reactivateUI();

			}
		}).start();
		
	}
	

	private void addAttributes(Protein bna, GraphDBTransportNode node) {
		//Experiments
		int i = 0;
		for(i = 0; i<node.biodata.length; i++){
			bna.addAttribute(NodeAttributeTypes.EXPERIMENT,node.biodata[i], node.biodataEntries[i]);
		}
		
		//Database IDs
		for(DatabaseEntry de :node.dbIds){
			bna.addAttribute(NodeAttributeTypes.DATABASE_ID, de.getDatabase(), de.getId());
		}				
		
		//Annotations
		for(i = 0; i<node.biologicalProcess.length; i++){
			bna.addAttribute(NodeAttributeTypes.ANNOTATION,NodeAttributeNames.GO_BIOLOGICAL_PROCESS, node.biologicalProcess[i]);
		}
		for(i = 0; i<node.cellularComponent.length; i++){
			bna.addAttribute(NodeAttributeTypes.ANNOTATION,NodeAttributeNames.GO_CELLULAR_COMPONENT, node.cellularComponent[i]);
		}
		for(i = 0; i<node.molecularFunction.length; i++){
			bna.addAttribute(NodeAttributeTypes.ANNOTATION,NodeAttributeNames.GO_MOLECULAR_FUNCTION, node.molecularFunction[i]);
		}	
	}

	/**
	 * Set adjacency list, usually called by SearchCallback
	 * 
	 * @param adjacencylist
	 */
	public void setAdjacencyList(
			HashMap<GraphDBTransportNode, HashSet<GraphDBTransportNode>> adjacencylist) {
		this.adjacencylist = adjacencylist;

	}
}
