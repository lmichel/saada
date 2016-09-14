/*
 *                    Delegating cart download to an external service.
 *                    
 * This service allows to transfer to a remote service (neither you SaadaDB WEB server nor your web client)
 * all parameters he needs to handle the shopping cart facility.
 * That can be used for instance to feed a workflow with data selected within the SaadaDB 
 *                    
 * This feature is activated if the if the variable PeerCartClient is set (in JavaScript sense).
 * It must be uncommented and set with a string value giving the URL of the third part client.
 * A button "delegate" is than added to the shopping cart management window.
 * This button sends a POST request to the URL pointed by PeerCartClient with 2 parameters:
 * 	1) saadadburl  : The url of the UWS service returning the cart archive.
 * 	2) cartcontent : A JSON description of the cart content.
 * 
 * The PeerCartClient must act as an UWS (http://www.ivoa.net/Documents/UWS) client to download the archive. 
 * This asynchronous process runs in 3 steps. 
 * Each step returns an XML file with any job information :
 * 		- Job identifier (JOBID) :  XMLPath = ROOT/uws:job/uws:jobId
 *  	- Job status (JOBSTATUS) :  XMLPath = ROOT/uws:job/uws:phase. The status is usually EXECUTING, COMPLETED or ERROR. 
 *  	                            See the IVOA doc for more detail.
 *      - Job result (JOBRESULT) : The first instance of the XMLPath = ROOT/uws:job/uws:results/uws:result. The content of 
 *                                 this job is a URL from  where the ZIP archive can be downloaded
 * 
 *	Step 1) Run the archive building: Send a POST request to saadadburl with the following parameters
 * 		- PHASE=RUN
 *     	- FORMAT=json
 *      - CART=cartcontent (content of the cartcontent POST parameter)
 *  	This request initiates the archive building. From that point, the client must wait on the job completion.
 *  
 *	Step 2) Check the job status: Send a request to saadadburl/JOBID and take the JOBSTATUS out from the answer.
 *          If the status neither EXECUTING nor COMPLETED, something wrong happened. The reason might be found out 
 *          in the XML file.
 *	Step 3) Download the ZIP archive when the JOB is COMPLETE: Just get the data returned by JOBRESULT
 *
 * The job can be killed a any time by sending a DELETE HTTP request to saadadburl/JOBID
 *  
 * WARNING 1: The cart management is HTTP session AWARE. WEB browser must have coolkies enable. In any other cases
 * you have to make sure your client properly handle sessions (e.g. see  --keep-session-cookies option for wget).
 * The way to do so depends on the technology you are using (servlet, CGI, php....)
 * 
 * WARNING 2: There is no guaranty about what happens if the "Delegate" service is used  at the same time as the 
 * standard cart facility.
 * 
 * This feature can be tested with the resource /cartclient of the current Saadadb url.
 * It just prints out the content of the request parameters.
 * 
 * This service has been suggested by the GAIABOT team from the Observatoire de Paris (christophe.barache@obspm.fr)
 * L. MICHEL 12/2011
 * 
 */
// Uncomment to enable feature 
//PeerCartClient = 'http://xcatdb.u-strasbg.fr/saadasvn/cartclient';