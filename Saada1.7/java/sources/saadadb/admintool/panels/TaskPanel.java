package saadadb.admintool.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ToolBarPanel;

/**
 * Input panel for administration task (create collection ......)
 * @author laurent
 * @version $Id$
 *
 */
@SuppressWarnings("serial")
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
		c.insets = new Insets(3, 3, 3, 3);
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
	
	protected void setProcessControlBar(Component[] buttons, JLabel statusLabel, JLabel[] hardwareLights) {
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setPreferredSize(new Dimension(1000,48));
		tPanel.setMaximumSize(new Dimension(1000,48));
		tPanel.setBackground(LIGHTBACKGROUND);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 3, 3, 3);
		c.gridy = 0; c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.weightx = 0;
		for(Component comp: buttons){
			tPanel.add(comp, c);
			c.gridx++;
		}
		
		c.gridx++;
		c.weightx = 1; c.weighty = 0;	
		c.anchor = GridBagConstraints.SOUTHEAST;
		tPanel.add((statusLabel = new JLabel("STATUS")), c);

		c.gridx++;
		c.weightx = 1; c.weighty = 0;	
		c.anchor= GridBagConstraints.SOUTHEAST;
		JPanel stsPanel = new JPanel();
		stsPanel.setLayout(new BoxLayout(stsPanel, BoxLayout.LINE_AXIS));
		Border empty = new EmptyBorder(new Insets(2,2,2,2));
		for( JLabel cmp: hardwareLights) {
			cmp.setBorder(empty);
			stsPanel.add(cmp);
			
		}
		tPanel.add(stsPanel, c);	
		this.add(tPanel);	
	}
	
	
	public CmdThread getCmdThread() {
		return cmdThread;
	}
	
	/**
	 * invoked by @link RunTaskButton when the run button is created
	 */
	public abstract void initCmdThread();
	
	/**
	 * Give input parameters to the cmd thread an hash map
	 * @return
	 * @throws Exception 
	 */
	public boolean setCmdParams(boolean withConfirm) throws Exception {
		if( cmdThread != null ) {
			Map<String, Object> map = this.getParamMap();
			if( map == null ) {
				AdminComponent.showFatalError(rootFrame, "NO parameters have been given to the command");
				return false;				
			}
			cmdThread.setParams(map);
			/*
			 * Check param here to avoid opening the process panel in case of failing check
			 */
			return cmdThread.checkParams(withConfirm);
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
