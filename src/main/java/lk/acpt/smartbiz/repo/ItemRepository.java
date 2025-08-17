package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByBusiness(Business business);
    Optional<Item> findByItemIdAndBusiness(Long itemId, Business business);
    Optional<Item> findByNameAndBusiness(String name, Business business);

    // New for dashboard
    @Query("SELECT SUM(i.quantity * i.unitPrice) FROM Item i WHERE i.business = ?1")
    Double getInventoryValue(Business business);
}