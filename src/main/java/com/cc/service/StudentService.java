package com.cc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cc.DTO.StudentDTO;
import com.cc.modals.AddmissionManager;
import com.cc.modals.Agent;
import com.cc.modals.College;
import com.cc.modals.Location;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.UniversityPerson;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.AddmissionManagerRepository;
import com.cc.repository.AgentRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.CompanyLogoRepository;
import com.cc.repository.LocationRepository;
import com.cc.repository.StudentRepository;
import com.cc.repository.UniversityPersonRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
@SuppressWarnings("null")
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

    @Autowired
    private AddmissionManagerRepository addmissionManagerRepository;

    @Autowired
    private UniversityPersonRepository universityPersonRepository;

    @Autowired
    private AccountantService accountantService;

    // ═══════════════════════════════════════════════════════════════════════════
    // ADD STUDENT WITH FILE UPLOADS
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public Student addStudent(StudentDTO dto,
            MultipartFile adhaarFile,
            MultipartFile passportFile,
            MultipartFile marksheetFile,
            MultipartFile profileImage) {

        // Step 1: Fetch Agent
        Agent agent = agentRepository.findByUserId(dto.getUserNameAgent())
                .orElseThrow(() -> new RuntimeException("Agent not found: " + dto.getUserNameAgent()));

        // Step 2: Validate College & Location Access
        if (dto.getCollegeIds() == null || dto.getCollegeIds().isEmpty()) {
            throw new RuntimeException("At least one college must be selected.");
        }
        Long collegeId = dto.getCollegeIds().get(0);
        College selectedCollege = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found: " + collegeId));

        Location studentDistrict = locationRepository.findById(dto.getDistrictId().longValue())
                .orElseThrow(() -> new RuntimeException("District not found: " + dto.getDistrictId()));

        // Find access rule for this college
        boolean hasAccess = false;
        if (agent.getCollegeAccess() != null) {
            for (com.cc.modals.AgentCollegeAccess access : agent.getCollegeAccess()) {
                if (access.getCollege().getId().equals(selectedCollege.getId())) {
                    // Check location access
                    if (access.isAccessAllLocations()) {
                        hasAccess = true;
                    } else if (access.getAllowedLocations() != null) {
                        for (Location loc : access.getAllowedLocations()) {
                            if (loc.getId().equals(studentDistrict.getId())) {
                                hasAccess = true;
                                break;
                            }
                        }
                    }
                    break; // Found the college, stop searching
                }
            }
        }

        if (!hasAccess) {
            throw new RuntimeException("Agent does not have access to College: " + selectedCollege.getCollegeName() +
                    " for Location: " + studentDistrict.getDistrict());
        }

        // Step 4: Map Basic Fields
        Student student = new Student();
        student.setFullName(dto.getFullName());
        student.setDob(dto.getDob());
        student.setAge(dto.getAge());
        student.setEmail(dto.getEmail());
        student.setContactNo(dto.getContactNo());
        student.setAddress(dto.getAddress());
        student.setEmergencyContactNo(dto.getEmergencyContactNo());
        student.setEmergencyRelation(dto.getEmergencyRelation());
        student.setMothersName(dto.getMothersName());
        student.setFathersName(dto.getFathersName());
        student.setPassportNo(dto.getPassportNo());
        student.setPassportValidTill(dto.getPassportValidTill());
        student.setAdhaarCardNo(dto.getAdhaarCardNo());

        student.setClass12thPassingYear(dto.getClass12thPassingYear());
        student.setBaords(dto.getBaords());
        student.setClass12thMarks(dto.getClass12thMarks());
        student.setMaxMarks(dto.getMaxMarks());

        student.setHasGivenNEET(dto.isHasGivenNEET());
        if (dto.isHasGivenNEET()) {
            student.setNeetYear(dto.getNeetYear());
            student.setNeetScore(dto.getNeetScore());
        }

        student.setIntake(dto.getIntake());
        student.setAppliedFor(dto.getAppliedFor());
        student.setCreatedAt(LocalDate.now());
        student.setApplicationStatus(
                dto.getApplicationStatus() != null ? StudentStatus.valueOf(dto.getApplicationStatus())
                        : StudentStatus.REGISTERED);

        // Generate Unique Application ID
        student.setApplicationId(generateUniqueApplicationId());

        // Step 5: Assign Relationships
        student.setCollege(selectedCollege);

        // Assign Admission Manager (Robust Logic)
        AddmissionManager manager = selectedCollege.getAdmissionManager();
        if (manager == null) {
            System.out.println(
                    "LOG: Admission Manager not found in College entity. Attempting fallback lookup for College ID: "
                            + selectedCollege.getId());
            // Fallback: Try to find manager by college ID
            manager = addmissionManagerRepository.findByCollegesId(selectedCollege.getId()).orElse(null);
        }

        if (manager != null) {
            System.out.println("LOG: Assigned Admission Manager: " + manager.getUsername()
                    + " to Student Application ID: " + student.getApplicationId());
        } else {
            System.err.println("LOG: FAILED to assign Admission Manager for College ID: " + selectedCollege.getId());
        }

        student.setAdmissionManager(manager);
        student.setAgent(agent);

        // Step 6: SAVE FILES
        String safeName = dto.getFullName().replaceAll("\\s+", "_");

        String savedAdhaarPath = saveUploadedFile(adhaarFile, "adhaar", safeName + "_adhaar");
        String savedPassportPath = saveUploadedFile(passportFile, "passport", safeName + "_passport");
        String savedMarksheetPath = saveUploadedFile(marksheetFile, "marksheet", safeName + "_marks");
        String savedProfilePath = saveUploadedFile(profileImage, "profile", safeName + "_profile");

        // Set File Paths to Entity
        student.setAdhaarPath(savedAdhaarPath);
        student.setMarkseetPath(savedMarksheetPath);
        student.setProfileImagePath(savedProfilePath);
        student.setPassportPath(savedPassportPath);

        // set action
        student.setAction(action.FORWARD_TO_UNI);
        student.setActionOnUser(actionOnUser.admission_manager);

        StudentStatusHistory history = new StudentStatusHistory();

        history.setStudentApplicationStatus(StudentStatus.REGISTERED);
        history.setUpdatedBy(dto.getUserNameAgent());
        history.setUpdatedAt(LocalDateTime.now());
        history.setStudent(student);

        student.getStudentStatusHistory().add(history);

        // Step 7: Assign Accountant
        List<com.cc.modals.Accountant> accountants = accountantService.getAll();
        if (!accountants.isEmpty()) {
            student.setAccountant(accountants.get(0));
        }

        // Step 8: Save Student
        Student savedStudent = studentRepository.save(student);

        // Step 8: Generate PDF Confirmation
        try {
            String confirmationFileName = "confirmation_" + savedStudent.getId() + "_" + UUID.randomUUID() + ".pdf";
            Path uploadPath = Paths.get("uploads/confirmation-pages");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            generateConfirmationPdf(savedStudent, uploadPath.resolve(confirmationFileName).toString());
            savedStudent.setConfirmationPagePath("uploads/confirmation-pages/" + confirmationFileName);
            savedStudent = studentRepository.save(savedStudent);

        } catch (IOException e) {
            throw new RuntimeException("PDF Generation failed: " + e.getMessage());
        }

        return savedStudent;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FILE UPLOAD HELPER METHOD
    // ═══════════════════════════════════════════════════════════════════════════

    private String saveUploadedFile(MultipartFile file, String folderName, String filePrefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Validate PDF content if extension is .pdf
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            if (".pdf".equalsIgnoreCase(extension)) {
                try (java.io.InputStream is = file.getInputStream()) {
                    byte[] header = new byte[4];
                    int bytesRead = is.read(header);
                    String headerString = new String(header);
                    if (bytesRead < 4 || !headerString.startsWith("%PDF")) {
                        String hexHeader = "";
                        for (int i = 0; i < bytesRead; i++) {
                            hexHeader += String.format("%02X ", header[i]);
                        }
                        throw new RuntimeException("Invalid PDF file: " + originalFilename +
                                ". Expected header '%PDF' but found '" + headerString + "' (Hex: " + hexHeader + ")");
                    }
                }
            }

            // Create upload directory
            String uploadDir = "uploads/" + folderName;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String newFilename = filePrefix + "_" + UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFilename);

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for database
            return uploadDir + "/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Could not save file " + file.getOriginalFilename(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PDF GENERATION
    // ═══════════════════════════════════════════════════════════════════════════

    private void generateConfirmationPdf(Student student, String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(40, 40, 40, 40);

        // Create Fonts
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontLight = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        // Enhanced Color Palette
        com.itextpdf.kernel.colors.Color PRIMARY_BLUE = new com.itextpdf.kernel.colors.DeviceRgb(41, 98, 255);
        com.itextpdf.kernel.colors.Color DARK_BLUE = new com.itextpdf.kernel.colors.DeviceRgb(30, 58, 138);
        com.itextpdf.kernel.colors.Color CHARCOAL = new com.itextpdf.kernel.colors.DeviceRgb(31, 41, 55);
        com.itextpdf.kernel.colors.Color DARK_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(75, 85, 99);
        com.itextpdf.kernel.colors.Color MID_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(156, 163, 175);
        com.itextpdf.kernel.colors.Color LIGHT_BG = new com.itextpdf.kernel.colors.DeviceRgb(249, 250, 251);
        com.itextpdf.kernel.colors.Color BLUE_BG = new com.itextpdf.kernel.colors.DeviceRgb(239, 246, 255);
        com.itextpdf.kernel.colors.Color BORDER = new com.itextpdf.kernel.colors.DeviceRgb(229, 231, 235);
        com.itextpdf.kernel.colors.Color SUCCESS_GREEN = new com.itextpdf.kernel.colors.DeviceRgb(16, 185, 129);
        com.itextpdf.kernel.colors.Color WHITE = com.itextpdf.kernel.colors.ColorConstants.WHITE;

        // --- HEADER SECTION WITH BANNER ---
        Table headerBanner = new Table(1);
        headerBanner.setWidth(UnitValue.createPercentValue(100));
        headerBanner.setMarginBottom(20);

        Cell bannerCell = new Cell()
                .setBackgroundColor(PRIMARY_BLUE)
                .setBorder(null)
                .setPadding(25)
                .setTextAlignment(TextAlignment.CENTER);

        bannerCell.add(new Paragraph("✓ APPLICATION CONFIRMED")
                .setFont(fontBold)
                .setFontSize(20)
                .setFontColor(WHITE)
                .setMarginBottom(5));

        bannerCell.add(new Paragraph("Your application has been successfully registered")
                .setFont(fontLight)
                .setFontSize(10)
                .setFontColor(WHITE));

        headerBanner.addCell(bannerCell);
        document.add(headerBanner);

        // --- REFERENCE NUMBER BOX ---
        Table refBox = new Table(1);
        refBox.setWidth(UnitValue.createPercentValue(100));
        refBox.setMarginBottom(25);

        Cell refCell = new Cell()
                .setBackgroundColor(LIGHT_BG)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER, 1))
                .setPadding(15)
                .setTextAlignment(TextAlignment.CENTER);

        refCell.add(new Paragraph("Application Reference Number")
                .setFont(fontRegular)
                .setFontSize(9)
                .setFontColor(DARK_GRAY)
                .setMarginBottom(5));

        refCell.add(new Paragraph(student.getApplicationId())
                .setFont(fontBold)
                .setFontSize(16)
                .setFontColor(DARK_BLUE)
                .setMarginBottom(3));

        refCell.add(new Paragraph("Generated: " + LocalDate.now().toString())
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MID_GRAY));

        refBox.addCell(refCell);
        document.add(refBox);

        // --- QR CODE SECTION ---
        Table qrSection = new Table(1);
        qrSection.setWidth(UnitValue.createPercentValue(100));
        qrSection.setMarginBottom(25);

        Cell qrCell = new Cell()
                .setBackgroundColor(BLUE_BG)
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(PRIMARY_BLUE, 2))
                .setPadding(20)
                .setTextAlignment(TextAlignment.CENTER);

        // QR Code Title
        qrCell.add(new Paragraph("📱 Student Portal Access")
                .setFont(fontBold)
                .setFontSize(14)
                .setFontColor(DARK_BLUE)
                .setMarginBottom(10));

        // QR Code Image
        try {
            String qrCodeUrl = "http://localhost:8080/api/config/qrcode";
            com.itextpdf.io.image.ImageData qrImageData = com.itextpdf.io.image.ImageDataFactory
                    .create(new java.net.URL(qrCodeUrl));
            com.itextpdf.layout.element.Image qrImage = new com.itextpdf.layout.element.Image(qrImageData);
            qrImage.setWidth(150);
            qrImage.setHeight(150);
            qrImage.setMarginBottom(15);
            qrCell.add(qrImage);
        } catch (Exception e) {
            // Fallback if QR code cannot be loaded
            qrCell.add(new Paragraph("[QR CODE]")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setFontColor(MID_GRAY)
                    .setBorder(new com.itextpdf.layout.borders.DashedBorder(BORDER, 2))
                    .setPadding(40)
                    .setMarginBottom(15));
        }

        // Application ID below QR
        qrCell.add(new Paragraph("Application ID: " + student.getApplicationId())
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(CHARCOAL)
                .setMarginBottom(12));

        // Instructions
        Paragraph instructions = new Paragraph()
                .setFont(fontRegular)
                .setFontSize(9)
                .setFontColor(DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8);

        instructions.add("Please scan the QR code above to generate your\n");
        instructions.add(new com.itextpdf.layout.element.Text("Student Username and Password")
                .setFont(fontBold)
                .setFontColor(PRIMARY_BLUE));
        instructions.add("\nto track all your application updates.");

        qrCell.add(instructions);

        // Important Note
        qrCell.add(new Paragraph("⚠ Keep this document safe for future reference")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(DARK_GRAY)
                .setFont(fontItalic)
                .setMarginTop(5));

        qrSection.addCell(qrCell);
        document.add(qrSection);

        // --- APPLICANT DETAILS SECTION ---
        document.add(new Paragraph("APPLICANT DETAILS")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(DARK_BLUE)
                .setMarginBottom(12)
                .setMarginTop(10));

        Table infoGrid = new Table(new float[] { 1.2f, 2.8f, 1.2f, 2.8f });
        infoGrid.setWidth(UnitValue.createPercentValue(100));
        infoGrid.setBackgroundColor(LIGHT_BG);
        infoGrid.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER, 1));

        addStyledInfoRow(infoGrid, "Full Name", student.getFullName(), DARK_GRAY, CHARCOAL, fontRegular, fontBold);
        addStyledInfoRow(infoGrid, "Date of Birth", student.getDob() != null ? student.getDob().toString() : "-",
                DARK_GRAY, CHARCOAL, fontRegular, fontBold);
        addStyledInfoRow(infoGrid, "Email", student.getEmail(), DARK_GRAY, CHARCOAL, fontRegular, fontBold);
        addStyledInfoRow(infoGrid, "Phone", student.getContactNo(), DARK_GRAY, CHARCOAL, fontRegular, fontBold);
        addStyledInfoRow(infoGrid, "Passport No.", student.getPassportNo(), DARK_GRAY, CHARCOAL, fontRegular, fontBold);
        addStyledInfoRow(infoGrid, "Passport Valid Till",
                student.getPassportValidTill() != null ? student.getPassportValidTill().toString() : "-", DARK_GRAY,
                CHARCOAL, fontRegular, fontBold);

        document.add(infoGrid);

        // --- COLLEGE ASSIGNMENT SECTION ---
        document.add(new Paragraph("COLLEGE ASSIGNMENT")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(DARK_BLUE)
                .setMarginBottom(12)
                .setMarginTop(20));

        Table collegeCard = new Table(1);
        collegeCard.setWidth(UnitValue.createPercentValue(100));
        collegeCard.setBackgroundColor(LIGHT_BG);
        collegeCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(SUCCESS_GREEN, 2));

        Cell collegeCell = new Cell()
                .setBorder(null)
                .setPadding(15);

        collegeCell.add(new Paragraph("Primary College Assignment")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(DARK_GRAY)
                .setMarginBottom(5));

        collegeCell.add(new Paragraph(
                "▸ " + (student.getCollege() != null ? student.getCollege().getCollegeName() : "Not Assigned"))
                .setFont(fontBold)
                .setFontSize(11)
                .setFontColor(SUCCESS_GREEN)
                .setMarginBottom(8));

        collegeCell
                .add(new Paragraph("Applied For: " + (student.getAppliedFor() != null ? student.getAppliedFor() : "-"))
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(CHARCOAL));

        collegeCard.addCell(collegeCell);
        document.add(collegeCard);

        // --- FOOTER ---
        document.add(new Paragraph("\n"));
        Table footerTable = new Table(1);
        footerTable.setWidth(UnitValue.createPercentValue(100));
        footerTable.setMarginTop(20);

        Cell footerCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(BORDER, 1))
                .setPaddingTop(15)
                .setTextAlignment(TextAlignment.CENTER);

        footerCell.add(new Paragraph("This is an automatically generated confirmation document")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MID_GRAY)
                .setMarginBottom(3));

        footerCell.add(new Paragraph("For queries, please contact your assigned admission manager")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(MID_GRAY));

        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.close();
    }

    // Enhanced helper method for styled info rows
    private void addStyledInfoRow(Table table, String label, String value,
            com.itextpdf.kernel.colors.Color labelColor,
            com.itextpdf.kernel.colors.Color valueColor,
            PdfFont fontRegular,
            PdfFont fontBold) {

        Cell labelCell = new Cell()
                .add(new Paragraph(label)
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(labelColor))
                .setBorder(null)
                .setPadding(8)
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255));

        Cell valueCell = new Cell()
                .add(new Paragraph(value != null ? value : "-")
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setFontColor(valueColor))
                .setBorder(null)
                .setPadding(8)
                .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(255, 255, 255));

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // OTHER SERVICE METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public Student uploadAdmissionLetter(Long studentId, String fileName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        student.setAdmissionLetterPath("uploads/admission-letters/" + fileName);
        student.setApplicationStatus(StudentStatus.ADMISSION_LETTER_GENERATED);

        return studentRepository.save(student);
    }

    @Transactional
    public Student updateStudentStatus(Long studentId, com.cc.DTO.StudentStatusUpdateDTO dto) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        if (dto.getPresentStatus() != null) {
            student.setApplicationStatus(dto.getPresentStatus());
        }

        if (dto.getUpdatedByUsername() != null) {
            student.setUpdatedBy(dto.getUpdatedByUsername());
        }

        return studentRepository.save(student);
    }

    public List<Student> getGeneratedAdmissionLettersByManagerUsername(String username) {
        return studentRepository.findByUpdatedByAndApplicationStatus(
                username,
                StudentStatus.ADMISSION_LETTER_GENERATED);
    }

    @Transactional
    public Student uploadAdmissionLetter(Long studentId, String fileName, String username) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        student.setAdmissionLetterPath("uploads/admission-letters/" + fileName);
        student.setApplicationStatus(StudentStatus.ADMISSION_LETTER_GENERATED);

        if (username != null) {
            student.setUpdatedBy(username);
        }

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public List<Student> getStudentsByAgent(Long id) {
        return studentRepository.findByAgent_Id(id);
    }

    /**
     * Retrieves all students assigned to a specific Admission Manager username,
     * regardless of status.
     * 
     * @param username The username of the Admission Manager.
     * @return List of students assigned to that manager.
     */
    public List<Student> getStudentsByAdmissionManagerUsername(String username) {
        // Step 1: Find the Admission Manager entity by username
        AddmissionManager manager = addmissionManagerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admission Manager not found with username: " + username));

        // Step 2: Return the list of students associated with that manager
        // This relies on the @JsonManagedReference(value = "manager-student") in
        // AddmissionManager
        // which populates the students list automatically when the manager is fetched.
        return manager.getStudents();

        // Alternatively, if manager.getStudents() is slow/empty, use a direct
        // repository query:
        // return studentRepository.findByAdmissionManager(manager);
    }

    public void assigningStudentToUniversityPerson(Long studentId, String userName) {

        // Step 1: Fetch the student
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Step 2: Extract the college from the student
        College college = student.getCollege();
        if (college == null) {
            throw new RuntimeException("Student has no college assigned");
        }

        // Step 3: Fetch the UniversityPerson using the username (Optional handled)
        UniversityPerson person = universityPersonRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Admission Manager not found"));

        // Step 4: Ensure this UniversityPerson belongs to the same college
        if (!person.getCollege().getId().equals(college.getId())) {
            throw new RuntimeException("This Admission Manager does not belong to the student's college");
        }

        // Step 5: Assign the student to UniversityPerson
        student.setUniversityPerson(person);

        // Step 6: Persist student assignment
        studentRepository.save(student);
    }

    @Transactional
    public Student initiateTelexProcess(Long studentId, MultipartFile passport, String agentUsername) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Validate status
        if (student.getApplicationStatus() != StudentStatus.ADMISSION_LETTER_GENERATED) {
            throw new RuntimeException("Student status must be ADMISSION_LETTER_GENERATED to initiate Telex. Current: "
                    + student.getApplicationStatus());
        }

        // Validate Agent
        if (student.getAgent() == null || !student.getAgent().getUserId().equals(agentUsername)) {
            throw new RuntimeException("Unauthorized: Only the assigned Agent can initiate Telex");
        }

        // Save Passport
        String safeName = student.getFullName().replaceAll("\\s+", "_");
        String savedPassportPath = saveUploadedFile(passport, "passport", safeName + "_passport_telex");
        student.setPassportPath(savedPassportPath);

        // Update Status
        student.setApplicationStatus(StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED);

        // Add History
        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudent(student);
        history.setStudentApplicationStatus(StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED);
        history.setUpdatedBy(agentUsername);
        history.setUpdatedAt(LocalDateTime.now());
        student.getStudentStatusHistory().add(history);

        return studentRepository.save(student);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private String generateUniqueApplicationId() {
        LocalDate now = LocalDate.now();
        int yearLastDigit = now.getYear() % 10;
        String datePart = String.format("%02d%02d", now.getMonthValue(), now.getDayOfMonth());
        String prefix = yearLastDigit + datePart;

        java.util.Random random = new java.util.Random();
        String applicationId;

        do {
            int randomNum = random.nextInt(1000); // 0 to 999
            String randomPart = String.format("%03d", randomNum);
            applicationId = prefix + randomPart;
        } while (studentRepository.existsByApplicationId(applicationId));

        return applicationId;
    }

}