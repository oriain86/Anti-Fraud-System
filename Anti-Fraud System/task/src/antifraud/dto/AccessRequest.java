package antifraud.dto;

import jakarta.validation.constraints.NotBlank;

public class AccessRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Operation is required")
    private String operation;  // Expected to be "LOCK" or "UNLOCK"

    // Constructors
    public AccessRequest() {}

    public AccessRequest(String username, String operation) {
        this.username = username;
        this.operation = operation;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }
    public String getOperation() {
        return operation;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
}
