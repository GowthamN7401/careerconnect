package jobportal.job.dto;

public class LoginResponse {

    private boolean success;
    private String message;
    private String name;
    private String email;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, String name, String email, String role) {
        this.success = success;
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}