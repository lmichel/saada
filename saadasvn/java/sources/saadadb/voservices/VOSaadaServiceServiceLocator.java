package saadadb.voservices;

public class VOSaadaServiceServiceLocator extends org.apache.axis.client.Service implements saadadb.voservices.VOSaadaServiceService {

    // Use to get a proxy class for VOSaada
    private final java.lang.String VOSaada_address = "http://localhost:8080/axis/services/VOSaada";

    public java.lang.String getVOSaadaAddress() {
        return VOSaada_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String VOSaadaWSDDServiceName = "VOSaada";

    public java.lang.String getVOSaadaWSDDServiceName() {
        return VOSaadaWSDDServiceName;
    }

    public void setVOSaadaWSDDServiceName(java.lang.String name) {
        VOSaadaWSDDServiceName = name;
    }

    public saadadb.voservices.VOSaadaService getVOSaada() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(VOSaada_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getVOSaada(endpoint);
    }

    public saadadb.voservices.VOSaadaService getVOSaada(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            saadadb.voservices.VOSaadaSoapBindingStub _stub = new saadadb.voservices.VOSaadaSoapBindingStub(portAddress, this);
            _stub.setPortName(getVOSaadaWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (saadadb.voservices.VOSaadaService.class.isAssignableFrom(serviceEndpointInterface)) {
                saadadb.voservices.VOSaadaSoapBindingStub _stub = new saadadb.voservices.VOSaadaSoapBindingStub(new java.net.URL(VOSaada_address), this);
                _stub.setPortName(getVOSaadaWSDDServiceName());
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
        String inputPortName = portName.getLocalPart();
        if ("VOSaada".equals(inputPortName)) {
            return getVOSaada();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://localhost:8080/axis/services/VOSaada", "VOSaadaServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("VOSaada"));
        }
        return ports.iterator();
    }

}
