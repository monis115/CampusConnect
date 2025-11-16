package com.cc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cc.DTO.StudentDTO;
import com.cc.modals.Agent;
import com.cc.modals.College;
import com.cc.modals.Location;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.repository.AgentRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.CompanyLogoRepository;
import com.cc.repository.LocationRepository;
import com.cc.repository.StudentRepository;
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

        // Step 2: Validate Location
        Location studentDistrict = locationRepository.findById(dto.getDistrictId().longValue())
                .orElseThrow(() -> new RuntimeException("District not found: " + dto.getDistrictId()));

        if (agent.getAccessibleLocations().stream()
                .noneMatch(loc -> Objects.equals(loc.getId(), studentDistrict.getId()))) {
            throw new RuntimeException("Access denied for district: " + studentDistrict.getDistrict());
        }

        // Step 3: Validate College
        if (dto.getCollegeIds() == null || dto.getCollegeIds().isEmpty()) {
            throw new RuntimeException("At least one college must be selected.");
        }
        
        Long collegeId = dto.getCollegeIds().get(0);
        College selectedCollege = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("College not found: " + collegeId));

        if (agent.getCollegePrivileges().stream()
                .noneMatch(col -> Objects.equals(col.getId(), selectedCollege.getId()))) {
            throw new RuntimeException("Access denied for college: " + selectedCollege.getCollegeName());
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
        student.setApplicationStatus(dto.getApplicationStatus() != null ? 
                StudentStatus.valueOf(dto.getApplicationStatus()) : StudentStatus.Submitted);

        // Step 5: Assign Relationships
        student.setCollege(selectedCollege);
        student.setCollegePreferences(selectedCollege);
        student.setAdmissionManager(selectedCollege.getAdmissionManager());
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

        // Step 7: Save Student
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
            // Create upload directory
            String uploadDir = "uploads/" + folderName;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
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
        document.setMargins(35, 35, 35, 35);

        // Create Fonts
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Colors
        com.itextpdf.kernel.colors.Color CHARCOAL = new com.itextpdf.kernel.colors.DeviceRgb(40, 40, 40);
        com.itextpdf.kernel.colors.Color DARK_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(70, 70, 70);
        com.itextpdf.kernel.colors.Color MID_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(120, 120, 120);
        com.itextpdf.kernel.colors.Color SECTION_BG = new com.itextpdf.kernel.colors.DeviceRgb(245, 247, 250);
        com.itextpdf.kernel.colors.Color BORDER_LIGHT = new com.itextpdf.kernel.colors.DeviceRgb(230, 232, 235);
        com.itextpdf.kernel.colors.Color ACCENT = new com.itextpdf.kernel.colors.DeviceRgb(41, 98, 255);

        // --- HEADER ---
        document.add(new Paragraph("APPLICATION CONFIRMATION")
                .setFont(fontBold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));
                
        document.add(new Paragraph("REF: " + student.getId() + "" + LocalDate.now().getYear())
                .setFont(fontRegular)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));

        // --- APPLICANT DETAILS ---
        addCompactSectionTitle(document, "APPLICANT DETAILS", CHARCOAL, fontBold);
        Table infoGrid = new Table(new float[]{1, 2, 1, 2});
        infoGrid.setWidth(UnitValue.createPercentValue(100));
        
        addCompactInfo(infoGrid, "Name", student.getFullName(), DARK_GRAY, CHARCOAL, fontRegular);
        addCompactInfo(infoGrid, "Email", student.getEmail(), DARK_GRAY, CHARCOAL, fontRegular);
        addCompactInfo(infoGrid, "Phone", student.getContactNo(), DARK_GRAY, CHARCOAL, fontRegular);
        addCompactInfo(infoGrid, "Passport", student.getPassportNo(), DARK_GRAY, CHARCOAL, fontRegular);
        
        document.add(infoGrid);

        // --- COLLEGE ASSIGNMENT ---
        addCompactSectionTitle(document, "COLLEGE ASSIGNMENT", CHARCOAL, fontBold);
        
        Table collegeCard = new Table(new float[]{1});
        collegeCard.setWidth(UnitValue.createPercentValue(100));
        collegeCard.setBackgroundColor(SECTION_BG);
        collegeCard.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER_LIGHT, 0.5f));
        
        Cell collegeCell = new Cell().setBorder(null).setPadding(8);

        collegeCell.add(new Paragraph("Primary College")
                .setFont(fontRegular)
                .setFontSize(7)
                .setFontColor(DARK_GRAY));
        
        collegeCell.add(new Paragraph("▸ " + (student.getCollege() != null ? 
                student.getCollege().getCollegeName() : "N/A"))
                .setFont(fontBold)
                .setFontSize(8)
                .setFontColor(ACCENT)
                .setMarginBottom(6));

        if (student.getCollegePreferences() != null) {
            collegeCell.add(new Paragraph("Student Preference")
                    .setFont(fontRegular)
                    .setFontSize(7)
                    .setFontColor(DARK_GRAY));
            
            collegeCell.add(new Paragraph("• " + student.getCollegePreferences().getCollegeName())
                    .setFont(fontRegular)
                    .setFontSize(7)
                    .setFontColor(MID_GRAY)
                    .setMarginLeft(8));
        }

        collegeCard.addCell(collegeCell);
        document.add(collegeCard);

        document.close();
    }

    // PDF Helper Methods
    private void addCompactSectionTitle(Document document, String title, 
                                       com.itextpdf.kernel.colors.Color color, 
                                       PdfFont font) throws IOException {
        document.add(new Paragraph("\n" + title)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(color));
    }

    private void addCompactInfo(Table table, String label, String value, 
                               com.itextpdf.kernel.colors.Color c1, 
                               com.itextpdf.kernel.colors.Color c2, 
                               PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(label)
                .setFont(font).setFontSize(8).setFontColor(c1))
                .setBorder(null));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "-")
                .setFont(font).setFontSize(8).setFontColor(c2))
                .setBorder(null));
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
            StudentStatus.ADMISSION_LETTER_GENERATED
        );
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
}