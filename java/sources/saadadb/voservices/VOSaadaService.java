package saadadb.voservices;

/**
 * @author laurentmichel
 * * @version $Id: VOSaadaService.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public interface VOSaadaService extends java.rmi.Remote {
    public java.lang.String serviceFile(java.lang.String in0) throws java.rmi.RemoteException;
    public java.lang.String serviceURL(java.lang.String in0) throws java.rmi.RemoteException;
}
