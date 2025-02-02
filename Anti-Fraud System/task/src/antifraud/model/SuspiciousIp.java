package antifraud.model;

import jakarta.persistence.*;

@Entity
@Table(name = "suspicious_ips")
public class SuspiciousIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ip;

    // Constructors
    public SuspiciousIp() {}

    public SuspiciousIp(String ip) {
        this.ip = ip;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
