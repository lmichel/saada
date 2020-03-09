package junit;

import static org.junit.Assert.*;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import saadadb.dataloader.testprov.ProductFileReport;

public class VizierTest_id12_gal_survey {
	static final String fileName = "id12_GAL-Survey+50.fpsf.fits";
	static final String dirName =  "/home/michel/Desktop/datasample/vizier/";
	
	
	static final String[] args = {
			"-filename=" + ReportUtils.getFilename(dirName, fileName),
					"-collection=Foo",
					"-category=image",
					"-obscollection='TEST'",
					"-classifier=im_TEST",
					"-noindex",
					"-repository=no",
					"-polarstates=POL",
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
