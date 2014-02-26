package preproc.ogip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import cds.savot.model.FieldSet;
import cds.savot.model.ParamSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.TDSet;
import cds.savot.writer.SavotWriter;

public class VOTableGenerator {

	
				/* ##################################
				 * 			ATTRIBUTES
				 * #################################*/
	private Product product;

	private SavotWriter writer;
	
	private Vector<String> vFileName;
	
	
				/* ##################################
				 * 			CONSTRUCTORS
				 * #################################*/
	public VOTableGenerator(Product product){
		
		this.product = product;
		
		this.writer = new SavotWriter();
		
		this.vFileName = new Vector<String>();
		
		this.generateDocument();
	}
	
	
				/* ###################################
				 * 				METHODES
				 * ##################################*/
	public void generateDocument(){
		
		String name = this.product.getName().substring( 0, this.product.getName().lastIndexOf(".") );
		String pathRepository = Database.getRepository()+"/voreports/";
		
		for(int k=0; k<this.product.getVMapAttribute().size(); k++){
			
			String filename = pathRepository+name+k+".xml";
			this.vFileName.add(filename);
			
			FileOutputStream ostream = null;
			try {
				ostream = new FileOutputStream(  filename );
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			this.writer.initStream(ostream);
			
			SavotVOTable votable = new SavotVOTable();
			votable.setXmlnsxsi("http://www.w3.org/2001/XMLSchema-instance");
			votable.setXsinoschema("xmlns:http://www.ivoa..net/xml/VOTable-1.1.xsd");
			
			
			this.writer.writeDocumentHead(votable);
			SavotResource resource = new SavotResource();
			resource.setUtype("sed:SED");
			this.writer.writeResourceBegin(resource);
			
					//first table with all parameters
			SavotTable table = new SavotTable();
			//table.setName("Results");
			table.setUtype("sed:SED");
			this.writer.writeTableBegin(table);
			
			GregorianCalendar myDate = new GregorianCalendar();
			DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
			ParamSet paramSet = new ParamSet();
			
			SavotParam param = new SavotParam();
			param.setName("Date");
			param.setDataType("String");
			param.setUcd("time;meta.dataset");
			param.setValue( df.format(myDate.getTime()) );
			param.setUtype("sed:Date");
			param.setArraySize("*");
			paramSet.addItem(param);
			
			param = new SavotParam();
			param.setName("Creator");
			param.setDataType("String");
			param.setUcd("meta.id");
			param.setValue("Saada 1.5 from FITSFILENAME");
			param.setUtype("sed:Creator");
			param.setArraySize("*");
			paramSet.addItem(param);
			
			param = new SavotParam();
			param.setName("Nseg");
			param.setDataType("int");
			param.setUcd("meta.number");
			param.setValue("1");
			param.setUtype("sed:NSegments");
			paramSet.addItem(param);
			
			HashMap<String, AttributeHandler> mapAttribute = this.product.getVMapAttribute().get(k);
			Collection<AttributeHandler> listAttribute = mapAttribute.values();
			Iterator<AttributeHandler> ite = listAttribute.iterator();
			
			
			while(ite.hasNext()){
				
				AttributeHandler currentAttribute = ite.next();
				param = new SavotParam();
				param.setName(currentAttribute.getNameattr());
				param.setDataType(currentAttribute.getType());
				param.setValue(currentAttribute.getValue());
				paramSet.addItem(param);
			}
			param = new SavotParam();
			param.setName("MinWavelenght");
			param.setValue("1.0");
			param.setDataType("float");
			param.setUcd("em.wl;stat.min");
			param.setUtype("sed:SpectralMinimumWavelength");
			param.setUnit("");
			paramSet.addItem(param);
			
			param = new SavotParam();
			param.setName("MaxWavelenght");
			param.setValue((float)(this.product.getVSpectraData().get(0).size())+"");
			param.setDataType("float");
			param.setUcd("em.wl;stat.max");
			param.setUtype("sed:SpectralMaximumWavelength");
			param.setUnit("");
			paramSet.addItem(param);
			
			this.writer.writeParam(paramSet);
			this.writer.writeTableEnd();
			
					//second table with all the fields
			table = new SavotTable();
			table.setName(name+k+".xml");
			table.setUtype("sed:Segment");
			this.writer.writeTableBegin(table);
			
			FieldSet fields = new FieldSet();
			
			SavotField field = new SavotField();
			field.setDataType("float");
			field.setName("Count");
			field.setUcd("em.wl");
			field.setUtype("sed:Segment.Points.SpectralCoord.Value");
			fields.addItem(field);
			
			field = new SavotField();
			field.setDataType("float");
			field.setName(this.product.getFieldName());
			field.setUnit(this.product.getUnit());
			field.setUcd("phys.luminosity;em.wl;meta.modelled");
			field.setUtype("sed:Segment.Points.Flux.Value");
			fields.addItem(field);
			
			paramSet = new ParamSet();
			param = new SavotParam();
			param.setDataType("String");
			param.setName("Segtype");
			param.setUtype("sed:Segment.SegmentType" );
			param.setValue("Spectrum");
			param.setArraySize("*");
			paramSet.addItem(param);
			
			this.writer.writeField(fields);
			this.writer.writeParam(paramSet);

			this.writer.writeDataBegin();
			this.writer.writeTableDataBegin();
			
			//for(int i=0; i<1/*this.product.getVSpectraData().size()*/; i++){
				Vector<Float> vTmp = this.product.getVSpectraData().get(k);
				for(int j=0; j<vTmp.size(); j++){
					SavotTR tr = new SavotTR();
					TDSet tds = new TDSet();
					SavotTD td = new SavotTD();
					/*
					 * Force spectral coordinate to float  (required by specview
					 */
					td.setContent(((float)(j+1))+"");
					tds.addItem(td);
					td = new SavotTD();
					td.setContent(vTmp.get(j)+"");
					tds.addItem(td);
					tr.setTDSet(tds);
					this.writer.writeTR(tr);
				}
			//}
			
			this.writer.writeTableDataEnd();
			this.writer.writeDataEnd();
			this.writer.writeTableEnd();
			this.writer.writeResourceEnd();
			this.writer.writeDocumentEnd();
			
			try {
				ostream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
				/* ##################################
				 * 			GETTERS AND SETTERS
				 * #################################*/
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}


	public Vector<String> getVFileName() {
		return vFileName;
	}


	public void setVFileName(Vector<String> fileName) {
		vFileName = fileName;
	}
}
