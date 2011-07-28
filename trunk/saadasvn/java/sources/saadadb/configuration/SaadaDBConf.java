package saadadb.configuration;


/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg-CNES</p>
 * @ "stagaire ou codeur" :NGUYEN NGOC HOAN
 * @version 00000001
 */
public class SaadaDBConf{

    public String name;
    public String administrator;
    public String password;
    public String sql_driver;
    public String jdbc_url;
    public String relation_server;
    public String relation_port;
    public String db_root_dir;
    public String repository_root_dir;
    //attribute pour interface web
    public  String url_root;
    public  String context;
    //attribute pour systeme coordonnees
    public String system;
    public String equinox;

    public String abcisseColumnType = "ENERGY";
    public String abcisseUnit = "eV";
    public String ordinateUnit;
    
    public void setNamedb(String name){
	this.name = name;
    }
    
    public String getNamedb(){
	return name;
    }
    
    public void setAdministrator(String administrator){
	this.administrator = administrator;
    }
    
    public String getAdministrator(){
	return this.administrator;
    }
    
    public void setPassword(String password){
	this.password = password;
    }
    
    public String getPassword(){
	return this.password;
    }
    
    public void setSql_driver(String sql_driver){
	this.sql_driver = sql_driver;
    }
    
    public  String getSql_driver(){
	return this.sql_driver;
    }
    
    public void setJdbc_url(String jdbc_url){
	this.jdbc_url = jdbc_url;
    }
    
    public String getJdbc_url(){
	return jdbc_url;
    }
    
    public void setDb_root_dir(String db_root_dir){
    	/*
    	 * windows path filtering
    	 */
     	this.db_root_dir = db_root_dir.replaceAll("(\\\\|\\/)+", "@");
    	this.db_root_dir = this.db_root_dir.replaceAll("@ ", " ");
    	this.db_root_dir = this.db_root_dir.replaceAll("@", "\\" + System.getProperty("file.separator"));
    }
    
    public String getDb_root_dir(){
	return this.db_root_dir;
    }
    
    public void setRepository_root_dir(String repository_root_dir){
	this.repository_root_dir = repository_root_dir;
    }
    
    public String getRepository_root_dir(){
	return this.repository_root_dir;
    }
    
    public void setRelation_server(String server_name){
	this.relation_server = server_name;
    }
    
    public String getRelation_server(){
	return this.relation_server;
    }
    
    public void setRelation_port(String relation_port){
	this.relation_port = relation_port;
    }
    
    public String getRelation_port(){
	return this.relation_port;
    }
    
    public void setUrl_root(String url_root){
	this.url_root = url_root;
    }
    
    public String getUrl_root(){
	return this.url_root;
    }
    
    public void setContext(String context){
	this.context = context;
    }
    
    public String getContext(){
	return this.context;
    }
    
    public void setSystem(String system){
	this.system = system;
    }
    
    public String getSystem(){
	return this.system;
    }
    
    public void setEquinox(String equinox){
	this.equinox = equinox;
    }
    
    public String getEquinox(){
	return this.equinox;
    }
    
    public String getAbcisseColumnType(){
	return abcisseColumnType;
    }
    
    public void setAbcisseColumnType(String abcisseColumnType){
	this.abcisseColumnType = abcisseColumnType;
    }

    public String getAbcisseUnit(){
	return abcisseUnit;
    }
    
    public void setAbcisseUnit(String abcisseUnit){
	this.abcisseUnit = abcisseUnit;
    }

    public String getOrdinateUnit(){
	return ordinateUnit;
    }
    
    public void setOrdinateUnit(String ordinateUnit){
	this.ordinateUnit = ordinateUnit;
    }
}
  
