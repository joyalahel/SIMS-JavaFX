package sims;

public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;
    private String fullName;
    private int userId;
    
    private UserSession() {
        // Private constructor to prevent instantiation
    }
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setUser(int userId, String username, String role, String fullName) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getRole() {
        return role;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public boolean isAdmin() {
        return "admin".equals(role);
    }
    
    public boolean isInstructor() {
        return "instructor".equals(role);
    }
    
    public boolean isStudent() {
        return "student".equals(role);
    }
    
    public void clearSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
        this.fullName = null;
    }
}