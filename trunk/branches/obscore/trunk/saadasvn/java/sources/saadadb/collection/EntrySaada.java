package saadadb.collection;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;

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
public class EntrySaada extends Position {

  public long oidtable;


  public EntrySaada() {
    super();
  }

 
  public long getOidtable()
  {
    return this.oidtable;
  }

  public void setOidtable(long _oid)
  { 
    this.oidtable=_oid;
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
   out.print("<th>"+this.pos_ra_csa+" </th>" );
   out.print("<th>"+this.pos_dec_csa+" </th>" );
   out.print("</tr>" );

  }

  /* (non-Javadoc)
   * @see saadadb.collection.SaadaInstance#getLoaderConfig()
   */
  public ConfigurationDefaultHandler getLoaderConfig() throws SaadaException {
	  IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not get loader configuration for entries: try with the table");
	  return null;
  }

}
  
