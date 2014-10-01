package saadadb.vo.request.formator.votable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.collection.obscoremin.SpectrumSaada;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.DateUtils;
import saadadb.util.Messenger;
import cds.savot.binary.DataBinaryReader;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;

/**
 * @author laurent
 * @version 07/2011
 */
public class SsapVotableFormator extends VotableFormator {

	public SsapVotableFormator() throws QueryException {
		setDataModel("SSA");
		limit = 100;
		protocolN = "SSAP";
		protocolV = "1.1";
		this.infoMap.put("SERVICE_PROTOCOL", new infoEntry(this.protocolV,
				this.protocolN));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see saadadb.vo.request.formator.Formator#supportResponseInRelation()
	 */
	public boolean supportResponseInRelation() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query
	 * .result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet)
			throws QueryException {
		this.saadaInstanceResultSet = saadaInstanceResultSet;
	}
//TODO DELETE
	private void truc() throws Exception {
		for (Object f : dataModelFieldSet.getItems()) {
			SavotField sf = (SavotField) f;
			String utype = sf.getUtype();
			String id = sf.getId();
			MetaCollection col = Database.getCachemeta().getCollection(
					"VizierData");

			HashMap<String, AttributeHandler> map = new HashMap<String, AttributeHandler>();
			map.put("EMIN", col.getAttribute_handlers_spectrum().get("e_min"));
			map.put("EMAX", col.getAttribute_handlers_spectrum().get("e_max"));

			ColumnExpressionSetter ces = new ColumnExpressionSetter("test","(EMIN+EMAX)/2", map);
			ces.calculateExpression();
			System.out.println(ces);

			AttributeHandler ah;

		}
	}

//	@Override
//	protected void writeRowData(SaadaInstance si) throws Exception {
//		// SpectrumSaada obj = (SpectrumSaada) (si);
//		SaadaInstance obj = si;
//		
//		for (Object f : dataModelFieldSet.getItems()) {
//			SavotField sf = (SavotField) f;
//			
//			String utype = sf.getUtype();
//			String id = sf.getId();
//			String val = "";
//			boolean cdata = false;
//			// System.out.println("Utype " + utype);
//			if (utype.equals("Target.Pos")) {
//				val = obj.s_ra + " " + obj.s_dec;
//			} else if (utype
//					.endsWith("Char.SpatialAxis.Coverage.Location.Value")) {
//				val = obj.s_ra + " " + obj.s_dec;
//				/*
//				 * Just to be overridden for XMM
//				 */
//				// val = "";
//			} else if (utype
//					.endsWith("Char.SpectralAxis.Coverage.Location.Value")) {
//				try {
//					/*
//					 * Conversion must be first done because it could be non
//					 * linear (e.g. Kev -> m) Hence converting the mean is no
//					 * equivalent to the mean of converted values
//					 */
//					double v1 = SpectralCoordinate.convertSaada(
//							Database.getSpect_unit(), "m", obj.em_min);
//					double v2 = SpectralCoordinate.convertSaada(
//							Database.getSpect_unit(), "m", obj.em_max);
//					val = (Double.toString((v1 + v2) / 2));
//				} catch (Exception e) {
//					e.printStackTrace();
//					val = "";
//				}
//			} else if (utype
//					.endsWith("Char.SpectralAxis.Coverage.Bounds.Extent")) {
//				try {
//					double v1 = SpectralCoordinate.convertSaada(
//							Database.getSpect_unit(), "m", obj.em_min);
//					double v2 = SpectralCoordinate.convertSaada(
//							Database.getSpect_unit(), "m", obj.em_max);
//					double v = v2 - v1;
//					if (v < 0)
//						v *= -1.;
//					val = (Double.toString(v));
//				} catch (Exception e) {
//					e.printStackTrace();
//					val = "";
//				}
//			} else if (utype.endsWith("Access.Size")) {
//				val = Long.toString(obj.getFileSize() / 1024);
//			} else if (utype.endsWith("Access.Reference")) {
//				val = obj.getDownloadURL(true);
//				cdata = true;
//			} else if (utype.endsWith("Access.Format")) {
//				val = obj.getMimeType();
//				cdata = true;
//			}
//			// else if(utype.endsWith("DataID.Creator")){
//			// FIND CREATOR
//			// }
//			// else if(utype.endsWith("DataID.CreatorDID")){
//			// FIND CREATORDID
//			// }
//			else if (utype.endsWith("DataID.Collection")) {
//				cdata = true;
//				val = obj.getObs_collection();
//			}
//			// else if(utype.endsWith("DataID.DataSource")){
//			// TODO DataID.DataSource
//			// }
//			// else if(utype.endsWith("DataID.CreationType")){
//			// TODO DataID.CreationType
//			// }
//			else if (utype.endsWith("Dataset.DataModel")) {
//				cdata = true;
//				val = "ivo://ivoa.net/std/ObsCore-1.0";
//			} else if (utype.endsWith("Curation.Reference")) {
//				// TODO Curation.Reference
//			}
//			// else if(utype.endsWith("Char.SpatialAxis.Accuracy.StatError")){
//			// TODO Accuracy.StatError
//			// }
//			else if (utype.endsWith("Char.SpatialAxis.Calibration")) {
//				val = String.valueOf(obj.getCalib_level());
//			} else if (utype.endsWith("Char.SpatialAxis.Resolution")) {
//				val = String.valueOf(obj.s_resolution);
//			} else if (utype
//					.endsWith("Char.SpatialAxis.Coverage.Bounds.Extent")) {
//				val = String.valueOf(obj.getS_fov());
//			}
//			// else if(utype.endsWith("Char.SpectralAxis.Ucd")){
//			// TODO Char.SpectralAxis.Ucd
//			// }
//			else if (utype.endsWith("Char.SpectralAxis.Coverage.Bounds.Start")) {
//				val = String.valueOf(obj.em_min);
//			} else if (utype.endsWith("Char.SpectralAxis.Coverage.Bounds.Stop")) {
//				val = String.valueOf(obj.em_max);
//			} else if (utype.endsWith("Char.SpectralAxis.Calibration")) {
//				val = String.valueOf(obj.getCalib_level());
//			}
//			// else if (utype.endsWith("Char.SpectralAxis.Resolution")){
//			// TODO Char.SpectralAxis.Resolution
//			// }
//			else if (utype.endsWith("Char.TimeAxis.Coverage.Location.Value")) {
//				val = String.valueOf((obj.t_exptime / 2) / 86400);
//			} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Extent")) {
//				val = String.valueOf(obj.t_max - obj.t_min);
//			} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Start")) {
//				val = String.valueOf(obj.t_min);
//			} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Stop")) {
//				val = String.valueOf(obj.t_max);
//			}
//			// else if(utype.endsWith("Query.Score")){
//			// TODO Query.Score
//			// }
//			else if (utype.endsWith("CoordSys.SpaceFrame.Name")) {
//				val = Database.getAstroframe().name;
//			} else if (utype.endsWith("Char.FluxAxis.Ucd")) {
//				val = obj.getO_ucd();
//			} else if (utype.endsWith("Char.FluxAxis.Calibration")) {
//				val = obj.getO_calib_status();
//			} else if (utype.endsWith("Target.Name")) {
//				cdata = true;
//				val = obj.target_name;
//
//			} else if (utype.endsWith("Curation.PublisherDID")) {
//				cdata = true;
//				val = obj.getObs_publisher_did();
//			} else if (utype.endsWith("DataID.Title")) {
//				val = obj.obs_id;
//				cdata = true;
//			} else if (id.equals("LinktoPixels")) {
//				val = obj.getURL(true);
//				cdata = true;
//			} else {
//				// Tries to find a match in its extended attributes
//				val = lookForAMatch(obj, sf);
//				if (sf.getDataType().equalsIgnoreCase("char")
//						|| sf.getDataType().equalsIgnoreCase("string"))
//					cdata = true;
//
//			}
//			// TODO Check with xcatdb later
//			// if (obj.getClass().getName().endsWith("EpicSrcSpect")) {
//			// String vxmm = getXMMData(utype, obj);
//			// if (vxmm != null)
//			// val = vxmm;
//			// }
//			if (cdata) {
//				addCDataTD(val);
//			} else {
//				addTD(val);
//			}
//		}
//	}

