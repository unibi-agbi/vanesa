package cluster.slave;

public class JobTypes {
	/**
	 * Decorator for Cluster job types
	 */

	public static final int CYCLE_JOB_OCCURRENCE = 10;
	public static final int CYCLE_JOB_NEIGHBORS = 11;
	public static final int CLIQUE_JOB_OCCURRENCE = 20;
	public static final int CLIQUE_JOB_NEIGHBORS = 21;
	public static final int CLIQUE_JOB_PATHSLESS = 22;
	public static final int CLIQUE_JOB_CONNECTIVITY = 23;
	public static final int APSP_CLUSTERING_JOB_OCCURENCE = 30;
	public static final int APSP_CLUSTERING_JOB_SCORING = 31;
	public static final int SPECTRAL_CLUSTERING_JOB = 40;
	public static final int LAYOUT_FR_JOB = 50;
	public static final int LAYOUT_MULTILEVEL_JOB = 60;
	public static final int LAYOUT_MDS_FR_JOB = 70;
	public static final int LAYOUT_SMACOF_JOB = 71;
	public static final int DCB_CLUSTERING_CLUSTERS = 80;
	public static final int DCB_CLUSTERING_GRID = 81;

	
	
	
	/**
	 * Graphdb only jobs
	 */
	public static final int MAPPING_HPRD = 110;
	public static final int MAPPING_BRENDA = 111;
	public static final int MAPPING_INTACT = 112;
	public static final int MAPPING_KEGG = 113;
	public static final int MAPPING_MINT = 114;
	public static final int MAPPING_UNIPROT = 115;

	public static final int SEARCH_DEPTH = 100;
	public static final int SEARCH_MINIMAL_NETWORK = 101;
	public static final int SEARCH_DATASET = 102;
	public static final int SEARCH_GO_CELLULAR_COMPONENT = 103;
	public static final int SEARCH_GO_BIOLOGICAL_PROCESS = 104;
	public static final int SEARCH_GO_MOLECULAR_FUNCTION = 105;
	public static final int SEARCH_DIRECTION_BOTH = 1000;
	public static final int SEARCH_DIRECTION_INCOMING = 1001;
	public static final int SEARCH_DIRECTION_OUTGOING = 1002;	
}
