package cluster.clientimpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

import cluster.master.IClusterJobs;
import gui.MainWindow;
import gui.MyPopUp;

public class ClusterComputeThread extends Thread {

	private Hashtable<Integer, Double> resulttable;
	private HashSet<HashSet<Integer>> resultset;
	private byte [] jobinformation; 
	private int job;
	private IClusterJobs jobinterface;
	private ComputeCallback helper;
	private MainWindow mw;

	public ClusterComputeThread(int job, byte[] jobinformation,ComputeCallback helper) {
		this.job = job;
		this.jobinformation = jobinformation;
		this.helper = helper;
	}

	@Override
	public void run() {
		// compute job on server
		submitJob();
	}

	private void submitJob() {

		//URL of Master server
		String url = "rmi://cassiopeidae/ClusterJobs";

//		String url = "rmi://griseue/ClusterJobs";
		// String url = "rmi://nero/Server";
		resulttable = new Hashtable<Integer, Double>();
		try {
			jobinterface = (IClusterJobs) Naming.lookup(url);
		
			if (!jobinterface.submitJob(job,jobinformation,helper)) {
				displayNotice("Queue is at maximum capacity!");
			}
			

		}catch (NotBoundException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MyPopUp.getInstance().show("Error", "RMI Interface could not be established.");
				}
			});

			mw = MainWindow.getInstance();
			mw.closeProgressBar();

		} catch (RemoteException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MyPopUp.getInstance().show("Error", "Cluster not reachable.");
				}
			});

			mw = MainWindow.getInstance();
			mw.closeProgressBar();

		} catch (MalformedURLException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					MyPopUp.getInstance().show("Error", "Clusteradress could not be resolved.");
				}
			});

			mw = MainWindow.getInstance();
			mw.closeProgressBar();
		}

	}
	
	private void displayNotice(String string) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MyPopUp.getInstance().show("Information", string);
			}
		});		
	}

	
	public Hashtable<Integer, Double> getResultTable() {
		return resulttable;
	}

	public HashSet<HashSet<Integer>> getResultSet() {
		return resultset;
	}
}
