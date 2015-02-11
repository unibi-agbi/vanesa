package graph.layouts.hctLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D.Float;
import java.awt.geom.QuadCurve2D.Double;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biologicalObjects.edges.BiologicalEdgeAbstract;
import biologicalObjects.nodes.BiologicalNodeAbstract;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.GradientEdgePaintTransformer;
import graph.GraphInstance;
import graph.layouts.HierarchicalCircleLayout;
import graph.layouts.hebLayout.Circle;

public class HCTLayout extends HierarchicalCircleLayout{
	
	HashMap<BiologicalNodeAbstract, HashMap<Integer,List<BiologicalNodeAbstract>>> groups = new HashMap<BiologicalNodeAbstract, HashMap<Integer,List<BiologicalNodeAbstract>>>();
	
	protected HashMap<Integer,List<BiologicalNodeAbstract>> bnaGroups;
	protected List<Integer> groupKeys;
	
	public HCTLayout(Graph<BiologicalNodeAbstract, BiologicalEdgeAbstract> g) {
		this(g,null);
	}
	
	public HCTLayout(Graph<BiologicalNodeAbstract, BiologicalEdgeAbstract> g, BiologicalNodeAbstract root){
		super(g);
		if(root!=null){
			rootNode = root;
		} else {
			rootNode = computeRootNode();
		}
	}
	
	public HCTLayout(Graph<BiologicalNodeAbstract, BiologicalEdgeAbstract> g, List<BiologicalNodeAbstract> order, BiologicalNodeAbstract root){
		super(g,order);
		if(root!=null){
			rootNode = root;
		} else {
			rootNode = computeRootNode();
		}
	}
	
	public HCTLayoutConfig getConfig(){
		return HCTLayoutConfig.getInstance();
	}
	
	@Override
	public void initialize()
    {
		Dimension d = getSize();

        if (d != null)
        {
            if (bnaGroups == null){
            	computeCircleNumbers();
                groupNodes();
            }
            
            computeCircleData(d);

            int group_no = 0;
            int vertex_no = 0;
            
            //larger circle for a larger number of nodes on the outter circle.
            setRadius(getRadius()*Math.log10(graphNodes.size()));
            
            //distance between two ndoes of the same group
            final double nodeDistance = HCTLayoutConfig.nodeDistance(bnaGroups.size(), graphNodes.size());
            
            //distance between two groups (added to small distance between two nodes)
            final double groupDistance = HCTLayoutConfig.groupDistance(nodeDistance);
            
            //positions of the outter circle nodes
            for(Integer i : groupKeys){
            	for(BiologicalNodeAbstract v : bnaGroups.get(i)){
            		double angle = group_no*groupDistance+vertex_no*nodeDistance;
            		GraphInstance.getMyGraph().moveVertex(v, 
            				Math.cos(angle) * getRadius() * maxCircle + centerPoint.getX(),
            				Math.sin(angle) * getRadius() * maxCircle + centerPoint.getY());

            		apply(v);
        			CircleVertexData data = circleVertexDataMap.get(v);
        			data.setCircleNumber(maxCircle);            		
        			data.setVertexAngle(angle);
            		vertex_no++;
            	}
                group_no++;
            }
            
            //positions of the inner circles nodes.
     	   	Set<Point2D> childNodePoints = new HashSet<Point2D>();
            for(BiologicalNodeAbstract n : myGraph.getAllVertices()){
            	childNodePoints.clear();
            	if(circles.get(n)>=1 && !n.isCoarseNode()){
            		if(n!=rootNode){
     				   BiologicalNodeAbstract parentNode = n.getParentNode();
     				   for(BiologicalNodeAbstract pNode : n.getAllParentNodes()){
     					   if(pNode.getRootNode()==n){
     						   parentNode = pNode;
     						   break;
     					   }
     				   }
     				   for(BiologicalNodeAbstract child : parentNode.getCurrentShownChildrenNodes(myGraph)){
     					   if(child!=n){
     						   childNodePoints.add(myGraph.getVertexLocation(child));
     					   }
     				   }
     			   	}
            		apply(n);
            		CircleVertexData data = circleVertexDataMap.get(n);
            		data.setCircleNumber(maxCircle-circles.get(n));            		
            		data.setVertexAngle(Circle.getAngle(getCenterPoint(),Circle.averagePoint(childNodePoints)));
            		Point2D nodeLocation = Circle.getPointOnCircle(getCenterPoint(), getRadius() * data.getCircleNumber(), data.getVertexAngle());
            		myGraph.moveVertex(n,nodeLocation.getX(),nodeLocation.getY());
     		   	}
     	   	}
        }
        setEdgeShapes();
    }
	
