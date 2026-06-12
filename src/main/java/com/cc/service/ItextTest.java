package com.cc.service;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;

public class ItextTest {
    public void test() throws Exception {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont italic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
        Paragraph p = new Paragraph("Hello").setFont(bold);
    }
}
