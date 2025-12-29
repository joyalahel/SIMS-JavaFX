package sims.service;

import sims.dao.StudentDao;
import sims.dao.CourseDao;
import sims.dao.InstructorDao;
import sims.dao.EnrollmentDao;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class DataRefreshService {
    private StudentDao studentDao;
    private CourseDao courseDao;
    private InstructorDao instructorDao;
    private EnrollmentDao enrollmentDao;
    private boolean isRunning = false;
    
    public DataRefreshService() {
        this.studentDao = new StudentDao();
        this.courseDao = new CourseDao();
        this.instructorDao = new InstructorDao();
        this.enrollmentDao = new EnrollmentDao();
    }
    
    public ScheduledService<Void> createRefreshService() {
        ScheduledService<Void> refreshService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        if (!isRunning) {
                            isRunning = true;
                            refreshAllData();
                            isRunning = false;
                        }
                        return null;
                    }
                };
            }
        };
        
        // REFRESH EVERY 5 SECONDS (was 30 seconds)
        refreshService.setPeriod(Duration.seconds(5));
        refreshService.setDelay(Duration.seconds(2)); // Start 2 seconds after initialization
        
        // Add error handling
        refreshService.setOnFailed(e -> {
            System.err.println("Data refresh failed: " + refreshService.getException().getMessage());
            isRunning = false;
        });
        
        return refreshService;
    }
    
    private void refreshAllData() {
        long startTime = System.currentTimeMillis();
        System.out.println("Background data refresh started...");
        
        try {
            // Refresh students (non-blocking)
            Platform.runLater(() -> {
                studentDao.loadAllStudents();
            });
            
            // Refresh courses (non-blocking)
            Platform.runLater(() -> {
                courseDao.loadAllCourses();
            });
            
            // Refresh instructors (non-blocking)
            Platform.runLater(() -> {
                instructorDao.loadAllInstructors();
            });
            
            // Refresh enrollments (non-blocking)
            Platform.runLater(() -> {
                enrollmentDao.loadAllEnrollments();
            });
            
            long endTime = System.currentTimeMillis();
            System.out.println("Background data refresh completed in " + (endTime - startTime) + "ms");
            
        } catch (Exception e) {
            System.err.println("Error during data refresh: " + e.getMessage());
        }
    }
    
    // REMOVED: refreshDataNow() method since manual refresh is not needed
    // REMOVED: Individual refresh methods since they're not used
}