package sims.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Attendance {
    private int attendanceId;
    private String studentId;
    private String courseId;
    private LocalDate attendanceDate;
    private LocalTime attendanceTime;
    private String status;
    private String instructorId;
    private Integer weekNumber;
    private String studentName;
    private String courseName;
    
    // Constructors
    public Attendance() {}
    
    public Attendance(String studentId, String courseId, String status, String instructorId, int weekNumber) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.instructorId = instructorId;
        this.weekNumber = weekNumber;
        this.attendanceDate = LocalDate.now();
        this.attendanceTime = LocalTime.now();
    }
    
    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    
    public LocalTime getAttendanceTime() { return attendanceTime; }
    public void setAttendanceTime(LocalTime attendanceTime) { this.attendanceTime = attendanceTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }
    
    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    @Override
    public String toString() {
        return String.format("Attendance{student=%s, course=%s, status=%s, week=%d}", 
            studentId, courseId, status, weekNumber);
    }
}