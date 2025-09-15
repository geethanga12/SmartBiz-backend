package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.UsageLog;
import lk.acpt.smartbiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {

    List<UsageLog> findAllByBusinessOrderByTimestampDesc(Business business);

    List<UsageLog> findAllByUserOrderByTimestampDesc(User user);

    Page<UsageLog> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT COUNT(ul) FROM UsageLog ul WHERE ul.timestamp >= ?1")
    long countLogsSince(LocalDateTime since);

    @Query("SELECT ul.action, COUNT(ul) FROM UsageLog ul WHERE ul.timestamp >= ?1 GROUP BY ul.action")
    List<Object[]> getActionStatsSince(LocalDateTime since);

    @Query("SELECT ul FROM UsageLog ul WHERE ul.timestamp >= ?1 AND ul.timestamp <= ?2 ORDER BY ul.timestamp DESC")
    List<UsageLog> findLogsBetweenDates(LocalDateTime start, LocalDateTime end);
}