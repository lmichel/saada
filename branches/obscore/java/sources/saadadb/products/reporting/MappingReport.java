package saadadb.products.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.ProductBuilder;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * Class taking in charge the reporting about the data mapping
 * Purpose: splitting the builder code 
 * @author michel
 * @version $Id$
 */
public class MappingReport {
	public ProductBuilder builder;

	public MappingReport(ProductBuilder builder){
		this.builder = builder;
	}

	/**
	 * Print out the report
	 * @throws Exception
	 */
	public void printReport() throws Exception {
		for( java.util.Map.Entry<String, ColumnSetter> e: this.getReport().entrySet()){
			System.out.print(e.getKey() + "=");
			ColumnSetter ah = e.getValue();
			System.out.print(ah.getValue());
			System.out.println(" <" + ah.getComment() + ">");
		}
	}


	/**
	 * Build a map with all collection level value of the current instance.
	 * Values are stored in AttributeHandler having the mapping mode into the comment field
	 * @throws Exception
	 */
	/**
	 * @return
	 * @throws Exception
	 */
	/**
	 * @return
	 * @throws Exception
	 */
	public Map<String, ColumnSetter> getReport() throws Exception {
		this.builder.updateAttributeHandlerValues();
		this.builder.productIngestor.bindInstanceToFile();
		SaadaInstance si = this.builder.productIngestor.saadaInstance;

		Map<String, ColumnSetter> retour = new LinkedHashMap<String, ColumnSetter>();
		retour.put("obs_id", this.builder.obs_idSetter);
		this.builder.obs_idSetter.storedValue = si.obs_id;
		retour.put("obs_publisher_did", this.builder.obs_publisher_didSetter);
		this.builder.obs_publisher_didSetter.storedValue = si.getObs_publisher_did();
		retour.put("calib_level", this.builder.calib_levelSetter);
		this.builder.calib_levelSetter.storedValue = si.getCalib_level();
		retour.put("obs_collection", this.builder.obs_collectionSetter);
		this.builder.obs_collectionSetter.storedValue = si.getObs_collection();
		retour.put("target_name", this.builder.target_nameSetter);
		this.builder.target_nameSetter.storedValue = si.target_name;
		retour.put("facility_name", this.builder.facility_nameSetter);
		this.builder.facility_nameSetter.storedValue = si.facility_name;
		retour.put("instrument_name", this.builder.instrument_nameSetter);
		this.builder.instrument_nameSetter.storedValue = si.instrument_name;

		retour.put("s_ra", this.builder.s_raSetter);
		this.builder.s_raSetter.storedValue = si.s_ra;
		retour.put("s_dec", this.builder.s_decSetter);
		this.builder.s_decSetter.storedValue = si.s_dec;
		retour.put("s_resolution",this.builder.s_resolutionSetter);
		this.builder.s_resolutionSetter.storedValue = si.s_resolution;
		retour.put("s_fov", this.builder.s_fovSetter);
		this.builder.s_fovSetter.storedValue = si.getS_fov();
		retour.put("s_region", this.builder.s_regionSetter);
		this.builder.s_regionSetter.storedValue = si.getS_region();
		retour.put("system", this.builder.astroframeSetter);
		this.builder.astroframeSetter.calculateExpression();

		retour.put("em_min", this.builder.em_minSetter);
		this.builder.em_minSetter.storedValue = si.em_min;
		retour.put("em_max", this.builder.em_maxSetter);
		this.builder.em_maxSetter.storedValue = si.em_max;
		retour.put("em_res_power", this.builder.em_res_powerSetter);
		this.builder.em_res_powerSetter.storedValue = si.em_res_power;
		retour.put("em_unit", this.builder.em_unitSetter);
		this.builder.em_unitSetter.storedValue = this.builder.em_unitSetter.getValue();

		retour.put("t_min", this.builder.t_minSetter);
		this.builder.t_minSetter.storedValue = si.t_min;
		retour.put("t_max", this.builder.t_maxSetter);
		this.builder.t_maxSetter.storedValue = si.t_max;
		retour.put("t_exptime", this.builder.t_exptimeSetter);
		this.builder.t_exptimeSetter.storedValue = si.t_exptime;
		retour.put("t_resolution", this.builder.t_resolutionSetter);
		this.builder.t_resolutionSetter.storedValue = si.t_resolution;

		retour.put("o_ucd", this.builder.o_ucdSetter);
		this.builder.o_ucdSetter.storedValue = si.getO_ucd();
		retour.put("o_unit", this.builder.o_unitSetter);
		this.builder.o_unitSetter.storedValue = si.getO_unit();
		retour.put("o_calib_status", this.builder.o_calib_statusSetter);
		this.builder.o_calib_statusSetter.storedValue = si.getO_calib_status();

		retour.put("pol_states", this.builder.pol_statesSetter);
		this.builder.pol_statesSetter.storedValue = si.pol_states;
		/*
		 * extended attributes
		 */
		Map<String , AttributeHandler> eatt = Database.getCachemeta().getAtt_extend(this.builder.getCategory());
		for( String eattname: eatt.keySet()) {
			ColumnExpressionSetter ces = this.builder.extended_attributesSetter.get(eattname);
			retour.put(eattname, ces);
			ces.storedValue = si.getFieldValue(eattname);

		}

		for( Field f: si.getCollLevelPersisentFields() ){
			String fname = f.getName();
			if( retour.get(fname) == null ){
				AttributeHandler ah = new AttributeHandler();
				ah.setNameattr(fname); ah.setNameorg(fname); 
				Object o = si.getFieldValue(fname);
				ah.setValue((o == null)? SaadaConstant.STRING:o.toString());
				ah.setComment("Computed internally by Saada");		
				//ColumnExpressionSetter cs = new ColumnExpressionSetter(ah, ColumnSetMode.BY_SAADA);
				ColumnExpressionSetter cs = new ColumnExpressionSetter(fname, ah,ColumnSetMode.BY_SAADA, true);

				cs.storedValue = ah.getValue();
				retour.put(fname, cs);
			}
		}
		

		return retour;
	}

