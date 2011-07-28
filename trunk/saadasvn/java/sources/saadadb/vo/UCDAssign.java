package saadadb.vo;
// package generated with the following command
// java org.apache.axis.wsdl.WSDL2Java http://cdsws.u-strasbg.fr/axis/services/UCD?wsdl
import java.sql.ResultSet;

import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import UCD_pkg.UCD;
import UCD_pkg.UCDService;
import UCD_pkg.UCDServiceLocator;

public class UCDAssign{
    // locator creation
    private UCDService locator = null;
    // UCD object
    private UCD mya = null;
    private boolean force=false;

    public UCDAssign(boolean force) {
        this.force = force;
        try  {
            this.locator = new UCDServiceLocator();
            this.mya = locator.getUCD();
            }
        catch( javax.xml.rpc.ServiceException e ) {
            Messenger.printMsg(Messenger.ERROR, "Can't initiate WS connection: Please try later");
            Messenger.printStackTrace(e);
            this.locator = null;
            this.mya = null;
            }
        }
    public UCDAssign() {
        this(true);
        }
    public boolean assignAll() {
        Messenger.printMsg(Messenger.TRACE, "Start to assign all UCDs: You have now time make some shopping");
        if( this.assignImages() == false || this.assignSpectra() == false ||
            this.assignTables() == false ) {          
            Messenger.printMsg(Messenger.ERROR, "UCD assignement failed");
            return false;
            }
        else {
            return true;
            }
        }
    public boolean assignImages() {
        Messenger.printMsg(Messenger.TRACE, "Start to assign UCDs for images 2D");
        return this.assign("saada_metaclass_image");
        }
    public boolean assignSpectra() {
        Messenger.printMsg(Messenger.TRACE, "Start to assign UCDs for spectrum");
        return this.assign("saada_metaclass_spectrum");
        }
    public boolean assignTables() {
        Messenger.printMsg(Messenger.TRACE, "Start to assign UCDs for tables");
        Messenger.printMsg(Messenger.TRACE, "Let's starts with entries..");
        if( this.assign("saada_metaclass_entry") == true ) {
            Messenger.printMsg(Messenger.TRACE, ".. and continue with tables");
            return this.assign("saada_metaclass_table");
            }
        else {
            return false;
            }
        }
    private String getUCD(String comment, String att) {
        String ucd = null;
        int nb_attemp =1;
        try {
            if( comment.equals("") == false ) {
                if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Try with description \"" + comment + "\"");
                ucd = mya.assign(comment.replaceAll("[^0-9,a-z,A-Z,\\s]", " ")).trim();
                }
            if( ucd == null || ucd.indexOf("Could not find") >= 0 ) {
                if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Description \"" + comment + "\" empty or not understood, try with " + att.substring(1).replace('_', ' '));
                ucd = mya.assign(att.substring(1).replaceAll("[^0-9,a-z,A-Z,\\s]", " ")).trim();
                if( ucd.indexOf("Could not find") >= 0 ) {
                    return "";
                    }
                else {
                    return ucd;
                    }
                }
            else {
                return ucd;
                }
            }
        catch(java.rmi.RemoteException e) {
            if( nb_attemp < 4 ) {
                nb_attemp++;
                Messenger.printMsg(Messenger.WARNING, e.getMessage() + ": Socket timed out (" + nb_attemp + "): Let's try again");
                this.getUCD(comment, att);
                }
            else {
                Messenger.printMsg(Messenger.ERROR, nb_attemp + " failures: UCDs assignement cancelled. Try later");
                return "Crashed";
                }
            }
        /*catch(java.net.SocketTimeoutException e) {
            }*/
        return "";
        }
    private boolean assign(String data_type) {
        int nb_attemp =1;
        try {
			SQLQuery squery = new SQLQuery();
            ResultSet rs = squery.run("select pk,comment,name_ucd,name_class,name_coll,name_attr from saada_metaclass_image");
      
            while( rs.next() ) {
                String comment    = rs.getString("comment").trim();
                String name_ucd   = rs.getString("name_ucd").trim();
                String name_class = rs.getString("name_class").trim();
                String name_coll  = rs.getString("name_coll").trim();
                String name_attr  = rs.getString("name_attr").trim();
                String ucd        = "";
                if( force == false && name_ucd.equals("") == false ) {
                    if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, name_coll + "->" + name_class + "->" + name_attr + " (" + comment + "): UCD already set :" + name_ucd);
                    }       
                else {
                    ucd = this.getUCD(comment,name_attr);
                    if( ucd.equals("Crashed")  ) {
                        return false;
                        }
                    else if( ucd.equals("") || ucd.indexOf("Could not find") >= 0 ) {
                         Messenger.printMsg(Messenger.TRACE, name_coll + "->" + name_class + "->" + name_attr + " (" + comment + "): No UCD found");
                        }
                    else {
                        Messenger.printMsg(Messenger.TRACE, name_coll + "->" + name_class + "->" + name_attr + " (" + comment + "): UCD = " + ucd);
                        rs.updateString("name_ucd", ucd);
                        rs.updateRow();
                        }
                    }
                 }
            squery.close();
            SQLTable.commitTransaction();
            return true;
            }
        catch(Exception e) {
           Messenger.printMsg(Messenger.ERROR, e.getMessage() + ": UCDs assignement cancelled. Try later");
            return false;                
            }
        }

    public static void main(String [] args) throws Exception {
        (new UCDAssign()).assignAll();
        }
    }

  
