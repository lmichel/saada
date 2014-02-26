package saadadb.relationship;
/** * @version $Id: CalculDistance.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001 create 04/03:2004
 */

public class CalculDistance {


  public static double  distance( double ra1d, double de1d, double ra2d, double de2d)
  {


    double result;
    double ra1, de1, ra2, de2;
    double valx1, valx2, valy1, valy2, valz1, valz2;

    ra1 = deg2rad(ra1d);
    de1 = deg2rad(de1d);
    ra2 = deg2rad(ra2d);
    de2 = deg2rad(de2d);

    valx1 = java.lang.Math.cos(de1) * java.lang.Math.cos(ra1);
    valy1 = java.lang.Math.cos(de1) * java.lang.Math.sin(ra1);
    valz1 = java.lang.Math.sin(de1);
    valx2 = java.lang.Math.cos(de2) * java.lang.Math.cos(ra2);
    valy2 = java.lang.Math.cos(de2) * java.lang.Math.sin(ra2);
    valz2 = java.lang.Math.sin(de2);

    result = 2 * java.lang.Math.asin(java.lang.Math.sqrt(((valx1 - valx2)*(valx1 - valx2))
                           + ((valy1 - valy2)*(valy1 - valy2))
                           + ((valz1 - valz2)*(valz1 - valz2)))/2);

    return (rad2deg(result));
  }

  public static double rad2deg(double d)
  {
    return( (180.0/java.lang.Math.PI) * d);
  }

  public static double deg2rad(double d)
  {
    return( (java.lang.Math.PI/180.0) * d);
  }



}
  