	/**
	 * Computes for all graph nodes and their parents the circle they are part of.
	 * @author tloka
	 */
	public void computeCircleNumbers(){
		circles = new HashMap<BiologicalNodeAbstract,Integer>();
		maxCircle = 0;
		int c;
		for(BiologicalNodeAbstract node : getGraph().getVertices()){
			c=0;
			BiologicalNodeAbstract p = node;
			BiologicalNodeAbstract rootNode;
			while(p!=null){
				rootNode = p.getRootNode();
				if(circles.containsKey(p)){
					circles.put(p,Math.max(c, circles.get(p)));
				} else {
					circles.put(p, c);
				}
				if(rootNode!=null){
					if(circles.containsKey(rootNode)){
						circles.put(rootNode,Math.max(c, circles.get(rootNode)));
					} else {
						circles.put(rootNode, c);
					}
				}
				maxCircle = Math.max(c, maxCircle);
				p=p.getParentNode();
				c+=1;		
			}
		}
		maxCircle+=1;
		circles.put(this.rootNode,maxCircle);
	}
	
	@Override
	public void groupNodes() {
		graphNodes = new HashSet<BiologicalNodeAbstract>();
		graphNodes.addAll(graph.getVertices());

		if(graphNodes.size()<2){return;}

		order = computeOrder();
		
		bnaGroups = new HashMap<Integer,List<BiologicalNodeAbstract>>();
		Set<BiologicalNodeAbstract> addedNodes = new HashSet<BiologicalNodeAbstract>();
		groupKeys = new ArrayList<Integer>();
		BiologicalNodeAbstract currentNode;
		BiologicalNodeAbstract referenceParent;

		for(BiologicalNodeAbstract node : order){
			currentNode = node.getCurrentShownParentNode(myGraph);
			if(addedNodes.contains(currentNode)){
				continue;
			}
			if(circles.get(currentNode)!=0){
				graphNodes.remove(currentNode);
				continue;
			}
			
			referenceParent = currentNode.getLastParentNode();
			if(referenceParent==null){
				groupKeys.add(currentNode.getID());
				bnaGroups.put(currentNode.getID(), new ArrayList<BiologicalNodeAbstract>());
				bnaGroups.get(currentNode.getID()).add(currentNode);
				addedNodes.add(currentNode);
				continue;
			}
			
			if(!groupKeys.contains(referenceParent.getID())){
				groupKeys.add(referenceParent.getID());
				bnaGroups.put(referenceParent.getID(),new ArrayList<BiologicalNodeAbstract>());
			}
			bnaGroups.get(referenceParent.getID()).add(currentNode);
			addedNodes.add(currentNode);
		}
	}
	
	public BiologicalNodeAbstract getGroupParent(BiologicalNodeAbstract n){
		if(n == rootNode){
			return n;
		}
		if(n.getLastParentNode()==null){
			return n;
		}
		return n.getLastParentNode();
	}
	
