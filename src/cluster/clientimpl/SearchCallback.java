package cluster.clientimpl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import cluster.graphdb.GraphDBTransportNode;
import cluster.slave.ISearchCallback;
import database.unid.UNIDSearch;
import gui.MainWindow;
import gui.MyPopUp;

public class SearchCallback extends UnicastRemoteObject implements ISearchCallback, Serializable {

	private static final long serialVersionUID = 7891201262766307950L;

	private UNIDSearch usearch;

	public SearchCallback(UNIDSearch usearch) throws RemoteException {
		super();
		this.usearch = usearch;
	}

	@Override
	public void setResultAdjacencyList(HashMap<GraphDBTransportNode, HashSet<GraphDBTransportNode>> adjacencylist)
			throws RemoteException {

		// check for empty search
		if (adjacencylist.size() < 1) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MyPopUp.getInstance().show("Error", "No elements could be found.");
					usearch.reactivateUI();
				}
			});
		} else {
			// Preset adjacency list in Search object
			usearch.setAdjacencyList(adjacencylist);
			MainWindow.getInstance().showProgressBar("Applying Layout..");

			// DEBUG
//			HashSet<String> allnodes = new HashSet<String>();
//			for (Entry<String, HashSet<String>> e : adjacencylist.entrySet()) {
//				allnodes.add(e.getKey());
//				allnodes.addAll(e.getValue());
//			}
//			System.out.println("Found " + allnodes.size()+" Nodes.");

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					usearch.createNetworkFromSearch();
				}
			});
		}

	}

	@Override
	public void progressNotify(String message) throws RemoteException {
		MainWindow.getInstance().showProgressBar(message);
	}
}
