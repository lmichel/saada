package saadadb.voservices;

public interface VOSaadaServiceService extends javax.xml.rpc.Service {
    public java.lang.String getVOSaadaAddress();

    public saadadb.voservices.VOSaadaService getVOSaada() throws javax.xml.rpc.ServiceException;

    public saadadb.voservices.VOSaadaService getVOSaada(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
