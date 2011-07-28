package saadadb.util;

/**
 * @author michel
 *
 */
public abstract class HardwareDescriptor {

	/**
	 * Returns a library name for an arch dependent library to be found in some jar.
	 * e.g. for Linux 32bits: returns prefic_linux_i386.lib
	 * @param prefix
	 * @return
	 */
	public static String getArchDependentLibName(String prefix) {
		String arch = System.getProperty("os.name").toLowerCase().replaceAll(" ", "");
		String suffixe;
		if( arch.indexOf("mac") != -1 ) {
			suffixe = ".dylib";
		}
		else if( arch.indexOf("linux") != -1 ) {
			suffixe = ".so";
		}
		else {
			suffixe = ".dll";
		}
		String osarch = System.getProperty("os.arch");
		if( "i386".equalsIgnoreCase(osarch)) {
			osarch = "x86";
		}
		return prefix + "_" + arch
		+ "_" 
		+ osarch.toLowerCase() 
		+ suffixe;
	}
	
	public static void main(String[] args) {
		System.out.println(HardwareDescriptor.getArchDependentLibName("libSQLITEProc"));
	}
}
