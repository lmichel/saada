package saadadb.util;

/** * @version $Id$

 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */

public class convertirIEEE {
  public convertirIEEE() {
  }
  public static String convertir(String s)
  {
    String num="";
    if (s.indexOf(".")>=0)
    {
      String sign="";
      if (s.indexOf("-")==0 || s.indexOf("+")==0)
        sign =s.substring(0,1);
      String s1 =s.substring(0,s.indexOf("."));
      String s2 =s.substring(s.indexOf(".")+1);
      if (s1.equals("-0") ||s1.equals("") || s1.equals("+0") || s1.equals("0"))
      {
        if (!s2.equals("0"))
        {
          int k = 0, i = 0;
          String st = s2;
          while (st.indexOf("0") == 0) {
            k = k + 1;
            st = st.substring(1);
          }
          num = s2.substring(k, k + 1) + ".";
          num = num + s2.substring(k + 1);
          k = k + 1;
          num = num + "E-" + k;
          return sign + num;
        }
      }
    }
    return s;

  }
  public  static String convertir(double d)
  {
    String s=""+d;
    String num="";

    if (s.indexOf(".")>=0)
    {
      String sign="";
      if (s.indexOf("-")==0 || s.indexOf("+")==0)
        sign =s.substring(0,1);
      String s1 =s.substring(0,s.indexOf("."));
      String s2 =s.substring(s.indexOf(".")+1);
      if (s1.equals("-0") ||s1.equals("") || s1.equals("+0") || s1.equals("0"))
      {
        if (!s2.equals("0"))
        {
          int k = 0, i = 0;
          String st = s2;
          while (st.indexOf("0") == 0) {
            k = k + 1;
            st = st.substring(1);
          }
          num = s2.substring(k, k + 1) + ".";
          num = num + s2.substring(k + 1);
          k = k + 1;
          num = num + "E-" + k;
          return sign + num;
        }
      }
    }
    return s;

  }

}
  
