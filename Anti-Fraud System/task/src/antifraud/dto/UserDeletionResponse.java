package antifraud.dto;

public class UserDeletionResponse {

    private String username;
    private String status;

    // Constructors
    public UserDeletionResponse() {}

    public UserDeletionResponse(String username, String status) {
        this.username = username;
        this.status = status;
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
