package saadadb.dataloader.mapping;

import java.util.ArrayList;
import java.util.List;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.enums.ClassifierMode;
import saadadb.enums.RepositoryMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.HeaderRef;
import saadadb.products.DataFile;
import saadadb.products.FlatFileBuilder;
import saadadb.products.Image2DBuilder;
import saadadb.products.MiscBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;

public class ProductMapping {
	public final String name;
	public final String loaderParams;
	private RepositoryMode repositoryMode;
	private String collection;
	protected int category;
	private String extension;
	private boolean noVignette = false;
	private boolean noIndex = false;
    protected STEOMapping stoeMapping;
    protected Signature  signature = new Signature();
    private HeaderRef headerRef = new HeaderRef(0);
	private ClassMapping classMapping;
	private EntryMapping entryMapping;
	/**
	 * Available for the dataloader to store column index for any purpose
	 */
	public List<Integer> freeIndex = new ArrayList<Integer>(); 

   
    public ProductMapping(String name, ArgsParser ap) throws SaadaException {
    	this.name           = name;
		this.loaderParams   = ap.toString();
    	this.stoeMapping    = new STEOMapping(ap, false);
		this.collection     = ap.getCollection();
		this.category       = Category.getCategory(ap.getCategory());
		this.extension      = ap.getExtension();
		if( this.extension == null ) this.extension = "";
		this.noVignette     = ap.isNovignette();
		this.noIndex        = ap.isNoindex();
		this.repositoryMode = ap.getRepository();		
		this.classMapping = new ClassMapping(ap);

		signature.addElement(this.collection, true);
		signature.addElement(this.extension, false);
		signature.addElement(this.classMapping.toString(), false);
		if( this.category == Category.TABLE ) {
			this.entryMapping = new EntryMapping(name, ap); 
		}
		signature.addElement(this.stoeMapping.getSignature(), false);
    } 
    /**
     * @param name
     * @param ap
     * @param entryMode: not used just here to overload a constructor
     * @throws SaadaException
     */
    protected ProductMapping(String name, ArgsParser ap, boolean entryMode) throws SaadaException {
    	this.name           = name;
		this.loaderParams   = ap.toString();
    	this.stoeMapping    = new STEOMapping(ap, entryMode);
		this.collection     = ap.getCollection();
		this.category       = Category.ENTRY;
		this.extension      = ap.getExtension();
		if( this.extension == null ) this.extension = "";
		this.noVignette     = ap.isNovignette();
		this.noIndex        = ap.isNoindex();
		this.repositoryMode = ap.getRepository();		
		this.classMapping = new ClassMapping(ap);

		signature.addElement(this.collection, true);
		signature.addElement(this.extension, false);
		signature.addElement(this.classMapping.toString(), false);
		signature.addElement(this.stoeMapping.getSignature(), false);
    }
 
    /**
     * @return
     */
    public String getName() {
    	return this.name;
    }
    /**
     * @return
     */
    public String getLoaderParams() {
    	return this.loaderParams;
    }
    /**
     * @return
     */
    public boolean noIndex() {
    	return this.noIndex;
    }
    /**
     * @return
     */
    public boolean noVignette() {
    	return this.noVignette;
    }
    /**
     * @return
     */
    public String getCollection() {
    	return this.collection;
    }
    /**
     * @return
     */
    public int getCategory() {
    	return this.category;
    }
    /**
     * @return
     */
    public String getExtension(){
    	return this.extension;
    }
    /**
     * @return
     */
    public HeaderRef getHeaderRef() {
    	return this.headerRef;
    }
	/**
	 * @return
	 */
	public ClassifierMode getClassifier() {
		return this.classMapping.getClassifier();
	}
	/**
	 * @return
	 */
	public String getClassName() {
		return this.classMapping.getClassName();
	}

    /**
     * @return
     */
    public String getSignature() {
    	return this.signature.getMd5Key();
    }
    /**
     * @return
     */
    public String getSignatureWithoutColl() {
    	return this.signature.getMd5KeyWithoutColl();
    }
    public EntryMapping getEntryMapping() {
    	return this.entryMapping;
    }
    /**
     * @return
     */
    public RepositoryMode getRepositoryMode(){
    	return this.repositoryMode;
    }
    public SpaceMapping getSpaceAxisMapping() throws FatalException {
    	return (SpaceMapping) stoeMapping.getAxisMapping(Axis.SPACE);
    }
    public AxisMapping getTimeAxisMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.TIME);
    }
    public AxisMapping getEnergyAxisMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.ENERGY);
    }
    public AxisMapping getObservationAxisMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.OBSERVATION);
    }
    public AxisMapping getObservableAxisMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.OBSERVABLE);
    }
    public AxisMapping getPolarizationAxisMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.POLARIZATION);
    }
    public AxisMapping getExtenedAttMapping() throws FatalException {
    	return stoeMapping.getAxisMapping(Axis.EXTENDEDATT);
    }
    public List<String>  getIgnoredAttributes() {
    	return this.stoeMapping.getIgnoredAttributes();
    }
	public boolean isAttributeIgnored(String name){
		for( String ia: this.stoeMapping.getIgnoredAttributes() ) {
			if( name.equals(ia)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Probably not usefull: just for compat with the Configuration default handler
	 * @return
	 */
	public boolean isProductValid(Object prd) {
		return true;
	}
	/**
	 * Return true if index is in freeIndex
	 * @param index
	 * @return
	 */
	public boolean hasInFreeIndex(int index){
		if (index>=0 && index <this.freeIndex.size() ) {
			for( int i:this.freeIndex) {
				if( i == index ) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * @param index
	 */
	public void addToFreeIndex(int index){
		this.freeIndex.add(index);
	}
    
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.name);
		try {
			sb.append(" " + this.getClass().getName() +" " + this.collection + " " + Category.explain(this.category) );
		} catch (FatalException e) {}
		sb.append("\n");
		sb.append(this.loaderParams);
		sb.append("\n");
		sb.append(this.stoeMapping);
		sb.append("\n");
		return sb.toString();
	}
	/**
	 * @param file
	 * @return
	 * @throws IgnoreException
	 */
	public  ProductBuilder getNewProductBuilderInstance(DataFile file) throws SaadaException {
		switch( this.category ) {
		case Category.TABLE: return new TableBuilder(file, this) ;
		case Category.MISC : return new MiscBuilder(file, this) ;
		case Category.SPECTRUM : return new SpectrumBuilder(file, this) ;
		case Category.IMAGE : return new Image2DBuilder(file, this) ;
		case Category.FLATFILE : return new FlatFileBuilder(file, this) ;
		default: IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Can't handle product category <" + Category.explain(this.category) + ">");
		return null;
		}
		
	}

}
