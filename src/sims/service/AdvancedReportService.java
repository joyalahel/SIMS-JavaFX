package sims.service;

// Excel imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// PDF imports for iTextPDF 5
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;

import sims.dao.StudentDao;
import sims.dao.CourseDao;
import sims.dao.InstructorDao;
import sims.dao.EnrollmentDao;
import sims.model.Student;
import sims.model.Course;
import sims.model.Instructor;
import sims.model.Enrollment;

import javafx.concurrent.Task;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdvancedReportService {
    private StudentDao studentDao;
    private CourseDao courseDao;
    private InstructorDao instructorDao;
    private EnrollmentDao enrollmentDao;
    
    public AdvancedReportService() {
        this.studentDao = new StudentDao();
        this.courseDao = new CourseDao();
        this.instructorDao = new InstructorDao();
        this.enrollmentDao = new EnrollmentDao();
    }
    
  public Task<Void> generateExcelSystemReport() {
    return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Creating Excel workbook...");
            updateProgress(0.1, 1.0);
            
            Workbook workbook = null;
            FileOutputStream outputStream = null;
            
            try {
                // Load fresh data
                updateMessage("Loading student data...");
                updateProgress(0.2, 1.0);
                studentDao.loadAllStudents();
                List<Student> students = studentDao.getStudentData();
                
                // Validate student data
                if (students == null) {
                    throw new IllegalStateException("Failed to load student data");
                }
                System.out.println("Loaded " + students.size() + " students");
                Thread.sleep(300);
                
                updateMessage("Loading course data...");
                updateProgress(0.35, 1.0);
                courseDao.loadAllCourses();
                List<Course> courses = courseDao.getCourseData();
                
                // Validate course data
                if (courses == null) {
                    throw new IllegalStateException("Failed to load course data");
                }
                System.out.println("Loaded " + courses.size() + " courses");
                Thread.sleep(300);
                
                updateMessage("Loading instructor data...");
                updateProgress(0.5, 1.0);
                instructorDao.loadAllInstructors();
                List<Instructor> instructors = instructorDao.getInstructorData();
                
                // Validate instructor data
                if (instructors == null) {
                    throw new IllegalStateException("Failed to load instructor data");
                }
                System.out.println("Loaded " + instructors.size() + " instructors");
                Thread.sleep(300);
                
                updateMessage("Loading enrollment data...");
                updateProgress(0.65, 1.0);
                enrollmentDao.loadAllEnrollments();
                List<Enrollment> enrollments = enrollmentDao.getEnrollmentData();
                
                // Validate enrollment data
                if (enrollments == null) {
                    throw new IllegalStateException("Failed to load enrollment data");
                }
                System.out.println("Loaded " + enrollments.size() + " enrollments");
                Thread.sleep(300);
                
                updateProgress(0.75, 1.0);
                updateMessage("Creating Excel file structure...");
                
                // Create Excel workbook
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "reports/system_report_" + timestamp + ".xlsx";
                
                // Ensure reports directory exists
                File reportsDir = new File("reports");
                if (!reportsDir.exists()) {
                    boolean created = reportsDir.mkdirs();
                    System.out.println("Reports directory created: " + created);
                }
                
                workbook = new XSSFWorkbook();
                System.out.println("Excel workbook created");
                
                // Create summary sheet
                updateMessage("Creating summary sheet...");
                updateProgress(0.80, 1.0);
                createSummarySheet(workbook, students.size(), courses.size(), 
                                 instructors.size(), enrollments.size());
                System.out.println("Summary sheet created");
                Thread.sleep(200);
                
                // Create students sheet
                updateMessage("Creating students sheet...");
                updateProgress(0.85, 1.0);
                createStudentsSheet(workbook, students);
                System.out.println("Students sheet created");
                Thread.sleep(200);
                
                // Create courses sheet
                updateMessage("Creating courses sheet...");
                updateProgress(0.90, 1.0);
                createCoursesSheet(workbook, courses);
                System.out.println("Courses sheet created");
                Thread.sleep(200);
                
                // Create instructors sheet - FIXED: was calling createCoursesSheet again
                updateMessage("Creating instructors sheet...");
                updateProgress(0.93, 1.0);
                createInstructorsSheet(workbook, instructors);
                System.out.println("Instructors sheet created");
                Thread.sleep(200);
                
                // Create enrollments sheet (NEW)
                updateMessage("Creating enrollments sheet...");
                updateProgress(0.96, 1.0);
                createEnrollmentsSheet(workbook, enrollments);
                System.out.println("Enrollments sheet created");
                Thread.sleep(200);
                
                // Write to file
                updateMessage("Writing Excel file to disk...");
                updateProgress(0.98, 1.0);
                outputStream = new FileOutputStream(filename);
                workbook.write(outputStream);
                System.out.println("Excel file written successfully: " + filename);
                
                updateProgress(1.0, 1.0);
                updateMessage("Excel report generated successfully!");
                Thread.sleep(300);
                
            } catch (IllegalStateException e) {
                updateMessage("Data validation error: " + e.getMessage());
                System.err.println("Data Validation Error: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                updateMessage("Error generating Excel report: " + e.getMessage());
                System.err.println("Excel Generation Error Details:");
                System.err.println("  - Error type: " + e.getClass().getSimpleName());
                System.err.println("  - Error message: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                // Close resources in finally block
                if (workbook != null) {
                    try {
                        workbook.close();
                        System.out.println("Workbook closed successfully");
                    } catch (Exception e) {
                        System.err.println("Error closing workbook: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                        System.out.println("Output stream closed successfully");
                    } catch (Exception e) {
                        System.err.println("Error closing output stream: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            return null;
        }
    };
}
    
    public Task<Void> generatePdfSystemReport() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Creating PDF document...");
                updateProgress(0.1, 1.0);
                Thread.sleep(800);
                
                FileOutputStream fileOutputStream = null;
                Document document = null;
                
                try {
                    // Load fresh data
                    updateMessage("Loading system data...");
                    updateProgress(0.3, 1.0);
                    studentDao.loadAllStudents();
                    courseDao.loadAllCourses();
                    instructorDao.loadAllInstructors();
                    enrollmentDao.loadAllEnrollments();
                    Thread.sleep(1000);
                    
                    updateProgress(0.6, 1.0);
                    updateMessage("Generating PDF content...");
                    Thread.sleep(800);
                    
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String filename = "reports/system_report_" + timestamp + ".pdf";
                    
                    // Create reports directory
                    java.io.File reportsDir = new java.io.File("reports");
                    if (!reportsDir.exists()) {
                        reportsDir.mkdirs();
                    }
                    
                    // Create PDF document
                    document = new Document();
                    fileOutputStream = new FileOutputStream(filename);
                    PdfWriter.getInstance(document, fileOutputStream);
                    
                    document.open();
                    
                    // Add title
                    Paragraph title = new Paragraph("STUDENT INFORMATION MANAGEMENT SYSTEM\n\n");
                    title.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(title);
                    
                    // Add date
                    Paragraph date = new Paragraph("Report Generated: " + new Date() + "\n\n");
                    date.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(date);
                    
                    // Add summary table
                    PdfPTable table = new PdfPTable(2);
                    table.setWidthPercentage(100);
                    
                    // Add table headers
                    table.addCell(createPdfCell("Metric", true));
                    table.addCell(createPdfCell("Count", true));
                    
                    // Add table data
                    table.addCell(createPdfCell("Total Students", false));
                    table.addCell(createPdfCell(String.valueOf(studentDao.getStudentData().size()), false));
                    
                    table.addCell(createPdfCell("Total Courses", false));
                    table.addCell(createPdfCell(String.valueOf(courseDao.getCourseData().size()), false));
                    
                    table.addCell(createPdfCell("Total Instructors", false));
                    table.addCell(createPdfCell(String.valueOf(instructorDao.getInstructorData().size()), false));
                    
                    table.addCell(createPdfCell("Total Enrollments", false));
                    table.addCell(createPdfCell(String.valueOf(enrollmentDao.getEnrollmentData().size()), false));
                    
                    document.add(table);
                    document.add(new Paragraph("\n"));
                    
                    // Add footer
                    Paragraph footer = new Paragraph("This report was automatically generated by SIMS");
                    footer.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(footer);
                    
                    document.close();
                    
                    updateProgress(1.0, 1.0);
                    updateMessage("PDF report generated successfully!");
                    Thread.sleep(300);
                    
                } catch (Exception e) {
                    updateMessage("Error generating PDF: " + e.getMessage());
                    e.printStackTrace();
                    if (document != null && document.isOpen()) {
                        document.close();
                    }
                    throw e;
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                return null;
            }
        };
    }
    /**
 * Creates an Enrollments sheet in the Excel workbook with detailed enrollment information
 */
private void createEnrollmentsSheet(Workbook workbook, List<Enrollment> enrollments) {
    Sheet sheet = workbook.createSheet("Enrollments");
    
    // Create header style
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 12);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    
    // Create data style
    CellStyle dataStyle = workbook.createCellStyle();
    dataStyle.setBorderBottom(BorderStyle.THIN);
    dataStyle.setBorderTop(BorderStyle.THIN);
    dataStyle.setBorderLeft(BorderStyle.THIN);
    dataStyle.setBorderRight(BorderStyle.THIN);
    
    // Create grade style for numeric grades
    CellStyle gradeStyle = workbook.createCellStyle();
    gradeStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0"));
    gradeStyle.setBorderBottom(BorderStyle.THIN);
    gradeStyle.setBorderTop(BorderStyle.THIN);
    gradeStyle.setBorderLeft(BorderStyle.THIN);
    gradeStyle.setBorderRight(BorderStyle.THIN);
    
    // Define headers
    String[] headers = {"Student ID", "Course Code", "Course Name", "Grade", "Letter Grade", "Status"};
    
    // Create header row
    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }
    
    // Add enrollment data
    for (int i = 0; i < enrollments.size(); i++) {
        Enrollment enrollment = enrollments.get(i);
        Row row = sheet.createRow(i + 1);
        
        // Student ID
        Cell studentIdCell = row.createCell(0);
        studentIdCell.setCellValue(enrollment.getStudentId());
        studentIdCell.setCellStyle(dataStyle);
        
        // Course Code
        Cell courseCodeCell = row.createCell(1);
        courseCodeCell.setCellValue(enrollment.getCourseId());
        courseCodeCell.setCellStyle(dataStyle);
        
        // Course Name
        Cell courseNameCell = row.createCell(2);
        courseNameCell.setCellValue(enrollment.getCourseName() != null ? 
            enrollment.getCourseName() : "N/A");
        courseNameCell.setCellStyle(dataStyle);
        
        // Numeric Grade
        Cell gradeCell = row.createCell(3);
        if (enrollment.getGrade() != null) {
            gradeCell.setCellValue(enrollment.getGrade());
            gradeCell.setCellStyle(gradeStyle);
        } else {
            gradeCell.setCellValue("N/A");
            gradeCell.setCellStyle(dataStyle);
        }
        
        // Letter Grade
        Cell letterGradeCell = row.createCell(4);
        letterGradeCell.setCellValue(getLetterGrade(enrollment.getGrade()));
        letterGradeCell.setCellStyle(dataStyle);
        
        // Status
        Cell statusCell = row.createCell(5);
        statusCell.setCellValue(enrollment.getStatus() != null ? 
            enrollment.getStatus() : "Unknown");
        statusCell.setCellStyle(dataStyle);
    }
    
    // Add summary row
    int summaryRowIndex = enrollments.size() + 2;
    Row summaryRow = sheet.createRow(summaryRowIndex);
    
    CellStyle summaryStyle = workbook.createCellStyle();
    Font summaryFont = workbook.createFont();
    summaryFont.setBold(true);
    summaryFont.setColor(IndexedColors.DARK_BLUE.getIndex());
    summaryStyle.setFont(summaryFont);
    
    Cell summaryCell = summaryRow.createCell(0);
    summaryCell.setCellValue("Total Enrollments: " + enrollments.size());
    summaryCell.setCellStyle(summaryStyle);
    
    // Auto-size all columns
    for (int i = 0; i < headers.length; i++) {
        sheet.autoSizeColumn(i);
        // Add a bit of padding
        sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
    }
    
    System.out.println("Created Enrollments sheet with " + enrollments.size() + " records");
}
  public Task<Void> generateExcelStudentReport(String studentId) {
    return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            updateMessage("Creating Excel student report...");
            updateProgress(0.1, 1.0);
            
            Workbook workbook = null;
            FileOutputStream outputStream = null;
            
            try {
                // Load student data
                updateMessage("Loading student information...");
                updateProgress(0.2, 1.0);
                Student student = studentDao.getStudentById(studentId);
                if (student == null) {
                    throw new Exception("Student not found with ID: " + studentId);
                }
                Thread.sleep(300);
                
                updateMessage("Loading enrollment data...");
                updateProgress(0.4, 1.0);
                List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
                Thread.sleep(300);
                
                updateProgress(0.6, 1.0);
                updateMessage("Generating Excel file...");
                
                // Create Excel workbook
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filename = "reports/student_report_" + studentId + "_" + timestamp + ".xlsx";
                new java.io.File("reports").mkdirs();
                
                workbook = new XSSFWorkbook();
                
                // Create student info sheet
                createStudentInfoSheet(workbook, student, enrollments);
                
                // Create grades sheet
                createStudentGradesSheet(workbook, enrollments);
                
                // Write to file
                outputStream = new FileOutputStream(filename);
                workbook.write(outputStream);
                
                updateProgress(1.0, 1.0);
                updateMessage("Excel student report generated successfully!");
                Thread.sleep(300);
                
            } catch (Exception e) {
                updateMessage("Error generating Excel student report: " + e.getMessage());
                System.out.println("Excel Student Report Error: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                if (workbook != null) {
                    try { workbook.close(); } catch (Exception e) {}
                }
                if (outputStream != null) {
                    try { outputStream.close(); } catch (Exception e) {}
                }
            }
            
            return null;
        }
    };
}
  
    public Task<Void> generatePdfStudentReport(String studentId) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Creating PDF student report...");
                updateProgress(0.1, 1.0);
                Thread.sleep(800);
                
                FileOutputStream fileOutputStream = null;
                Document document = null;
                
                try {
                    // Load student data
                    updateMessage("Loading student information...");
                    updateProgress(0.3, 1.0);
                    Student student = studentDao.getStudentById(studentId);
                    List<Enrollment> enrollments = enrollmentDao.getEnrollmentsByStudent(studentId);
                    Thread.sleep(1000);
                    
                    updateProgress(0.6, 1.0);
                    updateMessage("Generating PDF content...");
                    Thread.sleep(800);
                    
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String filename = "reports/student_report_" + studentId + "_" + timestamp + ".pdf";
                    
                    // Create reports directory
                    java.io.File reportsDir = new java.io.File("reports");
                    if (!reportsDir.exists()) {
                        reportsDir.mkdirs();
                    }
                    
                    // Create PDF document
                    document = new Document();
                    fileOutputStream = new FileOutputStream(filename);
                    PdfWriter.getInstance(document, fileOutputStream);
                    
                    document.open();
                    
                    // Add title
                    Paragraph title = new Paragraph("STUDENT TRANSCRIPT\n\n");
                    title.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(title);
                    
                    // Add student info
                    Paragraph studentInfo = new Paragraph(
                        "Student ID: " + student.getStudentId() + "\n" +
                        "Name: " + student.getFirstName() + " " + student.getLastName() + "\n" +
                        "Email: " + student.getEmail() + "\n" +
                        "Major: " + student.getMajor() + "\n\n"
                    );
                    document.add(studentInfo);
                    
                    // Add grades table
                    PdfPTable table = new PdfPTable(4);
                    table.setWidthPercentage(100);
                    
                    // Add table headers
                    table.addCell(createPdfCell("Course Code", true));
                    table.addCell(createPdfCell("Course Name", true));
                    table.addCell(createPdfCell("Grade", true));
                    table.addCell(createPdfCell("Status", true));
                    
                    // Add table data
                    for (Enrollment enrollment : enrollments) {
                        table.addCell(createPdfCell(enrollment.getCourseId(), false));
                        table.addCell(createPdfCell(enrollment.getCourseName(), false));
                        table.addCell(createPdfCell(enrollment.getGrade() != null ? String.valueOf(enrollment.getGrade()) : "N/A", false));
                        table.addCell(createPdfCell(enrollment.getStatus(), false));
                    }
                    
                    document.add(table);
                    document.add(new Paragraph("\n"));
                    
                    // Calculate and add GPA
                    double gpa = calculateGPA(enrollments);
                    Paragraph gpaInfo = new Paragraph("Cumulative GPA: " + String.format("%.2f", gpa));
                    gpaInfo.setAlignment(Paragraph.ALIGN_RIGHT);
                    document.add(gpaInfo);
                    
                    document.add(new Paragraph("\n"));
                    
                    // Add footer
                    Paragraph footer = new Paragraph("Official Student Transcript - Generated by SIMS");
                    footer.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(footer);
                    
                    document.close();
                    
                    updateProgress(1.0, 1.0);
                    updateMessage("PDF student report generated successfully!");
                    Thread.sleep(300);
                    
                } catch (Exception e) {
                    updateMessage("Error generating PDF student report");
                    throw e;
                } finally {
                    if (document != null && document.isOpen()) {
                        document.close();
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                return null;
            }
        };
    }
    
    private PdfPCell createPdfCell(String content, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(content));
        if (isHeader) {
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        }
        return cell;
    }
    
    private void createSummarySheet(Workbook workbook, int studentCount, int courseCount, int instructorCount, int enrollmentCount) {
        Sheet sheet = workbook.createSheet("System Summary");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        
        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Metric", "Count"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        String[][] data = {
            {"Total Students", String.valueOf(studentCount)},
            {"Total Courses", String.valueOf(courseCount)},
            {"Total Instructors", String.valueOf(instructorCount)},
            {"Total Enrollments", String.valueOf(enrollmentCount)},
            {"Total Records", String.valueOf(studentCount + courseCount + instructorCount + enrollmentCount)}
        };
        
        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data[i].length; j++) {
                org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createStudentsSheet(Workbook workbook, List<Student> students) {
        Sheet sheet = workbook.createSheet("Students");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        String[] headers = {"Student ID", "First Name", "Last Name", "Email", "Major"};
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add student data
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(student.getStudentId());
            row.createCell(1).setCellValue(student.getFirstName());
            row.createCell(2).setCellValue(student.getLastName());
            row.createCell(3).setCellValue(student.getEmail());
            row.createCell(4).setCellValue(student.getMajor());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createCoursesSheet(Workbook workbook, List<Course> courses) {
        Sheet sheet = workbook.createSheet("Courses");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        String[] headers = {"Course Code", "Course Name", "Instructor", "Credits", "Department"};
        
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(course.getCourseCode());
            row.createCell(1).setCellValue(course.getCourseName());
            row.createCell(2).setCellValue(course.getInstructorName());
            row.createCell(3).setCellValue(course.getCredits());
            row.createCell(4).setCellValue(course.getDepartment());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createInstructorsSheet(Workbook workbook, List<Instructor> instructors) {
        Sheet sheet = workbook.createSheet("Instructors");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        String[] headers = {"Instructor ID", "First Name", "Last Name", "Email", "Department"};
        
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        for (int i = 0; i < instructors.size(); i++) {
            Instructor instructor = instructors.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(instructor.getInstructorId());
            row.createCell(1).setCellValue(instructor.getFirstName());
            row.createCell(2).setCellValue(instructor.getLastName());
            row.createCell(3).setCellValue(instructor.getEmail());
            row.createCell(4).setCellValue(instructor.getDepartment());
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createStudentInfoSheet(Workbook workbook, Student student, List<Enrollment> enrollments) {
        Sheet sheet = workbook.createSheet("Student Information");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        
        // Student Information Section
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("STUDENT INFORMATION");
        headerRow.getCell(0).setCellStyle(headerStyle);
        
        // Student Details
        String[][] studentData = {
            {"Student ID:", student.getStudentId()},
            {"First Name:", student.getFirstName()},
            {"Last Name:", student.getLastName()},
            {"Email:", student.getEmail()},
            {"Major:", student.getMajor()},
            {"Total Courses:", String.valueOf(enrollments.size())}
        };
        
        for (int i = 0; i < studentData.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.createCell(0).setCellValue(studentData[i][0]);
            row.createCell(1).setCellValue(studentData[i][1]);
            
            // Style the label cells
            row.getCell(0).setCellStyle(headerStyle);
            row.getCell(1).setCellStyle(dataStyle);
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void createStudentGradesSheet(Workbook workbook, List<Enrollment> enrollments) {
        Sheet sheet = workbook.createSheet("Grades & Courses");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Create data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        
        // Create grade style for numeric grades
        CellStyle gradeStyle = workbook.createCellStyle();
        gradeStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0"));
        
        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Course Code", "Course Name", "Grade", "Letter Grade", "Status", "Credits"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add enrollment data
        for (int i = 0; i < enrollments.size(); i++) {
            Enrollment enrollment = enrollments.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(enrollment.getCourseId());
            row.createCell(1).setCellValue(enrollment.getCourseName());
            
            // Grade cell with numeric formatting
            Cell gradeCell = row.createCell(2);
            if (enrollment.getGrade() != null) {
                gradeCell.setCellValue(enrollment.getGrade());
                gradeCell.setCellStyle(gradeStyle);
            } else {
                gradeCell.setCellValue("N/A");
            }
            
            row.createCell(3).setCellValue(getLetterGrade(enrollment.getGrade()));
            row.createCell(4).setCellValue(enrollment.getStatus());
            row.createCell(5).setCellValue(3); // Assuming 3 credits per course
            
            // Apply data style to all cells
            for (int j = 0; j < headers.length; j++) {
                row.getCell(j).setCellStyle(dataStyle);
            }
        }
        
        // Add GPA summary
        int lastRow = enrollments.size() + 3;
        Row gpaRow = sheet.createRow(lastRow);
        gpaRow.createCell(0).setCellValue("Cumulative GPA:");
        
        Cell gpaCell = gpaRow.createCell(1);
        double gpa = calculateGPA(enrollments);
        gpaCell.setCellValue(gpa);
        gpaCell.setCellStyle(gradeStyle);
        
        // Style the GPA row
        CellStyle gpaHeaderStyle = workbook.createCellStyle();
        Font gpaFont = workbook.createFont();
        gpaFont.setBold(true);
        gpaHeaderStyle.setFont(gpaFont);
        gpaRow.getCell(0).setCellStyle(gpaHeaderStyle);
        
        CellStyle gpaValueStyle = workbook.createCellStyle();
        gpaValueStyle.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        Font gpaValueFont = workbook.createFont();
        gpaValueFont.setBold(true);
        gpaValueStyle.setFont(gpaValueFont);
        gpaCell.setCellStyle(gpaValueStyle);
        
        // Auto-size all columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    // Helper method to get letter grade
    private String getLetterGrade(Double grade) {
        if (grade == null) return "Not Graded";
        else if (grade >= 97) return "A+";
        else if (grade >= 93) return "A";
        else if (grade >= 90) return "A-";
        else if (grade >= 87) return "B+";
        else if (grade >= 83) return "B";
        else if (grade >= 80) return "B-";
        else if (grade >= 77) return "C+";
        else if (grade >= 73) return "C";
        else if (grade >= 70) return "C-";
        else if (grade >= 67) return "D+";
        else if (grade >= 63) return "D";
        else if (grade >= 60) return "D-";
        else return "F";
    }
    
    // Helper method to calculate GPA
    private double calculateGPA(List<Enrollment> enrollments) {
        double totalPoints = 0;
        int totalCredits = 0;
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getGrade() != null) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                totalPoints += gradePoints * 3; // Assuming 3 credits per course
                totalCredits += 3;
            }
        }
        
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    
    // Helper method to convert numeric grade to grade points
    private double convertGradeToPoints(double grade) {
        if (grade >= 90) return 4.0;
        else if (grade >= 80) return 3.0;
        else if (grade >= 70) return 2.0;
        else if (grade >= 60) return 1.0;
        else return 0.0;
    }
   
}