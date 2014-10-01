package saadadb.vo.request.formator.votable;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.collection.obscoremin.SpectrumSaada;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaRelation;
import saadadb.meta.UTypeHandler;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.request.formator.QueryResultFormator.infoEntry;
import saadadb.vocabulary.KeyMapper;
import cds.savot.model.CoosysSet;
import cds.savot.model.FieldRefSet;
import cds.savot.model.FieldSet;
import cds.savot.model.GroupSet;
import cds.savot.model.InfoSet;
import cds.savot.model.LinkSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotFieldRef;
import cds.savot.model.SavotGroup;
import cds.savot.model.SavotInfo;
import cds.savot.model.SavotLink;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.writer.SavotWriter;

/**
 * Ancestor for any parser which transforms a list of SaadaQL OIDs into a File
 * or a String. We use the SAVOT 'cds.savot.writer.jar' and
 * 'cds.savot.model.jar' packages, developped by Andr� Schaaff for the Centre de
 * Donnees astronomiques de Strasbourg URL :
 * http://cdsweb.u-strasbg.fr/devcorner.gml
 */
/**
 * @author laurentmichel
 * @version $Id: VotableFormator.java 1341 2014-08-28 13:38:58Z laurent.mistahl
 *          $
 */
public abstract class VotableFormator extends QueryResultFormator {

	protected SavotWriter writer;
	protected SavotTable table;

	protected FieldSet dataModelFieldSet;
	protected FieldSet fieldSet_housekeeping;

	protected TDSet tdSet;

