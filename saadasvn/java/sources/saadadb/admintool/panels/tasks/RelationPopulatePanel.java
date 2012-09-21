package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadRelationPopulate;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.correlator.CollectionCoverage;
import saadadb.admintool.components.correlator.KNNEditor;
import saadadb.admintool.components.correlator.KeywordConditionEditor;
import saadadb.admintool.components.correlator.QualifierSetter;
import saadadb.admintool.components.correlator.RelationChooser;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaRelation;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;

public class RelationPopulatePanel extends TaskPanel {
	private static final long serialVersionUID = 1L;

	private RelationChooser relationChooser;
	private CollectionCoverage collectionCoverage;
	private KNNEditor knnEditor;
	private KeywordConditionEditor keywordConditionEditor;
	private QualifierSetter qualifierSetter;
	private RunTaskButton runButton;
	private RelationConf relationConf;
	private SaveButton saveButton;


	public RelationPopulatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, POPULATE_RELATION, null, ancestor);
	}


	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationPopulate(rootFrame, POPULATE_RELATION);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		try {
			this.relationChooser.setDataTreePath(null);
			this.setSelectedResource("", null);

			if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) {
				this.collectionCoverage.reset();
				this.knnEditor.reset();
				this.keywordConditionEditor.reset();
				this.qualifierSetter.reset();
				this.relationChooser.setDataTreePath(dataTreePath);
				this.relationConf = null;
				this.relationChooser.setCollapsed(false);
				this.collectionCoverage.setCollapsed(true);
				this.knnEditor.setCollapsed(true);
				this.keywordConditionEditor.setCollapsed(true);
				this.qualifierSetter.setCollapsed(true);
				this.saveButton.setEnabled(false);
			}
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();
		this.setConfig();
		retour.put("config", this.relationConf);
		return retour;
	}


	/**
	 * @return
	 */
	public String getCorrelator() {
		return collectionCoverage.getCorrelator() +
		keywordConditionEditor.getCorrelator() +
		knnEditor.getCorrelator() ; 
	}

	/**
	 * 
	 */
	private void setConfig(){
		if( this.relationConf != null ) {
			String name = this.relationConf.getNameRelation();
			MetaRelation mr = Database.getCachemeta().getRelation(name);
			if( mr == null  ) {
				showFatalError(rootFrame, "Relationship <" + name + "> does not exist exists into the DB");
				return;
			} else {
				/*
				 * Copy the meta relation into the configuration ....
				 */
				RelationConf relationConfiguration = new RelationConf();

				relationConfiguration.setNameRelation(name);
				relationConfiguration.setDescription(mr.getDescription());
				relationConfiguration.setColPrimary_name(mr.getPrimary_coll());
				relationConfiguration.setColPrimary_type(mr.getPrimary_category());

				relationConfiguration.setColSecondary_name(mr.getSecondary_coll());
				relationConfiguration.setColSecondary_type(mr.getSecondary_category());
				relationConfiguration.setClass_name(name);	
				for( String q: mr.getQualifier_names()) {
					relationConfiguration.setQualifier(q, "double");
				}
				/*
				 * ... except the correlator query which is read from the panel
				 */
				relationConfiguration.setQuery(this.getCorrelator().trim());
				this.relationConf = relationConfiguration;
			}
		} else {
			showFatalError(rootFrame, "Not selected relationship");
		}

	}
	/**
	 * @throws Exception
	 */
	public void save()  throws Exception {
		if( this.relationConf != null ) {
			this.setConfig();
			SQLTable.beginTransaction();
			Table_Saada_Relation.saveCorrelator(this.relationConf);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			showSuccess(this.rootFrame, "Relationship <" + this.relationConf.getNameRelation() +"> saved, but the correlator has not been applied"); 
			this.cancelChanges();
		} else {
			showFatalError(rootFrame, "Not selected relationship");
		}

	}
	/**
	 * @param relationConf
	 */
	public void load() {
		this.relationConf = (new RelationManager(relationChooser.getSelectedRelation())).getRelation_conf();
		this.collectionCoverage.load(relationConf);
		this.knnEditor.load(relationConf);
		this.keywordConditionEditor.load(relationConf);

		if( this.relationConf.getQualifier().size() > 0 ) {
			this.qualifierSetter.setVisible(true);
			this.qualifierSetter.load(relationConf);
		}
		else {
			this.qualifierSetter.setVisible(false);
		}
		this.updateAvailableAttributes();
		this.saveButton.setEnabled(true);
		this.updateUI();
	}
	/**
	 * @param prefix
	 */
	public void updateAvailableAttributes() {
		keywordConditionEditor.updateAvailableAttributes(collectionCoverage.getPrimaryCLasses()
				, collectionCoverage.getSecondaryCLasses());
		qualifierSetter.updateAvailableAttributes(collectionCoverage.getPrimaryCLasses()
				, collectionCoverage.getSecondaryCLasses());
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	/**
	 * Programaticaly select a relation. So the panel can be open on the relation just created
	 * @param relationName
	 */
	public void setSelectedResource(String relationName, String comment){
		super.setSelectedResource(relationName, comment);
		relationChooser.selectRelation(relationName);
	}

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);
		saveButton = new SaveButton(this);
		JPanel tPanel = this.addSubPanel("Correlator Editor");
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new GridBagLayout());
		editorPanel.setBackground(LIGHTBACKGROUND);

		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;

		relationChooser = new RelationChooser(this, runButton);
		editorPanel.add(relationChooser, emc);

		collectionCoverage = new CollectionCoverage(this, runButton);
		emc.newRow();
		editorPanel.add(collectionCoverage, emc);

		emc.newRow();
		knnEditor = new KNNEditor(this, null);
		editorPanel.add(knnEditor, emc);

		emc.newRow();
		keywordConditionEditor = new KeywordConditionEditor(this, null);
		editorPanel.add(keywordConditionEditor, emc);

		emc.newRow();
		qualifierSetter = new QualifierSetter(this);
		editorPanel.add(qualifierSetter, emc);
		qualifierSetter.setVisible(false);

		MyGBC imcep = new MyGBC(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(editorPanel), imcep);


		this.setActionBar(new Component[]{runButton, saveButton
				, debugButton});
	}
}
