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
                        String conditionalOfferFileName = generateConditionalOfferLetter(savedStudent);

                        // Update student with conditional offer letter path
                        savedStudent.setConditionalOfferLetterPath(
                                        "uploads/conditional-offer-letters/" + conditionalOfferFileName);
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
                String fileName = "conditional_offer_" + student.getId() + "_" + java.util.UUID.randomUUID() + ".pdf";
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads/conditional-offer-letters");

                if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                }

                String fullPath = uploadPath.resolve(fileName).toString();

                // Generate the PDF
                generateConditionalOfferPdf(student, fullPath);

                return fileName;
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
}