/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpsf.extractor;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.*;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

public final class TestHPSFPropertiesExtractor {
	private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();

	@Test
	public void testNormalProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestMickey.doc"));
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
    		// Check each bit in turn
    		String summary = ext.getSummaryInformationText();
    		String docSummary = ext.getDocumentSummaryInformationText();

    		assertContains(summary, "TEMPLATE = Normal");
    		assertContains(summary, "SUBJECT = sample subject");
    		assertContains(docSummary, "MANAGER = sample manager");
    		assertContains(docSummary, "COMPANY = sample company");
    
    		// Now overall
    		String text = ext.getText();
    		assertContains(text, "TEMPLATE = Normal");
    		assertContains(text, "SUBJECT = sample subject");
    		assertContains(text, "MANAGER = sample manager");
    		assertContains(text, "COMPANY = sample company");
		} finally {
		    ext.close();
		}
	}

	@Test
	public void testNormalUnicodeProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestUnicode.xls"));
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
    		// Check each bit in turn
    		String summary = ext.getSummaryInformationText();
    		String docSummary = ext.getDocumentSummaryInformationText();
    
    		assertContains(summary, "AUTHOR = marshall");
    		assertContains(summary, "TITLE = Titel: \u00c4h");
    		assertContains(docSummary, "COMPANY = Schreiner");
    		assertContains(docSummary, "SCALE = false");
    
    		// Now overall
    		String text = ext.getText();
    		assertContains(text, "AUTHOR = marshall");
    		assertContains(text, "TITLE = Titel: \u00c4h");
    		assertContains(text, "COMPANY = Schreiner");
    		assertContains(text, "SCALE = false");
		} finally {
		    ext.close();
		}
	}

	@Test
	public void testCustomProperties() throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(
				_samples.openResourceAsStream("TestMickey.doc")
		);
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
		try {
    		// Custom properties are part of the document info stream
    		String dinfText = ext.getDocumentSummaryInformationText();
    		assertContains(dinfText, "Client = sample client");
    		assertContains(dinfText, "Division = sample division");
    
    		String text = ext.getText();
    		assertContains(text, "Client = sample client");
    		assertContains(text, "Division = sample division");
        } finally {
            ext.close();
        }
	}

	@Test
	public void testConstructors() throws IOException {
		POIFSFileSystem fs;
		HSSFWorkbook wb;
		try {
			fs = new POIFSFileSystem(_samples.openResourceAsStream("TestUnicode.xls"));
			wb = new HSSFWorkbook(fs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ExcelExtractor excelExt = new ExcelExtractor(wb);

		final String fsText;
		HPSFPropertiesExtractor fsExt = new HPSFPropertiesExtractor(fs);
		fsExt.setFilesystem(null); // Don't close re-used test resources!
		try {
		    fsText = fsExt.getText();
		} finally {
		    fsExt.close();
		}
        
        final String hwText; 
		HPSFPropertiesExtractor hwExt = new HPSFPropertiesExtractor(wb);
		hwExt.setFilesystem(null); // Don't close re-used test resources!
		try {
		    hwText = hwExt.getText();
		} finally {
		    hwExt.close();
		}
		
        final String eeText;
        HPSFPropertiesExtractor eeExt = new HPSFPropertiesExtractor(excelExt);
        eeExt.setFilesystem(null); // Don't close re-used test resources!
        try {
            eeText = eeExt.getText();
        } finally {
            eeExt.close();
            wb.close();
        }

		assertEquals(fsText, hwText);
		assertEquals(fsText, eeText);

		assertContains(fsText, "AUTHOR = marshall");
		assertContains(fsText, "TITLE = Titel: \u00c4h");
	}

	@Test
	public void test42726() throws IOException {
		HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(HSSFTestDataSamples.openSampleWorkbook("42726.xls"));
		try {
    		String txt = ext.getText();
    		assertContains(txt, "PID_AUTHOR");
    		assertContains(txt, "PID_EDITTIME");
    		assertContains(txt, "PID_REVNUMBER");
    		assertContains(txt, "PID_THUMBNAIL");
        } finally {
            ext.close();
        }
	}

	@Test
    public void testThumbnail() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestThumbnail.xls"));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        Thumbnail thumbnail = new Thumbnail(wb.getSummaryInformation().getThumbnail());
        assertEquals(-1, thumbnail.getClipboardFormatTag());
        assertEquals(3, thumbnail.getClipboardFormat());
        assertNotNull(thumbnail.getThumbnailAsWMF());
        wb.close();
    }

	@Test
    public void test52258() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(_samples.openResourceAsStream("TestVisioWithCodepage.vsd"));
        HPSFPropertiesExtractor ext = new HPSFPropertiesExtractor(fs);
        try {
            assertNotNull(ext.getDocSummaryInformation());
            assertNotNull(ext.getDocumentSummaryInformationText());
            assertNotNull(ext.getSummaryInformation());
            assertNotNull(ext.getSummaryInformationText());
            assertNotNull(ext.getText());
        } finally {
            ext.close();
        }
    }

	@Test
	public void test61300Extractor() throws NoPropertySetStreamException, MarkUnsupportedException, IOException {
		HPSFPropertiesExtractor.main(new String[]{
				POIDataSamples.getPOIFSInstance().getFile("61300.bin").getAbsolutePath()
		});
	}
}
