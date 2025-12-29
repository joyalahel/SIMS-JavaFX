package sims.model;

public class Enrollment {
    private int enrollmentId;
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseName;
    private Double grade;
    private String status;
    private int credits;
    
    public Enrollment() {}
    
    // Getters and Setters
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    
    public String getGradeDisplay() {
    return grade != null ? String.format("%.1f", grade) : "Not Graded";
}

public String getLetterGrade() {
    if (grade == null) {
        return "Not Graded";
    } else if (grade >= 97) return "A+";
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
    @Override
    public String toString() {
        return studentName + " - " + courseName + " (" + getGradeDisplay() + ")";
    }
}