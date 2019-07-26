package junit;

import static org.junit.Assert.*;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import saadadb.dataloader.testprov.ProductFileReport;

public class VizierTest_f002 {
	static final String fileName = "f002.fit";
	static final String dirName =  "/home/michel/Desktop/datasample/vizier/";
	
	
	static final String[] args = {
			"-filename=" + ReportUtils.getFilename(dirName, fileName),
					"-obscollection='VIII/6'",
					"-repository=no",
					"-obsmapping=first",
					"-polarmapping=first",
					"-posmapping=first",
					"-spcmapping=first",
					"-timemapping=first",
					"-name=im_VIII_6_",
					"-target=OBJECT",
					"-tmin=DATE-OBS",
					"-spcunit='Hz'",
					"-instrument=INSTRUME",
					"-facility=TELESCOP",
					"-ukw",
					"bib_reference='1985AJ.....90.2540C'",
					"-ukw",
					"has_wcs='7'",
					"-ukw",
					"has_wcs='7'",
					"-category=image",
					"-collection=Foo",
					"-debug=on",
					"Vizier"
	         };
	static final String reportDir = ReportUtils.getReportFilename(dirName, fileName);
	static final String reportRefDir = ((new File(".")).getAbsolutePath()) + "/java/sources/junit/reports/" + fileName + ".txt";
	
	@Test
	public void testReport() {
		ProductFileReport.process(args);
		assertTrue(reportDir + " does not exist", (new File(reportDir)).exists());
		assertTrue(reportRefDir + " does not exist", (new File(reportDir)).exists());
		try {
			assertEquals("The files differ!", 
					ReportUtils.filterReportContent(FileUtils.readFileToString(new File(reportDir), "utf-8")), 
					ReportUtils.filterReportContent(FileUtils.readFileToString(new File(reportRefDir), "utf-8"))); 
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	


}
