package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.ItemDto;
import lk.acpt.smartbiz.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/item")
@PreAuthorize("hasRole('OWNER')") // Owner-only
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<?> saveItem(@RequestBody ItemDto dto) {
        ItemDto result = itemService.saveItem(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id) {
        ItemDto result = itemService.deleteItem(id);
        if (result != null) return new ResponseEntity<>(result, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No item with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllItems() {
        return new ResponseEntity<>(itemService.getAllItems(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto dto, @PathVariable Long id) {
        dto.setId(id);
        ItemDto updated = itemService.updateItem(dto);
        if (updated != null) return new ResponseEntity<>(updated, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No item with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@PathVariable Long id) {
        ItemDto item = itemService.getItemById(id);
        if (item != null) return new ResponseEntity<>(item, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No item found with id " + id);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get_by_name/{name}")
    public ResponseEntity<Object> getItemByName(@PathVariable String name) {
        ItemDto byName = itemService.getItemByName(name);
        if (byName != null) return new ResponseEntity<>(byName, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No item with name " + name + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
}