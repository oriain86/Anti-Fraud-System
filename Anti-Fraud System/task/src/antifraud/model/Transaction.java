package antifraud.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private LocalDateTime date;

    // Feedback is initially empty.
    private String feedback = "";

    // Result is the validation result (ALLOWED, MANUAL_PROCESSING, PROHIBITED)
    @Column(nullable = false)
    private String result;

    public Transaction() {}

    public Transaction(Long amount, String ip, String number, String region, LocalDateTime date, String result) {
        this.amount = amount;
        this.ip = ip;
        this.number = number;
        this.region = region;
        this.date = date;
        this.result = result;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getAmount() { return amount; }
    public String getIp() { return ip; }
    public String getNumber() { return number; }
    public String getRegion() { return region; }
    public LocalDateTime getDate() { return date; }
    public String getFeedback() { return feedback; }
    public String getResult() { return result; }

    public void setId(Long id) { this.id = id; }
    public void setAmount(Long amount) { this.amount = amount; }
    public void setIp(String ip) { this.ip = ip; }
    public void setNumber(String number) { this.number = number; }
    public void setRegion(String region) { this.region = region; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setResult(String result) { this.result = result; }
}
