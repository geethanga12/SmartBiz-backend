package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.ItemDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Item;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.ItemRepository;
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
public class ItemServiceIMPL implements ItemService {

    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public ItemServiceIMPL(ItemRepository itemRepo, UserRepository userRepo, BusinessRepository businessRepo) {
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private ItemDto toDto(Item item) {
        return new ItemDto(item.getItemId(), item.getItemName(), item.getDescription(), item.getPrice(), item.getQuantity());
    }

    @Override
    public ItemDto saveItem(ItemDto dto) {
        Business business = currentBusiness();

        if (itemRepo.findByItemNameAndBusiness(dto.getName(), business).isPresent()) {
            throw new RuntimeException("Item with this name already exists");
        }

        Item saved = itemRepo.save(new Item(null, business, dto.getName(), dto.getDescription(), dto.getPrice(), dto.getQuantity()));
        return toDto(saved);
    }

    @Override
    public ItemDto deleteItem(Long id) {
        Business business = currentBusiness();
        Optional<Item> byId = itemRepo.findByItemIdAndBusiness(id, business);
        if (byId.isPresent()) {
            itemRepo.delete(byId.get());
            return toDto(byId.get());
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
            i.setItemName(dto.getName());
            i.setDescription(dto.getDescription());
            i.setPrice(dto.getPrice());
            i.setQuantity(dto.getQuantity());
            return toDto(itemRepo.save(i));
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
        return itemRepo.findByItemNameAndBusiness(name, business).map(this::toDto).orElse(null);
    }
}
