package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.ItemDto;
import lk.acpt.smartbiz.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/items")
@PreAuthorize("hasRole('OWNER')") // Owner-only
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<?> saveItem(@RequestBody ItemDto dto) {
        return new ResponseEntity<>(itemService.saveItem(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return new ResponseEntity<>(itemService.getAllItems(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        ItemDto dto = itemService.getItemById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", "Item not found"));
    }

    @GetMapping("/get_by_name/{name}")
    public ResponseEntity<?> getItemByName(@PathVariable String name) {
        ItemDto dto = itemService.getItemByName(name);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", "Item not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemDto dto) {
        dto.setId(id);
        ItemDto updated = itemService.updateItem(dto);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", "Item not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        ItemDto deleted = itemService.deleteItem(id);
        return deleted != null ? ResponseEntity.ok(deleted) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("result", "Item not found"));
    }
}