	@Override
	public List<BiologicalNodeAbstract> getNodesGroup(BiologicalNodeAbstract n) {
		List<BiologicalNodeAbstract> selection = new ArrayList<BiologicalNodeAbstract>();
		BiologicalNodeAbstract parent;
		switch (HCTLayoutConfig.SELECTION){
		case SINGLE:
			selection.add(n);
			break;
		case SUBPATH:
			parent = n.getParentNode();
			if(parent!=null && parent.getRootNode()==n){
				for(BiologicalNodeAbstract child : parent.getCurrentShownChildrenNodes(myGraph)){
					if(!selection.contains(child)){
						selection.add(child);
					}
				}
			} else {
				for(BiologicalNodeAbstract child : n.getCurrentShownChildrenNodes(myGraph)){
					if(!selection.contains(child)){
						selection.add(child);
					}
				}
			}
			break;
		case FINESTGROUP:
			parent = n.getParentNode();
			if(parent==null){
				break;
			}
			for(BiologicalNodeAbstract child : parent.getCurrentShownChildrenNodes(myGraph)){
				if(!selection.contains(child) && child!=parent.getRootNode()){
					selection.add(child);
				}
			}
			break;
		case ROUGHESTGROUP:
			parent = n.getLastParentNode();
			for(BiologicalNodeAbstract child : parent.getCurrentShownChildrenNodes(myGraph)){
				if(!selection.contains(child)){
					selection.add(child);
				}
			}
			break;
			
		default:
			selection.add(n);
			break;
		}
		return selection;
	}

	/**
	 * Computes the necessary root node for the HCT Layout.
	 * @return The root node.
	 */
	public BiologicalNodeAbstract computeRootNode(){
		int maxNeighborNodes = 0;
		BiologicalNodeAbstract rootNode = null;
		for(BiologicalNodeAbstract n : graph.getVertices()){
			if(n.isCoarseNode()){
				continue;
			}
			if(n.getParentNode()!=GraphInstance.getPathwayStatic() && n.getParentNode()!=null){
				continue;
			}
			if(graph.getNeighborCount(n)>maxNeighborNodes){
				maxNeighborNodes = graph.getNeighborCount(n);
				rootNode = n;
			}
		}
		return rootNode;
	}		
	
	@Override
	public void setEdgeShapes() {
		GraphInstance.getMyGraph().getVisualizationViewer().getRenderContext().setEdgeShapeTransformer(new HctEdgeShape());
		GraphInstance.getMyGraph().getVisualizationViewer().getRenderContext().setEdgeDrawPaintTransformer(
				new HCTEdgePaintTransformer(GraphInstance.getMyGraph().getVisualizationViewer()));

	}
	
	protected class HCTEdgePaintTransformer extends GradientEdgePaintTransformer<BiologicalNodeAbstract, BiologicalEdgeAbstract>{
		public HCTEdgePaintTransformer(VisualizationViewer<BiologicalNodeAbstract, BiologicalEdgeAbstract> vv) {
			super(getConfig().INTERNAL_EDGE_COLOR, getConfig().EXTERNAL_EDGE_COLOR,vv);
		}
		
		@Override
		public Paint transform(BiologicalEdgeAbstract e){
			GradientPaint gp = (GradientPaint) super.transform(e);
			BiologicalNodeAbstract first = e.getFrom();
			BiologicalNodeAbstract second = e.getTo();
			List<BiologicalNodeAbstract> firstParentRootNodes = new ArrayList<BiologicalNodeAbstract>();
			List<BiologicalNodeAbstract> secondParentRootNodes = new ArrayList<BiologicalNodeAbstract>();
			for(BiologicalNodeAbstract n : first.getAllParentNodesSorted()){
				if(n.getRootNode()!=null){
					firstParentRootNodes.add(n.getRootNode());
				}
			}
			for(BiologicalNodeAbstract n : second.getAllParentNodesSorted()){
				if(n.getRootNode()!=null){
					secondParentRootNodes.add(n.getRootNode());
				}
			}
			if(!(firstParentRootNodes.contains(second) || secondParentRootNodes.contains(first)) && first!=rootNode && second!=rootNode){
				return new GradientPaint(0, 0, getConfig().EXTERNAL_EDGE_COLOR, 0, 0, getConfig().EXTERNAL_EDGE_COLOR, true);
			}
			return new GradientPaint(0,0 , getConfig().INTERNAL_EDGE_COLOR, 0,0, getConfig().INTERNAL_EDGE_COLOR, true);
		}
	}
	
