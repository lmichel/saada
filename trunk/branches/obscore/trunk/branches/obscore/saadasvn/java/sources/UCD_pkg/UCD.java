package UCD_pkg;

public interface UCD extends java.rmi.Remote {
    public java.lang.String UCDList() throws java.rmi.RemoteException;
    public java.lang.String resolveUCD(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String UCDofCatalog(java.lang.String catalog_designation) throws java.rmi.RemoteException;
    public java.lang.String translate(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String upgrade(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String validate(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String explain(java.lang.String descr) throws java.rmi.RemoteException;
    public java.lang.String assign(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String suggest(java.lang.String ucd) throws java.rmi.RemoteException;
    public java.lang.String getAvailability() throws java.rmi.RemoteException;
}