	public VotableFormator() {
		this.defaultSuffix = QueryResultFormator.getFormatExtension("votable");
		this.infoMap.put("QUERY_STATUS", new infoEntry("OK", null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see saadadb.vo.request.formator.VOResultFormator#buildDataResponse()
	 */
	public void buildDataResponse() throws Exception {
		this.initWriter();
		this.writeBeginingVOTable();

		InfoSet infoSet = new InfoSet();
		for (Entry<String, infoEntry> e : this.infoMap.entrySet()) {
			SavotInfo info = new SavotInfo();
			info.setName(e.getKey());
			infoEntry val = e.getValue();
			// Set value & description
			if (val.getValue() != null)
				info.setValue(val.getValue());

			if (val.getDescription() != null)
				info.setContent(val.getDescription());

			infoSet.addItem(info);
		}
		this.writer.writeInfo(infoSet);

		this.table = new SavotTable();
		this.table.setName("Results");

		this.writer.writeTableBegin(table);
		this.writeMetaData();
		this.writeData();
		this.writer.writeTableEnd();
		this.writer.writeResourceEnd();
		/*
		 * Add tables with linked data
		 */
		this.writeExtensions();
		this.writer.writeDocumentEnd();
		Messenger.printMsg(Messenger.TRACE, "Data written in  "
				+ this.responseFilePath);
	}

	/**
	 * Build meta data VOTable
	 * 
	 * @throws Exception
	 */
	public void buildMetaResponse() throws Exception {
		initWriter();
		writeBeginingVOTable();

		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("OK");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);

		info = new SavotInfo();
		// info.setValue(this.protocolName);
		info.setValue(this.protocolV);
		info.setName("SERVICE_PROTOCOL");
		info.setValue(this.protocolV);
		// info.setContent(this.protocolName);
		info.setContent(this.protocolN);
		infoSet.addItem(info);
		writer.writeInfo(infoSet);

		writeProtocolParamDescription();

		table = new SavotTable();
		table.setId("OutputFields");
		table.setDescription("data_desc");

		writer.writeTableBegin(table);
		writeMetaData();
		writer.writeTableEnd();
		writer.writeResourceEnd();
		writer.writeDocumentEnd();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * saadadb.vo.request.formator.Formator#buildErrorResponse(java.lang.Exception
	 * )
	 */
	public void buildErrorResponse(Exception e) throws Exception {
		initWriter();
		writeBeginingVOTable();

		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("ERROR");
		info.setName("QUERY_STATUS");
		info.setContent("<![CDATA[" + e + "]]>");
		infoSet.addItem(info);
		writer.writeInfo(infoSet);

		ParamSet paramSet = new ParamSet();
		SavotParam sp = new SavotParam();
		sp.setId("Error");
		sp.setName("Error");
		sp.setValue(protectSpecialCharacters(e.toString()));
		paramSet.addItem(sp);
		writer.writeParam(paramSet);
		writer.writeResourceEnd();
		writer.writeDocumentEnd();
	}

	/**
	 * @param file
	 * @throws Exception
	 */
	protected void initWriter() throws Exception {
		writer = new SavotWriter();
		writer.enableAttributeEntities(false);
		writer.enableElementEntities(false);
		writer.initStream(this.responseFilePath);
	}

	/**
	 * Looks if SavotField sf's name exists in the extended attributes of the
	 * right category. If yes, it will get and return its value after checking
	 * the units consistency
	 * 
	 * @param obj
	 * @param sf
	 * @param category
	 *            The category to look in
	 * @return
	 * @throws Exception
	 */
	protected String lookForAMatch(SaadaInstance obj, SavotField sf)
			throws Exception {

		String retour = "";
		if (Messenger.debug_mode) {
			Messenger.printMsg(Messenger.DEBUG,
					"Trying to find " + sf.getName()
							+ "\tIn the extended attributes");
		}
		// Get an AttributeHandler which name match sf.getName() or null if
		// there is none
		AttributeHandler hdlr = this.getAttr_extended(sf.getName());

		if (hdlr != null) {
			// If there is a match, try to get its value
			Object value = obj.getFieldValue(hdlr.getNameattr());
			if (value != null) {
				// if(Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, sf.getName() + " Found");
				// Check if the attributeHandler has a unit
				if (!hdlr.getUnit().isEmpty()) {
					// if yes, compare it to the Savotfield
					if (!hdlr.getUnit().equals(sf.getUnit())) {
						// If the units don't match, print a debug message
						if (Messenger.debug_mode) {
							Messenger.printMsg(Messenger.DEBUG,
									"Unit '" + hdlr.getUnit()
											+ "' of extended attribute '"
											+ hdlr.getNameattr()
											+ "' doesn't match with unit '"
											+ sf.getUnit() + "'");
							// TODO ADD a unit conversion system
						}
					}

				}
				retour = value.toString();
			}
		}

		return retour;
	}

	/*
	 * To be overridden by child
	 */
	/**
	 * (To be overridden by child) Returns an AttributeHandler if a match is
	 * found in its extended attributes or null if none
	 * 
	 * @param name
	 *            Name of the attribute to look for
	 * @return
	 * @throws Exception
	 */
	protected AttributeHandler getAttr_extended(String name) throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * saadadb.vo.request.formator.VOResultFormator#writeDMFieldsAndGroups()
	 */
	protected void writeDMFieldsAndGroups() throws Exception {

		GroupSet groupSet = new GroupSet();
		dataModelFieldSet = new FieldSet();
		/*
		 * Create one field for each DM attribute
		 */
		String[] groups = dataModel.groupNames();
		int cpt = 100;
		for (String group_name : groups) {
			UTypeHandler[] uths = dataModel.getGroupUtypeHandlers(group_name);
			for (UTypeHandler uth : uths) {
				if (uth.getValue().length() == 0
						|| "null".equalsIgnoreCase(uth.getValue())) {
					dataModelFieldSet.addItem(uth.getSavotField(cpt++));
				}
			}
		}
		writer.writeField(dataModelFieldSet);
		/*
		 * Arrange fields and DM params in groups
		 */
		for (String group_name : groups) {
			SavotField sf;
			SavotGroup group = new SavotGroup();
			ParamSet paramSet = new ParamSet();
			FieldRefSet fieldRefSet = new FieldRefSet();
			group.setName(group_name);
			paramSet = new ParamSet();
			UTypeHandler[] uths = dataModel.getGroupUtypeHandlers(group_name);
			for (UTypeHandler uth : uths) {
				if (uth.getValue().length() == 0
						|| "null".equalsIgnoreCase(uth.getValue())) {
					for (Object f : dataModelFieldSet.getItems()) {
						sf = (SavotField) f;
						if (("".equals(uth.getUtype()) || sf.getUtype().equals(
								uth.getUtype()))
								&& ("".equals(uth.getUcd()) || sf.getUcd()
										.equals(uth.getUcd()))
								&& ("".equals(uth.getNickname()) || sf
										.getName().equals(uth.getNickname()))) {
							SavotFieldRef fieldRef = new SavotFieldRef();
							fieldRef.setRef(sf.getRef());
							fieldRefSet.addItem(fieldRef);
						}
					}
				} else if (uth.getUtype().equals("Dataset.Length")) {
					if (oids != null) {
						paramSet.addItem(uth.getSavotParam(
								Integer.toString(oids.size()), ""));
					}
				} else if (uth.getUtype().equals("Dataset.Size") /*
																 * not a param:
																 * defined par
																 * file ||
																 * uth.getUtype
																 * ().equals(
																 * "Access.Size"
																 * )
																 */) {
					paramSet.addItem(uth.getSavotParam(
							Integer.toString(oids.size()), ""));
				} else if (uth.getUtype().equals("CoordSys.SpaceFrame.Name")) {
					paramSet.addItem(uth.getSavotParam(Database.getCoord_sys(),
							""));
				} else if (uth.getUtype().equals("CoordSys.SpaceFrame.Equinox")) {
					paramSet.addItem(uth.getSavotParam(
							Database.getCoord_equi(), ""));
				} else {
					paramSet.addItem(uth.getSavotParam(uth.getValue(), ""));
				}
			}
			if (paramSet.getItemCount() > 0) {
				group.setParams(paramSet);
			}
			if (fieldRefSet.getItemCount() > 0) {
				group.setFieldsRef(fieldRefSet);
			}
			groupSet.addItem(group);
		}
		writer.writeGroup(groupSet);
	}

	/**
	 * Write the XML header of the VOTable
	 * 
	 * @param description
	 * @param resType
	 * @throws Exception
	 */
	protected void writeBeginingVOTable() throws Exception {
		SavotVOTable votable = new SavotVOTable();

		votable.setXmlns("http://www.ivoa.net/xml/VOTable/v1.1");
		votable.setXmlnsxsi("http://www.w3.org/2001/XMLSchema-instance");
		votable.setVersion("1.1");
		writer.writeDocumentHead(votable);

		String description = "<![CDATA[\nSaadaDB:\n" + "   name : "
				+ Database.getDbname() + "\n" + "   url  : "
				+ Database.getUrl_root() + "\n" + "   date : " + (new Date())
				+ "\n";

		description += "Query parameters:\n";
		for (Entry<String, String> s : this.protocolParams.entrySet()) {
			description += "   " + s.getKey() + ": " + s.getValue() + "\n";
		}
		description += "Archive generated by SAADA: http://saada.u-strasbg.fr\n]]>";
		writer.writeDescription(description);

		CoosysSet coosysSet = new CoosysSet();
		SavotCoosys savotCoosys = new SavotCoosys();
		savotCoosys.setId(Database.getAstroframe().toString());
		if (Database.getCoord_equi() != 0)
			savotCoosys.setEquinox(Double.toString(Database.getCoord_equi()));
		savotCoosys.setSystem(Database.getCoord_sys());
		coosysSet.addItem(savotCoosys);
		writer.writeCoosys(coosysSet);

		SavotResource resource = new SavotResource();
		if (this.hasExtensions == true)
			resource.setUtype("this.vores_type");
		writer.writeResourceBegin(resource);
		// writer.writeDescription(this.protocolN);

	}

	/**
	 * Fill the data table
	 * 
	 * @throws Exception
	 */
	protected void writeData() throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		if (oids != null) {
			int i = 0;
			for (long oid : oids) {
				if (i >= this.limit) {
					break;
				}
				i++;
				SaadaInstance si = Database.getCache().getObject(oid);
				SavotTR savotTR = new SavotTR();
				tdSet = new TDSet();
				this.writeRowData(si);
				this.writeHouskeepingData(si);
				// this.writeMappedUtypeData(oid);
				// this.writeAttExtendData(oid);
				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				if (this.limit > 0 && i >= this.limit) {
					Messenger.printMsg(Messenger.TRACE, "result truncated to "
							+ i);
					break;
				}
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}

	/**
	 * Add to current row data taken on the object si
	 * 
	 * @param obj
	 * @throws Exception
	 */
	// abstract protected void writeRowData(SaadaInstance obj) throws Exception;
	protected void writeRowData(SaadaInstance obj) throws Exception {

		KeyMapper mapper = new KeyMapper(obj);
		AttributeHandler tmp = new AttributeHandler();
		for (Object f : dataModelFieldSet.getItems()) {
			SavotField sf = (SavotField) f;
			String ucd = sf.getUcd();
			String utype = sf.getUtype();
			String name = sf.getName();
			//String id = sf.getId();
			String val = "";
			boolean cdata = false;
			System.out.println("Utype " + utype);
			tmp = mapper.search(ucd, utype, name);

			if (tmp != null) {
				val = tmp.getValue();
				System.out.println("Value: "+val);
				//Use sf get get the type of the value as the KeyMapper and ColumnExpressionSetter classes don't provide it
				//TODO provide Type Information
				if (sf.getType().equalsIgnoreCase("string")
						|| sf.getType().equalsIgnoreCase("char")) {
					cdata =true;	
				}
			}
			else {
				val = lookForAMatch(obj, sf);
			}
			
			if (cdata) {
				addCDataTD(val);
			} else {
				addTD(val);
			}
		}
	}

	/**
	 * *************************************************************************
	 * ****************************** Method managing housekeeping data
	 */

	/**
	 * Set housekeeping fields
	 * 
	 * @throws Exception
	 */
	protected void writeHousekeepingFieldAndGroup() throws Exception {
		fieldSet_housekeeping = new FieldSet();
		/*
		 * Param used by Aladin to order the response tree
		 * 
		 * <PARAM name="SORTORDER" datatype="char" arraysize="*"
		 * value="TonNomDeChampPourLesSurvey SpectralAxis.coverage.Location" />
		 */
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setName("SORTORDER");
		param.setDataType("char");
		param.setArraySize("*");
		/*
		 * Param specifying order criteria for Aladin take the utype
		 * SpectralAxis.coverage.location.coord.ScalarCoordinate.Value as second
		 * criterium.
		 */
		param.setValue("SaadaOID SpatialAxis_covloc SpectralAxiscovloccoorScalVal");
		paramSet.addItem(param);
		writer.writeParam(paramSet);
		writeSaadaLinksMetaReferences("SaadaOID", fieldSet_housekeeping);
		writer.writeField(fieldSet_housekeeping);
	}

	/**
	 * @param oid
	 * @throws SaadaException
	 */
	protected void writeHouskeepingData(SaadaInstance obj)
			throws SaadaException {
		addTD(Long.toString(obj.oidsaada));
		addCDataTD(obj.obs_id.replaceAll("#", ""));
	}

	/**
	 * Create fields used by Aladin to put an URL within the data pane.
	 * 
	 * @param writer
	 * @param category
	 */
	protected void writeSaadaLinksMetaReferences(String key_field,
			FieldSet fieldset) {
		SavotField field = new SavotField();
		// field.setId("LinktoPixels");
		// field.setDataType("char");
		// field.setArraySize("*");
		// field.setUcd("meta.ref.url");
		// fieldset.addItem(field);

		field = new SavotField();
		field.setId(key_field);
		field.setName(key_field);
		field.setRef(key_field);
		field.setType("hidden");
		field.setDataType("char");
		field.setArraySize("*");
		field.setUcd("meta.id");
		fieldset.addItem(field);

		SavotLink link = new SavotLink();
		LinkSet links = new LinkSet();
		link.setHref(Database.getUrl_root() + "/getinstance?oid=${" + key_field
				+ "}");
		links.addItem(link);
		field = new SavotField();
		field.setName("Saada Anchor");
		field.setUcd("meta.ref.url");
		field.setDataType("char");
		field.setArraySize("*");
		field.setLinks(links);
		fieldset.addItem(field);
	}

	/**
	 * *************************************************************************
	 * ****************************** Method managing Extension in VOTables
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * saadadb.vo.request.formator.VOResultFormator#writeExtMetaReferences()
	 */
	protected void writeExtMetaReferences() throws QueryException {
		if (this.hasExtensions == true) {
			try {
				for (String relation : this.relationsToInclude) {
					String ref_id = relation.toString() + "_OID";
					/*
					 * Field: Unique ID identifying the association instance
					 */
					FieldSet fieldSet = new FieldSet();
					SavotField field = new SavotField();
					field.setRef(relation.toString());
					field.setId(ref_id);
					/*
					 * Association.ID is the utype recommened by SSA as
					 * association key Bu use Observation/Identifier while
					 * Aladin requires it field.setUtype("Association.ID");
					 */
					field.setUtype("Observation/Identifier");
					field.setType("hidden");
					field.setRef(relation);
					fieldSet.addItem(field);
					writer.writeField(fieldSet);
					GroupSet gs = new GroupSet();
					SavotGroup sg = new SavotGroup();
					sg.setDescription("Association through the Saada relationship: "
							+ relation);
					/*
					 * ref to the fieldField: Unique ID identifying the
					 * association instance
					 */
					FieldRefSet frs = new FieldRefSet();
					SavotFieldRef fieldRef = new SavotFieldRef();
					fieldRef.setRef(ref_id);
					frs.addItem(fieldRef);
					sg.setFieldsRef(frs);
					/*
					 * Param : Type of association
					 */
					ParamSet ps = new ParamSet();
					SavotParam sp = new SavotParam();
					sp.setUtype("Association.Type");
					sp.setValue(Category.explain(Database.getCachemeta()
							.getRelation(relation).getSecondary_category()));
					ps.addItem(sp);
					sg.setParams(ps);
					/*
					 * Param Unique key different for each element of
					 * association
					 */
					sp = new SavotParam();
					sp.setUtype("Association.Key");
					sp.setValue(ref_id);
					ps.addItem(sp);
					sg.setParams(ps);
					gs.addItem(sg);

					writer.writeGroup(gs);
				}
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(
						SaadaException.METADATA_ERROR,
						"Getting relations for "
								+ this.getProtocolParam("collection") + "."
								+ this.getProtocolParam("collection") + "  "
								+ e.getMessage());
			}
		}
	}

	/**
	 * Append to the main tbale the field value used as associations
	 * 
	 * @param oid
	 * @param category
	 */
	protected void writeExtReferences(SaadaInstance si) {
		if (this.hasExtensions == true) {
			for (int i = 0; i < this.relationsToInclude.size(); i++) {
				addTD(Long.toString(si.oidsaada));
			}
		}
	}

	/**
	 * Appends to the VOTable all extensions matching Saada relationships
	 * 
	 * @param oids
	 * @param category
	 * @throws SaadaException
	 */
	protected void writeExtensions() throws SaadaException {
		if (this.hasExtensions == true) {
			try {
				SavotResource res = new SavotResource();
				res.setName("Extensions");
				res.setType("");
				writer.writeResourceBegin(res);
				for (String relation : this.relationsToInclude) {
					SavotTable ext_table = new SavotTable();
					ext_table.setName(relation);
					ext_table.setId(relation);
					ext_table.setDescription("Saada counterparts of "
							+ this.protocolParams.get("collection") + "."
							+ this.protocolParams.get("category")
							+ " in relation " + relation);
					writer.writeTableBegin(ext_table);
					this.writeExtMetaData(relation);
					this.writeExtData(relation);
					writer.writeTableEnd();
				}
				writer.writeResourceEnd();
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(
						SaadaException.METADATA_ERROR,
						"Getting relation list for "
								+ this.getProtocolParam("collection") + "."
								+ this.getProtocolParam("collection") + "  "
								+ e.getMessage());
			}
		}
	}

	/**
	 * @param rs
	 * @param category
	 * @throws SaadaException
	 */
	protected void writeExtensions(SaadaQLResultSet rs, int category)
			throws SaadaException {
		if (this.hasExtensions == true) {
			try {
				SavotResource res = new SavotResource();
				res.setName("Extensions");
				res.setType("");
				writer.writeResourceBegin(res);
				for (String relation : this.relationsToInclude) {
					SavotTable ext_table = new SavotTable();
					ext_table.setName(relation);
					ext_table.setId(relation);
					ext_table.setDescription("Saada counterparts of "
							+ this.getProtocolParam("collection") + "."
							+ this.getProtocolParam("collection")
							+ " in relation " + relation);
					writer.writeTableBegin(ext_table);
					this.writeExtMetaData(relation);
					this.writeExtData(relation, rs);
					writer.writeTableEnd();
				}
				writer.writeResourceEnd();
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(
						SaadaException.METADATA_ERROR,
						"Getting relation list for "
								+ this.getProtocolParam("collection") + "."
								+ this.getProtocolParam("collection") + "  "
								+ e.getMessage());
			}
		}
	}

	/**
	 * Write field for the extension table matching the Saada relation
	 * 
	 * @param relation
	 * @throws SaadaException
	 * @throws IOException
	 */
	protected void writeExtMetaData(String relation) throws SaadaException,
			IOException {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		if (mr == null) {
			Messenger.printMsg(Messenger.ERROR,
					"Can't found meta data for relation " + relation);
		} else {
			FieldSet fieldSet = new FieldSet();
			SavotField field;

			field = new SavotField();
			field.setId("Access_For");
			field.setUtype("Access.Format");
			field.setDataType("char");
			field.setArraySize("*");
			field.setType("hidden");
			fieldSet.addItem(field);

			field = new SavotField();
			field.setId("Access_Ref");
			field.setUtype("Access.Reference");
			field.setDataType("char");
			field.setArraySize("*");
			field.setType("hidden");
			fieldSet.addItem(field);

			field = new SavotField();
			field.setId("namesaada");
			field.setDataType("char");
			field.setArraySize("*");
			field.setUcd("meta.title");
			fieldSet.addItem(field);

			/*
			 * Entry are not listed individulally in the table but there is just
			 * one CS reference for all sources
			 */
			if (mr.getSecondary_category() != Category.ENTRY) {
				writeSaadaLinksMetaReferences(relation + "_OID", fieldSet);
				if (mr.getSecondary_category() == Category.SPECTRUM
						|| mr.getSecondary_category() == Category.IMAGE) {

					field = new SavotField();
					field.setName("Char_SpaCovLocVal");
					field.setUtype("Char.SpatialAxis.Coverage.Location.Value");
					field.setDataType("char");
					field.setArraySize("*");
					fieldSet.addItem(field);
				}
				String[] qn = mr.getQualifier_names().toArray(new String[0]);
				for (int i = 0; i < qn.length; i++) {
					field = new SavotField();
					field.setId(qn[i]);
					field.setName("relation_qua_table_" + qn[i]);
					field.setDataType("double");
					fieldSet.addItem(field);
				}
			} else {
				field = new SavotField();
				field.setId(relation + "_OID");
				field.setRef(relation + "_OID");
				field.setType("hidden");
				field.setDataType("char");
				field.setArraySize("*");
				fieldSet.addItem(field);
			}
			writer.writeField(fieldSet);
			if (mr.getSecondary_category() != Category.ENTRY) {
				writer.writeGroup(this.writeSaadaReferencesGroup(mr));
			}
		}
	}

	/**
	 * Returns a SavotGroup with all Savot params referecing the relation
	 * 
	 * @param mr
	 * @return
	 * @throws SaadaException
	 */
	private GroupSet writeSaadaReferencesGroup(MetaRelation relation)
			throws SaadaException {
		GroupSet gs = new GroupSet();
		ParamSet ps = new ParamSet();
		SavotGroup sg = new SavotGroup();
		gs.addItem(sg);
		sg.setName("Saada References");
		sg.setParams(ps);
		SavotParam sp = new SavotParam();
		sp.setName("Saada Collection");
		sp.setValue(relation.getSecondary_coll());
		sp.setDescription("Saada collection where data can be retreived");
		ps.addItem(sp);

		sp = new SavotParam();
		sp.setName("Saada Category");
		sp.setValue(Category.explain(relation.getSecondary_category()));
		sp.setDescription("Saada product category where data can be retreived");
		ps.addItem(sp);

		sp = new SavotParam();
		sp.setName("Saada Relationship");
		sp.setValue(relation.getName());
		sp.setDescription("Saada relationship pointing to these data");
		ps.addItem(sp);

		return gs;
	}

	/**
	 * Write table data for the extension matching the relation
	 * 
	 * @param relation
	 * @param oids
	 * @throws Exception
	 */
	protected void writeExtData(String relation) throws Exception {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		String[] qn = mr.getQualifier_names().toArray(new String[0]);
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		for (long oid : oids) {
			SaadaInstance obj = (SaadaInstance) Database.getCache().getObject(
					oid);
			SaadaLink[] links = obj.getStartingLinks(relation);
			if (links.length == 0) {
				continue;
			}
			int sec_cat = mr.getSecondary_category();

			for (int k = 0; k < links.length; k++) {
				SaadaInstance counterpart = Database.getCache().getObject(
						links[k].getEndindOID());
				SavotTR savotTR = new SavotTR();
				tdSet = new TDSet();
				// Access.format
				if (sec_cat == Category.ENTRY) {
					addCDataTD("text/xml");
				} else {
					addCDataTD(counterpart.getMimeType());
				}
				// Access.reference
				if (sec_cat == Category.ENTRY) {
					addCDataTD(Database.getUrl_root() + "/conesearch?relation="
							+ relation + "&primoid=" + oid);
				} else {
					addCDataTD(counterpart.getDownloadURL(true));
				}
				/*
				 * Entry are not listed individulally in the table but there is
				 * just one CS reference for all sources
				 */
				if (sec_cat == Category.ENTRY) {
					// Saada name (Savot doesn't support # in dataset, even in a
					// CDATA)
					addCDataTD("Source Counterparts");
					savotTR.setTDs(tdSet);
					addTD(Long.toString(oid));
					writer.writeTR(savotTR);
					break;
				}
				// Saada name (Savot doesn't support # in dataset, even in a
				// CDATA)
				addCDataTD(counterpart.obs_id.replace("#", "_").trim());
				// Links to pixels
				addCDataTD(obj.getURL(true));
				// OID used as key for the Aladin link
				addTD(Long.toString(oid));
				// Links for Aladin
				addTD("SaadaDB Anchor");
				// qualifiers
				if (mr.getSecondary_category() == Category.ENTRY
						|| mr.getSecondary_category() == Category.SPECTRUM
						|| mr.getSecondary_category() == Category.IMAGE) {
					addTD(obj.s_ra + " " + obj.s_dec);
				}
				for (int j = 0; j < qn.length; j++) {
					addTD(Double.toString(links[k].getQualifierValue(qn[j])));
				}
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();

	}

	/**
	 * @param relation
	 * @param rs
	 * @throws Exception
	 */
	protected void writeExtData(String relation, SaadaQLResultSet rs)
			throws Exception {
		MetaRelation mr = Database.getCachemeta().getRelation(relation);
		String[] qn = mr.getQualifier_names().toArray(new String[0]);

		writer.writeDataBegin();
		writer.writeTableDataBegin();
		while (rs.next()) {
			long oid = rs.getOid();
			SaadaInstance obj = (SaadaInstance) Database.getCache().getObject(
					oid);
			SaadaLink[] links = obj.getStartingLinks(relation);
			if (links.length == 0) {
				continue;
			}
			int sec_cat = mr.getSecondary_category();

			for (int k = 0; k < links.length; k++) {
				SaadaInstance counterpart = Database.getCache().getObject(
						links[k].getEndindOID());
				SavotTR savotTR = new SavotTR();
				tdSet = new TDSet();
				// Access.format
				if (sec_cat == Category.ENTRY) {
					addCDataTD("text/xml");
				} else {
					addCDataTD(counterpart.getMimeType());
				}
				// Access.reference
				if (sec_cat == Category.ENTRY) {
					addCDataTD(Database.getUrl_root() + "/conesearch?relation="
							+ relation + "&primoid=" + oid);
				} else {
					addCDataTD(counterpart.getDownloadURL(true));
				}
				/*
				 * Entry are not listed individulally in the table but there is
				 * just one CS reference for all sources
				 */
				if (sec_cat == Category.ENTRY) {
					// Saada name (Savot doesn't support # in dataset, even in a
					// CDATA)
					addCDataTD("Source Counterparts");
					savotTR.setTDs(tdSet);
					addTD(Long.toString(oid));
					writer.writeTR(savotTR);
					break;
				}
				// Saada name (Savot doesn't support # in dataset, even in a
				// CDATA)
				addCDataTD(counterpart.obs_id.replace("#", "_").trim());
				// Links to pixels
				addCDataTD(obj.getURL(true));
				// OID used as key for the Aladin link
				addTD(Long.toString(oid));
				// Links for Aladin
				addTD("SaadaDB Anchor");
				// qualifiers
				if (mr.getSecondary_category() == Category.ENTRY
						|| mr.getSecondary_category() == Category.SPECTRUM
						|| mr.getSecondary_category() == Category.IMAGE) {
					addTD(obj.s_ra + " " + obj.s_dec);
				}
				for (int j = 0; j < qn.length; j++) {
					addTD(Double.toString(links[k].getQualifierValue(qn[j])));
				}
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
			}
			writer.writeTableDataEnd();
			writer.writeDataEnd();
		}

	}

	/**
	 * *************************************************************************
	 * ****************************** Some VOTable utilities
	 */

	/**
	 * @param content
	 */
	protected void addTD(String content) {
		SavotTD td = new SavotTD();
		if (content != null)
			td.setContent(content);
		else {
			td.setContent("");
		}
		tdSet.addItem(td);
	}

	/**
	 * @param content
	 */
	protected void addCDataTD(String content) {
		SavotTD td = new SavotTD();
		/*
		 * content prepended with a ' ' to help Aladin parser to get not lost
		 */
		td.setContent("<![CDATA[" + content + "]]>");
		tdSet.addItem(td);
	}

	/**
	 * @param val
	 */
	protected void addValTD(Object val) {
		if (val != null) {
			String sval = val.toString();
			if (!(sval.equals("Infinity") || sval.equals("NaN") || sval
					.equals("2147483647"))) {
				addCDataTD(sval.replaceAll("#", ""));
			} else {
				addTD("");
			}
		} else {
			addTD("");
		}
	}

	/**
	 * courtesy of
	 * http://stackoverflow.com/questions/439298/best-way-to-encode-text
	 * -data-for-xml-in-java
	 * 
	 * @param msg
	 * @return
	 */
	public static String protectSpecialCharacters(String msg) {
		if (msg == null) {
			return null;
		}
		boolean anyCharactersProtected = false;

		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < msg.length(); i++) {
			char ch = msg.charAt(i);

			boolean controlCharacter = ch < 32;
			boolean unicodeButNotAscii = ch > 126;
			boolean characterWithSpecialMeaningInXML = ch == '"' || ch == '<'
					|| ch == '&' || ch == '>';

			if (characterWithSpecialMeaningInXML || unicodeButNotAscii
					|| controlCharacter) {
				stringBuffer.append("&#" + (int) ch + ";");
				anyCharactersProtected = true;
			} else {
				stringBuffer.append(ch);
			}
		}
		if (anyCharactersProtected == false) {
			return msg;
		}
		return stringBuffer.toString();
	}

}