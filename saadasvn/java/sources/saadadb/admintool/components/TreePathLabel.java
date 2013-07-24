package saadadb.admintool.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Panel;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.utils.DataTreePath;

public class TreePathLabel extends Panel 
{
	protected JLabel collection, category, classe;
	
	public TreePathLabel() 
	{
		super(new FlowLayout());
		this.setBackground(AdminComponent.LIGHTBACKGROUND);

		collection = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Database_small.png")));
		collection.setVisible(false);
		collection = AdminComponent.setSubTitleLabelProperties(collection);
		this.add(collection);
		
		category = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Bluecube_small.png")));
		category.setVisible(false);
		category = AdminComponent.setSubTitleLabelProperties(category);
		this.add(category);

		classe = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/SQLTable_small.png")));
		this.add(classe);
		classe = AdminComponent.setSubTitleLabelProperties(classe);
		classe.setVisible(false);
		
		this.setForegroundtreePathPanel(AdminComponent.NEW_HEADER);
		//this.initTreePathLabel();
	}
	
	public void setTextTreePathPanel(DataTreePath dataTreePath)
	{
		if (dataTreePath != null)
		{	
			// Collection JLabel
			if (dataTreePath.collection!=null)
			{
				collection.setText(dataTreePath.collection + " ");
				collection.setVisible(true);
			}
			else
			{
				collection.setVisible(false);
			}
			// Category JLabel
			if (dataTreePath.category!=null)
			{
				category.setText(dataTreePath.category + " ");
				category.setVisible(true);
			}
			else
			{
				category.setVisible(false);
			}
			// Classe JLabel
			if (dataTreePath.classe!=null)
			{
				classe.setText(dataTreePath.classe + " ");
				classe.setVisible(true);
			}
			else
			{
				classe.setVisible(false);
			}
		}
	}
	
	public void setForegroundtreePathPanel(Color color)
	{
		collection.setForeground(color);
		category.setForeground(color);
		classe.setForeground(color);
	}
}
