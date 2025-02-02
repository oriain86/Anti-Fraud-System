package antifraud.dto;

import jakarta.validation.constraints.NotBlank;

public class SuspiciousIpRequest {

    @NotBlank(message = "IP is required")
    private String ip;

    // Constructors
    public SuspiciousIpRequest() {}

    public SuspiciousIpRequest(String ip) {
        this.ip = ip;
    }

    // Getter and Setter
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
