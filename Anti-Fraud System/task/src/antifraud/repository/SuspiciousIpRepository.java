package antifraud.repository;

import antifraud.model.SuspiciousIp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuspiciousIpRepository extends JpaRepository<SuspiciousIp, Long> {
    Optional<SuspiciousIp> findByIp(String ip);
    boolean existsByIp(String ip);
    void deleteByIp(String ip);
}
