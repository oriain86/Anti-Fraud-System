package antifraud.controller;

import antifraud.dto.StatusResponse;
import antifraud.dto.StolenCardRequest;
import antifraud.dto.SuspiciousIpRequest;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIp;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
@Validated
public class AntiFraudController {

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public AntiFraudController(SuspiciousIpRepository suspiciousIpRepository, StolenCardRepository stolenCardRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.stolenCardRepository = stolenCardRepository;
    }

    // ----- Suspicious IP Endpoints (Accessible only by SUPPORT) -----
    @PreAuthorize("hasAuthority('SUPPORT')")
    @PostMapping("/suspicious-ip")
    public ResponseEntity<?> addSuspiciousIp(@RequestBody @Valid SuspiciousIpRequest request) {
        String ip = request.getIp().trim();
        // Validate IPv4 using regex.
        if (!ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (suspiciousIpRepository.existsByIp(ip)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        SuspiciousIp newIp = new SuspiciousIp(ip);
        suspiciousIpRepository.save(newIp);
        return new ResponseEntity<>(newIp, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SUPPORT')")
    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<?> deleteSuspiciousIp(@PathVariable String ip) {
        ip = ip.trim();
        if (!ip.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!suspiciousIpRepository.existsByIp(ip)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        suspiciousIpRepository.deleteByIp(ip);
        String statusMessage = "IP " + ip + " successfully removed!";
        return new ResponseEntity<>(new StatusResponse(statusMessage), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SUPPORT')")
    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<SuspiciousIp>> getSuspiciousIps() {
        List<SuspiciousIp> ips = suspiciousIpRepository.findAll()
                .stream()
                .sorted((ip1, ip2) -> ip1.getId().compareTo(ip2.getId()))
                .toList();
        return new ResponseEntity<>(ips, HttpStatus.OK);
    }

    // ----- Stolen Card Endpoints (Accessible only by SUPPORT) -----
    @PreAuthorize("hasAuthority('SUPPORT')")
    @PostMapping("/stolencard")
    public ResponseEntity<?> addStolenCard(@RequestBody @Valid StolenCardRequest request) {
        String number = request.getNumber().trim();
        // Validate card number format: 16 digits and valid by Luhn algorithm.
        if (!number.matches("^\\d{16}$") || !luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (stolenCardRepository.existsByNumber(number)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        StolenCard newCard = new StolenCard(number);
        stolenCardRepository.save(newCard);
        return new ResponseEntity<>(newCard, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SUPPORT')")
    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<?> deleteStolenCard(@PathVariable String number) {
        number = number.trim();
        if (!number.matches("^\\d{16}$") || !luhnCheck(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!stolenCardRepository.existsByNumber(number)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        stolenCardRepository.deleteByNumber(number);
        String statusMessage = "Card " + number + " successfully removed!";
        return new ResponseEntity<>(new StatusResponse(statusMessage), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SUPPORT')")
    @GetMapping("/stolencard")
    public ResponseEntity<List<StolenCard>> getStolenCards() {
        List<StolenCard> cards = stolenCardRepository.findAll()
                .stream()
                .sorted((c1, c2) -> c1.getId().compareTo(c2.getId()))
                .toList();
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    // ----- Helper: Luhn Algorithm Implementation -----
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
