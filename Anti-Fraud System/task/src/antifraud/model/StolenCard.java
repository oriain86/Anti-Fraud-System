package antifraud.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stolen_cards")
public class StolenCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String number;

    // Constructors
    public StolenCard() {}

    public StolenCard(String number) {
        this.number = number;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
