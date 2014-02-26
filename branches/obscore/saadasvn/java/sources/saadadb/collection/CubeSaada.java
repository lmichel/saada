package saadadb.collection;


/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */
import saadadb.util.SaadaConstant;
public class CubeSaada  extends WCSSaada {
  public double size_alpha_csa=SaadaConstant.DOUBLE;
  public double size_delta_csa=SaadaConstant.DOUBLE;
  public String shape_csa=SaadaConstant.STRING;
  public double ra1=SaadaConstant.DOUBLE;
  public double dec1=SaadaConstant.DOUBLE;
  public double ra2=SaadaConstant.DOUBLE;
  public double dec2=SaadaConstant.DOUBLE;

  public CubeSaada() {
    super();
  }

  public void setSize_alpha_csa(double size)
  {
    this.size_alpha_csa=size;

  }

  public double getSize_alpha_csa()
  {
    return this.size_alpha_csa;
  }

  public void setSize_delta_csa(double size)
  {
    this.size_delta_csa=size;
  }

  public double getSize_delta_csa()
  {
    return this.size_delta_csa;
  }

  public void setShape_csa(String shape)
  {
    this.shape_csa=shape;
  }

  public String getShape_csa()
  {
    return this.shape_csa;
  }



  public void calculCoordWCS()
  {
/*
    Point2D[] pixel=new Point2D[2];
    pixel[0] =new Point2D(0.0,0.0);

    pixel[1] =new Point2D(this.getSize_alpha_csa(),this.getSize_delta_csa());
    Point2D[] point=this.getPointWCS(pixel);
    ra1=point[0].getX();dec1=point[0].getY();
    ra2=point[1].getX();dec2=point[1].getY();
*/
  }

  public double getRa1()
  {
    return this.ra1;
  }

  public void setRa1(double _ra1)
  {
    this.ra1=_ra1;
  }

  public double getRa2()
  {
    return this.ra2;
  }

  public void setRa2(double _ra2)
  {
    this.ra2=_ra2;
  }
  public double getDec1()
  {
    return this.dec1;
  }

  public void setDec1(double _dec1)
  {
    this.dec1=_dec1;
  }

  public double getDec2()
  {
    return this.dec2;
  }

  public void setDec2(double _dec2)
  {
    this.dec2=_dec2;
  }



}
  
