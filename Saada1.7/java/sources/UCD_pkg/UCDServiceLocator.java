package UCD_pkg;

public class UCDServiceLocator extends org.apache.axis.client.Service implements UCD_pkg.UCDService {

    public UCDServiceLocator() {
    }


    public UCDServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public UCDServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for UCD
    private java.lang.String UCD_address = "http://cdsws.u-strasbg.fr/axis/services/UCD";

    public java.lang.String getUCDAddress() {
        return UCD_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String UCDWSDDServiceName = "UCD";

    public java.lang.String getUCDWSDDServiceName() {
        return UCDWSDDServiceName;
    }

    public void setUCDWSDDServiceName(java.lang.String name) {
        UCDWSDDServiceName = name;
    }

    public UCD_pkg.UCD getUCD() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(UCD_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getUCD(endpoint);
    }

    public UCD_pkg.UCD getUCD(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            UCD_pkg.UCDSoapBindingStub _stub = new UCD_pkg.UCDSoapBindingStub(portAddress, this);
            _stub.setPortName(getUCDWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setUCDEndpointAddress(java.lang.String address) {
        UCD_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (UCD_pkg.UCD.class.isAssignableFrom(serviceEndpointInterface)) {
                UCD_pkg.UCDSoapBindingStub _stub = new UCD_pkg.UCDSoapBindingStub(new java.net.URL(UCD_address), this);
                _stub.setPortName(getUCDWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("UCD".equals(inputPortName)) {
            return getUCD();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:UCD", "UCDService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:UCD", "UCD"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("UCD".equals(portName)) {
            setUCDEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