	/**
	 * Write in directory a report of the mapped product 
	 * The report name is the same as this of the data file, suffixed with ".report"
	 * The report file contain a list of the read keywords followed by the mapping into the collection model
	 * @param directory
	 */
	public void writeCompleteReport(String directory, ArgsParser ap) throws Exception{
		String reportName = directory +  File.separator + this.builder.dataFile.getName() + ".txt";
		Messenger.printMsg(Messenger.TRACE, "Write report in " + reportName);

		FileWriter fw = new FileWriter(reportName);
		fw.write("====================================================\n");
		fw.write("    " + this.builder.dataFile.getName() + "\n");
		fw.write("    " + (new Date()) + "\n");
		fw.write("====================================================\n");
		if( ap != null ){
			fw.write("\n========= User parameters\n");
			fw.write(ap.toString().replace("-", "\n    -") +"\n");
		}
		fw.write("\n========= Keywords read\n");
		int cpt = 1;
		for( AttributeHandler ah: this.builder.getProductAttributeHandler().values()) {
			fw.write("HEADER " + cpt + " " + ah + "\n");
			cpt++;
		}
		this.writeColumnReport(fw);
		fw.write("\n========= Loaded extensions\n");
		for( ExtensionSetter es: this.getReportOnLoadedExtension()) {
			fw.write(es + "\n");
		}

		Map<String, ColumnSetter> r = this.getReport();

		fw.write("\n========= Signature\n");
		fw.write("MD5 hash code " + this.builder.productIngestor.saadaInstance.contentsignature+" \n");

		fw.write("\n========= Mapping report\n");
		for( Entry<String, ColumnSetter> e:r.entrySet()){
			fw.write(String.format("%20s",e.getKey()) + "     ");;
			ColumnSetter ah = e.getValue();
			fw.write(ah.getFullMappingReport() +  " ");
			if( !ah.isNotSet() ) {
				fw.write("storedValue=" + ah.storedValue+" \n");
			} else {
				fw.write("\n");
			}
		}
		fw.close();
	}


	/**
	 * @param fw
	 * @throws IOException 
	 */
	protected void writeColumnReport(FileWriter fw) throws IOException {}
	
	

	/**
	 * @return
	 */
	public List<ExtensionSetter> getReportOnLoadedExtension() {
		return this.builder.dataFile.reportOnLoadedExtension();
	}
	/**
	 * Build a map with all collection level value of the current instance.
	 * Values are stored in AttributeHandler having the mapping mode into the comment field
	 * @throws Exception
	 */
	public Map<String, ColumnSetter> getEntryReport() throws Exception {
		return null;
	}



}
