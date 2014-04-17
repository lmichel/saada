-------------------------------------------------
Version Saada 2.0

L. MICHEL
April 2014
-------------------------------------------------

---- New data model based on Obscore ---------
Includes new dataloder parameters

---- DB connection spooler ----------
Connection spooler used everywhere

---- Spatial indexation -------------
Use of healpix in place of QBox
Upgrade tool available

---- Meta data editing --------------
Extended attributes can be edited from the Meta panel of the admin tool

---- MySQL -----------
Make the relationship table in mode ROW_WIDTH=FIXED.
Indexes are built much more faster

---- Jars ----------------
upgrade of apache.io from 1 to 2.4

---- SQLIte --------------
Procedure in native C replaced with Java code

---- PostgresQL -----------
Version 9 dos not support old style escape sequence: \'
Uses '' instead

---- MySQL -----------
Support last version (5.7) which requires user name to be suffixed by @localhost

---- Java 7 ---------
Saada is configured to run with Java 1.7 (perfectly supported by JDK1.7)
For those who want to go back to 1.5 compatibility, just edit SAADA_HOME/dbtemplate/nin/javarun.properties 

---- New Admin tool ------------
Go in SAAADA_DB_HOME
run ./admintool

---- SaadaDB Upgrade -----------
Install the new Saada distrib
go in SAADA_HOME/bin
run ./upgrade

---- MAC  ----------------------

The Mac distrib has been tested on this configuration
Model Name:       iMacBookPro
Processor Name:   Intel Core 2 Duo
Processor Speed:  2.66 GHz
OS:               Leopard 10.6.7

---- XP/SP2 VISTA (7 not tested yet) -----------------

WARNING  SQLite is not supported by Saada on MS Windows

Saada works on the 2 plateforms with the condition to 
share with both postgres and tomcat (if exist) users 
the directories where are installed saada, the saadadbs 
and the reposities.
There is a quick note in the troubleshooting rubrique on 
the web site.

A database creation failure has been mentioned on a small
notebook (MSI Wind Notebook). The error occurs at the end
of the servlet compilation. 
In that case, go in SAADA_DB_HOME\bin and run:
sant servlet.compile
That will finish the install process.

---- Linux ------------------------

Sometime, Saada tools can not be spawned with a double click on
a file explorer. In that case, run tools from the command line.

---- SaadaDB Upgrade --------------


From Saada1.5.0 to higher: 
* Install Saada1.5.0.x in NEW_SAADA_HOME
* Copy NEW_SAADA_HOME/dbtemplate/lib/saadadb.jar in SAADA_DB_HOME/lib
* Copy NEW_SAADA_HOME/dbtemplate/bin/build.xml in SAADA_DB_HOME/bin
* Copy NEW_SAADA_HOME/dbtemplate/java/sources/servlet/* in SAADA_DB_HOME/java/sources/servlet
* Run "./sant servlet.rebuild" from SAADA_DB_HOME/java/bin
* Deploy the Web app. 


From Saada1.5.beta3 to Saada1.5.0.x:
* Install Saada1.5.0.x
* Build a dummy SaadaDB (e.g. database DUMMY locted in DUMMY_HOME)
* Make the following copies
   NEW_SAADA_HOME/dbtemplate/java to  SAADA_DB_HOME
   NEW_SAADA_HOME/dbtemplate/lib/saadadb_jar to  SAADA_DB_HOME/lib
* Rebuild the Web interface
	cp -R NEW_SAADA_HOME/dbtemplate/web/* SAADA_DB_HOME/web
	cp SAADA_DB_HOME/config/saadadb.xml NEW_SAADA_HOME/config
	cd NEW_SAADA_HOME/bin
	./sant saadadb.webapp.again
* Update the DB trap:
	copy DUMMY_HOME/class_mapping/SaadaDBSTrap.java to SAADA_DB_HOME/class_mapping/
	edit SAADA_DB_HOME/class_mapping/SaadaDBSTrap.java
	Set the right DB name in the first line (package...)
	Update the line "jdbc_....." with the values read in SAADA_DB_HOME/config/saadadb.xml
	go to SAADA_DB_HOME/bin
	run ./sant  saadadb.webapp.again
* Deploy the Web app. 
* Remove the old Saada install for safety