	@Override
	protected void writeProtocolParamDescription() throws Exception {
		ParamSet paramSet = new ParamSet();
		SavotParam param = new SavotParam();
		param.setDataType("char");
		param.setArraySize("*");
		param.setName("INPUT:POS");
		param.setDescription("The center of the region of interest."
				+ "The coordinate values are specified in list format (comma separated) in decimal degrees "
				+ "with no embedded white space followed by an optional coord. systems such as GALACTIC;"
				+ "default is ICRS.");
		param.setValue(this.protocolParams.get("pos"));
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:SIZE");
		param.setDataType("double");
		param.setUnit("deg");
		param.setDescription("The diameter of the circular region of interest in decimal degrees."
				+ "A special case is SIZE=0. It will cause a search in the service defined "
				+ "default sized region of 0.1 degrees resulting in a patch of 0.01*pi sq.deg.");
		param.setValue(this.protocolParams.get("size"));
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:BAND");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("");
		param.setValue(this.protocolParams.get("band"));
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:TIME");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("");
		param.setDescription("Requested format of time (returns null values currently)");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:FORMAT");
		param.setValue("COMPLIANT");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("Requested format of spectra");
		paramSet.addItem(param);

		param = new SavotParam();
		param.setName("INPUT:collection");
		param.setDataType("char");
		param.setArraySize("*");
		param.setDescription("Gives the collections and possibly the classes in which we want to search. The format is as follows : collection=coll1,coll2 OR collection=coll1[class1, class2]");
		param.setValue(this.protocolParams.get("collection"));
		paramSet.addItem(param);

		writer.writeParam(paramSet);
	}

	@Override
	protected AttributeHandler getAttr_extended(String name) throws Exception {
		return Database.getCachemeta().getAtt_extend_spectra(name);
	}

