package graph.gui;

import graph.GraphInstance;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import biologicalElements.ElementNamesSingelton;
import biologicalElements.Elementdeclerations;
import biologicalElements.GraphElementAbstract;
import biologicalElements.Pathway;
import biologicalObjects.nodes.BiologicalNodeAbstract;

public class ParameterWindow implements ActionListener{

	
	private JPanel panel;
	private String[] details = new String[3];
	private JOptionPane pane;
	private JTextField name = new JTextField("a");
	private JComboBox<String> elementNames = new javax.swing.JComboBox<String>();
	private JComboBox<String> compartment = new JComboBox<String>();
	private GraphInstance graphInstance = new GraphInstance();
	private Pathway pw = graphInstance.getPathway();
	private JComboBox<String> box = new JComboBox<String>();
	private JTextField value = new JTextField("1");
	private JTextField unit = new JTextField("u");
	private JButton add;
	private GraphElementAbstract gea;
	
	boolean addedNewValues = false;
	private JDialog dialog;
	//private HashMap<JButton, Parameter> parameters = new HashMap<JButton, Parameter>();

	
	public ParameterWindow(GraphElementAbstract gea) {
		this.gea = gea;
		//System.out.println("constr.");
		
		MigLayout layout = new MigLayout("", "[left]");
		
		//DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<String>(ElementNamesSingelton.getInstance().getEnzymes());
		//elementNames.setEditable(true);
		//elementNames.setModel(dcbm);
		
		//elementNames.setMaximumSize(new Dimension(250,40));
		//elementNames.setSelectedItem(" ");
		//AutoCompleteDecorator.decorate(elementNames);
		
		panel = new JPanel(layout);
		add = new JButton("Add");
		add.setActionCommand("add");
		add.addActionListener(this);
		
		
		//panel.add(value, "span,wrap 5,growx ,gaptop 2");
		
		//panel.add(new JLabel("Label"), "span 2, gaptop 2 ");
		//panel.add(elementNames, "span,wrap,growx ,gap 10, gaptop 2");
		//panel.add(new JSeparator(), "span, growx, wrap 10, gaptop 7 ");

		
	
		pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		
		dialog = pane.createDialog(null, "Parameters");
		this.repaint();
		dialog.pack();
		//dialog.show();
		dialog.setVisible(true);
		
		

	}
	
	private void listParameters(){
		Parameter p;
		panel.add(new JLabel("Name"), "span 2, gaptop 2");
		panel.add(new JLabel("Value"), "span 4, gapright 4");
		panel.add(new JLabel("Unit"), "span 4, gapright 4, wrap");
		
		for(int i = 0; i< gea.getParameters().size(); i++){
			p = gea.getParameters().get(i);
			panel.add(new JLabel(p.getName()), "span 2, gaptop 2");
			
			panel.add(new JLabel(p.getValue()+""), "span 4, gapright 4");
			
			panel.add(new JLabel(p.getUnit()), "span 4, gapright 4");
			
			JButton del = new JButton("Delete");
			del.setActionCommand("del"+i);
			
			del.addActionListener(this);
			
			//parameters.put(del, p);
			
			
			panel.add(del, "span 4, gapright 4, wrap");
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if("add".equals(e.getActionCommand())){
			Parameter p;
			for(int i = 0; i< gea.getParameters().size(); i++){
				p = gea.getParameters().get(i);
				if(p.getName().equals(name.getText())){
					System.out.println("schon vorhanden");
					return;
				}
			}
			
			gea.getParameters().add(new Parameter(name.getText(), Double.valueOf(value.getText()), unit.getText()));
			panel.add(new JLabel(name.getText()), "span 2, gaptop 2 ");
			panel.add(new JLabel(value.getText()),"span 4, gapright 4");
			panel.add(new JLabel(unit.getText()), "span 4, gapright 4, wrap");
			
			this.repaint();
			
		}else if(e.getActionCommand().startsWith("del")){
			System.out.println("drin");
			System.out.println(e.getActionCommand().substring(3));
			
			int idx = Integer.parseInt(e.getActionCommand().substring(3));
			this.gea.getParameters().remove(idx);
			this.repaint();
		}
		
		
		
	}
	
	private void repaint(){
		panel.removeAll();
		//parameters.clear();
		
		panel.add(new JLabel("Name"), "span 2, gaptop 2 ");
		panel.add(name, "span,wrap,growx ,gap 10, gaptop 2");
		
		panel.add(new JLabel("Value"), "span 4, gapright 4");
		panel.add(value, "span,wrap,growx ,gap 10, gaptop 2");
		
		panel.add(new JLabel("Unit"), "span 4, gapright 4");
		panel.add(unit, "span,wrap,growx ,gap 10, gaptop 2");
		
		
		panel.add(add, "wrap");
		this.listParameters();
		panel.repaint();
		//pane.repaint();
		//dialog.repaint();
		dialog.pack();
		
		
		
	}
	
}