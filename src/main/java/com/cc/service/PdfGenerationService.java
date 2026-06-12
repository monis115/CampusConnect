package com.cc.service;

import com.cc.modals.AccountantCredit;
import com.cc.repository.AccountantCreditRepository;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

        @Autowired
        private AccountantCreditRepository accountantCreditRepository;

        // Brand colors
        private static final Color BRAND_NAVY = new DeviceRgb(15, 23, 42);
        private static final Color BRAND_SLATE = new DeviceRgb(71, 85, 105);
        private static final Color BRAND_LIGHT = new DeviceRgb(248, 250, 252);
        private static final Color ACCENT_GREEN = new DeviceRgb(16, 185, 129);
        private static final Color ACCENT_GREEN_DARK = new DeviceRgb(6, 95, 70);
        private static final Color BORDER_COLOR = new DeviceRgb(226, 232, 240);
        private static final Color ROW_ALT = new DeviceRgb(248, 250, 252);

        public byte[] generateClosureCertificate(Long creditId) throws Exception {
                AccountantCredit credit = accountantCreditRepository.findById(creditId)
                                .orElseThrow(() -> new IllegalArgumentException("Credit not found"));

                if (!"CLOSED".equals(credit.getStatus()) && !"FORECLOSED".equals(credit.getStatus())) {
                        throw new IllegalStateException("Cannot generate closure certificate for active loans.");
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4);
                document.setMargins(40, 50, 40, 50);

                // ═══════════════════════════════════════════════════════════
                // TOP ACCENT BAR (like a bank letterhead stripe)
                // ═══════════════════════════════════════════════════════════
                Table topBar = new Table(UnitValue.createPercentArray(new float[] { 100 })).useAllAvailableWidth();
                Cell barCell = new Cell()
                                .setBackgroundColor(BRAND_NAVY)
                                .setHeight(6)
                                .setBorder(Border.NO_BORDER);
                topBar.addCell(barCell);
                document.add(topBar);
                document.add(new Paragraph("\n").setFontSize(4));

                // ═══════════════════════════════════════════════════════════
                // HEADER SECTION
                // ═══════════════════════════════════════════════════════════
                Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 70, 30 }))
                                .useAllAvailableWidth();
                headerTable.setBorder(Border.NO_BORDER);

                // Left: Company Name + Tagline
                Cell leftHeader = new Cell().setBorder(Border.NO_BORDER);
                leftHeader.add(new Paragraph("EDUPORTAL")
                                .setFontSize(22)
                                .setFontColor(BRAND_NAVY)
                                .setMarginBottom(2));
                leftHeader.add(new Paragraph("by Rare Education Pvt. Ltd.")
                                .setFontSize(10)
                                .setFontColor(BRAND_SLATE)
                                .setMarginBottom(4));
                leftHeader.add(new Paragraph("Financial Services Division")
                                .setFontSize(9)
                                .setFontColor(BRAND_SLATE));
                headerTable.addCell(leftHeader);

                // Right: Document Reference
                Cell rightHeader = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
                rightHeader.add(new Paragraph("CERTIFICATE")
                                .setFontSize(11)
                                .setFontColor(BRAND_SLATE)
                                .setMarginBottom(4));
                rightHeader.add(new Paragraph("Ref: CC/NOC/" + LocalDate.now().getYear() + "/"
                                + String.format("%05d", credit.getId()))
                                .setFontSize(9)
                                .setFontColor(BRAND_SLATE)
                                .setMarginBottom(2));
                rightHeader.add(new Paragraph(
                                "Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                                .setFontSize(9)
                                .setFontColor(BRAND_SLATE));
                headerTable.addCell(rightHeader);

                document.add(headerTable);

                // Divider line
                SolidLine solidLine = new SolidLine(1.5f);
                solidLine.setColor(BRAND_NAVY);
                LineSeparator ls = new LineSeparator(solidLine);
                ls.setMarginTop(12);
                ls.setMarginBottom(20);
                document.add(ls);

                // ═══════════════════════════════════════════════════════════
                // CERTIFICATE TITLE
                // ═══════════════════════════════════════════════════════════
                String titleText = "FORECLOSED".equals(credit.getStatus())
                                ? "FORECLOSURE SETTLEMENT CERTIFICATE"
                                : "NO DUES / CLOSURE CERTIFICATE";

                Paragraph title = new Paragraph(titleText)
                                .setFontSize(16)
                                .setFontColor(BRAND_NAVY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(6);
                document.add(title);

                // Decorative underline
                Table underline = new Table(UnitValue.createPercentArray(new float[] { 35, 30, 35 }))
                                .useAllAvailableWidth();
                underline.addCell(new Cell().setBorder(Border.NO_BORDER));
                Cell lineCell = new Cell().setBorder(Border.NO_BORDER)
                                .setBorderBottom(new SolidBorder(ACCENT_GREEN, 2))
                                .setHeight(1);
                underline.addCell(lineCell);
                underline.addCell(new Cell().setBorder(Border.NO_BORDER));
                underline.setMarginBottom(24);
                document.add(underline);

                // ═══════════════════════════════════════════════════════════
                // BODY TEXT
                // ═══════════════════════════════════════════════════════════
                String creditDateStr = credit.getCreditDate() != null
                                ? credit.getCreditDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                                : "N/A";
                String amountStr = credit.getAmount() != null
                                ? "₹" + credit.getAmount().setScale(2, RoundingMode.HALF_UP).toString()
                                : "₹0.00";
                String methodLabel = getMethodLabel(credit);

                Paragraph toWhom = new Paragraph("TO WHOMSOEVER IT MAY CONCERN")
                                .setFontSize(11)
                                .setFontColor(BRAND_NAVY)
                                .setMarginBottom(16);
                document.add(toWhom);

                String bodyPara1 = String.format(
                                "This is to certify that the credit/loan facility bearing Reference No. CR-%d, " +
                                                "originally disbursed on %s for a principal amount of %s through %s, " +
                                                "has been fully settled and the account has been officially closed.",
                                credit.getId(), creditDateStr, amountStr, methodLabel);
                document.add(new Paragraph(bodyPara1)
                                .setFontSize(11)
                                .setFontColor(BRAND_SLATE)
                                .setTextAlignment(TextAlignment.JUSTIFIED)
                                .setMarginBottom(12));

                String statusDesc = "FORECLOSED".equals(credit.getStatus())
                                ? "The borrower has opted for early settlement (foreclosure) of the above loan. " +
                                                "All outstanding principal, accrued interest, and applicable foreclosure charges have been received in full."
                                : "All dues including principal, interest, and any applicable charges have been fully received. "
                                                +
                                                "There are no outstanding balances, penalties, or liabilities remaining against this account.";

                document.add(new Paragraph(statusDesc)
                                .setFontSize(11)
                                .setFontColor(BRAND_SLATE)
                                .setTextAlignment(TextAlignment.JUSTIFIED)
                                .setMarginBottom(20));

                // ═══════════════════════════════════════════════════════════
                // ACCOUNT SUMMARY TABLE
                // ═══════════════════════════════════════════════════════════
                document.add(new Paragraph("ACCOUNT SUMMARY")
                                .setFontSize(11)
                                .setFontColor(BRAND_NAVY)
                                .setMarginBottom(10));

                Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 45, 55 }))
                                .useAllAvailableWidth()
                                .setMarginBottom(24);
                summaryTable.setBorder(new SolidBorder(BORDER_COLOR, 1));

                addSummaryRow(summaryTable, "Loan Reference No.", "CR-" + credit.getId(), true);
                addSummaryRow(summaryTable, "Disbursement Date", creditDateStr, false);
                addSummaryRow(summaryTable, "Principal Amount", amountStr, true);
                addSummaryRow(summaryTable, "Total Amount Repaid",
                                "₹" + (credit.getAmountRepaid() != null
                                                ? credit.getAmountRepaid().setScale(2, RoundingMode.HALF_UP)
                                                : "0.00"),
                                false);
                addSummaryRow(summaryTable, "Interest Rate",
                                (credit.getInterestRate() != null ? credit.getInterestRate() + "%" : "N/A"),
                                true);
                addSummaryRow(summaryTable, "Credit Source", methodLabel, false);
                addSummaryRow(summaryTable, "Tenure",
                                (credit.getLoanTenureMonths() != null ? credit.getLoanTenureMonths() + " months"
                                                : "N/A"),
                                true);
                addSummaryRow(summaryTable, "Final Status",
                                "FORECLOSED".equals(credit.getStatus()) ? "FORECLOSED (Settled)"
                                                : "CLOSED (Fully Paid)",
                                false);

                document.add(summaryTable);

                // ═══════════════════════════════════════════════════════════
                // CONFIRMATION BLOCK (Green box)
                // ═══════════════════════════════════════════════════════════
                Table confirmBox = new Table(UnitValue.createPercentArray(new float[] { 100 })).useAllAvailableWidth();
                Cell confirmCell = new Cell()
                                .setBackgroundColor(new DeviceRgb(236, 253, 245))
                                .setBorder(new SolidBorder(ACCENT_GREEN, 1))
                                .setPadding(16);
                confirmCell.add(new Paragraph("✓  ACCOUNT CLOSURE CONFIRMED")
                                .setFontSize(12)
                                .setFontColor(ACCENT_GREEN_DARK)
                                .setMarginBottom(6));
                confirmCell.add(new Paragraph(
                                "We hereby confirm that the above-mentioned loan account stands CLOSED with NIL outstanding balance. "
                                                +
                                                "No further claims shall be made against this account.")
                                .setFontSize(10)
                                .setFontColor(ACCENT_GREEN_DARK)
                                .setTextAlignment(TextAlignment.JUSTIFIED));
                confirmBox.addCell(confirmCell);
                confirmBox.setMarginBottom(30);
                document.add(confirmBox);

                // ═══════════════════════════════════════════════════════════
                // SIGNATORY SECTION
                // ═══════════════════════════════════════════════════════════
                Table sigTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
                sigTable.setBorder(Border.NO_BORDER);

                Cell sigLeft = new Cell().setBorder(Border.NO_BORDER).setPaddingTop(30);
                sigLeft.add(new Paragraph("________________________")
                                .setFontSize(10).setFontColor(BRAND_SLATE));
                sigLeft.add(new Paragraph("Authorized Signatory")
                                .setFontSize(9).setFontColor(BRAND_SLATE).setMarginTop(4));
                sigLeft.add(new Paragraph("Campus Connect Financial Services")
                                .setFontSize(8).setFontColor(BRAND_SLATE));
                sigTable.addCell(sigLeft);

                Cell sigRight = new Cell().setBorder(Border.NO_BORDER).setPaddingTop(30)
                                .setTextAlignment(TextAlignment.RIGHT);
                sigRight.add(new Paragraph("________________________")
                                .setFontSize(10).setFontColor(BRAND_SLATE));
                sigRight.add(new Paragraph("Accounts Department")
                                .setFontSize(9).setFontColor(BRAND_SLATE).setMarginTop(4));
                sigRight.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                                .setFontSize(8).setFontColor(BRAND_SLATE));
                sigTable.addCell(sigRight);

                document.add(sigTable);

                // ═══════════════════════════════════════════════════════════
                // FOOTER
                // ═══════════════════════════════════════════════════════════
                SolidLine footerLine = new SolidLine(0.5f);
                footerLine.setColor(BORDER_COLOR);
                LineSeparator footerSep = new LineSeparator(footerLine);
                footerSep.setMarginTop(30);
                footerSep.setMarginBottom(8);
                document.add(footerSep);

                document.add(new Paragraph(
                                "This is a system-generated document. For verification, contact accounts@campusconnect.in | Ref: "
                                                +
                                                "CC/NOC/" + LocalDate.now().getYear() + "/"
                                                + String.format("%05d", credit.getId()))
                                .setFontSize(7)
                                .setFontColor(new DeviceRgb(148, 163, 184))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(4));

                document.add(new Paragraph("© " + LocalDate.now().getYear()
                                + " Campus Connect by Rare Education Pvt. Ltd. All rights reserved.")
                                .setFontSize(7)
                                .setFontColor(new DeviceRgb(148, 163, 184))
                                .setTextAlignment(TextAlignment.CENTER));

                document.close();
                return baos.toByteArray();
        }

        private String getMethodLabel(AccountantCredit credit) {
                if ("FRIEND".equalsIgnoreCase(credit.getMethod())) {
                        return "Personal Loan from "
                                        + (credit.getFriendName() != null ? credit.getFriendName() : "N/A");
                } else if ("LOAN".equalsIgnoreCase(credit.getMethod())) {
                        return "Bank Loan — " + (credit.getBankName() != null ? credit.getBankName() : "N/A");
                } else if ("CREDIT_CARD".equalsIgnoreCase(credit.getMethod())) {
                        return "Credit Card — " + (credit.getCardInfo() != null ? credit.getCardInfo() : "N/A");
                }
                return credit.getMethod();
        }

        private void addSummaryRow(Table table, String label, String value, boolean isOdd) {
                Color bgColor = isOdd ? ROW_ALT : ColorConstants.WHITE;

                Cell labelCell = new Cell()
                                .add(new Paragraph(label).setFontSize(10).setFontColor(BRAND_SLATE))
                                .setBackgroundColor(bgColor)
                                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                                .setPadding(10)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                table.addCell(labelCell);

                Cell valueCell = new Cell()
                                .add(new Paragraph(value != null ? value : "N/A").setFontSize(10)
                                                .setFontColor(BRAND_NAVY))
                                .setBackgroundColor(bgColor)
                                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                                .setPadding(10)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);
                table.addCell(valueCell);
        }
}
