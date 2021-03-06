package preproc.xid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.HashSet;

import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.prdconfiguration.ConfigurationImage;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.sqltable.SQLQuery;
import saadadb.util.ImageUtils;
import saadadb.util.Messenger;
import saadadb.vo.formator.ConeSearchToVOTableFormator;
import ajaxservlet.formator.DefaultFormats;

public class MakeAJS {
	static final String script_dir = "/data/XID_WFIfc/";
	static final String fc_dir = "/rawdata/XIDResult/wfi_iap/fc/";
	static final String root = "http://obs-he-lm:8888/xidresult";
	/**
	 * @param si
	 * @return
	 * @throws FatalException
	 */
	private static String getCPImageScript(EntrySaada si) throws Exception {
		SQLQuery sq = new SQLQuery("SELECT oidsaada\n"
				+ " FROM WideFieldData_IMAGE \n"
				+ " WHERE boxoverlaps(" + si.getPos_ra_csa() + "," + si.getPos_dec_csa() + ", " + si.getError_maj_csa() 
				+ ", WideFieldData_IMAGE.pos_ra_csa, WideFieldData_IMAGE.pos_dec_csa, WideFieldData_IMAGE.size_alpha_csa, WideFieldData_IMAGE.size_delta_csa) limit 1000\n");
		ResultSet rs = sq.run();
		String retour = "";		
		while( rs.next() ) {
			long cpoid = rs.getLong(1);
			SaadaInstance cpsi =  Database.getCache().getObject(cpoid);
			String vignette_file = "vignette" + si.getOid()  + ".fits";
			ImageUtils.buildTileFile(si.getPos_ra_csa(), si.getPos_dec_csa(), 1/60.0, 1/60.0
					, cpsi.getRepositoryPath()
					, new ConfigurationImage("", new ArgsParser(new String[]{"-collection=WideFieldData"}))
			, Database.getVOreportDir() + Database.getSepar() + vignette_file);
			retour += "\"WFIImage\"" + "=get file(" + root  + "/getproduct?report="  + vignette_file  + ");\n";
			retour += "cm \"WFIImage\";\n";
			break;

		}
		return retour;
	}

	/**
	 * @param si
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private static  String getCPSourceScript(EntrySaada si) throws Exception {
		String retour = "";
		long[] cpoids = si.getCounterparts("WideFieldCounterpart");
		if( si.getCounterparts("WideFieldCounterpart").length > 0 ) {
			String primary_file = "WideFieldCounterpart" + si.getOid() + "_"  + ".vot";
			FileOutputStream os = new FileOutputStream(new File(Database.getVOreportDir() + Database.getSepar() + primary_file));
			(new ConeSearchToVOTableFormator("collection")).processVOQuery(si.getCounterparts("WideFieldCounterpart"), os);
			retour += "WFISources" + "=get file(" + root  + "/getproduct?report="  + primary_file + ");\n";
			retour += "set " + "WFISources" + " color=green;\n";
			int angle = 315;
			HashSet<String> already_drawn = new HashSet<String>();
			for( Long cpoid: cpoids) {
				EntrySaada es = (EntrySaada) Database.getCache().getObject(cpoid);
				String name = es.getNameSaada();
				if( !already_drawn.contains(name)) {
					retour += "draw tag(" + es.getPos_ra_csa() + ", " +  es.getPos_dec_csa() 
					+ ", \"" + name + "\", 50, " + angle + ", arrow, 10);\n";
					angle -= 15;
					already_drawn.add(name);
				}
			}
		}
		return retour;
	}


	public static void main(String[] args) {
		try {
			Database.init("XIDResult");
			Messenger.debug_mode= false;

			SaadaQLResultSet srs = (new Query()).runQuery("Select ENTRY From * In TXMMiData "
					+ "  WhereRelation { \n"
					+ "  matchPattern { WideFieldCounterpart }\n"
					+ "  } \n");
			int num_script = 1;
			int cpt = 0;
			int max = 20000;
			int burst=100;
			boolean one_shoot = false;
			BufferedWriter bw;
			if( !one_shoot )
				bw = new BufferedWriter(new FileWriter(script_dir + "ajs/" + num_script + ".ajs"));
			else 
				bw = new BufferedWriter(new FileWriter("/home/michel/Desktop/fc.ajs"));
			bw.write("#!ajs\n");
			while( srs.next() ) {
				long oid = srs.getOid();
				EntrySaada es = (EntrySaada) Database.getCache().getObject(oid);
				String fc_path = fc_dir + "/" + es.getNameSaada() + ".png";
				cpt++;
				if( (new File(fc_path)).exists()  ) {
					Messenger.printMsg(Messenger.TRACE, fc_path + " already drawn (" + cpt + "/" + max + ")");
					continue;
				}
				Messenger.printMsg(Messenger.TRACE, cpt + "/" + max  + ": " + es.getNameSaada());
				bw.write("rm -all;\n");
				bw.write("grid on;\n");
				bw.write("reticle off;\n");

				bw.write(getCPImageScript(es) +";\n");
				bw.write("draw mode(RADEC);\n");

				bw.write("draw circle( " + es.getPos_ra_csa() + ", " +  es.getPos_dec_csa() + ", "+ 3600*es.getError_maj_csa()/10  +"arcsec); \n");
				bw.write("draw circle( " + es.getPos_ra_csa() + ", " +  es.getPos_dec_csa() + ", "+ 3600*es.getError_maj_csa()*1.65  +"arcsec); \n");
				bw.write("draw tag(" + es.getPos_ra_csa() + ", " +  es.getPos_dec_csa() + ", \"" + es.getNameSaada() + "\", 50, 135" +
				", arrow, 10);\n");
				bw.write(getCPSourceScript(es) + ";\n"); 		

				bw.write("draw mode(XY);\n");
				bw.write("set \"Drawing\" color=red;\n");
				bw.write("Title=draw string( \"13\", \"60\", \"" + es.getNameSaada() + "\")\n");   
				bw.write("Title=draw string( \"13\", \"52\", \"" + DefaultFormats.getHMSCoord(es.getPos_ra_csa(), es.getPos_dec_csa()) + "\")\n");   
				bw.write("sync;\n");		
				bw.write("save " + fc_path + ";\n");		
				if( (cpt % burst) == 0  ) {
					bw.write("quit;\n");
					bw.close();		
					num_script++;
					bw = new BufferedWriter(new FileWriter(script_dir + "ajs/" + num_script + ".ajs"));

				}
				if( cpt > max ) {
					Messenger.printMsg(Messenger.TRACE, "quit (" + cpt + "/" + max + ")");
					break;
				}
				if( one_shoot ) {
					Messenger.printMsg(Messenger.TRACE, "oneshoot (" + cpt + "/" + max + ")");
					break;
				}
			}
			if( !one_shoot ) bw.write("quit;\n");
			bw.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.close();
	}
}
