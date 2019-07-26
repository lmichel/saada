package junit;

import static org.junit.Assert.*;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import saadadb.dataloader.testprov.ProductFileReport;

public class VizierTest_qso21cm {
	static final String fileName = "qso21cm.fits";
	static final String dirName =  "/home/michel/Desktop/datasample/vizier/";
	
	
	static final String[] args = {
			"-filename=" + ReportUtils.getFilename(dirName, fileName),
					"-collection=Foo",
					"-category=image",
					"-obscollection='TEST'",
					"-classfusion=im_TEST",
					"-noindex",
					"-repository=no",
					"-obsmapping=first",
					"-polarmapping=first",
					"-posmapping=first",
					"-spcmapping=first",
					"-timemapping=first",
					"-name=im_TEST_qso21cm.fits",
					"-position='274.86061408,38.7504962'",
					"-system='ICRS'",
					"-target='QSO J1819+3845'",
					"-tmin='54969'",
					"-spcrespower='Infinity'",
					"-instrument=INSTRUME",
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
