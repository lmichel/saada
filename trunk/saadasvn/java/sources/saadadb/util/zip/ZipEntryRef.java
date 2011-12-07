/**
 * 
 */
package saadadb.util.zip;

import java.util.Date;
import java.util.List;
import java.util.Map;

import saadadb.util.Messenger;

/**
 * ZipEntryRef class models one entry of the ZIP archive
 * It is usually set by {@link saadadb.vo.cart.CartDecoder CartDecoder}
 * @author laurent
 * @version $Id$
 */
@SuppressWarnings("rawtypes")
public class ZipEntryRef implements Comparable {
	/**
	 * The entry refers to a file product stored within the DB
	 * In this case, the oid is taken as uri
	 */
	public static final int SINGLE_FILE = 0;
	/**
	 * The entry refers to a query result. 
	 * In this case, the query is taken as uri
	 */
	public static final int QUERY_RESULT = 1;
	/**
	 * INdicated whether the entry refers to a file product or to a query
	 */
	private int type;
	/**
	 * Flag indicating that some associated data must be added to the ZIP archive
	 */
	public static final int WITH_REL = 1;
	/**
	 * String specifying that all relations must be considered
	 */
	public static final String ANY_REL = "any-relations";
	/**
	 * Use when try set an empty name 
	 */
	public static final String DEFAULT_NAME = "DefaultName";
	/**
	 * Entry option: Only relationship as supported yet
	 */
	private int options;
	/**
	 * Name of the entry in the archive
	 */
	private String name;
	/**
	 * URI of the entry data (oid or query)
	 */
	private String uri;	
	/**
	 * List of relations whose links must be added to the archive entry.
	 * "any-relation" means all relation. 
	 * Relations is not supported yet
	 */
	private String[] relationFilter = {ANY_REL};
	
	/**
	 * @param type
	 * @param name
	 * @param uri
	 * @param options
	 */
	public ZipEntryRef(int type, String name, String uri, int options) {
		super();
		this.type     = type;
		this.setName(name);
		this.setUri(uri);
		this.options  = options;
	}

	public ZipEntryRef(int type, String name, String uri) {
		super();
		this.type     = type;
		this.name     = name;
		this.uri      = uri;
		this.options  = 0;
	}
	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if( name == null || name.trim().length() == 0 ) {
			Messenger.printMsg(Messenger.WARNING, "Set an empty name to a ZipEntryRef: takes the default");
			this.name = DEFAULT_NAME;
		}
		else {
			this.name = name.replaceAll("[^\\w\\.]", "_");

		}
	}

	/**
	 * @return
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * @param uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * @return
	 */
	public boolean includeLinkedData() {
		return (this.options & WITH_REL) != 0;
	}
	/**
	 * @return
	 */
	public boolean supportRelationFiltering() {
		return ( relationFilter != null && relationFilter[0].equals("any-relation"));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.type + " " + this.name + " " + this.uri;
	}
	
	/**
	 * @param headerFields
	 * @return
	 */
	public String getFilenameFromHttpHeader(Map<String, List<String>>  headerFields) {
		if( this.name == null || this.name.length() == 0 || this.name.toLowerCase().equals("preserve")) {
			/*
			 * Try first to get the filename from the Content-Disposition header fields
			 */
			List<String> cds = null;
			if( ( cds = headerFields.get("Content-Disposition")) != null ) {
				String[] cd = cds.get(0).split("=");
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "take " + cd[cd.length-1] + " as filename");
				return cd[cd.length-1].replaceAll("\"", "");
			}
			/*
			 * Other take some dummy name
			 */
			this.name = "DefaultName" + (new Date()).getTime();
		}
		/*
		 * Then add a suffix
		 */
		List<String> cts = null;
		String suffix = ".nosuffix";
		if( ( cts = headerFields.get("Content-Type")) != null ) {
			String[] cd = cts.get(0).split("/");
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "take " + cd[cd.length-1] + " as suffix");
			suffix = "." + cd[cd.length-1];
		}
		List<String> ccs = null;
		String comp = "";
		if( ( ccs = headerFields.get("Content-Encoding")) != null ) {
			comp = "." +  ccs.get(0);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "take " + comp + " as encoding");
		}
		return this.name + suffix + comp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return o.toString().compareTo(this.name);
	}

}
