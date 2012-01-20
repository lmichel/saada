package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadCreateCollection;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ComponentTitledBorder;
import saadadb.admintool.components.DebugButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.exceptions.SaadaException;

/**
 * Input panel for administration task (create collection ......)
 * @author laurent
 * @version $Id$
 *
 */
public abstract class TaskPanel extends AdminPanel implements PropertyChangeListener{
	protected CmdThread cmdThread ;
	
	public TaskPanel(AdminTool rootFrame, String title, String icon, String ancestor) {
		super(rootFrame, title, icon, ancestor);
	}
	
	/**
	 * 
	 */
	protected void setToolBar() {
		this.add(new ToolBarPanel(this, false, false, false));
	}

	/**
	 * Add a fixed size panel filled with the buttons commanding the task
	 * @param buttons
	 */
	protected void setActionBar(Component[] buttons) {
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setPreferredSize(new Dimension(1000,48));
		tPanel.setMaximumSize(new Dimension(1000,48));
		tPanel.setBackground(LIGHTBACKGROUND);
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0; c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.weightx = 0;
		for(Component comp: buttons){
			tPanel.add(comp, c);
			c.gridx++;
		}
		/*
		 * Just to push all previous components to the left
		 */
		c.weightx = 1;
		tPanel.add(new JLabel(" "), c);
		this.add(tPanel);	
	}
	
	
	public CmdThread getCmdThread() {
		return cmdThread;
	}
	
	public abstract void initCmdThread();
	
	/**
	 * Give input parameters to the cmd thread an hash map
	 * @return
	 * @throws SaadaException
	 */
	public boolean setCmdParams() throws SaadaException {
		if( cmdThread != null ) {
			Map<String, Object> map = this.getParamMap();
			if( map == null ) {
				AdminComponent.showFatalError(rootFrame, "NO parameters have been given to the command");
				return false;				
			}
			cmdThread.setParams(map);
			return true;
		}
		else {
			AdminComponent.showFatalError(rootFrame, "There is no command attached to this task panel");
			return false;
		}
	}

	/**
	 * Returns an hashmap with parameters required by the cmd thread
	 * Map keys are supposed to be understood by the cmd thread. There is no
	 * consistency check at this level
	 * @return
	 */
	protected abstract Map<String, Object> getParamMap();
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#active()
	 */
	public void active() {
		debugButton.active();
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	}


	

}
