package biologicalObjects.nodes;

import biologicalElements.Elementdeclerations;
import graph.jung.graphDrawing.VertexShapes;

public class Other extends BiologicalNodeAbstract {

	public Other(String label, String name){		
		super(label,name);
		setBiologicalElement(Elementdeclerations.others);
		shapes = new VertexShapes();
		attributeSetter(this.getClass().getSimpleName(), this);
	}
	
	@Override
	public void rebuildShape(VertexShapes vs){
		//setShape(vs.getEllipse(getVertex()));
	}
	
}
