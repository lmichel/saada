package saadadb.voservices;

/**
 * @author laurentmichel
 * * @version $Id: VOSaadaServiceService.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public interface VOSaadaServiceService extends javax.xml.rpc.Service {
    public java.lang.String getVOSaadaAddress();

    public saadadb.voservices.VOSaadaService getVOSaada() throws javax.xml.rpc.ServiceException;

    public saadadb.voservices.VOSaadaService getVOSaada(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
