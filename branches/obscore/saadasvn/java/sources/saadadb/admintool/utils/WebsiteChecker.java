package saadadb.admintool.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JOptionPane;
import java.util.Arrays;

public class WebsiteChecker 
{
	static final String[] browsers = { "google-chrome", "firefox", "opera",
		"epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
	static final String errMsg = "Error attempting to launch web browser";
	
	public static Object[] checkIfURLExists(String targetUrl) 
	{
		Object[] checker = new Object[2];
		// checker[0] = isWorking (boolean), checker[1] = errorMessage (String);
		boolean isWorking = false;
		String errorMessage = "";
		HttpURLConnection httpUrlConn;
		try 
		{
			httpUrlConn = (HttpURLConnection) new URL(targetUrl).openConnection();
			httpUrlConn.setRequestMethod("HEAD");
			// Set timeouts in milliseconds
			httpUrlConn.setConnectTimeout(5000);
			httpUrlConn.setReadTimeout(5000);

			isWorking = (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
			errorMessage = httpUrlConn.getResponseMessage();
		} 
		catch (Exception e) 
		{
			errorMessage = e.getMessage();
			isWorking = false;
		}
		checker[0] = isWorking;
		checker[1] = errorMessage;
		return checker;
	}

		/**
		 * Opens the specified web page in the user's default browser
		 * @param url A web address (URL) of a web page
		 */
		public static void openURL(String url) 
		{
			try 
			{  // Attempt to use Desktop library from JDK 1.6+
				Class<?> d = Class.forName("java.awt.Desktop");
				d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
					d.getDeclaredMethod("getDesktop").invoke(null),
					new Object[] {java.net.URI.create(url)});
				// Above code mimicks:  java.awt.Desktop.getDesktop().browse()
			}
			catch (Exception ignore) 
			{  // Library not available or failed
				String osName = System.getProperty("os.name");
				try 
				{
					if (osName.startsWith("Mac OS")) 
					{
						Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
							"openURL", new Class[] {String.class}).invoke(null,
							new Object[] {url});
					}
					else if (osName.startsWith("Windows"))
						Runtime.getRuntime().exec(
							"rundll32 url.dll,FileProtocolHandler " + url);
					else 
					{ //assume Unix or Linux
						String browser = null;
						for (String b : browsers)
							if (browser == null && Runtime.getRuntime().exec(new String[]{"which", b}).getInputStream().read() != -1)
								Runtime.getRuntime().exec(new String[] {browser = b, url});
						if (browser == null)
							throw new Exception(Arrays.toString(browsers));
					}
				}
				catch (Exception e) 
				{
					JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
				}
			}
		}
}
