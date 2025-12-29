package sims.model;

import java.sql.Date;

public class Student {
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private Date dateOfBirth;
    private String major;
    private Double gpa;
    private int enrolledCourses;
    private int gradedCourses;
    
    public Student() {}
    
    public Student(String studentId, String firstName, String lastName, String email, Date dateOfBirth, String major) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.major = major;
    }
    
    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public Double getGpa() { return gpa != null ? gpa : 0.0; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    
    public int getEnrolledCourses() {  return enrolledCourses; }
    public void setEnrolledCourses(int enrolledCourses) { this.enrolledCourses = enrolledCourses; }
    
    public int getGradedCourses() { return gradedCourses; }
    public void setGradedCourses(int gradedCourses) { this.gradedCourses = gradedCourses; }
    
    // Helper method for full name display
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", major='" + major + '\'' +
                ", gpa='" + gpa + '\'' +
                ", enrolledCourses='" + enrolledCourses + '\'' +
                ", gradedCourses='" + gradedCourses + '\'' +
                '}';
    }
}