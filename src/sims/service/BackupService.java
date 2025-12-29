package sims.service;

import sims.DatabaseConnection;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupService {
    private static final long BACKUP_INTERVAL_MINUTES = 15; // Every 15 minutes
    private static final int MAX_BACKUPS = 48; // Keep 48 backups (12 hours worth)
    
    public ScheduledService<Void> createScheduledBackup() {
        ScheduledService<Void> scheduledService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("Scheduled backup started at: " + new Date());
                        updateMessage("Performing scheduled backup...");
                        performBackup();
                        cleanupOldBackups();
                        updateMessage("Backup completed successfully");
                        return null;
                    }
                };
            }
        };
        
        scheduledService.setPeriod(Duration.minutes(BACKUP_INTERVAL_MINUTES));
        scheduledService.setDelay(Duration.minutes(1)); // Start 1 minute after app launch
        
        // Add error handling for the scheduled service
        scheduledService.setOnFailed(e -> {
            System.err.println("Scheduled backup failed: " + scheduledService.getException().getMessage());
        });
        
        return scheduledService;
    }
    
    public Task<Void> performManualBackup() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Starting manual backup...");
                updateProgress(0.1, 1.0);
                
                performBackup();
                updateProgress(0.8, 1.0);
                
                cleanupOldBackups();
                updateProgress(1.0, 1.0);
                updateMessage("Backup completed successfully!");
                
                return null;
            }
        };
    }
    
    private void performBackup() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupDir = "backups/";
        String filename = backupDir + "sims_backup_" + timestamp + ".sql";
        
        // Create backups directory if it doesn't exist
        File dir = new File(backupDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("❌ Failed to create backups directory");
                return;
            }
        }
        
        try (Connection conn = DatabaseConnection.connect();
             FileWriter writer = new FileWriter(filename)) {
            
            if (conn == null) {
                throw new Exception("Failed to connect to database");
            }
            
            writer.write("-- SIMS Database Backup\n");
            writer.write("-- Generated on: " + new Date() + "\n");
            writer.write("-- Backup interval: Every 15 minutes\n\n");
            
            // Backup tables in order to maintain referential integrity
            backupTable(conn, writer, "users", "id, username, password, role, full_name, email, created_date, is_active");
            backupTable(conn, writer, "students", "student_id, first_name, last_name, email, date_of_birth, major");
            backupTable(conn, writer, "instructors", "instructor_id, first_name, last_name, email, department, hire_date");
            backupTable(conn, writer, "courses", "course_id, course_name, course_code, instructor_id, credits, department, description");
            backupTable(conn, writer, "enrollments", "enrollment_id, student_id, course_id, grade, status");
            
            writer.write("\n-- Backup completed successfully\n");
            
            System.out.println("✅ Backup completed: " + filename);
            
        } catch (Exception e) {
            System.err.println("❌ Backup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void backupTable(Connection conn, FileWriter writer, String tableName, String columns) throws Exception {
        writer.write("-- Backup of " + tableName + " table\n");
        writer.write("DELETE FROM " + tableName + ";\n\n");
        
        String query = "SELECT " + columns + " FROM " + tableName;
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            int rowCount = 0;
            while (rs.next()) {
                StringBuilder insert = new StringBuilder("INSERT INTO " + tableName + " (" + columns + ") VALUES (");
                
                int columnCount = columns.split(",").length;
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value == null) {
                        insert.append("NULL");
                    } else {
                        // Escape single quotes and wrap in quotes
                        insert.append("'").append(value.replace("'", "''")).append("'");
                    }
                    
                    if (i < columnCount) {
                        insert.append(", ");
                    }
                }
                
                insert.append(");\n");
                writer.write(insert.toString());
                rowCount++;
            }
            
            writer.write("-- " + rowCount + " records backed up\n\n");
        }
    }
    
    private void cleanupOldBackups() {
        File backupDir = new File("backups/");
        if (!backupDir.exists()) return;
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("sims_backup_") && name.endsWith(".sql"));
        
        if (backupFiles != null && backupFiles.length > MAX_BACKUPS) {
            // Sort by last modified (oldest first)
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            
            // Delete oldest files beyond the limit
            int deletedCount = 0;
            for (int i = 0; i < backupFiles.length - MAX_BACKUPS; i++) {
                if (backupFiles[i].delete()) {
                    deletedCount++;
                    System.out.println("Deleted old backup: " + backupFiles[i].getName());
                }
            }
            
            if (deletedCount > 0) {
                System.out.println("Cleaned up " + deletedCount + " old backup files");
            }
        }
    }
    
    // Method to get backup statistics
    public String getBackupStats() {
        File backupDir = new File("backups/");
        if (!backupDir.exists()) {
            return "No backups created yet";
        }
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.startsWith("sims_backup_") && name.endsWith(".sql"));
        
        if (backupFiles == null || backupFiles.length == 0) {
            return "No backups found";
        }
        
        // Find oldest and newest backup
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        
        String oldest = new SimpleDateFormat("MM/dd HH:mm").format(new Date(backupFiles[0].lastModified()));
        String newest = new SimpleDateFormat("MM/dd HH:mm").format(new Date(backupFiles[backupFiles.length - 1].lastModified()));
        long totalSize = 0;
        for (File file : backupFiles) {
            totalSize += file.length();
        }
        
        return String.format("%d backups | Size: %.1f MB | Oldest: %s | Newest: %s", 
                           backupFiles.length, 
                           totalSize / (1024.0 * 1024.0),
                           oldest, newest);
    }
}