	protected class HctEdgeShape extends EdgeShape.Line<BiologicalNodeAbstract, BiologicalEdgeAbstract>{
		public HctEdgeShape(){
			super();
		}
		
		@Override
		public Shape transform(Context<Graph<BiologicalNodeAbstract, BiologicalEdgeAbstract>, BiologicalEdgeAbstract> context){
			BiologicalEdgeAbstract edge = context.element;
			Pair<BiologicalNodeAbstract> endpoints = graph.getEndpoints(edge);
			BiologicalNodeAbstract first = endpoints.getFirst();
			BiologicalNodeAbstract second = endpoints.getSecond();
			List<BiologicalNodeAbstract> firstParentRootNodes = new ArrayList<BiologicalNodeAbstract>();
			List<BiologicalNodeAbstract> secondParentRootNodes = new ArrayList<BiologicalNodeAbstract>();
			for(BiologicalNodeAbstract n : first.getAllParentNodesSorted()){
				if(n.getRootNode()!=null){
					firstParentRootNodes.add(n.getRootNode());
				}
			}
			for(BiologicalNodeAbstract n : second.getAllParentNodesSorted()){
				if(n.getRootNode()!=null){
					secondParentRootNodes.add(n.getRootNode());
				}
			}
			
			if(first.getParentNode()!=null && first.getParentNode().getRootNode()==second){
				return new Line2D.Double(0.0,0.0,1.0,0.0);
			}
			if(second.getParentNode()!=null && second.getParentNode().getRootNode()==first){
				return new Line2D.Double(0.0,0.0,1.0,0.0);
			}
			if(first==rootNode || second==rootNode){
				return new Line2D.Double(0.0,0.0,1.0,0.0);
			}
			
			if(!(firstParentRootNodes.contains(second) || secondParentRootNodes.contains(first))){
				if(getConfig().getShowExternalEdges()){
					return new Line2D.Double(0.0,0.0,1.0,0.0);
				}
				return new Line2D.Double(0.0,0.0,0.0,0.0);
			}
			
			Path2D path = new Path2D.Double();
			Point2D lastPoint = new Point2D.Double(0.0,0.0);
			BiologicalNodeAbstract startNode = first;
			BiologicalNodeAbstract endNode = second;
			if(circles.get(first)>circles.get(second)){
				startNode = second;
				endNode = first;
				lastPoint = new Point2D.Double(1.0,0.0);
			}
			BiologicalNodeAbstract parentNode = startNode.getParentNode();
			while(parentNode!=null && parentNode.getRootNode()!=endNode){
				Set<Point2D> childNodePoints = new HashSet<Point2D>();
				for(BiologicalNodeAbstract child : parentNode.getCurrentShownChildrenNodes(myGraph)){
					childNodePoints.add(myGraph.getVertexLocation(child));
	     		}         	
	   			double angle = Circle.getAngle(getCenterPoint(),Circle.averagePoint(childNodePoints));
	     		Point2D location = Circle.getPointOnCircle(getCenterPoint(), getRadius()*(maxCircle-circles.get(parentNode)), angle);
	     		location = Circle.computeControlPoint(location, getCenterPoint(), myGraph.getVertexLocation(first), myGraph.getVertexLocation(second));
	     		Line2D line = new Line2D.Double(lastPoint,location);
	     		path.append(line, true);
	     		lastPoint = location;
	     		parentNode = parentNode.getParentNode();
	     	}
			Line2D line;
			if(endNode==first){
				line = new Line2D.Double(lastPoint,new Point2D.Double(0.0,0.0));

			} else {
				line = new Line2D.Double(lastPoint,new Point2D.Double(1.0,0.0));
			}
	     	path.append(line, true);
	     	return path;
			
		}
	}
}
