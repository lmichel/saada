package saadadb.admintool.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import saadadb.database.Database;
import saadadb.util.Messenger;

public class LogsDisplayer extends JPanel 
{
	private static final long serialVersionUID = 1L;
	private JXTable outputArea;
	private DefaultTableModel dm;
	private JTextField jtfQuickSearch;
	
	public LogsDisplayer(String type)
	{
		super(new GridBagLayout());
		this.createGraphics();
		this.loadLogsFolder(type);
	}
	
	private void createGraphics()
	{
		outputArea = new JXTable(dm);
		outputArea.setRowSelectionAllowed(false);
		outputArea.setShowHorizontalLines(false);
		outputArea.setShowVerticalLines(false);
		outputArea.setHorizontalScrollEnabled(true);
		JTableHeader tableHeader = this.outputArea.getTableHeader();
		
		class IconTableCellRenderer extends DefaultTableCellRenderer {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setPreferredSize(new Dimension(0,0));
				return label;
			}
		}
		tableHeader.setDefaultRenderer(new IconTableCellRenderer());
		
		this.dm = new DefaultTableModel();
		this.dm.addColumn("");
		
		JScrollPane jcp = new JScrollPane(outputArea);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 3;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(jcp, c);
		jtfQuickSearch = new JTextField();
		jtfQuickSearch.addKeyListener(new KeyListener() 
		{
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) 
			{
				if (e.getSource() instanceof JTextField)
				{
					String strFilter = ((JTextField) e.getSource()).getText();
					RowFilter<TableModel, Integer> filter = RowFilter.regexFilter("(?i)" + strFilter);
					outputArea.setRowFilter(filter);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		JPanel jspPanel = new JPanel(new GridBagLayout());
		GridBagConstraints mc_jsp = new GridBagConstraints();
		mc_jsp.insets = new Insets(3, 3, 0, 3);
		mc_jsp.gridx = 0; mc_jsp.gridy = 0; mc_jsp.anchor = GridBagConstraints.FIRST_LINE_END; mc_jsp.weightx = 0;
		jspPanel.add(AdminComponent.getPlainLabel("Quick search (regex) "), mc_jsp);
		mc_jsp.gridx = 1; mc_jsp.gridy = 0; mc_jsp.anchor = GridBagConstraints.FIRST_LINE_START; mc_jsp.weightx = 0.5;
		mc_jsp.fill = GridBagConstraints.HORIZONTAL;
		jspPanel.add(jtfQuickSearch, mc_jsp);
		mc_jsp.gridx = 0; mc_jsp.gridy = 1;
		mc_jsp.weightx = 0.5; mc_jsp.weighty = 0; mc_jsp.fill = GridBagConstraints.BOTH; mc_jsp.gridwidth = 2;
		this.add(jspPanel, mc_jsp);
	}
	
	private void loadLogsFolder(String type)
	{
		String databaseRep = "";
		String webLogFile = null;
		if (type.equals(AdminComponent.LOGS_DISPLAY_ADMINTOOL))
		{
			databaseRep = Database.getRepository() + Database.getSepar() + "logs";
		}
		else // Web logs
		{
			databaseRep = Database.getConnector().getWebapp_home() + Database.getSepar() + "logs";
		}
		Messenger.printMsg(Messenger.DEBUG, "Open logs folder : " + databaseRep);
		File f = new File(databaseRep);
		String[] logFiles = f.list();
		// Load logFiles into application
		Date[] dateToSort = new Date[logFiles.length];
		HashMap<Date, String> map = new HashMap<Date, String>();
		DateFormat dfm = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		for (int i=0 ; i<logFiles.length ; i++)
		{
			if (type.equals(AdminComponent.LOGS_DISPLAY_ADMINTOOL))
			{
				String[] nameSplit = logFiles[i].split("[.]");
				String[] dateSplit = nameSplit[1].split("_");
				try 
				{
					String dateFormatter = dateSplit[0] + "-" + dateSplit[1] + "-" + (2000+Integer.parseInt(dateSplit[2])) + " " + dateSplit[3] + ":" + dateSplit[4] + ":" + dateSplit[5];
					Date dateTmp = (Date) dfm.parse(dateFormatter).clone();
					map.put(dateTmp, logFiles[i]);
					dateToSort[i] = dateTmp;
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				}
			}
			else // Web logs
			{
				if (logFiles[i].compareTo(Database.getName() + ".log")==0)
				{
					webLogFile = logFiles[i];
				}
			}
		}
		
		String toDisplay = "";
		if (type.equals(AdminComponent.LOGS_DISPLAY_ADMINTOOL))
		{
			// Need to sort logFiles by date
			Arrays.sort(dateToSort, new Comparator<Date>() 
			{
			    @Override
			    public int compare(Date lhs, Date rhs) 
			    {
			        if (lhs.getTime() < rhs.getTime())
			            return -1;
			        else if (lhs.getTime() == rhs.getTime())
			            return 0;
			        else
			            return 1;
			    }
			});
			
			// Prepare text to display
			for (int j=0; j<dateToSort.length ; j++)
			{
				toDisplay += "******************* Log " + dfm.format(dateToSort[j]) + " *******************\n";
				if (type.equals(AdminComponent.LOGS_DISPLAY_ADMINTOOL))
				{
					String logContent = LogsDisplayer.readFile(databaseRep + Database.getSepar() + map.get(dateToSort[j]));
					if (logContent == null)
						toDisplay += "No log for this session";
					else
						toDisplay += logContent;
				}
				else
				{
					//TODO
				}
				toDisplay += "\n";
			}
		}
		else // Web logs
		{
			// Prepare text to display
			if (webLogFile != null)
			{
				toDisplay = LogsDisplayer.readFile(databaseRep + Database.getSepar() + webLogFile);
			}
			else
			{
				toDisplay = "No log for this session";
			}
		}
		
		// Print logFiles in JTextArea
		String[] lineToDisplay = toDisplay.split("\n");
		for (int k=0 ; k<lineToDisplay.length ; k++)
		{
			String[] tmp = new String[] {lineToDisplay[k].replace("\t", "   ")};
			this.dm.addRow(tmp);
		}
		outputArea.setModel(dm);
		outputArea.packAll();
		outputArea.addComponentListener(new ComponentAdapter() 
		{
		    public void componentResized(ComponentEvent e) 
		    {
		    	outputArea.scrollRectToVisible(outputArea.getCellRect(outputArea.getRowCount()-1, 0, true));
		    }
		});
	}
	
	private static String readFile (String filename)
	{
	   String content = null;
	   File file = new File(filename); //for ex foo.txt
	   try 
	   {
	       FileReader reader = new FileReader(file);
	       char[] chars = new char[(int) file.length()];
	       reader.read(chars);
	       content = new String(chars);
	       reader.close();
	   } 
	   catch (IOException e) 
	   {
	       e.printStackTrace();
	   }
	   return content;
	}

}
