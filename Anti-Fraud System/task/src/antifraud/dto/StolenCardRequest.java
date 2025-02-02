package antifraud.dto;

import jakarta.validation.constraints.NotBlank;

public class StolenCardRequest {

    @NotBlank(message = "Card number is required")
    private String number;

    // Constructors
    public StolenCardRequest() {}

    public StolenCardRequest(String number) {
        this.number = number;
    }

    // Getter and Setter
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
