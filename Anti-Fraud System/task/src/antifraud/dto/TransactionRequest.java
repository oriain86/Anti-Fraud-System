package antifraud.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Long amount;

    @NotBlank(message = "IP is required")
    private String ip;

    @NotBlank(message = "Card number is required")
    private String number;

    @NotBlank(message = "Region is required")
    private String region;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    // Getters and Setters
    public Long getAmount() { return amount; }
    public String getIp() { return ip; }
    public String getNumber() { return number; }
    public String getRegion() { return region; }
    public LocalDateTime getDate() { return date; }
    public void setAmount(Long amount) { this.amount = amount; }
    public void setIp(String ip) { this.ip = ip; }
    public void setNumber(String number) { this.number = number; }
    public void setRegion(String region) { this.region = region; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
