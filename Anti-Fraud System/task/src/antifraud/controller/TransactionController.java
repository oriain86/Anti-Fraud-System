package antifraud.controller;

import antifraud.dto.*;
import antifraud.model.Transaction;
import antifraud.repository.TransactionRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.service.LimitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/antifraud")
@Validated
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;
    private final LimitService limitService;

    public TransactionController(TransactionRepository transactionRepository,
                                 SuspiciousIpRepository suspiciousIpRepository,
                                 StolenCardRepository stolenCardRepository,
                                 LimitService limitService) {
        this.transactionRepository = transactionRepository;
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.limitService = limitService;
    }

    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> validateTransaction(@Valid @RequestBody TransactionRequest request) {
        // Validate IP format
        String ip = request.getIp().trim();
        if (!ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate card number format and Luhn check
        String number = request.getNumber().trim();
        if (!number.matches("^\\d{16}$") || !luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Validate region against allowed codes
        String region = request.getRegion().trim();
        Set<String> allowedRegions = Set.of("EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA");
        if (!allowedRegions.contains(region)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // The date is automatically parsed as LocalDateTime (bad format will be handled by framework)

        // Apply base rules (amount-based)
        Long amount = request.getAmount();
        String result;
        List<String> reasons = new ArrayList<>();
        if (amount <= 200) {
            result = "ALLOWED";
        } else if (amount <= 1500) {
            result = "MANUAL_PROCESSING";
            reasons.add("amount");
        } else {
            result = "PROHIBITED";
            reasons.add("amount");
        }
        // Blacklist checks
        if (suspiciousIpRepository.existsByIp(ip)) {
            reasons.add("ip");
            result = "PROHIBITED";
        }
        if (stolenCardRepository.existsByNumber(number)) {
            reasons.add("card-number");
            result = "PROHIBITED";
        }
        // Correlation checks – last 1 hour for same card number
        LocalDateTime currentTime = request.getDate();
        LocalDateTime oneHourAgo = currentTime.minus(1, ChronoUnit.HOURS);
        List<Transaction> history = transactionRepository.findByNumberAndDateBetween(number, oneHourAgo, currentTime);
        // Exclude current transaction if already present (it won’t be since we haven’t saved it yet)
        long distinctOtherRegions = history.stream()
                .map(Transaction::getRegion)
                .filter(r -> !r.equals(region))
                .distinct()
                .count();
        long distinctOtherIps = history.stream()
                .map(Transaction::getIp)
                .filter(i -> !i.equals(ip))
                .distinct()
                .count();
        if (distinctOtherRegions > 2) {
            reasons.add("region-correlation");
            result = "PROHIBITED";
        } else if (distinctOtherRegions == 2) {
            reasons.add("region-correlation");
            if (!"PROHIBITED".equals(result)) result = "MANUAL_PROCESSING";
        }
        if (distinctOtherIps > 2) {
            reasons.add("ip-correlation");
            result = "PROHIBITED";
        } else if (distinctOtherIps == 2) {
            reasons.add("ip-correlation");
            if (!"PROHIBITED".equals(result)) result = "MANUAL_PROCESSING";
        }
        // Finalize info field
        String info;
        if ("ALLOWED".equals(result)) {
            info = "none";
        } else {
            Set<String> uniqueReasons = new HashSet<>(reasons);
            List<String> sortedReasons = new ArrayList<>(uniqueReasons);
            Collections.sort(sortedReasons);
            info = String.join(", ", sortedReasons);
        }
        // Save the transaction (with result, and empty feedback)
        Transaction transaction = new Transaction(amount, ip, number, region, currentTime, result);
        transactionRepository.save(transaction);
        TransactionResponse response = new TransactionResponse(result, info);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // PUT /api/antifraud/transaction to add feedback
    @PutMapping("/transaction")
    public ResponseEntity<?> addFeedback(@Valid @RequestBody TransactionFeedbackRequest feedbackRequest) {
        Long txId = feedbackRequest.getTransactionId();
        String feedback = feedbackRequest.getFeedback().trim().toUpperCase();
        // Allowed feedback values
        Set<String> allowedFeedback = Set.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED");
        if (!allowedFeedback.contains(feedback)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<Transaction> optTx = transactionRepository.findById(txId);
        if (optTx.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Transaction transaction = optTx.get();
        // Check if feedback already exists
        if (transaction.getFeedback() != null && !transaction.getFeedback().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // If feedback equals the original result, return 422
        if (transaction.getResult().equals(feedback)) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        // Otherwise, update limits based on the combination.
        // Using our interpretation of the table:
        String original = transaction.getResult();
        Long value = transaction.getAmount();
        // For example, if original is MANUAL_PROCESSING and feedback is ALLOWED → increase maxAllowed.
        // If original is ALLOWED and feedback is MANUAL_PROCESSING or PROHIBITED → decrease maxAllowed.
        // If original is PROHIBITED and feedback is ALLOWED → increase maxAllowed.
        // If original is PROHIBITED and feedback is MANUAL_PROCESSING → increase maxManual.
        // If original is MANUAL_PROCESSING and feedback is PROHIBITED → decrease maxManual.
        if ("MANUAL_PROCESSING".equals(original) && "ALLOWED".equals(feedback)) {
            limitService.updateMaxAllowedIncrease(value);
        } else if ("MANUAL_PROCESSING".equals(original) && "PROHIBITED".equals(feedback)) {
            limitService.updateMaxManualDecrease(value);
        } else if ("PROHIBITED".equals(original) && "ALLOWED".equals(feedback)) {
            limitService.updateMaxAllowedIncrease(value);
        } else if ("PROHIBITED".equals(original) && "MANUAL_PROCESSING".equals(feedback)) {
            limitService.updateMaxManualIncrease(value);
        } else if ("ALLOWED".equals(original) && ("MANUAL_PROCESSING".equals(feedback) || "PROHIBITED".equals(feedback))) {
            limitService.updateMaxAllowedDecrease(value);
        } else {
            // Should not reach here; if combination is not supported, return conflict.
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        // Save feedback
        transaction.setFeedback(feedback);
        transactionRepository.save(transaction);
        // Build response DTO (using TransactionHistoryResponse for consistency)
        TransactionHistoryResponse historyResponse = new TransactionHistoryResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getIp(),
                transaction.getNumber(),
                transaction.getRegion(),
                transaction.getDate(),
                transaction.getResult(),
                transaction.getFeedback()
        );
        return new ResponseEntity<>(historyResponse, HttpStatus.OK);
    }

    // GET /api/antifraud/history returns all transactions sorted by ID
    @GetMapping("/history")
    public ResponseEntity<List<TransactionHistoryResponse>> getHistory() {
        List<Transaction> transactions = transactionRepository.findAll()
                .stream()
                .sorted(Comparator.comparingLong(Transaction::getId))
                .collect(Collectors.toList());
        List<TransactionHistoryResponse> history = transactions.stream()
                .map(tx -> new TransactionHistoryResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getIp(),
                        tx.getNumber(),
                        tx.getRegion(),
                        tx.getDate(),
                        tx.getResult(),
                        tx.getFeedback()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    // GET /api/antifraud/history/{number} returns transactions for a given card number
    @GetMapping("/history/{number}")
    public ResponseEntity<?> getHistoryByCard(@PathVariable String number) {
        number = number.trim();
        // Validate card number format and Luhn check
        if (!number.matches("^\\d{16}$") || !luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Transaction> transactions = transactionRepository.findByNumber(number);
        if (transactions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<TransactionHistoryResponse> history = transactions.stream()
                .sorted(Comparator.comparingLong(Transaction::getId))
                .map(tx -> new TransactionHistoryResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getIp(),
                        tx.getNumber(),
                        tx.getRegion(),
                        tx.getDate(),
                        tx.getResult(),
                        tx.getFeedback()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    // Helper: Luhn algorithm
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9)
                    n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
