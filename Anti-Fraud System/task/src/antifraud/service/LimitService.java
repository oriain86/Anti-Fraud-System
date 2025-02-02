package antifraud.service;

import org.springframework.stereotype.Service;
import static java.lang.Math.ceil;

@Service
public class LimitService {
    private double maxAllowed;
    private double maxManual;

    public LimitService() {
        // Initialize default values (example defaults)
        this.maxAllowed = 200.0;
        this.maxManual = 1500.0;
    }

    public synchronized double getMaxAllowed() {
        return maxAllowed;
    }

    public synchronized double getMaxManual() {
        return maxManual;
    }

    public synchronized void updateMaxAllowedIncrease(double transactionValue) {
        double newLimit = 0.8 * maxAllowed + 0.2 * transactionValue;
        maxAllowed = ceil(newLimit);
    }

    public synchronized void updateMaxAllowedDecrease(double transactionValue) {
        double newLimit = 0.8 * maxAllowed - 0.2 * transactionValue;
        maxAllowed = ceil(newLimit);
    }

    public synchronized void updateMaxManualIncrease(double transactionValue) {
        double newLimit = 0.8 * maxManual + 0.2 * transactionValue;
        maxManual = ceil(newLimit);
    }

    public synchronized void updateMaxManualDecrease(double transactionValue) {
        double newLimit = 0.8 * maxManual - 0.2 * transactionValue;
        maxManual = ceil(newLimit);
    }
}
