package saadadb.admintool.VPSandbox.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JEditorPane;
import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.ExtensionSetter;
import saadadb.products.FitsDataFile;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.setter.ColumnSetter;

/**
 * Create and show the result of the choosen filter when you load a data
 * @author pertuy
 * @version $Id$
 */
public class VPPreviewPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ArrayList<String> loadedExtensionsList;
	private TreeMap<String,String[]> loadedValuesList;
	private JEditorPane loadedValuesTable,loadedExtensionsTable;

	/**
	 * Create a JPanel displaying the preview of a filter's application on an unique file
	 * @param ap
	 * @param f
	 */
	public VPPreviewPanel(ArgsParser ap,File f)
	{
		String loadedValues="<html><table border=1><caption><b>Field Values</b></caption><TR><TH> Field Name </TH> <TH> Field Value</TH>"
				+ "<TH>Mapping mode</TH><TH>Mapping message</TH>";
		String loadedExt="<html><table border=1><caption><b>Loaded Extension</b></caption><TR><TH> Title </TH></TR> ";
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;gbc.gridy=0;		
		try{
			createReport(ap,f);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		for(String s:loadedExtensionsList)
		{
			loadedExt+="<TR><TD>"+s+"</TD></TR>";
		}

		for(Entry<String, String[]> en: loadedValuesList.entrySet())
		{
			String[] temp = en.getValue();

			//To avoid any problem between the messages and the html
			if(temp[0]==null)
				temp[0]="NOT SET";
			
			if(temp[2].contains(">"))
			{
				System.out.println("AVANT :"+temp[2]);
				temp[2]=temp[2].replace(">", "&gt;");
				System.out.println("APRES:"+ temp[2]);
			}
			if(temp[2].contains("<"))
			{
				temp[2]=temp[2].replace("<", "&lt;");
			}
			loadedValues+="<TR><TD>"+en.getKey()+"</TD>"+"<TD>"+temp[0]+"</TD><TD>"+temp[1]+"</TD><TD>"+temp[2]+"</TD></TR>";
		}
		loadedValuesTable=new JEditorPane("text/html",loadedValues);
		loadedValuesTable.setEditable(false);
		loadedExtensionsTable=new JEditorPane("text/html",loadedExt);
		loadedExtensionsTable.setEditable(false);
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.CENTER;
		this.add(loadedExtensionsTable,gbc);
		gbc.gridy++;
		gbc.weighty=1;

		this.add(loadedValuesTable,gbc);

	}
	/**
	 * Generate the preview values
	 * @param ap
	 * @param file
	 * @throws FatalException
	 * @throws SaadaException
	 * @throws Exception
	 */
	private void createReport(ArgsParser ap,File file) throws FatalException, SaadaException, Exception
	{

		loadedExtensionsList = new ArrayList<String>();
		loadedValuesList = new TreeMap<String,String[]>();
		ProductBuilder product = null;
		switch( Category.getCategory(ap.getCategory()) ) {
		case Category.TABLE: product = new TableBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap), null);
				break;
		case Category.MISC : product = new MiscBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap), null);
				break;
		case Category.SPECTRUM: product = new SpectrumBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap), null);
				break;
		case Category.IMAGE: product = new Image2DBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap), null);
				break;
		}
		Map<String, ColumnSetter> r = product.getReport();

		for( ExtensionSetter es: product.getReportOnLoadedExtension()) {
			loadedExtensionsList.add(es.toString());
		}
		for( java.util.Map.Entry<String, ColumnSetter> e:r.entrySet()){
			String[] temp = new String[3];
			System.out.print(String.format("%20s",e.getKey()) + "     ");
			ColumnSetter ah = e.getValue();
			temp[1]=ah.getSettingMode().toString();
			temp[2]=ah.getMessage();
			if( !ah.isNotSet() ) 
				temp[0]=ah.storedValue.toString();
			loadedValuesList.put(e.getKey(),temp);

		}


	}

}
