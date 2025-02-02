package antifraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransactionFeedbackRequest {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotBlank(message = "Feedback is required")
    private String feedback;

    public Long getTransactionId() { return transactionId; }
    public String getFeedback() { return feedback; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
