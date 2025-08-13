package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.SupplierDto;
import lk.acpt.smartbiz.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/supplier")
@PreAuthorize("hasRole('OWNER')") // Owner-only
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<?> saveSupplier(@RequestBody SupplierDto dto) {
        SupplierDto result = supplierService.saveSupplier(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteSupplier(@PathVariable Long id) {
        SupplierDto result = supplierService.deleteSupplier(id);
        if (result != null) return new ResponseEntity<>(result, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No supplier with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        return new ResponseEntity<>(supplierService.getAllSuppliers(), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateSupplier(@RequestBody SupplierDto dto, @PathVariable Long id) {
        dto.setId(id);
        SupplierDto updated = supplierService.updateSupplier(dto);
        if (updated != null) return new ResponseEntity<>(updated, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No supplier with id " + id + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getSupplierById(@PathVariable Long id) {
        SupplierDto supplier = supplierService.getSupplierById(id);
        if (supplier != null) return new ResponseEntity<>(supplier, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No supplier found with id " + id);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/get_by_email/{email}")
    public ResponseEntity<Object> getSupplierByEmail(@PathVariable String email) {
        SupplierDto byEmail = supplierService.getSupplierByEmail(email);
        if (byEmail != null) return new ResponseEntity<>(byEmail, HttpStatus.OK);

        Map<String, Object> res = new HashMap<>();
        res.put("result", "No supplier with email " + email + " was found");
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
}
