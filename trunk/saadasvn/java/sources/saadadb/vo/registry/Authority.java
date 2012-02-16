package saadadb.vo.registry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Authority;

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
	
	private static Authority authorityInstance;
	
	private Authority() {
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
		this.authTitle = authTitle;
	}
	public String getAuthIdentifier() {
		return authIdentifier;
	}
	public void setAuthIdentifier(String authdentifier) {
		this.authIdentifier = authdentifier;
	}
	public String getAuthShortName() {
		return authShortName;
	}
	public void setAuthOrg(String authOrg) {
		this.authOrg = authOrg;
	}
	public String getAuthOrg() {
		return authOrg;
	}
	public void setAuthShortName(String shortName) {
		this.authShortName = shortName;
	}
	public String getCurationPublisher() {
		return curationPublisher;
	}
	public void setCurationPublisher(String curationPublisher) {
		this.curationPublisher = curationPublisher;
	}
	public String getCurationName() {
		return curationName;
	}
	public void setCurationName(String curationName) {
		this.curationName = curationName;
	}
	public String getCurationLogo() {
		return curationLogo;
	}
	public void setCurationLogo(String curationLogo) {
		this.curationLogo = curationLogo;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactAdresse() {
		return contactAdresse;
	}
	public void setContactAdresse(String contactAdresse) {
		this.contactAdresse = contactAdresse;
	}
	public String getContactMail() {
		return contactMail;
	}
	public void setContactMail(String contactMail) {
		this.contactMail = contactMail;
	}
	public String getContactTel() {
		return contactTel;
	}
	public void setContactTel(String contactTel) {
		this.contactTel = contactTel;
	}
	public String getContentSubject() {
		return contentSubject;
	}
	public void setContentSubject(String contentSubject) {
		this.contentSubject = contentSubject;
	}
	public String getContentRefURL() {
		return contentRefURL;
	}
	public void setContentRefURL(String contentRefURL) {
		this.contentRefURL = contentRefURL;
	}
	public String getContentDescription() {
		return contentDescription;
	}
	public void setContentDescription(String contentDescription) {
		this.contentDescription = contentDescription;
	}

	public String getXML() {
		return
		  "<title>" + this.getAuthTitle() + "</title>"
		+ "<shortName>" + this.getAuthShortName() + "</shortName>"
		+ "<managingOrg>" + this.getAuthOrg() + "</managingOrg>"
		+ "<identifier>" + this.getAuthIdentifier() + "</identifier>"
		+ "<curation>"
		+ "    <publisher>" + this.curationPublisher + "</publisher>"
		+ "    <creator>"
		+ "        <name>" + this.getCurationName() + "</name>"
		+ "        <logo>" + this.getCurationLogo() + "</logo>"
		+ "    </creator>"
		+ "    <contact>"
		+ "        <name>" + this.getContactName() + "</name>"
		+ "        <address>" + this.getContactAdresse() + "</address>"
		+ "        <email>" + this.getContactMail() + "</email>"
		+ "        <telephone>" + this.getContactTel() + "</telephone>"
		+ "    </contact>"
		+ "</curation>"
		+ "<content>"
		+ "    <subject>" + this.getContentSubject() + "</subject>"
		+ "    <description>" + this.getContentDescription() + "</description>"
		+ "    <referenceURL>" + this.getContentRefURL() + "</referenceURL>"
		+ "</content>";
		
	}

	@Override
	public void create(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(ArgsParser ap) throws SaadaException {
	}

	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void index(ArgsParser ap) throws SaadaException {
	}

	@Override
	public void comment(ArgsParser ap) throws SaadaException {
	}
	
	public static void main(String[] args) throws Exception {
		Database.init("ThreeXMM");
		Authority a = Authority.getInstance();
		a.setAuthShortName("moi");
		SQLTable.beginTransaction();
		Table_Saada_VO_Authority.createTable(a);
		SQLTable.commitTransaction();
	}
}
