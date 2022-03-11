package eu.europa.ec.digit.cmisrepo.test.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

/**
 * @author bentsth
 */
public class PdfDocUtil {

    public static void main(String[] args) throws Exception {
//    	createSimpleDoc(new File("./resources/test_doc.pdf"), 1);

    	PDDocument cDoc = PdfDocUtil.getPdfDoc(20);
		ByteArrayOutputStream cOut = new ByteArrayOutputStream();
		cDoc.save(cOut);
		System.out.println(cOut.toByteArray().length);
		cOut.close();
    } 

    public static void createSimpleDoc(File file, int pageCount) throws Exception {
        PDDocument cDoc = getPdfDoc(pageCount);
        cDoc.save(file);
        cDoc.close();
    }
    
    public static String[] getPages(int pageCount) {
        String[] acPages = new String[pageCount];
        for (int i = 0; i < pageCount; i++) {
            StringBuffer cBuf = new StringBuffer();
            for (int j = 0; j < 25; j++) {
                cBuf.append(MockupUtil.getRandomSentence(20));
            }
            acPages[i] = cBuf.toString();
        }  
        
        return acPages;
    }
    
    public static PDDocument getPdfDoc(int pageCount) throws Exception {
        String[] acPages = getPages(pageCount);
        return createPdfDoc(acPages);
    }

    public static PDDocument createPdfDoc(String[] a_cText) throws Exception {
        PDDocument cDocument = new PDDocument();
        PDFont cFont = PDTrueTypeFont.loadTTF(cDocument, new File("./resources/arialbd.ttf" ) );

        for (int i = 0; i < a_cText.length; i++) {
            PDPage cPage = new PDPage();
            cDocument.addPage(cPage);

            PDPageContentStream cContentStream = new PDPageContentStream(cDocument, cPage);
            cContentStream.setFont(cFont, 8F);

            LinkedList<String> cLstWords = new LinkedList<String>(Arrays.asList(a_cText[i].split(" ")));

            int nPosY = 730;
            while (!cLstWords.isEmpty()) {
                StringBuffer cBuf = new StringBuffer();
                while (!cLstWords.isEmpty() && cBuf.length() < 100) {
                    cBuf.append(cLstWords.removeFirst()).append(' ');
                }

                cContentStream.beginText();
                cContentStream.moveTextPositionByAmount(30, nPosY);
                cContentStream.drawString(cBuf.toString());
                cContentStream.endText();
                nPosY -= 17;
            }

            cContentStream.close();
        }
        
        return cDocument;
    }
}
