package saadadb.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author laurent
 * 06/2011: IP V6 address filtering
 */
public class HostAddress {

	public static String getCanonicalHostname() {
		try {
			/*
			 * Take the hostname associated with the first eth interface being not the loopback
			 */
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements()) {
				Enumeration<InetAddress> e2 = ((NetworkInterface) e.nextElement()).getInetAddresses();
				while( e2.hasMoreElements() ) {
					InetAddress ip = (InetAddress) e2.nextElement();
					if(!ip.isLoopbackAddress() && !ip.isLinkLocalAddress()) {
						String retour = ip.getCanonicalHostName();
						// Filter IPV6 address (e.g. 2002:824f:8099:d:216:ecff:fe92:873c%2)
						if( retour.indexOf(":") == -1) {
							return retour;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return "localhost";
	}

	public static void main (String[] args) {
		System.out.println(HostAddress.getCanonicalHostname());
	}
}
