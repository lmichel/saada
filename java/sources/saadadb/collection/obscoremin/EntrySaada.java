package saadadb.collection.obscoremin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;

/**
 * <p>Title: SAADA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 0.0001
 * @author michel
 * 04/2009: methods getLoaderConfig and getRepositoryPath added
 *
 */
public class EntrySaada extends SaadaInstance {
	/*
	 * Public fields are persistent
	 */
	public long oidtable;


	public EntrySaada() {
		super();
	}

	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#getRepositoryPath()
	 */
	public String getRepositoryPath() throws SaadaException {
		IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "No repository file  for entries");
		return null;
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#getVignetteName()
	 */
	public String getVignetteName() throws SaadaException {
		IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "No repository file  for entries");
		return null;
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#setVignetteFile()
	 */
	public void setVignetteFile() throws FatalException, IOException, SaadaException{
		this.vignetteFile = null;
	}

	public void getHTMLCol(PrintWriter out, HttpServletRequest req)
	{

		out.print("<tr>" );
		out.print("<th>"+this.s_ra+" </th>" );
		out.print("<th>"+this.s_dec+" </th>" );
		out.print("</tr>" );

	}

	/* (non-Javadoc)
	 * @see saadadb.collection.SaadaInstance#getLoaderConfig()
	 */
	public ProductMapping getLoaderConfig() throws SaadaException {
		IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not get loader configuration for entries: try with the table");
		return null;
	}



	@Override
	public void setAccess_url(String name) throws AbortException {
	}



	@Override
	public String getAccess_url() {
		return null;
	}

	@Override
	public void setDate_load(long time) throws AbortException {
	}

	@Override
	public long getDate_load() {
		return SaadaConstant.LONG;
	}

}

