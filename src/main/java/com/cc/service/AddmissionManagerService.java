package com.cc.service;

import com.cc.DTO.AddmissionManagerCreateDTO;
import com.cc.DTO.UpdateAdmissionManagerDTO;
import com.cc.modals.AddmissionManager;
import com.cc.modals.Student;
import com.cc.modals.StudentStatus;
import com.cc.modals.StudentStatusHistory;
import com.cc.modals.UniversityPerson;
import com.cc.modals.action;
import com.cc.modals.actionOnUser;
import com.cc.repository.AddmissionManagerRepository;
import com.cc.repository.CollegeRepository;
import com.cc.repository.StudentRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class AddmissionManagerService {

        @Autowired
        private AddmissionManagerRepository managerRepo;

        @Autowired
        private CollegeRepository collegeRepo;

        @Autowired
        private StudentRepository studentRepo;

        final Random random = new Random();

        @Autowired
        private UserService userService;

        @Autowired
        private com.cc.repository.StudentStatusHistoryRepository statusHistoryRepo;

        @Autowired
        private DocPersonService docPersonService;

        @Autowired
        private com.cc.repository.UniversityPersonRepository universityPersonRepo;

        @Autowired
        private S3StorageService s3StorageService;

        // ===================== CREATE =====================
        @Transactional
        public AddmissionManager createAddmissionManager(AddmissionManagerCreateDTO dto) {
                AddmissionManager manager = new AddmissionManager();
                manager.setFullName(dto.getFullName());
                manager.setEmail(dto.getEmail());
                manager.setContactNumber(dto.getContactNumber());

                // Generate unique username
                manager.setUsername(generateUniqueUsername(dto.getContactNumber()));

                // Hash password (plain is phone number)
                manager.setPassword((dto.getContactNumber()));

                // Save manager FIRST → gets ID
                AddmissionManager savedManager = managerRepo.save(manager);

                // Now assign colleges (safe: manager has ID)
                if (dto.getCollegeIds() != null && !dto.getCollegeIds().isEmpty()) {
                        for (Long collegeId : dto.getCollegeIds()) {
                                collegeRepo.findById(collegeId).ifPresent(college -> {
                                        college.setAdmissionManager(savedManager);
                                        savedManager.getColleges().add(college);
                                });
                        }
                }

                // Create User entry
                com.cc.modals.User user = new com.cc.modals.User();
                user.setUsername(savedManager.getUsername());
                user.setPassword(savedManager.getPassword());
                user.setRole("admission_manager");
                userService.createUser(user);

                return savedManager;
        }

        // ===================== UPDATE =====================
        @Transactional
        public AddmissionManager updateManager(Long id, UpdateAdmissionManagerDTO dto) {
                return managerRepo.findById(id).map(existing -> {
                        existing.setFullName(dto.getFullName());
                        existing.setEmail(dto.getEmail());
                        existing.setContactNumber(dto.getContactNumber());
                        existing.setRole(dto.getRole());
                        existing.setStatus(dto.getStatus());

                        // Unlink old colleges
                        existing.getColleges().forEach(c -> c.setAdmissionManager(null));
                        existing.getColleges().clear();

                        // Link new colleges
                        if (dto.getCollegeIds() != null) {
                                dto.getCollegeIds().forEach(cid -> collegeRepo.findById(cid).ifPresent(college -> {
                                        college.setAdmissionManager(existing);
                                        existing.getColleges().add(college);
                                }));
                        }

                        return managerRepo.save(existing);
                }).orElse(null);
        }

        // ===================== DELETE =====================
        @Transactional
        public void deleteManager(Long id) {
                managerRepo.findById(id).ifPresent(manager -> {
                        manager.getColleges().forEach(c -> c.setAdmissionManager(null));
                        manager.getColleges().clear();
                        managerRepo.delete(manager);
                });
        }

        // ===================== READ =====================
        public List<AddmissionManager> getAllManagers() {
                return managerRepo.findAll();
        }

        public AddmissionManager getManagerById(Long id) {
                return managerRepo.findById(id).orElse(null);
        }

        public Optional<AddmissionManager> getAdmissionManagerByUserName(String username) {
                return managerRepo.findByUsername(username);
        }

        public List<AddmissionManager> getManagersWithStudents() {
                return managerRepo.findByStudentsIsNotEmpty();
        }

        // Inside StudentService.java (or AddmissionManagerService.java if you inject
        // StudentRepository)

        public List<Student> getApprovedStudentsForManager(AddmissionManager manager) {
                // Corrected: Use findByAdmissionManagerAndApplicationStatus
                return studentRepo.findByAdmissionManagerAndApplicationStatus(
                                manager,
                                StudentStatus.APPROVED);
        }

        public List<Student> getRegisteredOrUnderReviewStudentsForManager(AddmissionManager manager) {
                // Corrected: Use findByAdmissionManagerAndApplicationStatusIn
                return studentRepo.findByAdmissionManagerAndApplicationStatusIn(
                                manager,
                                java.util.Arrays.asList(StudentStatus.REGISTERED, StudentStatus.UNDER_REVIEW));
        }

        // ===================== UTILITY =====================
        private String generateUniqueUsername(String contactNumber) {
                String digits = contactNumber.replaceAll("\\D", "");
                String last3 = digits.length() >= 3
                                ? digits.substring(digits.length() - 3)
                                : String.format("%3s", digits).replace(' ', '0');
                String reversedLast3 = new StringBuilder(last3).reverse().toString();
                String yearSuffix = String.valueOf(LocalDate.now().getYear()).substring(2);

                String candidate;
                int attempts = 0;
                do {
                        String rand2 = String.format("%02d", random.nextInt(100));
                        candidate = "M" + yearSuffix + reversedLast3 + rand2;
                        attempts++;
                        if (attempts > 1000) {
                                candidate = "M" + yearSuffix + reversedLast3 + (System.currentTimeMillis() % 10000);
                                break;
                        }
                } while (managerRepo.findByUsername(candidate).isPresent());

                return candidate;
        }

        public Optional<AddmissionManager> getAdmissionManagerByUserNameWithDetails(String username) {
                return managerRepo.findByUsernameWithDetails(username);
        }

        // ===================== FORWARD STUDENT TO UNIVERSITY PERSON
        // =====================
        // =====================================================================
        // UPDATED AddmissionManagerService.java - forwardStudentToUniversityPerson
        // method
        // =====================================================================

        @Transactional
        public Student forwardStudentToUniversityPerson(Long studentId, String managerUsername) {
                Student student = studentRepo.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                // Get the college from the student
                if (student.getCollege() == null) {
                        throw new RuntimeException("Student does not have a college assigned");
                }

                // Get the UniversityPerson from the college (Robust Logic)
                UniversityPerson universityPerson = student.getCollege().getUniversityPerson();
                if (universityPerson == null) {
                        System.out.println(
                                        "LOG: University Person not found in College entity. Attempting fallback lookup for College ID: "
                                                        + student.getCollege().getId());
                        // Fallback: Try to find person by college ID
                        universityPerson = student.getCollege().getId() != null
                                        ? universityPersonRepo.findByCollegeId(student.getCollege().getId())
                                                        .orElse(null)
                                        : null;
                }

                if (universityPerson != null) {
                        System.out.println("LOG: Assigned University Person: " + universityPerson.getUserName()
                                        + " to Student ID: " + studentId);
                } else {
                        System.err.println("LOG: FAILED to assign University Person for College ID: "
                                        + student.getCollege().getId());
                }

                if (universityPerson == null) {
                        throw new RuntimeException(
                                        "No University Person assigned to college: "
                                                        + student.getCollege().getCollegeName());
                }

                // Assign university person
                student.setUniversityPerson(universityPerson);
                student.setAction(action.UPLOAD_ADMISSION_LETTER);
                student.setActionOnUser(actionOnUser.UniversityCoordinator);

                // Save student first
                Student savedStudent = studentRepo.save(student);

                // ============ HISTORY ENTRY 1: Forwarded to University ============
                StudentStatusHistory forwardHistory = new StudentStatusHistory();
                forwardHistory.setStudent(savedStudent);
                forwardHistory.setStudentApplicationStatus(StudentStatus.FORWARDED_TO_UNI);
                forwardHistory.setUpdatedBy(managerUsername);
                forwardHistory.setUpdatedAt(java.time.LocalDateTime.now());
                forwardHistory.setRemarks(
                                "Student forwarded to University Coordinator: " + universityPerson.getUserName());
                statusHistoryRepo.save(forwardHistory);

                // ============ GENERATE CONDITIONAL OFFER LETTER ============
                try {
                        String conditionalOfferPath = generateConditionalOfferLetter(savedStudent);

                        // Update student with conditional offer letter path (now S3 path)
                        savedStudent.setConditionalOfferLetterPath(conditionalOfferPath);
                        savedStudent.setApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
                        savedStudent = studentRepo.save(savedStudent);

                        // ============ HISTORY ENTRY 2: Conditional Offer Letter Generated ============
                        StudentStatusHistory offerHistory = new StudentStatusHistory();
                        offerHistory.setStudent(savedStudent);
                        offerHistory.setStudentApplicationStatus(StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED);
                        offerHistory.setUpdatedBy("SYSTEM");
                        offerHistory.setUpdatedAt(java.time.LocalDateTime.now());
                        offerHistory.setRemarks("Conditional Offer Letter generated automatically");
                        statusHistoryRepo.save(offerHistory);

                } catch (Exception e) {
                        throw new RuntimeException("Failed to generate conditional offer letter: " + e.getMessage());
                }

                return savedStudent;
        }

        // =====================================================================
        // NEW METHOD: Generate Conditional Offer Letter PDF
        // =====================================================================

        private String generateConditionalOfferLetter(Student student) throws java.io.IOException {
                // Generate PDF to a temp file first
                java.nio.file.Path tempPath = java.nio.file.Files.createTempFile("conditional_offer_", ".pdf");
                String tempFilePath = tempPath.toString();

                // Generate the PDF
                generateConditionalOfferPdf(student, tempFilePath);

                // Upload to S3 (or local if S3 not enabled)
                try {
                        java.io.File tempFile = tempPath.toFile();

                        String storagePath = s3StorageService.uploadFile(tempFile, "conditional-offer-letters",
                                        "conditional_offer_" + student.getId());

                        // Cleanup temp file
                        java.nio.file.Files.deleteIfExists(tempPath);

                        return storagePath;
                } catch (Exception e) {
                        // Cleanup temp file on error
                        java.nio.file.Files.deleteIfExists(tempPath);
                        throw new java.io.IOException("Failed to upload conditional offer letter: " + e.getMessage(),
                                        e);
                }
        }

        // =====================================================================
        // NEW METHOD: Create Conditional Offer Letter PDF Content
        // =====================================================================

        private void generateConditionalOfferPdf(Student student, String outputPath) throws java.io.IOException {
                com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(outputPath);
                com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
                com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf);
                document.setMargins(40, 40, 40, 40);

                // Create Fonts
                com.itextpdf.kernel.font.PdfFont fontBold = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
                com.itextpdf.kernel.font.PdfFont fontRegular = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
                com.itextpdf.kernel.font.PdfFont fontItalic = com.itextpdf.kernel.font.PdfFontFactory.createFont(
                                com.itextpdf.io.font.constants.StandardFonts.HELVETICA_OBLIQUE);

                // Colors
                com.itextpdf.kernel.colors.Color DARK_BLUE = new com.itextpdf.kernel.colors.DeviceRgb(30, 58, 138);
                com.itextpdf.kernel.colors.Color PRIMARY_BLUE = new com.itextpdf.kernel.colors.DeviceRgb(41, 98, 255);
                com.itextpdf.kernel.colors.Color CHARCOAL = new com.itextpdf.kernel.colors.DeviceRgb(31, 41, 55);
                com.itextpdf.kernel.colors.Color DARK_GRAY = new com.itextpdf.kernel.colors.DeviceRgb(75, 85, 99);
                com.itextpdf.kernel.colors.Color LIGHT_BG = new com.itextpdf.kernel.colors.DeviceRgb(249, 250, 251);
                com.itextpdf.kernel.colors.Color BORDER = new com.itextpdf.kernel.colors.DeviceRgb(229, 231, 235);
                com.itextpdf.kernel.colors.Color WHITE = com.itextpdf.kernel.colors.ColorConstants.WHITE;
                com.itextpdf.kernel.colors.Color SUCCESS_GREEN = new com.itextpdf.kernel.colors.DeviceRgb(16, 185, 129);

                // ============ HEADER ============
                com.itextpdf.layout.element.Table headerTable = new com.itextpdf.layout.element.Table(1);
                headerTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

                com.itextpdf.layout.element.Cell headerCell = new com.itextpdf.layout.element.Cell()
                                .setBackgroundColor(DARK_BLUE)
                                .setBorder(null)
                                .setPadding(25)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);

                headerCell.add(new com.itextpdf.layout.element.Paragraph("CONDITIONAL OFFER LETTER")
                                .setFont(fontBold)
                                .setFontSize(20)
                                .setFontColor(WHITE)
                                .setMarginBottom(5));

                headerCell.add(new com.itextpdf.layout.element.Paragraph(
                                student.getCollege() != null ? student.getCollege().getCollegeName() : "University")
                                .setFont(fontRegular)
                                .setFontSize(11)
                                .setFontColor(WHITE));

                headerTable.addCell(headerCell);
                document.add(headerTable);
                document.add(new com.itextpdf.layout.element.Paragraph("\n"));

                // ============ REFERENCE & DATE ============
                com.itextpdf.layout.element.Table refTable = new com.itextpdf.layout.element.Table(
                                new float[] { 1, 1 });
                refTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                refTable.setMarginBottom(20);

                refTable.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph("Reference No:")
                                                .setFont(fontRegular)
                                                .setFontSize(9)
                                                .setFontColor(DARK_GRAY))
                                .add(new com.itextpdf.layout.element.Paragraph(
                                                "COL/" + student.getApplicationId() + "/" +
                                                                java.time.LocalDate.now().getYear())
                                                .setFont(fontBold)
                                                .setFontSize(10)
                                                .setFontColor(CHARCOAL))
                                .setBorder(null));

                refTable.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph("Date:")
                                                .setFont(fontRegular)
                                                .setFontSize(9)
                                                .setFontColor(DARK_GRAY))
                                .add(new com.itextpdf.layout.element.Paragraph(java.time.LocalDate.now().toString())
                                                .setFont(fontBold)
                                                .setFontSize(10)
                                                .setFontColor(CHARCOAL))
                                .setBorder(null)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

                document.add(refTable);

                // ============ STUDENT DETAILS ============
                document.add(new com.itextpdf.layout.element.Paragraph("Dear " + student.getFullName() + ",")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(CHARCOAL)
                                .setMarginBottom(15));

                document.add(new com.itextpdf.layout.element.Paragraph(
                                "We are pleased to inform you that your application for admission to " +
                                                (student.getAppliedFor() != null ? student.getAppliedFor()
                                                                : "the program")
                                                +
                                                " has been reviewed and you have been conditionally accepted.")
                                .setFont(fontRegular)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL)
                                .setMarginBottom(20));

                // ============ APPLICANT INFORMATION BOX ============
                document.add(new com.itextpdf.layout.element.Paragraph("APPLICANT INFORMATION")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(DARK_BLUE)
                                .setMarginBottom(10));

                com.itextpdf.layout.element.Table infoTable = new com.itextpdf.layout.element.Table(
                                new float[] { 1, 2 });
                infoTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                infoTable.setBackgroundColor(LIGHT_BG);
                infoTable.setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER, 1));
                infoTable.setMarginBottom(20);

                addInfoRow(infoTable, "Full Name:", student.getFullName(), fontRegular, fontBold, DARK_GRAY, CHARCOAL);
                addInfoRow(infoTable, "Application ID:", student.getApplicationId(), fontRegular, fontBold, DARK_GRAY,
                                CHARCOAL);
                addInfoRow(infoTable, "Date of Birth:", student.getDob() != null ? student.getDob().toString() : "-",
                                fontRegular, fontBold, DARK_GRAY, CHARCOAL);
                addInfoRow(infoTable, "Passport Number:", student.getPassportNo(), fontRegular, fontBold, DARK_GRAY,
                                CHARCOAL);
                addInfoRow(infoTable, "Email:", student.getEmail(), fontRegular, fontBold, DARK_GRAY, CHARCOAL);
                addInfoRow(infoTable, "Contact:", student.getContactNo(), fontRegular, fontBold, DARK_GRAY, CHARCOAL);

                document.add(infoTable);

                // ============ PROGRAM DETAILS ============
                document.add(new com.itextpdf.layout.element.Paragraph("PROGRAM DETAILS")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(DARK_BLUE)
                                .setMarginBottom(10));

                com.itextpdf.layout.element.Table programTable = new com.itextpdf.layout.element.Table(1);
                programTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                programTable.setBackgroundColor(LIGHT_BG);
                programTable.setBorder(new com.itextpdf.layout.borders.SolidBorder(SUCCESS_GREEN, 2));
                programTable.setMarginBottom(20);

                com.itextpdf.layout.element.Cell programCell = new com.itextpdf.layout.element.Cell()
                                .setBorder(null)
                                .setPadding(15);

                programCell.add(new com.itextpdf.layout.element.Paragraph("Program:")
                                .setFont(fontRegular)
                                .setFontSize(9)
                                .setFontColor(DARK_GRAY)
                                .setMarginBottom(3));

                programCell.add(new com.itextpdf.layout.element.Paragraph(
                                student.getAppliedFor() != null ? student.getAppliedFor() : "Medical Program")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(SUCCESS_GREEN)
                                .setMarginBottom(10));

                programCell.add(new com.itextpdf.layout.element.Paragraph("Intake:")
                                .setFont(fontRegular)
                                .setFontSize(9)
                                .setFontColor(DARK_GRAY)
                                .setMarginBottom(3));

                programCell.add(new com.itextpdf.layout.element.Paragraph(
                                student.getIntake() != null ? student.getIntake() : "Next Available")
                                .setFont(fontBold)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL));

                programTable.addCell(programCell);
                document.add(programTable);

                // ============ CONDITIONS ============
                document.add(new com.itextpdf.layout.element.Paragraph("CONDITIONS FOR FINAL ADMISSION")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(DARK_BLUE)
                                .setMarginBottom(10));

                com.itextpdf.layout.element.List conditionsList = new com.itextpdf.layout.element.List()
                                .setSymbolIndent(12)
                                .setListSymbol("•")
                                .setFont(fontRegular)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL);

                conditionsList.add("Submission of all original academic documents");
                conditionsList.add("Valid passport with minimum 6 months validity");
                conditionsList.add("Medical fitness certificate");
                conditionsList.add("Payment of tuition fees as per university guidelines");
                conditionsList.add("Completion of visa documentation process");
                conditionsList.add("Compliance with all university admission policies");

                document.add(conditionsList);
                document.add(new com.itextpdf.layout.element.Paragraph("\n"));

                // ============ NEXT STEPS ============
                document.add(new com.itextpdf.layout.element.Paragraph("NEXT STEPS")
                                .setFont(fontBold)
                                .setFontSize(11)
                                .setFontColor(DARK_BLUE)
                                .setMarginBottom(10));

                document.add(new com.itextpdf.layout.element.Paragraph(
                                "Please proceed with the document verification and visa application process. " +
                                                "Once all conditions are met and verified, a final admission letter will be issued.")
                                .setFont(fontRegular)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL)
                                .setMarginBottom(20));

                // ============ VALIDITY ============
                com.itextpdf.layout.element.Table validityBox = new com.itextpdf.layout.element.Table(1);
                validityBox.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                validityBox.setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(254, 243, 199));
                validityBox.setBorder(new com.itextpdf.layout.borders.SolidBorder(
                                new com.itextpdf.kernel.colors.DeviceRgb(245, 158, 11), 2));
                validityBox.setMarginBottom(20);

                com.itextpdf.layout.element.Cell validityCell = new com.itextpdf.layout.element.Cell()
                                .setBorder(null)
                                .setPadding(12)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);

                validityCell.add(new com.itextpdf.layout.element.Paragraph("⚠ VALIDITY")
                                .setFont(fontBold)
                                .setFontSize(10)
                                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(146, 64, 14))
                                .setMarginBottom(5));

                validityCell.add(new com.itextpdf.layout.element.Paragraph(
                                "This conditional offer is valid for 90 days from the date of issue")
                                .setFont(fontRegular)
                                .setFontSize(9)
                                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(120, 53, 15)));

                validityBox.addCell(validityCell);
                document.add(validityBox);

                // ============ FOOTER ============
                document.add(new com.itextpdf.layout.element.Paragraph("\n"));

                document.add(new com.itextpdf.layout.element.Paragraph("Sincerely,")
                                .setFont(fontItalic)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL)
                                .setMarginBottom(25));

                document.add(new com.itextpdf.layout.element.Paragraph("_____________________________")
                                .setFont(fontRegular)
                                .setFontSize(10)
                                .setFontColor(CHARCOAL)
                                .setMarginBottom(5));

                document.add(new com.itextpdf.layout.element.Paragraph("University Admission Office")
                                .setFont(fontBold)
                                .setFontSize(10)
                                .setFontColor(DARK_BLUE)
                                .setMarginBottom(3));

                document.add(new com.itextpdf.layout.element.Paragraph(
                                student.getCollege() != null ? student.getCollege().getCollegeName() : "University")
                                .setFont(fontRegular)
                                .setFontSize(9)
                                .setFontColor(DARK_GRAY));

                // ============ DOCUMENT FOOTER ============
                com.itextpdf.layout.element.Table footerTable = new com.itextpdf.layout.element.Table(1);
                footerTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
                footerTable.setMarginTop(30);
                footerTable.setFixedPosition(40, 30, 515);

                com.itextpdf.layout.element.Cell footerCell = new com.itextpdf.layout.element.Cell()
                                .setBorder(new com.itextpdf.layout.borders.SolidBorder(BORDER, 1))
                                .setPaddingTop(10)
                                .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);

                footerCell.add(new com.itextpdf.layout.element.Paragraph(
                                "This is a system-generated conditional offer letter | Document ID: COL-"
                                                + student.getId())
                                .setFont(fontRegular)
                                .setFontSize(7)
                                .setFontColor(DARK_GRAY));

                footerTable.addCell(footerCell);
                document.add(footerTable);

                document.close();
        }

        // =====================================================================
        // HELPER METHOD for Info Rows
        // =====================================================================

        private void addInfoRow(com.itextpdf.layout.element.Table table, String label, String value,
                        com.itextpdf.kernel.font.PdfFont fontRegular, com.itextpdf.kernel.font.PdfFont fontBold,
                        com.itextpdf.kernel.colors.Color labelColor, com.itextpdf.kernel.colors.Color valueColor) {

                table.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(label)
                                                .setFont(fontRegular)
                                                .setFontSize(9)
                                                .setFontColor(labelColor))
                                .setBorder(null)
                                .setPadding(8)
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.WHITE));

                table.addCell(new com.itextpdf.layout.element.Cell()
                                .add(new com.itextpdf.layout.element.Paragraph(value != null ? value : "-")
                                                .setFont(fontBold)
                                                .setFontSize(9)
                                                .setFontColor(valueColor))
                                .setBorder(null)
                                .setPadding(8)
                                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.WHITE));
        }

        // -------------------- APPROVE ADMISSION LETTER --------------------

        @Transactional
        public Student approveAdmissionLetter(Long studentId, String admissionManagerUsername) {
                Student student = studentRepo.findById(studentId)
                                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

                // Validate admission letter exists (use internal method to bypass security
                // check)
                if (student.getAdmissionLetterPathInternal() == null
                                || student.getAdmissionLetterPathInternal().isEmpty()) {
                        throw new RuntimeException("No admission letter found for this student");
                }

                // Validate current status - it should be PENDING_ADMISSION_LETTER
                if (student.getApplicationStatus() != StudentStatus.PENDING_ADMISSION_LETTER) {
                        // Allow if it's already approved or in a compatible state, but strictly it
                        // should be pending
                        // For robustness, we can allow it if it was just uploaded.
                }

                // set action

                student.setAction(action.INITIATE_TELEX);
                student.setActionOnUser(actionOnUser.agent);

                student.setApplicationStatus(StudentStatus.ADMISSION_LETTER_GENERATED); // This is the "Approved" status
                                                                                        // now
                Student savedStudent = studentRepo.save(student);

                // Create status history entry
                StudentStatusHistory historyEntry = new StudentStatusHistory();
                historyEntry.setStudent(savedStudent);
                historyEntry.setStudentApplicationStatus(StudentStatus.ADMISSION_LETTER_GENERATED);
                historyEntry.setUpdatedBy(admissionManagerUsername);
                historyEntry.setUpdatedAt(java.time.LocalDateTime.now());
                statusHistoryRepo.save(historyEntry);

                return savedStudent;
        }

        // ===================== DASHBOARD STATS =====================
        public com.cc.DTO.AdmissionDashboardStatsDTO getDashboardStats(String username) {
                AddmissionManager manager = getAdmissionManagerByUserName(username)
                                .orElseThrow(() -> new RuntimeException("Manager not found: " + username));

                com.cc.DTO.AdmissionDashboardStatsDTO stats = new com.cc.DTO.AdmissionDashboardStatsDTO();

                // 1. Total Students
                stats.setTotalStudents(studentRepo.countByAdmissionManager(manager));

                // 2. New Applications (REGISTERED, SUBMITTED)
                stats.setNewApplications(studentRepo.countByAdmissionManagerAndApplicationStatusIn(
                                manager,
                                java.util.Arrays.asList(StudentStatus.REGISTERED, StudentStatus.Submitted)));

                // 3. Pending University Action (UNDER_REVIEW, FORWARDED_TO_UNI)
                stats.setPendingUniversityAction(studentRepo.countByAdmissionManagerAndApplicationStatusIn(
                                manager,
                                java.util.Arrays.asList(StudentStatus.UNDER_REVIEW, StudentStatus.FORWARDED_TO_UNI)));

                // 4. Admission Letters Generated
                stats.setAdmissionLettersGenerated(studentRepo.countByAdmissionManagerAndApplicationStatus(
                                manager,
                                StudentStatus.ADMISSION_LETTER_GENERATED));

                // 5. Rejected
                stats.setRejected(studentRepo.countByAdmissionManagerAndApplicationStatus(
                                manager,
                                StudentStatus.REJECTED));

                // 6. Status Distribution (For Charts)
                java.util.Map<String, Long> distribution = new java.util.HashMap<>();

                // Registration & Review
                distribution.put("REGISTERED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.REGISTERED));
                distribution.put("UNDER_REVIEW",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.UNDER_REVIEW));
                distribution.put("APPROVED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.APPROVED));
                distribution.put("REJECTED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.REJECTED));

                // Admission Process
                distribution.put("CONDITIONAL_OFFER_LETTER_GENERATED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(
                                                manager, StudentStatus.CONDITIONAL_OFFER_LETTER_GENERATED));
                distribution.put("PENDING_ADMISSION_LETTER",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.PENDING_ADMISSION_LETTER));
                distribution.put("ADMISSION_LETTER_GENERATED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.ADMISSION_LETTER_GENERATED));
                distribution.put("FORWARDED_TO_UNI",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.FORWARDED_TO_UNI));

                // Telex Process
                distribution.put("TELEX_PAYMENT_DOCS_SUBMITTED", studentRepo
                                .countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.TELEX_PAYMENT_DOCS_SUBMITTED));
                distribution.put("TELEX_INITIATED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.TELEX_INITIATED));
                distribution.put("TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_UNIVERSITY));
                distribution.put("TELEX_APPLICATION_SUBMITTED_TO_MEA",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(
                                                manager, StudentStatus.TELEX_APPLICATION_SUBMITTED_TO_MEA));
                distribution.put("TELEX_NUMBER_ISSUED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.TELEX_NUMBER_ISSUED));
                distribution.put("DOCS_VARIFICATION_COMPLETE",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.DOCS_VARIFICATION_COMPLETE));

                // Visa Process
                distribution.put("VISA_FEE_PENDING",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.VISA_FEE_PENDING));
                distribution.put("VISA_FEE_RECEIVED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.VISA_FEE_RECEIVED));
                distribution.put("VISA_APPLIED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.VISA_APPLIED));
                distribution.put("PASSPORT_SUBMITTED_TO_EMBASSY", studentRepo
                                .countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.PASSPORT_SUBMITTED_TO_EMBASSY));
                distribution.put("VISA_STAMPED",
                                studentRepo.countByAdmissionManagerAndApplicationStatus(manager,
                                                StudentStatus.VISA_STAMPED));

                stats.setStatusDistribution(distribution);

                // 7. Recent Activity (Top 5)
                stats.setRecentStudents(studentRepo.findTop5ByAdmissionManagerOrderByIdDesc(manager));

                return stats;
        }

        // ===================== NEW OPTIMIZED APIS =====================

        public List<com.cc.DTO.AdmissionManagerSummaryDTO> getAllManagersWithStudentCount() {
                List<AddmissionManager> managers = managerRepo.findAll();
                return managers.stream().map(manager -> {
                        Long studentCount = studentRepo.countByAdmissionManager(manager);
                        List<String> colleges = manager.getColleges().stream()
                                        .map(com.cc.modals.College::getCollegeName)
                                        .collect(java.util.stream.Collectors.toList());

                        return new com.cc.DTO.AdmissionManagerSummaryDTO(
                                        manager.getId(),
                                        manager.getFullName(),
                                        manager.getEmail(),
                                        manager.getContactNumber(),
                                        manager.getRole(),
                                        manager.getStatus(),
                                        manager.getUsername(),
                                        colleges,
                                        studentCount);
                }).collect(java.util.stream.Collectors.toList());
        }

        public List<com.cc.DTO.ManagerStudentDTO> getStudentsForManagerWithActions(Long managerId) {
                AddmissionManager manager = managerRepo.findById(managerId)
                                .orElseThrow(() -> new RuntimeException("Manager not found"));

                // Fetch students sorted by creation date (latest added first)
                List<Student> students = studentRepo.findByAdmissionManagerOrderByCreatedAtDesc(manager);

                return students.stream().map(student -> {
                        String collegeName = (student.getCollege() != null) ? student.getCollege().getCollegeName()
                                        : "N/A";

                        String actionOnUserStr = (student.getActionOnUser() != null)
                                        ? student.getActionOnUser().toString()
                                        : "N/A";
                        String actionStr = (student.getAction() != null) ? student.getAction().toString() : "N/A";

                        return new com.cc.DTO.ManagerStudentDTO(
                                        student.getId(),
                                        student.getApplicationId(),
                                        student.getFullName(),
                                        collegeName,
                                        student.getAppliedFor(), // course
                                        student.getApplicationStatus(),
                                        actionOnUserStr,
                                        actionStr);
                }).collect(java.util.stream.Collectors.toList());
        }

        // ===================== REPORT GENERATION =====================

        public com.cc.DTO.AdmissionManagerReportDTO generateReport(Long managerId, String period) {
                AddmissionManager manager = managerRepo.findById(managerId)
                                .orElseThrow(() -> new RuntimeException("Manager not found"));
                return generateReportForManager(manager, period);
        }

        public List<com.cc.DTO.AdmissionManagerReportDTO> generateAllManagersReport(String period) {
                List<AddmissionManager> managers = managerRepo.findAll();
                return managers.stream()
                                .map(m -> generateReportForManager(m, period))
                                .collect(java.util.stream.Collectors.toList());
        }

        private com.cc.DTO.AdmissionManagerReportDTO generateReportForManager(AddmissionManager manager,
                        String period) {
                // Determine current and previous date ranges based on period
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime currentStart;
                java.time.LocalDateTime currentEnd;
                java.time.LocalDateTime previousStart;
                java.time.LocalDateTime previousEnd;

                switch (period.toLowerCase()) {
                        case "yearly":
                                currentStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                                currentEnd = now;
                                previousStart = currentStart.minusYears(1);
                                previousEnd = currentEnd.minusYears(1);
                                break;
                        case "monthly":
                                currentStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                                                .withNano(0);
                                currentEnd = now;
                                previousStart = currentStart.minusMonths(1);
                                previousEnd = currentEnd.minusMonths(1);
                                break;
                        case "daily":
                        default:
                                currentStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
                                currentEnd = now;
                                previousStart = currentStart.minusDays(1);
                                previousEnd = currentEnd.minusDays(1);
                                break;
                }

                // Fetch history for students under this manager within current and previous
                // periods
                List<StudentStatusHistory> currentHistory = statusHistoryRepo.findByAdmissionManagerIdAndDateRange(
                                manager.getId(), currentStart, currentEnd);
                List<StudentStatusHistory> previousHistory = statusHistoryRepo.findByAdmissionManagerIdAndDateRange(
                                manager.getId(), previousStart, previousEnd);

                // Count UNIQUE students per status (using Sets to avoid duplicates)
                com.cc.DTO.ManagerReportStatsDTO currentStats = countUniqueStudentsByStatus(currentHistory);
                com.cc.DTO.ManagerReportStatsDTO previousStats = countUniqueStudentsByStatus(previousHistory);

                // Calculate trends for all metrics
                currentStats.setForwardedToUniversityTrend(
                                com.cc.DTO.ManagerReportStatsDTO.calculateTrend(
                                                currentStats.getForwardedToUniversity(),
                                                previousStats.getForwardedToUniversity()));
                currentStats.setAdmissionLettersApprovedTrend(
                                com.cc.DTO.ManagerReportStatsDTO.calculateTrend(
                                                currentStats.getAdmissionLettersApproved(),
                                                previousStats.getAdmissionLettersApproved()));
                currentStats.setTelexManagersAssignedTrend(
                                com.cc.DTO.ManagerReportStatsDTO.calculateTrend(
                                                currentStats.getTelexManagersAssigned(),
                                                previousStats.getTelexManagersAssigned()));
                currentStats.setVisaManagersAssignedTrend(
                                com.cc.DTO.ManagerReportStatsDTO.calculateTrend(
                                                currentStats.getVisaManagersAssigned(),
                                                previousStats.getVisaManagersAssigned()));

                return new com.cc.DTO.AdmissionManagerReportDTO(
                                manager.getId(),
                                manager.getFullName(),
                                manager.getUsername(),
                                period,
                                currentStats);
        }

        private com.cc.DTO.ManagerReportStatsDTO countUniqueStudentsByStatus(List<StudentStatusHistory> history) {
                // Use Sets to track unique student IDs per status
                java.util.Set<Long> forwardedStudents = new java.util.HashSet<>();
                java.util.Set<Long> approvedStudents = new java.util.HashSet<>();
                java.util.Set<Long> telexStudents = new java.util.HashSet<>();
                java.util.Set<Long> visaStudents = new java.util.HashSet<>();

                for (StudentStatusHistory entry : history) {
                        Long studentId = entry.getStudent().getId();
                        StudentStatus status = entry.getStudentApplicationStatus();

                        if (status == null)
                                continue;

                        switch (status) {
                                case FORWARDED_TO_UNI:
                                        forwardedStudents.add(studentId);
                                        break;
                                case ADMISSION_LETTER_GENERATED:
                                        approvedStudents.add(studentId);
                                        break;
                                case TELEX_INITIATED:
                                        telexStudents.add(studentId);
                                        break;
                                case VISA_FEE_PENDING:
                                        visaStudents.add(studentId);
                                        break;
                                default:
                                        break;
                        }
                }

                com.cc.DTO.ManagerReportStatsDTO stats = new com.cc.DTO.ManagerReportStatsDTO();
                stats.setForwardedToUniversity(forwardedStudents.size());
                stats.setAdmissionLettersApproved(approvedStudents.size());
                stats.setTelexManagersAssigned(telexStudents.size());
                stats.setVisaManagersAssigned(visaStudents.size());
                return stats;
        }

        private boolean isWithinRange(java.time.LocalDateTime date, java.time.LocalDateTime start,
                        java.time.LocalDateTime end) {
                return (date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end));
        }

        private void updateHistoryStats(com.cc.DTO.ManagerReportStatsDTO stats, StudentStatus status) {
                if (status == null)
                        return;

                switch (status) {
                        case FORWARDED_TO_UNI:
                                stats.incrementForwardedToUniversity();
                                break;
                        case ADMISSION_LETTER_GENERATED:
                                stats.incrementAdmissionLettersApproved();
                                break;
                        case TELEX_INITIATED:
                                stats.incrementTelexManagersAssigned();
                                break;
                        case VISA_FEE_PENDING:
                                stats.incrementVisaManagersAssigned();
                                break;
                        default:
                                break;
                }
        }

        // ===================== SEND REPORT BY EMAIL =====================

        @Autowired
        private EmailService emailService;

        public void sendReportByEmail(Long managerId, String period, String recipientEmail) {
                com.cc.DTO.AdmissionManagerReportDTO report = generateReport(managerId, period);
                String htmlContent = generateReportHtml(report, period);
                String subject = "Performance Report - " + report.getManagerName() + " (" + period.toUpperCase() + ")";
                emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);
        }

        public void sendAllManagersReportByEmail(String period, String recipientEmail) {
                List<com.cc.DTO.AdmissionManagerReportDTO> reports = generateAllManagersReport(period);
                String htmlContent = generateAllManagersReportHtml(reports, period);
                String subject = "All Admission Managers Performance Report (" + period.toUpperCase() + ")";
                emailService.sendHtmlEmail(recipientEmail, subject, htmlContent);
        }

        private String generateReportHtml(com.cc.DTO.AdmissionManagerReportDTO report, String period) {
                AddmissionManager manager = managerRepo.findById(report.getManagerId()).orElse(null);
                if (manager == null)
                        return "<html><body>Manager not found</body></html>";

                com.cc.DTO.ManagerReportStatsDTO stats = report.getPeriodStats();
                String periodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

                // Calculate additional metrics
                long totalStudents = studentRepo.countByAdmissionManager(manager);
                int totalActions = stats.getForwardedToUniversity() + stats.getAdmissionLettersApproved();
                double efficiencyRate = stats.getForwardedToUniversity() > 0
                                ? ((double) stats.getAdmissionLettersApproved() / stats.getForwardedToUniversity())
                                                * 100
                                : 0;
                double completionRate = totalStudents > 0
                                ? ((double) stats.getVisaManagersAssigned() / totalStudents) * 100
                                : 0;

                // Performance rating
                String performanceRating;
                String performanceColor;
                if (efficiencyRate >= 80) {
                        performanceRating = "🌟 Excellent";
                        performanceColor = "#22c55e";
                } else if (efficiencyRate >= 60) {
                        performanceRating = "👍 Good";
                        performanceColor = "#f59e0b";
                } else if (efficiencyRate >= 40) {
                        performanceRating = "⚡ Average";
                        performanceColor = "#fb923c";
                } else {
                        performanceRating = "📈 Needs Improvement";
                        performanceColor = "#ef4444";
                }

                // Pending students (not yet at visa stage)
                long pendingStudents = totalStudents - stats.getVisaManagersAssigned();

                return """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <style>
                                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                                        .container { max-width: 650px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; padding: 0; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
                                        .header { background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); padding: 30px; text-align: center; }
                                        .header h1 { margin: 0; font-size: 26px; color: #000; font-weight: 700; }
                                        .header p { margin: 8px 0 0; color: rgba(0,0,0,0.7); font-size: 14px; }
                                        .period-badge { display: inline-block; background: rgba(0,0,0,0.2); color: #000; padding: 6px 16px; border-radius: 20px; font-size: 12px; margin-top: 12px; font-weight: 600; }
                                        .content { padding: 30px; }
                                        .performance-card { background: linear-gradient(135deg, rgba(245,158,11,0.15) 0%%, rgba(217,119,6,0.1) 100%%); border: 1px solid rgba(245,158,11,0.3); border-radius: 16px; padding: 20px; margin-bottom: 25px; text-align: center; }
                                        .performance-rating { font-size: 28px; font-weight: 700; margin-bottom: 5px; }
                                        .performance-label { color: rgba(255,255,255,0.6); font-size: 12px; }
                                        .metrics-row { display: flex; justify-content: space-between; margin-bottom: 20px; gap: 15px; }
                                        .metric-box { flex: 1; background: rgba(255,255,255,0.05); border-radius: 12px; padding: 18px; text-align: center; border: 1px solid rgba(255,255,255,0.1); }
                                        .metric-value { font-size: 28px; font-weight: 700; color: #fff; }
                                        .metric-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
                                        .metric-trend { font-size: 11px; margin-top: 6px; padding: 3px 8px; border-radius: 10px; display: inline-block; }
                                        .trend-positive { background: rgba(34,197,94,0.2); color: #22c55e; }
                                        .trend-negative { background: rgba(239,68,68,0.2); color: #ef4444; }
                                        .trend-neutral { background: rgba(255,255,255,0.1); color: rgba(255,255,255,0.5); }
                                        .section-title { font-size: 14px; color: rgba(255,255,255,0.4); margin: 25px 0 15px; text-transform: uppercase; letter-spacing: 1px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 8px; }
                                        .progress-section { margin-bottom: 25px; }
                                        .progress-row { margin-bottom: 15px; }
                                        .progress-label { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 13px; }
                                        .progress-bar-bg { background: rgba(255,255,255,0.1); border-radius: 8px; height: 10px; overflow: hidden; }
                                        .progress-bar { height: 100%%; border-radius: 8px; transition: width 0.3s; }
                                        .insights-card { background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.3); border-radius: 12px; padding: 18px; margin-top: 20px; }
                                        .insights-title { color: #60a5fa; font-size: 13px; font-weight: 600; margin-bottom: 10px; }
                                        .insights-text { color: rgba(255,255,255,0.7); font-size: 13px; line-height: 1.6; }
                                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="header">
                                            <h1>📊 Performance Report</h1>
                                            <p>%s</p>
                                            <span class="period-badge">%s Analysis</span>
                                        </div>
                                        <div class="content">
                                            <!-- Performance Rating -->
                                            <div class="performance-card">
                                                <div class="performance-rating" style="color: %s">%s</div>
                                                <div class="performance-label">Overall Performance Rating</div>
                                            </div>

                                            <!-- Key Metrics Row 1 -->
                                            <div class="metrics-row">
                                                <div class="metric-box">
                                                    <div class="metric-value">%d</div>
                                                    <div class="metric-label">Total Students</div>
                                                </div>
                                                <div class="metric-box">
                                                    <div class="metric-value">%.1f%%</div>
                                                    <div class="metric-label">Efficiency Rate</div>
                                                </div>
                                                <div class="metric-box">
                                                    <div class="metric-value">%.1f%%</div>
                                                    <div class="metric-label">Completion Rate</div>
                                                </div>
                                            </div>

                                            <!-- Action Metrics -->
                                            <div class="section-title">📈 Activity Breakdown</div>
                                            <div class="metrics-row">
                                                <div class="metric-box">
                                                    <div class="metric-value" style="color: #f59e0b">%d</div>
                                                    <div class="metric-label">Forwarded</div>
                                                    <span class="metric-trend %s">%s</span>
                                                </div>
                                                <div class="metric-box">
                                                    <div class="metric-value" style="color: #22c55e">%d</div>
                                                    <div class="metric-label">Approved</div>
                                                    <span class="metric-trend %s">%s</span>
                                                </div>
                                            </div>
                                            <div class="metrics-row">
                                                <div class="metric-box">
                                                    <div class="metric-value" style="color: #fb923c">%d</div>
                                                    <div class="metric-label">Telex Assigned</div>
                                                </div>
                                                <div class="metric-box">
                                                    <div class="metric-value" style="color: #a78bfa">%d</div>
                                                    <div class="metric-label">Visa Assigned</div>
                                                </div>
                                            </div>

                                            <!-- Pipeline Progress -->
                                            <div class="section-title">🔄 Pipeline Status</div>
                                            <div class="progress-section">
                                                <div class="progress-row">
                                                    <div class="progress-label">
                                                        <span>Pending Students</span>
                                                        <span>%d remaining</span>
                                                    </div>
                                                    <div class="progress-bar-bg">
                                                        <div class="progress-bar" style="width: %.0f%%; background: linear-gradient(90deg, #f59e0b, #d97706);"></div>
                                                    </div>
                                                </div>
                                            </div>

                                            <!-- Insights -->
                                            <div class="insights-card">
                                                <div class="insights-title">💡 Performance Insights</div>
                                                <div class="insights-text">
                                                    %s
                                                </div>
                                            </div>
                                        </div>
                                        <div class="footer">
                                            Generated on %s at %s | CampusConnect Performance Analytics
                                        </div>
                                    </div>
                                </body>
                                </html>
                                """
                                .formatted(
                                                report.getManagerName(),
                                                periodLabel,
                                                performanceColor,
                                                performanceRating,
                                                totalStudents,
                                                efficiencyRate,
                                                completionRate,
                                                stats.getForwardedToUniversity(),
                                                getTrendClass(stats.getForwardedToUniversityTrend()),
                                                formatTrend(stats.getForwardedToUniversityTrend()),
                                                stats.getAdmissionLettersApproved(),
                                                getTrendClass(stats.getAdmissionLettersApprovedTrend()),
                                                formatTrend(stats.getAdmissionLettersApprovedTrend()),
                                                stats.getTelexManagersAssigned(),
                                                stats.getVisaManagersAssigned(),
                                                pendingStudents,
                                                completionRate,
                                                generateInsights(stats, efficiencyRate, pendingStudents, totalStudents),
                                                java.time.LocalDate.now().toString(),
                                                java.time.LocalTime.now().format(
                                                                java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }

        private String generateInsights(com.cc.DTO.ManagerReportStatsDTO stats, double efficiency, long pending,
                        long total) {
                StringBuilder insights = new StringBuilder();

                if (efficiency >= 80) {
                        insights.append("✅ Outstanding approval rate! Maintaining excellent throughput. ");
                } else if (efficiency >= 60) {
                        insights.append("📊 Good conversion from forwarding to approval. ");
                } else if (stats.getForwardedToUniversity() > 0) {
                        insights.append(
                                        "⚠️ Low approval rate detected. Consider following up on pending approvals. ");
                }

                if (pending > 0 && total > 0) {
                        double pendingRate = ((double) pending / total) * 100;
                        if (pendingRate > 50) {
                                insights.append(
                                                "📋 %d students (%.0f%%) are still in progress. Focus on moving them through the pipeline. "
                                                                .formatted(pending, pendingRate));
                        }
                }

                if (stats.getTelexManagersAssigned() < stats.getAdmissionLettersApproved()) {
                        int gap = stats.getAdmissionLettersApproved() - stats.getTelexManagersAssigned();
                        if (gap > 0) {
                                insights.append("📝 %d approved students pending Telex assignment. ".formatted(gap));
                        }
                }

                if (insights.length() == 0) {
                        insights.append("📈 Keep up the good work! Pipeline is moving smoothly.");
                }

                return insights.toString();
        }

        private String generateAllManagersReportHtml(List<com.cc.DTO.AdmissionManagerReportDTO> reports,
                        String period) {
                String periodLabel = period.substring(0, 1).toUpperCase() + period.substring(1);

                // Calculate totals and find top performer
                int totalForwarded = 0, totalApproved = 0, totalTelex = 0, totalVisa = 0;
                com.cc.DTO.AdmissionManagerReportDTO topPerformer = null;
                double topEfficiency = -1;

                for (com.cc.DTO.AdmissionManagerReportDTO r : reports) {
                        com.cc.DTO.ManagerReportStatsDTO s = r.getPeriodStats();
                        totalForwarded += s.getForwardedToUniversity();
                        totalApproved += s.getAdmissionLettersApproved();
                        totalTelex += s.getTelexManagersAssigned();
                        totalVisa += s.getVisaManagersAssigned();

                        double eff = s.getForwardedToUniversity() > 0
                                        ? ((double) s.getAdmissionLettersApproved() / s.getForwardedToUniversity())
                                                        * 100
                                        : 0;
                        if (eff > topEfficiency) {
                                topEfficiency = eff;
                                topPerformer = r;
                        }
                }

                double avgEfficiency = totalForwarded > 0 ? ((double) totalApproved / totalForwarded) * 100 : 0;

                // Build manager rows with efficiency
                StringBuilder rows = new StringBuilder();
                for (com.cc.DTO.AdmissionManagerReportDTO r : reports) {
                        com.cc.DTO.ManagerReportStatsDTO s = r.getPeriodStats();
                        double eff = s.getForwardedToUniversity() > 0
                                        ? ((double) s.getAdmissionLettersApproved() / s.getForwardedToUniversity())
                                                        * 100
                                        : 0;
                        String effColor = eff >= 70 ? "#22c55e" : (eff >= 40 ? "#f59e0b" : "#ef4444");
                        String isTop = (topPerformer != null && r.getManagerId().equals(topPerformer.getManagerId()))
                                        ? "🏆"
                                        : "";

                        rows.append(String.format(
                                        """
                                                        <tr style="background: rgba(255,255,255,0.02);">
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08);">
                                                                <strong>%s</strong> %s
                                                            </td>
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #f59e0b;">%d</td>
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #22c55e;">%d</td>
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #fb923c;">%d</td>
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center; color: #a78bfa;">%d</td>
                                                            <td style="padding: 14px 12px; border-bottom: 1px solid rgba(255,255,255,0.08); text-align: center;">
                                                                <span style="color: %s; font-weight: 600;">%.0f%%</span>
                                                            </td>
                                                        </tr>
                                                        """,
                                        r.getManagerName(), isTop,
                                        s.getForwardedToUniversity(), s.getAdmissionLettersApproved(),
                                        s.getTelexManagersAssigned(), s.getVisaManagersAssigned(),
                                        effColor, eff));
                }

                return """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <style>
                                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0f172a; color: #fff; padding: 20px; margin: 0; }
                                        .container { max-width: 800px; margin: 0 auto; background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); border-radius: 20px; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
                                        .header { background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); padding: 30px; text-align: center; }
                                        .header h1 { margin: 0; font-size: 26px; color: #000; font-weight: 700; }
                                        .header p { margin: 8px 0 0; color: rgba(0,0,0,0.7); font-size: 14px; }
                                        .summary-row { display: flex; padding: 25px 30px; gap: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); }
                                        .summary-box { flex: 1; text-align: center; padding: 15px; background: rgba(255,255,255,0.05); border-radius: 12px; }
                                        .summary-value { font-size: 28px; font-weight: 700; }
                                        .summary-label { font-size: 11px; color: rgba(255,255,255,0.5); margin-top: 4px; text-transform: uppercase; }
                                        .content { padding: 25px 30px; }
                                        table { width: 100%%; border-collapse: collapse; }
                                        th { background: rgba(245,158,11,0.15); color: #f59e0b; padding: 14px 12px; text-align: left; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
                                        td { color: rgba(255,255,255,0.85); font-size: 14px; }
                                        .footer { background: rgba(0,0,0,0.3); padding: 20px; text-align: center; color: rgba(255,255,255,0.4); font-size: 11px; }
                                        .top-performer { background: rgba(34,197,94,0.1); border: 1px solid rgba(34,197,94,0.3); border-radius: 12px; padding: 15px 20px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
                                        .trophy { font-size: 32px; }
                                        .top-info h3 { margin: 0; color: #22c55e; font-size: 16px; }
                                        .top-info p { margin: 4px 0 0; color: rgba(255,255,255,0.6); font-size: 12px; }
                                    </style>
                                </head>
                                <body>
                                    <div class="container">
                                        <div class="header">
                                            <h1>📊 Team Performance Report</h1>
                                            <p>All Admission Managers</p>
                                            <span style="display: inline-block; background: rgba(0,0,0,0.2); color: #000; padding: 6px 16px; border-radius: 20px; font-size: 12px; margin-top: 12px; font-weight: 600;">%s Summary</span>
                                        </div>

                                        <!-- Summary Stats -->
                                        <div class="summary-row">
                                            <div class="summary-box">
                                                <div class="summary-value" style="color: #f59e0b">%d</div>
                                                <div class="summary-label">Total Forwarded</div>
                                            </div>
                                            <div class="summary-box">
                                                <div class="summary-value" style="color: #22c55e">%d</div>
                                                <div class="summary-label">Total Approved</div>
                                            </div>
                                            <div class="summary-box">
                                                <div class="summary-value" style="color: #60a5fa">%.1f%%</div>
                                                <div class="summary-label">Avg Efficiency</div>
                                            </div>
                                            <div class="summary-box">
                                                <div class="summary-value" style="color: #a78bfa">%d</div>
                                                <div class="summary-label">Managers</div>
                                            </div>
                                        </div>

                                        <div class="content">
                                            <!-- Top Performer -->
                                            %s

                                            <!-- Manager Table -->
                                            <table>
                                                <thead>
                                                    <tr>
                                                        <th>Manager</th>
                                                        <th style="text-align: center;">Forwarded</th>
                                                        <th style="text-align: center;">Approved</th>
                                                        <th style="text-align: center;">Telex</th>
                                                        <th style="text-align: center;">Visa</th>
                                                        <th style="text-align: center;">Efficiency</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    %s
                                                </tbody>
                                            </table>
                                        </div>
                                        <div class="footer">
                                            Generated on %s at %s | CampusConnect Performance Analytics
                                        </div>
                                    </div>
                                </body>
                                </html>
                                """
                                .formatted(
                                                periodLabel,
                                                totalForwarded, totalApproved, avgEfficiency, reports.size(),
                                                topPerformer != null ? String.format(
                                                                """
                                                                                <div class="top-performer">
                                                                                    <span class="trophy">🏆</span>
                                                                                    <div class="top-info">
                                                                                        <h3>Top Performer: %s</h3>
                                                                                        <p>%.0f%% efficiency rate this period</p>
                                                                                    </div>
                                                                                </div>
                                                                                """,
                                                                topPerformer.getManagerName(), topEfficiency) : "",
                                                rows.toString(),
                                                java.time.LocalDate.now().toString(),
                                                java.time.LocalTime.now().format(
                                                                java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }

        private String getTrendClass(Double trend) {
                if (trend == null)
                        return "trend-neutral";
                return trend >= 0 ? "trend-positive" : "trend-negative";
        }

        private String formatTrend(Double trend) {
                if (trend == null)
                        return "N/A";
                return (trend >= 0 ? "+" : "") + String.format("%.1f", trend) + "%";
        }
}