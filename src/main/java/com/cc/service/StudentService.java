package com.cc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.DTO.StudentDTO;
import com.cc.modals.Agent;
import com.cc.modals.College;
import com.cc.modals.CompanyLogo;
import com.cc.modals.Location;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.repository.AgentRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.CompanyLogoRepository;
import com.cc.repository.LocationRepository;
import com.cc.repository.StudentRepository;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CompanyLogoRepository companyLogoRepository;

    // 🔹 Add Student with Validation
    @Transactional
    public Student addStudent(StudentDTO dto) {

        // Step 1: Fetch Agent
        Agent agent = agentRepository.findByUserId(dto.getUserNameAgent())
                .orElseThrow(() -> new RuntimeException("Agent not found with username: " + dto.getUserNameAgent()));

        // Step 2: Validate Location Access
        Location studentDistrict = locationRepository.findById(dto.getDistrictId().longValue())
                .orElseThrow(() -> new RuntimeException("District not found with ID: " + dto.getDistrictId()));

        boolean hasAccessToDistrict = agent.getAccessibleLocations().stream()
                .anyMatch(loc -> Objects.equals(loc.getId(), studentDistrict.getId()));

        if (!hasAccessToDistrict) {
            throw new RuntimeException("Access denied: Agent does not have permission for district " + studentDistrict.getDistrict());
        }

        // Step 3: Validate College Access
        List<College> assignedColleges = new ArrayList<>();
        
        if (dto.getCollegeIds() == null || dto.getCollegeIds().isEmpty()) {
            throw new RuntimeException("Validation Error: At least one college ID must be provided.");
        }

        for (Long collegeId : dto.getCollegeIds()) {
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new RuntimeException("College not found with ID: " + collegeId));

            boolean hasAccessToCollege = agent.getCollegePrivileges().stream()
                    .anyMatch(col -> Objects.equals(col.getId(), college.getId()));

            if (!hasAccessToCollege) {
                throw new RuntimeException("Access denied: Agent does not have permission for college " + college.getCollegeName());
            }

            assignedColleges.add(college);
        }

        // Step 4: Create Student Object
        Student student = new Student();
        student.setFullName(dto.getFullName());
        student.setDob(dto.getDob());
        student.setAge(dto.getAge());
        student.setEmail(dto.getEmail());
        student.setContactNo(dto.getContactNo());
        student.setAddress(dto.getAddress());
        student.setEmergencyContactNo(dto.getEmergencyContactNo());
        student.setEmergencyRelation(dto.getEmergencyRelation());
        student.setPassportNo(dto.getPassportNo());
        student.setPassportValidTill(dto.getPassportValidTill());
        student.setHighestDegree(dto.getHighestDegree());
        student.setMarks(dto.getMarks());
        student.setAppliedFor(dto.getAppliedFor());
        
        student.setCreatedAt(LocalDate.now());
        student.setApplicationStatus(dto.getApplicationStatus() != null ? dto.getApplicationStatus() : "Submitted");
        student.setPresentStatus(StudentStatus.REGISTERED);
        student.setNextStep(StudentStatus.UNDER_REVIEW);

        // Step 5: Assign College & Manager (ManyToOne)
        College primaryCollege = assignedColleges.get(0); 
        student.setCollege(primaryCollege);
        student.setAdmissionManager(primaryCollege.getAdmissionManager());

        // Step 6: Link Agent
        student.setAgent(agent);

        // === THIS IS THE TWO-STEP SAVE FIX ===
        
        // Step 7: Save the Student FIRST (without preferences)
        Student savedStudent = studentRepository.save(student);

        // Step 8: FLUSH the persistence context
        studentRepository.flush();

        // Step 9: Now set the ManyToMany relationship
        savedStudent.setCollegePreferences(assignedColleges);

        // Step 10: Save AGAIN
        savedStudent = studentRepository.save(savedStudent);

        // Step 11: Generate Confirmation Page PDF
        String confirmationFileName = "confirmation_" + savedStudent.getId() + "_" + UUID.randomUUID() + ".pdf";
        Path uploadPath = Paths.get("uploads/confirmation-pages");
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(confirmationFileName);
            generateConfirmationPdf(savedStudent, filePath.toString());
            savedStudent.setConfirmationPagePath("uploads/confirmation-pages/" + confirmationFileName);
            savedStudent = studentRepository.save(savedStudent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate confirmation page: " + e.getMessage());
        }

        return savedStudent;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ULTRA-MODERN AESTHETIC PDF GENERATION METHOD
    // ═══════════════════════════════════════════════════════════════════════════
    private void generateConfirmationPdf(Student student, String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Compact professional margins
        document.setMargins(35, 35, 35, 35);

        // ═══════════════════════════════════════════════════════════════════════════
        // 🎨 ELEGANT COLOR PALETTE
        // ═══════════════════════════════════════════════════════════════════════════
        com.itextpdf.kernel.colors.Color CHARCOAL = new com.itextpdf.kernel.colors.DeviceRgb(40, 40, 40);
        com.itextpdf.kernel.colors.Color DARK_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(70, 70, 70);
        com.itextpdf.kernel.colors.Color MID_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(120, 120, 120);
        com.itextpdf.kernel.colors.Color SOFT_GRAY_BG = new com.itextpdf.kernel.colors.DeviceRgb(248, 248, 250);
        com.itextpdf.kernel.colors.Color SECTION_BG = new com.itextpdf.kernel.colors.DeviceRgb(245, 247, 250);
        com.itextpdf.kernel.colors.Color BORDER_LIGHT = new com.itextpdf.kernel.colors.DeviceRgb(230, 232, 235);
        com.itextpdf.kernel.colors.Color ACCENT = new com.itextpdf.kernel.colors.DeviceRgb(41, 98, 255);
        
        // ═══════════════════════════════════════════════════════════════════════════
        // STYLISH HEADER WITH PROPER SPACING
        // ═══════════════════════════════════════════════════════════════════════════
        
        // Add top spacing
        document.add(new Paragraph("\n"));
        
        Table headerTable = new Table(new float[]{2, 5, 3});
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        headerTable.setMarginBottom(8);

        // Logo Section (Left)
        Cell logoCell = new Cell();
        logoCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        logoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
        try {
            CompanyLogo logoEntity = companyLogoRepository.findByName("RARE Education Logo");
            if (logoEntity != null && logoEntity.getLogoData() != null) {
                Image logo = new Image(ImageDataFactory.create(logoEntity.getLogoData()));
                logo.setAutoScale(true);
                logo.setMaxWidth(60);
                logo.setMaxHeight(40);
                logoCell.add(logo);
            } else {
                // Modern text logo
                Table logoTextTable = new Table(1);
                logoTextTable.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
                Cell logoTextCell = new Cell()
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setBackgroundColor(ACCENT)
                    .setPadding(4)
                    .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(3));
                
                logoTextCell.add(new Paragraph("RARE")
                    .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                    .setFontSize(12)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0));
                
                logoTextTable.addCell(logoTextCell);
                logoCell.add(logoTextTable);
            }
        } catch (Exception e) {
            Paragraph logoText = new Paragraph("RARE")
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(12)
                .setFontColor(ACCENT);
            logoCell.add(logoText);
        }
        headerTable.addCell(logoCell);

        // Center spacer
        Cell spacerCell = new Cell();
        spacerCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        headerTable.addCell(spacerCell);

        // Company Info (Right) - Moved down with proper spacing
        Cell infoCell = new Cell();
        infoCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        infoCell.setTextAlignment(TextAlignment.RIGHT);
        infoCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
        infoCell.setPaddingTop(5);
        
        infoCell.add(new Paragraph("RARE EDUCATION")
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(8)
            .setFontColor(CHARCOAL)
            .setMarginBottom(1));
        
        infoCell.add(new Paragraph("www.rareeducation.com")
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(6.5f)
            .setFontColor(MID_GRAY)
            .setMarginBottom(0));
        
        infoCell.add(new Paragraph("support@rareeducation.com")
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(6.5f)
            .setFontColor(MID_GRAY));
        
        headerTable.addCell(infoCell);
        document.add(headerTable);

        // Elegant thin line
        document.add(new Paragraph("\n").setFontSize(4));
        com.itextpdf.layout.element.LineSeparator line1 = new com.itextpdf.layout.element.LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f));
        line1.setStrokeColor(BORDER_LIGHT);
        line1.setMarginBottom(15);
        document.add(line1);

        // ═══════════════════════════════════════════════════════════════════════════
        // TITLE & META INFORMATION BAR
        // ═══════════════════════════════════════════════════════════════════════════
        
        // Main Title
        Paragraph title = new Paragraph("APPLICATION CONFIRMATION")
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(10)
            .setFontColor(CHARCOAL)
            .setTextAlignment(TextAlignment.CENTER)
            .setCharacterSpacing(1.2f)
            .setMarginBottom(12);
        document.add(title);

        // Stylish Meta Bar with REF format: ID2025
        String refNumber = student.getId() + "" + LocalDate.now().getYear();
        
        Table metaBar = new Table(new float[]{1, 1, 1});
        metaBar.setWidth(UnitValue.createPercentValue(100));
        metaBar.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        metaBar.setBackgroundColor(SOFT_GRAY_BG);
        metaBar.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        metaBar.setMarginBottom(15);
        
        Cell ref = new Cell()
            .add(new Paragraph("REF: " + refNumber)
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(6.5f)
                .setFontColor(DARK_GRAY))
            .setPadding(6)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.LEFT);
        
        Cell status = new Cell()
            .add(new Paragraph("● " + student.getApplicationStatus().toUpperCase())
                .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
                .setFontSize(6.5f)
                .setFontColor(ACCENT))
            .setPadding(6)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER);
        
        Cell date = new Cell()
            .add(new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6.5f)
                .setFontColor(DARK_GRAY))
            .setPadding(6)
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT);
        
        metaBar.addCell(ref);
        metaBar.addCell(status);
        metaBar.addCell(date);
        document.add(metaBar);

        // ═══════════════════════════════════════════════════════════════════════════
        // APPLICANT DETAILS - COMPACT AESTHETIC DESIGN
        // ═══════════════════════════════════════════════════════════════════════════
        
        addCompactSectionTitle(document, "APPLICANT DETAILS", CHARCOAL);
        
        // Personal Info Card with grey background
        Table personalCard = new Table(new float[]{1});
        personalCard.setWidth(UnitValue.createPercentValue(100));
        personalCard.setBackgroundColor(SECTION_BG);
        personalCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        personalCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        personalCard.setMarginBottom(10);
        
        Cell personalCell = new Cell();
        personalCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        personalCell.setPadding(8);
        
        // Compact 2-column layout for personal info
        Table infoGrid = new Table(new float[]{1, 2, 1, 2});
        infoGrid.setWidth(UnitValue.createPercentValue(100));
        
        addCompactInfo(infoGrid, "Name", student.getFullName(), DARK_GRAY, CHARCOAL);
        addCompactInfo(infoGrid, "Date of Birth", 
            student.getDob().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            DARK_GRAY, CHARCOAL);
        
        addCompactInfo(infoGrid, "Age", student.getAge() + " years", DARK_GRAY, CHARCOAL);
        addCompactInfo(infoGrid, "Contact", student.getContactNo(), DARK_GRAY, CHARCOAL);
        
        addCompactInfo(infoGrid, "Email", student.getEmail(), DARK_GRAY, CHARCOAL);
        addCompactInfo(infoGrid, "Passport", student.getPassportNo(), DARK_GRAY, CHARCOAL);
        
        personalCell.add(infoGrid);
        personalCard.addCell(personalCell);
        document.add(personalCard);
        
        // Address in separate card
        Table addressCard = new Table(new float[]{1, 4});
        addressCard.setWidth(UnitValue.createPercentValue(100));
        addressCard.setBackgroundColor(SECTION_BG);
        addressCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        addressCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        addressCard.setMarginBottom(12);
        addressCard.setPadding(8);
        
        addCompactInfo(addressCard, "Address", student.getAddress(), DARK_GRAY, CHARCOAL);
        document.add(addressCard);

        // ═══════════════════════════════════════════════════════════════════════════
        // ACADEMIC & PROGRAM INFORMATION
        // ═══════════════════════════════════════════════════════════════════════════
        
        addCompactSectionTitle(document, "ACADEMIC INFORMATION", CHARCOAL);
        
        Table academicCard = new Table(new float[]{1});
        academicCard.setWidth(UnitValue.createPercentValue(100));
        academicCard.setBackgroundColor(SECTION_BG);
        academicCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        academicCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        academicCard.setMarginBottom(12);
        
        Cell academicCell = new Cell();
        academicCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        academicCell.setPadding(8);
        
        Table academicGrid = new Table(new float[]{1, 2, 1, 2});
        academicGrid.setWidth(UnitValue.createPercentValue(100));
        
        addCompactInfo(academicGrid, "Qualification", student.getHighestDegree(), DARK_GRAY, CHARCOAL);
        addCompactInfo(academicGrid, "Score", student.getMarks() + "%", DARK_GRAY, CHARCOAL);
        
        academicCell.add(academicGrid);
        academicCard.addCell(academicCell);
        document.add(academicCard);
        
        // Applied Program
        Table programCard = new Table(new float[]{1, 4});
        programCard.setWidth(UnitValue.createPercentValue(100));
        programCard.setBackgroundColor(SECTION_BG);
        programCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        programCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        programCard.setMarginBottom(12);
        programCard.setPadding(8);
        
        addCompactInfo(programCard, "Applied For", student.getAppliedFor(), DARK_GRAY, CHARCOAL);
        document.add(programCard);

        // ═══════════════════════════════════════════════════════════════════════════
        // COLLEGE PREFERENCES - AESTHETIC CARD DESIGN
        // ═══════════════════════════════════════════════════════════════════════════
        
        addCompactSectionTitle(document, "COLLEGE ASSIGNMENT", CHARCOAL);
        
        Table collegeCard = new Table(new float[]{1});
        collegeCard.setWidth(UnitValue.createPercentValue(100));
        collegeCard.setBackgroundColor(SECTION_BG);
        collegeCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        collegeCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        collegeCard.setMarginBottom(12);
        
        Cell collegeCell = new Cell();
        collegeCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        collegeCell.setPadding(8);
        
        // Primary College with icon
        Paragraph primaryLabel = new Paragraph("Primary College")
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(6.5f)
            .setFontColor(DARK_GRAY)
            .setMarginBottom(2);
        
        String collegeName = student.getCollege() != null ? 
            student.getCollege().getCollegeName() : "To Be Assigned";
        
        Paragraph primaryValue = new Paragraph("▸ " + collegeName)
            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
            .setFontSize(7f)
            .setFontColor(ACCENT)
            .setMarginBottom(6);
        
        collegeCell.add(primaryLabel);
        collegeCell.add(primaryValue);
        
        // College Preferences
        if (student.getCollegePreferences() != null && !student.getCollegePreferences().isEmpty()) {
            Paragraph prefLabel = new Paragraph("Student Preferences")
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6.5f)
                .setFontColor(DARK_GRAY)
                .setMarginBottom(2);
            collegeCell.add(prefLabel);
            
            for (College college : student.getCollegePreferences()) {
                Paragraph prefItem = new Paragraph("• " + college.getCollegeName())
                    .setFont(PdfFontFactory.createFont("Helvetica"))
                    .setFontSize(6.5f)
                    .setFontColor(MID_GRAY)
                    .setMarginLeft(8)
                    .setMarginBottom(1);
                collegeCell.add(prefItem);
            }
        }
        
        collegeCard.addCell(collegeCell);
        document.add(collegeCard);

        // ═══════════════════════════════════════════════════════════════════════════
        // PROCESSING STATUS - TIMELINE STYLE
        // ═══════════════════════════════════════════════════════════════════════════
        
        addCompactSectionTitle(document, "APPLICATION TIMELINE", CHARCOAL);
        
        Table statusCard = new Table(new float[]{1});
        statusCard.setWidth(UnitValue.createPercentValue(100));
        statusCard.setBackgroundColor(SECTION_BG);
        statusCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        statusCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        statusCard.setMarginBottom(12);
        
        Cell statusCell = new Cell();
        statusCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        statusCell.setPadding(8);
        
        // Timeline items
        Table timeline = new Table(new float[]{1, 1, 1, 1});
        timeline.setWidth(UnitValue.createPercentValue(100));
        
        addTimelineItem(timeline, "Registered", 
            student.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM")), 
            DARK_GRAY, MID_GRAY);
        
        addTimelineItem(timeline, "Current", 
            formatEnumValue(student.getPresentStatus().name()), 
            ACCENT, CHARCOAL);
        
        addTimelineItem(timeline, "Next Step", 
            formatEnumValue(student.getNextStep().name()), 
            DARK_GRAY, MID_GRAY);
        
        addTimelineItem(timeline, "Agent", 
            student.getAgent() != null ? student.getAgent().getUserId() : "N/A", 
            DARK_GRAY, MID_GRAY);
        
        statusCell.add(timeline);
        statusCard.addCell(statusCell);
        document.add(statusCard);

        // ═══════════════════════════════════════════════════════════════════════════
        // EMERGENCY CONTACT - COMPACT
        // ═══════════════════════════════════════════════════════════════════════════
        
        addCompactSectionTitle(document, "EMERGENCY CONTACT", CHARCOAL);
        
        Table emergencyCard = new Table(new float[]{1, 2, 1, 2});
        emergencyCard.setWidth(UnitValue.createPercentValue(100));
        emergencyCard.setBackgroundColor(SECTION_BG);
        emergencyCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        emergencyCard.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(4));
        emergencyCard.setMarginBottom(15);
        emergencyCard.setPadding(8);
        
        addCompactInfo(emergencyCard, "Contact", student.getEmergencyContactNo(), DARK_GRAY, CHARCOAL);
        addCompactInfo(emergencyCard, "Relation", student.getEmergencyRelation(), DARK_GRAY, CHARCOAL);
        
        document.add(emergencyCard);

        // ═══════════════════════════════════════════════════════════════════════════
        // CHECK IF WE NEED TO GO TO NEXT PAGE FOR IMPORTANT INFO
        // ═══════════════════════════════════════════════════════════════════════════
        
        // Calculate remaining space
        float currentY = document.getRenderer().getCurrentArea().getBBox().getBottom();
        
        // If less than 150 points remaining, go to next page
        if (currentY < 150) {
            document.add(new com.itextpdf.layout.element.AreaBreak());
        }
        
        // ═══════════════════════════════════════════════════════════════════════════
        // IMPORTANT INFORMATION - STYLISH BOX
        // ═══════════════════════════════════════════════════════════════════════════
        
        Table importantBox = new Table(1);
        importantBox.setWidth(UnitValue.createPercentValue(100));
        importantBox.setBorder(new com.itextpdf.layout.borders.DashedBorder(ACCENT, 0.8f));
        importantBox.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(247, 250, 255));
        importantBox.setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(5));
        importantBox.setMarginTop(10);
        importantBox.setMarginBottom(15);
        
        Cell importantCell = new Cell();
        importantCell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        importantCell.setPadding(12);
        
        // Header with icon
        Paragraph importantHeader = new Paragraph("ⓘ  IMPORTANT INFORMATION")
            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
            .setFontSize(7.5f)
            .setFontColor(ACCENT)
            .setMarginBottom(8);
        
        // Content with better formatting
        Table infoList = new Table(new float[]{0.5f, 9.5f});
        infoList.setWidth(UnitValue.createPercentValue(100));
        
        addInfoItem(infoList, "✓", "This confirmation acknowledges successful submission of your application.", MID_GRAY);
        addInfoItem(infoList, "✓", "Application review typically takes 5-7 business days.", MID_GRAY);
        addInfoItem(infoList, "✓", "Official admission letter will be sent to: " + student.getEmail(), MID_GRAY);
        addInfoItem(infoList, "✓", "Keep this document for your records (Reference: " + refNumber + ").", MID_GRAY);
        addInfoItem(infoList, "✓", "For assistance, contact your agent or email support@rareeducation.com", MID_GRAY);
        
        importantCell.add(importantHeader);
        importantCell.add(infoList);
        importantBox.addCell(importantCell);
        document.add(importantBox);

        // ═══════════════════════════════════════════════════════════════════════════
        // FOOTER - MINIMAL & ELEGANT
        // ═══════════════════════════════════════════════════════════════════════════
        
        // Push footer to bottom if space available
        document.add(new Paragraph("\n"));
        
        // Footer separator
        com.itextpdf.layout.element.LineSeparator footerLine = new com.itextpdf.layout.element.LineSeparator(
            new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.3f));
        footerLine.setStrokeColor(BORDER_LIGHT);
        footerLine.setMarginTop(5);
        footerLine.setMarginBottom(8);
        document.add(footerLine);
        
        // Three-column footer
        Table footer = new Table(new float[]{1, 1, 1});
        footer.setWidth(UnitValue.createPercentValue(100));
        footer.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        
        Cell footerLeft = new Cell()
            .add(new Paragraph("Document: " + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(5.5f)
                .setFontColor(MID_GRAY))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.LEFT);
        
        Cell footerCenter = new Cell()
            .add(new Paragraph("© 2025 RARE Education")
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(5.5f)
                .setFontColor(MID_GRAY))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER);
        
        Cell footerRight = new Cell()
            .add(new Paragraph("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(5.5f)
                .setFontColor(MID_GRAY))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT);
        
        footer.addCell(footerLeft);
        footer.addCell(footerCenter);
        footer.addCell(footerRight);
        document.add(footer);
        
        // Disclaimer
        Paragraph disclaimer = new Paragraph("This electronically generated document is for confirmation purposes only and does not constitute an official admission letter.")
            .setFont(PdfFontFactory.createFont("Helvetica-Oblique"))
            .setFontSize(5f)
            .setFontColor(MID_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(3);
        document.add(disclaimer);

        document.close();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS FOR AESTHETIC FORMATTING
    // ═══════════════════════════════════════════════════════════════════════════

    private void addCompactSectionTitle(Document document, String title, 
                                       com.itextpdf.kernel.colors.Color color) throws IOException {
        Paragraph header = new Paragraph(title)
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(7f)
            .setFontColor(color)
            .setCharacterSpacing(0.8f)
            .setMarginTop(8)
            .setMarginBottom(6);
        document.add(header);
    }

    private void addCompactInfo(Table table, String label, String value,
                               com.itextpdf.kernel.colors.Color labelColor,
                               com.itextpdf.kernel.colors.Color valueColor) throws IOException {
        
        Cell labelCell = new Cell()
            .add(new Paragraph(label)
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6.5f)
                .setFontColor(labelColor))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingTop(2)
            .setPaddingBottom(2)
            .setPaddingRight(4);
        
        Cell valueCell = new Cell()
            .add(new Paragraph(value != null ? value : "—")
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6.5f)
                .setFontColor(valueColor))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingTop(2)
            .setPaddingBottom(2);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTimelineItem(Table table, String label, String value,
                                com.itextpdf.kernel.colors.Color labelColor,
                                com.itextpdf.kernel.colors.Color valueColor) throws IOException {
        
        Cell cell = new Cell();
        cell.setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        cell.setPadding(4);
        
        Paragraph labelPara = new Paragraph(label)
            .setFont(PdfFontFactory.createFont("Helvetica"))
            .setFontSize(6f)
            .setFontColor(labelColor)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(2);
        
        Paragraph valuePara = new Paragraph(value)
            .setFont(PdfFontFactory.createFont("Helvetica-Bold"))
            .setFontSize(6.5f)
            .setFontColor(valueColor)
            .setTextAlignment(TextAlignment.CENTER);
        
        cell.add(labelPara);
        cell.add(valuePara);
        table.addCell(cell);
    }

    private void addInfoItem(Table table, String icon, String text, 
                            com.itextpdf.kernel.colors.Color textColor) throws IOException {
        
        Cell iconCell = new Cell()
            .add(new Paragraph(icon)
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6f)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(34, 197, 94)))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingRight(3)
            .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.TOP);
        
        Cell textCell = new Cell()
            .add(new Paragraph(text)
                .setFont(PdfFontFactory.createFont("Helvetica"))
                .setFontSize(6f)
                .setFontColor(textColor)
                .setFixedLeading(8))
            .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            .setPaddingBottom(3);
        
        table.addCell(iconCell);
        table.addCell(textCell);
    }

    private String formatEnumValue(String enumValue) {
        if (enumValue == null) return "—";
        
        String[] words = enumValue.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) formatted.append(" ");
            String word = words[i];
            if (!word.isEmpty()) {
                formatted.append(word.substring(0, 1).toUpperCase());
                if (word.length() > 1) {
                    formatted.append(word.substring(1));
                }
            }
        }
        
        return formatted.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OTHER SERVICE METHODS
    // ═══════════════════════════════════════════════════════════════════════════
    
    @Transactional
    public Student uploadAdmissionLetter(Long studentId, String fileName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        if (student.getPresentStatus() != StudentStatus.REGISTERED &&
            student.getNextStep() != StudentStatus.UNDER_REVIEW) {
            throw new RuntimeException("Student not eligible for admission letter upload.");
        }

        student.setAdmissionLetterPath("uploads/admission-letters/" + fileName);
        student.setPresentStatus(StudentStatus.ADMISSION_LETTER_GENERATED);
        student.setNextStep(StudentStatus.TELEX_INITIATED);

        return studentRepository.save(student);
    }

    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByAgent(Long agentId) {
        return studentRepository.findByAgent_Id(agentId);
    }
}