	/*
	 * Just to remind
	 * 
	 * @param utype
	 * 
	 * @param sp
	 * 
	 * @return
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private String getXMMData(String utype, SpectrumSaada sp) throws Exception {
		if (utype.equals("Target.Name")) {
			return sp.obs_id;
		} else if (utype.endsWith("Curation.PublisherDID")
				|| utype.endsWith("DataID.DatasetID")) {
			return "ivo://xcatdb/2xmmi/epicssa#"
					+ (new File(sp.getAccess_url())).getName();
		} else if (utype.endsWith("CoordSys.SpectralFrame.RefPos")) {
			return sp.getFieldValue("_tlmin1").toString();
		} else if (utype.endsWith("CoordSys.TimeFrame.Name")) {
			return sp.getFieldValue("SSA_TIMESYS").toString();
		} else if (utype.endsWith("CoordSys.TimeFrame.RefPos")) {
			return sp.getFieldValue("SSA_TIMEREF").toString();
		} else if (utype.endsWith("CoordSys.TimeFrame.Zero")) {
			return sp.getFieldValue("SSA_MJDREF").toString();
		} else if (utype.endsWith("Char.SpatialAxis.Coverage.Location.Value")) {
			return sp.getFieldValue("SSA_SPAT_LOC").toString();
		} else if (utype.endsWith("Char.SpatialAxis.Coverage.Bounds.Extent")) {
			double v1 = 2.0 * Double.parseDouble(sp.getFieldValue(
					"SSA_SPAT_EXT").toString());
			return "" + v1;
		} else if (utype.endsWith("Char.SpatialAxis.Coverage.Support.Area")) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_SPAT_EXT")
					.toString());
			return "Circle with (center, radius) = ("
					+ sp.getFieldValue("SSA_SPAT_LOC").toString() + " " + v1
					+ ")";
		} else if (utype
				.endsWith("Char.SpatialAxis.SamplingPrecision.SampleExtent")) {
			return sp.getFieldValue("SSA_SPAT_SAMPEXT").toString();
		} else if (utype.endsWith("Char.SpectralAxis.Coverage.Location.Value")) {
			double v1 = Double
					.parseDouble(sp.getFieldValueByUtype(
							"Char.SpectralAxis.Coverage.Bounds.Start", true)
							.toString());
			double v2 = Double.parseDouble(sp.getFieldValueByUtype(
					"Char.SpectralAxis.Coverage.Bounds.Stop", true).toString());
			return Double.toString((v1 + v2) / 2);
		} else if (utype.endsWith("Char.SpectralAxis.Coverage.Bounds.Extent")) {
			double v1 = Double
					.parseDouble(sp.getFieldValueByUtype(
							"Char.SpectralAxis.Coverage.Bounds.Start", true)
							.toString());
			double v2 = Double.parseDouble(sp.getFieldValueByUtype(
					"Char.SpectralAxis.Coverage.Bounds.Stop", true).toString());
			return Double.toString(v2 - v1 + 1);
		} else if (utype.endsWith("Char.TimeAxis.Unit")) {
			return sp.getFieldValue("SSA_TIMEUNIT").toString();
		} else if (utype.endsWith("Char.TimeAxis.Coverage.Location.Value")) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF")
					.toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTART")
					.toString());
			double v3 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTOP")
					.toString());
			return Double.toString(v1 + (0.5 * (v2 + v3) / 86400));
		} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Extent")) {
			return sp.getFieldValue("SSA_DURATION").toString();
		} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Start")) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF")
					.toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTART")
					.toString());
			return Double.toString(v1 + ((v2) / 86400));
		} else if (utype.endsWith("Char.TimeAxis.Coverage.Bounds.Stop")) {
			double v1 = Double.parseDouble(sp.getFieldValue("SSA_MJDREF")
					.toString());
			double v2 = Double.parseDouble(sp.getFieldValue("SSA_TIMESTOP")
					.toString());
			return Double.toString(v1 + ((v2) / 86400));
		} else if (utype.endsWith("Char.TimeAxis.Coverage.Support.Extent")) {
			return sp.getFieldValue("_exposure").toString();
		} else if (utype
				.endsWith("Char.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor")) {
			double v = Double.parseDouble(sp.getFieldValue("_exposure")
					.toString());
			return Double.toString(v
					/ Double.parseDouble(sp.getFieldValue("SSA_DURATION")
							.toString()));
		} else if (utype.equals("Background.File")) {
			return sp.getFieldValue("_backfile").toString();
		} else if (utype.equals("Response.File")) {
			return sp.getFieldValue("_respfile").toString();
		} else if (utype.equals("Ancillary.Response.File")) {
			return sp.getFieldValue("_ancrfile").toString();
		} else if (utype.equals("Pipeline.Software.Version")) {
			return sp.getFieldValue("_ppsvers").toString();
		}
		/*
		 * Return a ZIP ball with the calibration files
		 */
		else if (utype.equals("Allfiles.Zipball")) {
			return "<![CDATA[ "
					+ Database.getUrl_root()
					+ "/getproduct?reports="
					+ sp.getAccess_url().replace(".gz", "")
							.replace(".FIT", ".zip") + "]]>";
		} else {
			return null;
		}
	}

}
