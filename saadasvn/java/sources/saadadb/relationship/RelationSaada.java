package saadadb.relationship;

/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */
import saadadb.collection.SaadaInstance;
public   abstract class  RelationSaada {
  public SaadaInstance objPrimary;
  public SaadaInstance objSecondary ;

  public RelationSaada() {
  }

  public void setObjPrimary(SaadaInstance obj)
  { this.objPrimary=obj ;}

  public SaadaInstance getObjPrimary ()
  { return this.objPrimary;}

  public void setObjSecondary(SaadaInstance obj)
  { this.objSecondary=obj; }

  public SaadaInstance getObjSecondary()
  { return this.objSecondary; }

}
  
