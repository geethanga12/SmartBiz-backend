package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.ItemDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Item;
import lk.acpt.smartbiz.entity.Supplier;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.ItemRepository;
import lk.acpt.smartbiz.repo.SupplierRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;
    private final SupplierRepository supplierRepo;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepo,
                           UserRepository userRepo,
                           BusinessRepository businessRepo,
                           SupplierRepository supplierRepo) {
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
        this.supplierRepo = supplierRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private ItemDto toDto(Item i) {
        return new ItemDto(
                i.getItemId(),
                i.getSupplier() != null ? i.getSupplier().getSupplierId() : null,
                i.getName(),
                i.getDescription(),
                i.getUnitPrice(),
                i.getCostPrice(), // New
                i.getQuantity()
        );
    }

    @Override
    public ItemDto saveItem(ItemDto dto) {
        Business business = currentBusiness();

        if (itemRepo.findByNameAndBusiness(dto.getName(), business).isPresent()) {
            throw new RuntimeException("Item with this name already exists for your business");
        }

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepo.findBySupplierIdAndBusiness(dto.getSupplierId(), business)
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
        }

        if (dto.getQuantity() < 0 || dto.getUnitPrice() <= 0) {
            throw new RuntimeException("Quantity must be >= 0 and unit price > 0");
        }

        Item saved = itemRepo.save(new Item(
                null,
                business,
                supplier,
                dto.getName(),
                dto.getDescription(),
                dto.getUnitPrice(),
                dto.getCostPrice(),
                dto.getQuantity()
        ));
        return toDto(saved);
    }

    @Override
    public ItemDto deleteItem(Long id) {
        Business business = currentBusiness();
        Optional<Item> byId = itemRepo.findByItemIdAndBusiness(id, business);

        if (byId.isPresent()) {
            Item i = byId.get();
            itemRepo.delete(i);
            return toDto(i);
        }
        return null;
    }

    @Override
    public List<ItemDto> getAllItems() {
        Business business = currentBusiness();
        List<Item> all = itemRepo.findAllByBusiness(business);
        List<ItemDto> dtos = new ArrayList<>();
        for (Item i : all) dtos.add(toDto(i));
        return dtos;
    }

    @Override
    public ItemDto updateItem(ItemDto dto) {
        Business business = currentBusiness();
        Optional<Item> byId = itemRepo.findByItemIdAndBusiness(dto.getId(), business);

        if (byId.isPresent()) {
            Item i = byId.get();
            i.setName(dto.getName());
            i.setDescription(dto.getDescription());
            i.setUnitPrice(dto.getUnitPrice());
            i.setCostPrice(dto.getCostPrice()); // New
            i.setQuantity(dto.getQuantity());

            if (dto.getSupplierId() != null) {
                Supplier supplier = supplierRepo.findBySupplierIdAndBusiness(dto.getSupplierId(), business)
                        .orElseThrow(() -> new RuntimeException("Supplier not found"));
                i.setSupplier(supplier);
            } else {
                i.setSupplier(null);
            }

            if (i.getQuantity() < 0 || i.getUnitPrice() <= 0) {
                throw new RuntimeException("Quantity must be >= 0 and unit price > 0");
            }

            Item update = itemRepo.save(i);
            return toDto(update);
        }
        return null;
    }

    @Override
    public ItemDto getItemById(Long id) {
        Business business = currentBusiness();
        return itemRepo.findByItemIdAndBusiness(id, business).map(this::toDto).orElse(null);
    }

    @Override
    public ItemDto getItemByName(String name) {
        Business business = currentBusiness();
        return itemRepo.findByNameAndBusiness(name, business).map(this::toDto).orElse(null);
    }
}