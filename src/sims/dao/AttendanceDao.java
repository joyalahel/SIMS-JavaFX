package sims.dao;

import sims.model.Attendance;
import sims.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {
    
    // Mark attendance for a student (SQL Server MERGE statement)
    public boolean markAttendance(Attendance attendance) {
        String sql = "MERGE attendance AS target " +
                    "USING (VALUES (?, ?, ?, ?, ?, ?, ?)) AS source (student_id, course_id, attendance_date, attendance_time, status, instructor_id, week_number) " +
                    "ON target.student_id = source.student_id AND target.course_id = source.course_id AND target.week_number = source.week_number " +
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET status = source.status, attendance_date = source.attendance_date, attendance_time = source.attendance_time " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (student_id, course_id, attendance_date, attendance_time, status, instructor_id, week_number) " +
                    "    VALUES (source.student_id, source.course_id, source.attendance_date, source.attendance_time, source.status, source.instructor_id, source.week_number);";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, attendance.getStudentId());
            stmt.setString(2, attendance.getCourseId());
            stmt.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
            stmt.setTime(4, Time.valueOf(attendance.getAttendanceTime()));
            stmt.setString(5, attendance.getStatus());
            stmt.setString(6, attendance.getInstructorId());
            stmt.setInt(7, attendance.getWeekNumber());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Mark attendance for multiple students
    public boolean markMultipleAttendance(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return false;
        }
        
        String sql = "MERGE attendance AS target " +
                    "USING (VALUES (?, ?, ?, ?, ?, ?, ?)) AS source (student_id, course_id, attendance_date, attendance_time, status, instructor_id, week_number) " +
                    "ON target.student_id = source.student_id AND target.course_id = source.course_id AND target.week_number = source.week_number " +
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET status = source.status, attendance_date = source.attendance_date, attendance_time = source.attendance_time " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (student_id, course_id, attendance_date, attendance_time, status, instructor_id, week_number) " +
                    "    VALUES (source.student_id, source.course_id, source.attendance_date, source.attendance_time, source.status, source.instructor_id, source.week_number);";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Attendance attendance : attendanceList) {
                stmt.setString(1, attendance.getStudentId());
                stmt.setString(2, attendance.getCourseId());
                stmt.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
                stmt.setTime(4, Time.valueOf(attendance.getAttendanceTime()));
                stmt.setString(5, attendance.getStatus());
                stmt.setString(6, attendance.getInstructorId());
                stmt.setInt(7, attendance.getWeekNumber());
                stmt.addBatch();
            }
            
            int[] results = stmt.executeBatch();
            return results.length > 0;
            
        } catch (SQLException e) {
            System.err.println("Error marking multiple attendance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Get attendance for a specific course and week
    public List<Attendance> getAttendanceByCourseAndWeek(String courseId, int weekNumber) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, s.first_name, s.last_name, c.course_name " +
                    "FROM attendance a " +
                    "JOIN students s ON a.student_id = s.student_id " +
                    "JOIN courses c ON a.course_id = c.course_id " +
                    "WHERE a.course_id = ? AND a.week_number = ? " +
                    "ORDER BY s.last_name, s.first_name";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId);
            stmt.setInt(2, weekNumber);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendanceList.add(attendance);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting attendance by course and week: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceList;
    }
    
    // Get student's attendance for a specific course
    public List<Attendance> getStudentAttendance(String studentId, String courseId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, c.course_name " +
                    "FROM attendance a " +
                    "JOIN courses c ON a.course_id = c.course_id " +
                    "WHERE a.student_id = ? AND a.course_id = ? " +
                    "ORDER BY a.week_number";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attendance attendance = mapResultSetToAttendance(rs);
                attendanceList.add(attendance);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return attendanceList;
    }
    
    // Get all students enrolled in a course for attendance marking
    public List<Attendance> getStudentsForAttendance(String courseId, int weekNumber, String instructorId) {
        List<Attendance> students = new ArrayList<>();
        String sql = "SELECT e.student_id, s.first_name, s.last_name, c.course_id, c.course_name, " +
                    "a.status, a.attendance_date, a.attendance_time, a.instructor_id, a.week_number " +
                    "FROM enrollments e " +
                    "JOIN students s ON e.student_id = s.student_id " +
                    "JOIN courses c ON e.course_id = c.course_id " +
                    "LEFT JOIN attendance a ON e.student_id = a.student_id AND e.course_id = a.course_id AND a.week_number = ? " +
                    "WHERE e.course_id = ? AND e.status = 'enrolled' " +
                    "ORDER BY s.last_name, s.first_name";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, weekNumber);
            stmt.setString(2, courseId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Attendance attendance = new Attendance();
                attendance.setStudentId(rs.getString("student_id"));
                attendance.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                attendance.setCourseId(rs.getString("course_id"));
                attendance.setCourseName(rs.getString("course_name"));
                attendance.setInstructorId(instructorId);
                attendance.setWeekNumber(weekNumber);
                
                // Set status - use existing if available, otherwise default to "absent"
                String existingStatus = rs.getString("status");
                attendance.setStatus(existingStatus != null ? existingStatus : "absent");
                
                // Handle date
                java.sql.Date sqlDate = rs.getDate("attendance_date");
                if (sqlDate != null) {
                    attendance.setAttendanceDate(sqlDate.toLocalDate());
                } else {
                    attendance.setAttendanceDate(LocalDate.now());
                }
                
                // Handle time
                Time sqlTime = rs.getTime("attendance_time");
                if (sqlTime != null) {
                    attendance.setAttendanceTime(sqlTime.toLocalTime());
                } else {
                    attendance.setAttendanceTime(LocalTime.now());
                }
                
                students.add(attendance);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting students for attendance: " + e.getMessage());
            e.printStackTrace();
        }
        
        return students;
    }
    
    // Get attendance statistics for a course and student
    public AttendanceStatistics getAttendanceStatistics(String courseId, String studentId) {
        AttendanceStatistics stats = new AttendanceStatistics();
        String sql = "SELECT " +
                    "COUNT(*) as total_weeks, " +
                    "SUM(CASE WHEN status = 'present' THEN 1 ELSE 0 END) as present_weeks, " +
                    "SUM(CASE WHEN status = 'absent' THEN 1 ELSE 0 END) as absent_weeks " +
                    "FROM attendance " +
                    "WHERE course_id = ? AND student_id = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, courseId);
            stmt.setString(2, studentId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalWeeks(rs.getInt("total_weeks"));
                stats.setPresentWeeks(rs.getInt("present_weeks"));
                stats.setAbsentWeeks(rs.getInt("absent_weeks"));
                if (stats.getTotalWeeks() > 0) {
                    stats.setAttendanceRate((double) stats.getPresentWeeks() / stats.getTotalWeeks() * 100);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting attendance statistics: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    // Get all courses with attendance data for admin
    public List<AttendanceSummary> getAttendanceSummaryForAdmin() {
        List<AttendanceSummary> summary = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name, i.first_name, i.last_name, " +
                    "a.week_number, " +
                    "COUNT(a.student_id) as total_records, " +
                    "SUM(CASE WHEN a.status = 'present' THEN 1 ELSE 0 END) as present_count " +
                    "FROM courses c " +
                    "JOIN instructors i ON c.instructor_id = i.instructor_id " +
                    "LEFT JOIN attendance a ON c.course_id = a.course_id " +
                    "GROUP BY c.course_id, c.course_name, i.first_name, i.last_name, a.week_number " +
                    "ORDER BY c.course_name, a.week_number";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AttendanceSummary item = new AttendanceSummary();
                item.setCourseId(rs.getString("course_id"));
                item.setCourseName(rs.getString("course_name"));
                item.setInstructorName(rs.getString("first_name") + " " + rs.getString("last_name"));
                item.setWeekNumber(rs.getInt("week_number"));
                item.setTotalStudents(rs.getInt("total_records"));
                item.setPresentCount(rs.getInt("present_count"));
                summary.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting attendance summary for admin: " + e.getMessage());
            e.printStackTrace();
        }
        
        return summary;
    }
    
    // Check if attendance record exists
    public boolean attendanceExists(String studentId, String courseId, int weekNumber) {
        String sql = "SELECT 1 FROM attendance WHERE student_id = ? AND course_id = ? AND week_number = ?";
        
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            stmt.setInt(3, weekNumber);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("Error checking if attendance exists: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method to map ResultSet to Attendance object
    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(rs.getInt("attendance_id"));
        attendance.setStudentId(rs.getString("student_id"));
        attendance.setCourseId(rs.getString("course_id"));
        
        // Handle date conversion safely
        java.sql.Date sqlDate = rs.getDate("attendance_date");
        if (sqlDate != null) {
            attendance.setAttendanceDate(sqlDate.toLocalDate());
        }
        
        // Handle time conversion safely
        Time sqlTime = rs.getTime("attendance_time");
        if (sqlTime != null) {
            attendance.setAttendanceTime(sqlTime.toLocalTime());
        }
        
        attendance.setStatus(rs.getString("status"));
        attendance.setInstructorId(rs.getString("instructor_id"));
        attendance.setWeekNumber(rs.getInt("week_number"));
        
        // Set additional fields if they exist in result set
        if (hasColumn(rs, "first_name") && hasColumn(rs, "last_name")) {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            if (firstName != null && lastName != null) {
                attendance.setStudentName(firstName + " " + lastName);
            }
        }
        
        if (hasColumn(rs, "course_name")) {
            attendance.setCourseName(rs.getString("course_name"));
        }
        
        return attendance;
    }
    
    // Helper method to check if column exists in ResultSet
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    // Statistics inner class
    public static class AttendanceStatistics {
        private int totalWeeks;
        private int presentWeeks;
        private int absentWeeks;
        private double attendanceRate;
        
        public int getTotalWeeks() { return totalWeeks; }
        public void setTotalWeeks(int totalWeeks) { this.totalWeeks = totalWeeks; }
        
        public int getPresentWeeks() { return presentWeeks; }
        public void setPresentWeeks(int presentWeeks) { this.presentWeeks = presentWeeks; }
        
        public int getAbsentWeeks() { return absentWeeks; }
        public void setAbsentWeeks(int absentWeeks) { this.absentWeeks = absentWeeks; }
        
        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
        
        @Override
        public String toString() {
            return String.format("Stats{total=%d, present=%d, absent=%d, rate=%.1f%%}", 
                totalWeeks, presentWeeks, absentWeeks, attendanceRate);
        }
    }
    
    // Summary inner class for admin view
    public static class AttendanceSummary {
        private String courseId;
        private String courseName;
        private String instructorName;
        private int weekNumber;
        private int totalStudents;
        private int presentCount;
        
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        
        public String getInstructorName() { return instructorName; }
        public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
        
        public int getWeekNumber() { return weekNumber; }
        public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }
        
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        
        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
        
        public double getAttendancePercentage() {
            return totalStudents > 0 ? (double) presentCount / totalStudents * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("Summary{course=%s, week=%d, present=%d/%d, rate=%.1f%%}", 
                courseName, weekNumber, presentCount, totalStudents, getAttendancePercentage());
        }
    }
}