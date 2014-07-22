package saadadb.admintool.VPSandbox.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.components.AdminComponent;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.products.ColumnSetter;
import saadadb.products.ExtensionSetter;
import saadadb.products.FitsDataFile;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;

/**
 * Create and show the result of the choosen filter when you load a data
 * @author pertuy
 * @version $Id$
 */
public class VPPreviewPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private JButton closeButton;
	private ArrayList<String> loadedExtensionsList;
	private HashMap<String,String[]> loadedValuesList;
	private JEditorPane loadedValuesTable,loadedExtensionsTable;


	public VPPreviewPanel(ArgsParser ap,File f)
	{
		JScrollPane scroller;
		String loadedValues="<html><table border=1><caption><b>Field Values</b></caption><TR><TH> Field Name </TH> <TH> Field Value</TH>"
				+ "<TH>Mapping mode</TH><TH>Mapping message</TH>";
		String loadedExt="<html><table border=1><caption><b>Loaded Extension</b></caption><TR><TH> Title </TH></TR> ";
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;gbc.gridy=0;
		System.out.println(f.exists());
		
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
		scroller=new JScrollPane(loadedValuesTable);
		//scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		gbc.fill=GridBagConstraints.HORIZONTAL;
		//gbc.weightx=1;
		gbc.anchor=GridBagConstraints.CENTER;
		//gbc.gridwidth=GridBagConstraints.REMAINDER;
		this.add(loadedExtensionsTable,gbc);
		gbc.gridy++;
		gbc.weighty=1;

		this.add(scroller,gbc);

	}

	private void createReport(ArgsParser ap,File file) throws FatalException, SaadaException, Exception
	{

		loadedExtensionsList = new ArrayList<String>();
		loadedValuesList = new HashMap<String,String[]>();

		//		File[] allfiles = (file).listFiles();
		//		Set<File> files = new LinkedHashSet<File>();
		//		for( File f: allfiles) {
		//			files.add(f);
		//		}
		//System.out.println(ap.getFilename() + " " + new File(ap.getFilename()).exists());
		//		int cpt = 1;
		//		int MAX = 1;
		//		for( File f: files) {
		//			if( cpt == MAX ) {
		ProductBuilder product = null;
		switch( Category.getCategory(ap.getCategory()) ) {
		case Category.TABLE: product = new TableBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap));
		break;
		case Category.MISC : product = new MiscBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap));
		break;
		case Category.SPECTRUM: product = new SpectrumBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap));
		break;
		case Category.IMAGE: product = new Image2DBuilder((new FitsDataFile(file.getAbsolutePath()))
				, new ProductMapping("mapping", ap));
		break;
		}
		//	product.initProductFile();
		Map<String, ColumnSetter> r = product.getReport();
		//				System.out.println("======== " + f);	
		//				System.out.println("      -- Loaded extensions");	
		for( ExtensionSetter es: product.getReportOnLoadedExtension()) {
			loadedExtensionsList.add(es.toString());
		}
		//				System.out.println("      -- Field values");	
		for( java.util.Map.Entry<String, ColumnSetter> e:r.entrySet()){
			String[] temp = new String[3];
			System.out.print(String.format("%20s",e.getKey()) + "     ");
			ColumnSetter ah = e.getValue();
			temp[1]=ah.getMode();
			temp[2]=ah.getMessage();
			//System.out.print(ah.getMode() + " " + ah.message);
			if( !ah.notSet() ) 
				temp[0]=ah.storedValue.toString();
			loadedValuesList.put(e.getKey(),temp);
			//					System.out.println("");

		}
		//			}
		//			if( cpt > MAX ) break;
		//			cpt++;
		//		}
		//	} catch (Exception e) {
		//		// TODO Auto-generated catch block
		//		e.printStackTrace();
		//	} finally {
		//		Database.close();
		//	}
		//	System.exit(1);
		//}

	}

}
