package antifraud.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {

    private Long transactionId;
    private Long amount;
    private String ip;
    private String number;
    private String region;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    private String result;
    private String feedback;

    // Constructors, Getters, and Setters
    public TransactionHistoryResponse() {}

    public TransactionHistoryResponse(Long transactionId, Long amount, String ip, String number, String region,
                                      LocalDateTime date, String result, String feedback) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
        this.feedback = feedback;
    }

    public Long getTransactionId() { return transactionId; }
    public Long getAmount() { return amount; }
    public String getIp() { return ip; }
    public String getNumber() { return number; }
    public String getRegion() { return region; }
    public LocalDateTime getDate() { return date; }
    public String getResult() { return result; }
    public String getFeedback() { return feedback; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public void setAmount(Long amount) { this.amount = amount; }
    public void setIp(String ip) { this.ip = ip; }
    public void setNumber(String number) { this.number = number; }
    public void setRegion(String region) { this.region = region; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setResult(String result) { this.result = result; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
