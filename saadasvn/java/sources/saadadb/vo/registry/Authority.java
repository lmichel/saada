package saadadb.vo.registry;

import java.net.UnknownHostException;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Authority;
import saadadb.util.Messenger;

/**
 * Modeler of the VO authority
 * @author laurent
 * @version $Id$
 *
 */
public class Authority extends EntityManager{
	private String authTitle;
	private String authIdentifier;
	private String authShortName;
	private String authOrg;
	private String curationPublisher;
	private String curationName;
	private String curationLogo;
	private String contactName;
	private String contactAdresse;
	private String contactMail;
	private String contactTel;
	private String contentSubject;
	private String contentRefURL;
	private String contentDescription;
	private String contentType;
	private String contentLevel;

	
	private static Authority authorityInstance;
	
	Authority() {
	}
	
	public static Authority getInstance() {
		if( authorityInstance == null ){
			authorityInstance = new Authority();
		}
		return authorityInstance;
	}
	
	public String getAuthTitle() {
		return authTitle;
	}
	public void setAuthTitle(String authTitle) {
		if( authTitle== null || "null".equalsIgnoreCase(authTitle)){
			this.authTitle = "SaadaDB " + Database.getDbname();
		} else {
			this.authTitle = authTitle;
		}
	}
	public String getAuthIdentifier() {
		return authIdentifier;
	}
	public void setAuthIdentifier(String authIdentifier) {
		if( authIdentifier== null || "null".equalsIgnoreCase(authIdentifier)){
			this.authIdentifier = "ivo://";
		} else {
			this.authIdentifier = authIdentifier;
		}
	}
	public String getAuthShortName() {
		return authShortName;
	}
	public void setAuthOrg(String authOrg) {
		if( authOrg== null || "null".equalsIgnoreCase(authOrg)){
			this.authOrg = "";
		} else {
			this.authOrg = authOrg;
		}
	}
	public String getAuthOrg() {
		return authOrg;
	}
	public void setAuthShortName(String authShortName) {
		if( authShortName== null || "null".equalsIgnoreCase(authShortName)){
			this.authShortName = "SaadaDB " + Database.getDbname();
		} else {
			this.authShortName = authShortName;
		}
	}
	public String getCurationPublisher() {
		return curationPublisher;
	}
	public void setCurationPublisher(String curationPublisher) {
		if( curationPublisher== null || "null".equalsIgnoreCase(curationPublisher)){
			this.curationPublisher = "";
		} else {
			this.curationPublisher = curationPublisher;
		}
	}
	public String getCurationName() {
		return curationName;
	}
	public void setCurationName(String curationName) {
		if( curationName== null || "null".equalsIgnoreCase(curationName)){
			this.curationName = "";
		} else {
			this.curationName = curationName;
		}
	}
	public String getCurationLogo() {
		return curationLogo;
	}
	public void setCurationLogo(String curationLogo) {
		if( curationLogo== null || "null".equalsIgnoreCase(curationLogo)){
			this.curationLogo = "http://code.google.com/p/saada/logo?cct=1311864309";
		} else {
			this.curationLogo = curationLogo;
		}
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		if( contactName== null || "null".equalsIgnoreCase(contactName)){
			this.contactName = "";
		} else {
			this.contactName = contactName;
		}
	}
	public String getContactAdresse() {
		return contactAdresse;
	}
	public void setContactAdresse(String contactAdresse) {
		if( contactAdresse== null || "null".equalsIgnoreCase(contactAdresse)){
			this.contactAdresse = "";
		} else {
			this.contactAdresse = contactAdresse;
		}
	}
	public String getContactMail() {
		return contactMail;
	}
	public void setContactMail(String contactMail) {
		if( contactMail== null || "null".equalsIgnoreCase(contactMail)){
			try {
				this.contactMail = System.getProperty("user.name")  + "@" +  java.net.InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {}
		} else {
			this.contactMail = contactMail;
		}
	}
	public String getContactTel() {
		return contactTel;
	}
	public void setContactTel(String contactTel) {
		if( contactTel== null || "null".equalsIgnoreCase(contactTel)){
			this.contactTel = "";
		} else {
			this.contactTel = contactTel;
		}
	}
	public String getContentSubject() {
		return contentSubject;
	}
	public void setContentSubject(String contentSubject) {
		if( contentSubject== null || "null".equalsIgnoreCase(contentSubject)){
			this.contentSubject = "Content of the SaadaDB " + Database.getDbname();;
		} else {
			this.contentSubject = contentSubject;
		}
	}
	public String getContentRefURL() {
		return contentRefURL;
	}
	public void setContentRefURL(String contentRefURL) {
		if( contentRefURL== null || "null".equalsIgnoreCase(contentRefURL)){
			this.contentRefURL = Database.getUrl_root();
		} else {
			this.contentRefURL = contentRefURL;
		}
	}
	public String getContentDescription() {
		return contentDescription;
	}
	public void setContentDescription(String contentDescription) {
		if( contentDescription== null || "null".equalsIgnoreCase(contentDescription)){
			this.contentDescription = "Content of the SaadaDB " + Database.getDbname();
		} else {
			this.contentDescription = contentDescription;
		}
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		if( contentType== null || "null".equalsIgnoreCase(contentType)){
			this.contentType = "";
		} else {
			this.contentType = contentType;
		}
	}
	public String getContentLevel() {
		return contentLevel;
	}
	public void setContentLevel(String contentLevel) {
		if( contentLevel== null || "null".equalsIgnoreCase(contentLevel)){
			this.contentLevel = "";
		} else {
			this.contentLevel = contentLevel;
		}
	}
	public String getXML() {
		return
		  "<title>" + this.getAuthTitle() + "</title>\n"
		+ "<shortName>" + this.getAuthShortName() + "</shortName>\n"
		+ "<managingOrg>" + this.getAuthOrg() + "</managingOrg>\n"
		+ "<identifier>" + this.getAuthIdentifier() + "</identifier>\n"
		+ "<curation>\n"
		+ "    <publisher>" + this.curationPublisher + "</publisher>"
		+ "    <creator>\n"
		+ "        <name>" + this.getCurationName() + "</name>\n"
		+ "        <logo>" + this.getCurationLogo() + "</logo>\n"
		+ "    </creator>\n"
		+ "    <contact>\n"
		+ "        <name>" + this.getContactName() + "</name>\n"
		+ "        <address>" + this.getContactAdresse() + "</address>\n"
		+ "        <email>" + this.getContactMail() + "</email>\n"
		+ "        <telephone>" + this.getContactTel() + "</telephone>\n"
		+ "    </contact>\n"
		+ "</curation>\n"
		+ "<content>\n"
		+ "    <subject>" + this.getContentSubject() + "</subject>\n"
		+ "    <description>" + this.getContentDescription() + "</description>\n"
		+ "    <referenceURL>" + this.getContentRefURL() + "</referenceURL>\n"
		+ "</content>\n";
	}
	public void load() throws QueryException {
		try {
			Table_Saada_VO_Authority.loadTable(Authority.getInstance());
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	@Override
	public void create(ArgsParser ap) throws QueryException {
		try {
			Table_Saada_VO_Authority.createTable(Authority.getInstance());
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	@Override
	public void empty(ArgsParser ap) throws QueryException {
		try {
			Table_Saada_VO_Authority.emptyTable();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try {
			Table_Saada_VO_Authority.removeTable();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		create(ap);		
	}
	@Override
	public void index(ArgsParser ap) throws SaadaException {}
	@Override
	public void comment(ArgsParser ap) throws SaadaException {}
	
	public static void main(String[] args) throws SaadaException {
		Database.init("ThreeXMM");
		SQLTable.beginTransaction();
		Authority.getInstance().remove(null);
		SQLTable.commitTransaction();
	}
}
