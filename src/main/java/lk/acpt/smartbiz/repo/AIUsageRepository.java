package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.AIUsage;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AIUsageRepository extends JpaRepository<AIUsage, Long> {

    List<AIUsage> findAllByBusinessOrderByRequestTimeDesc(Business business);

    List<AIUsage> findAllByUserOrderByRequestTimeDesc(User user);

    Page<AIUsage> findAllByOrderByRequestTimeDesc(Pageable pageable);

    @Query("SELECT COUNT(au) FROM AIUsage au WHERE au.requestTime >= ?1 AND au.successful = true")
    long countSuccessfulRequestsSince(LocalDateTime since);

    @Query("SELECT SUM(au.cost) FROM AIUsage au WHERE au.requestTime >= ?1 AND au.successful = true")
    Double getTotalCostSince(LocalDateTime since);

    @Query("SELECT au.requestType, COUNT(au) FROM AIUsage au WHERE au.requestTime >= ?1 GROUP BY au.requestType")
    List<Object[]> getRequestTypeStatsSince(LocalDateTime since);

    @Query("SELECT SUM(au.tokensUsed) FROM AIUsage au WHERE au.business = ?1 AND au.requestTime >= ?2")
    Long getTotalTokensUsed(Business business, LocalDateTime since);

    @Query("SELECT COUNT(au) FROM AIUsage au WHERE au.business = ?1 AND au.requestTime >= ?2 AND au.requestTime <= ?3")
    long countRequestsBetweenDates(Business business, LocalDateTime start, LocalDateTime end);
}