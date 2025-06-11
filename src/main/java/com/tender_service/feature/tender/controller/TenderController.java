package com.tender_service.feature.tender.controller;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.feature.tender.service.TenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/tender")
@RequiredArgsConstructor
public class TenderController {

    @Autowired
    private TenderService tenderService;

    @Operation(summary = "get Parsed Tender for User", description = "Get Pared Tender by it id for user, if it in database already - return it")
    @GetMapping()
    public ResponseEntity<?> getTender(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam Long id
    ){
        return tenderService.getTenderById(auth, id);
    }

    @Operation(summary = "get Tenders for Role", description = "Get Tender by it id for role from database")
    @PreAuthorize("hasAuthority(#role)")
    @GetMapping("/{role}")
    public ResponseEntity<?> getTenderForJWT(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @PathVariable String role,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ){
        return tenderService.getTenderForJWT(auth, pageNumber, pageSize, sortBy, sortDirection, role, allParams);
    }

    @Operation(summary = "update Tender for User", description = "Update tender")
    @PreAuthorize("hasAuthority('USER')")
    @PatchMapping("")
    public ResponseEntity<?> updateByIdAndJWT(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestBody TenderCwk tender
    ){
        return tenderService.updateByIdAndJWT(auth, tender);
    }

    @Operation(summary = "Delete tender by tenderID for user", description = "Delete tender by tenderID and jwt")
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("")
    public ResponseEntity<?> deleteTender(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam Long id
    ){
        return tenderService.deleteByJWTandId(auth, id);
    }

    @Operation(summary = "get all tenders in database", description = "get all tenders in database, require admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> getAllTenderByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ){
        return tenderService.getAllTenders(pageNumber, pageSize, sortBy, sortDirection, allParams);
    }

    @Operation(summary = "Update tender By Admin", description = "Update tedner, require admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/admin")
    public ResponseEntity<?> updateById(
            @RequestParam(required = false) Long tendererId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long userId,
            @RequestBody TenderCwk tender
    ){
        return tenderService.updateById(tender, tendererId, supplierId, userId);
    }

    @Operation(summary = "Delete tender by tenderID", description = "Delete tender by tenderID, require admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin")
    public ResponseEntity<?> deleteById(
            @RequestParam Long id
    ){
        return tenderService.deleteById(id);
    }

    @Operation(summary = "add parsed tenders by id to UserID", description = "add parsed tenders by id to UserID, require admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<?> addTender(
            @RequestParam Long id,
            @RequestParam Long tenderId
    ){
        return tenderService.addTenderForIdByAdmin(id, tenderId);
    }

    @Operation(summary = "get all units", description = "get all units from database")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/units")
    public ResponseEntity<?> getUnits(){
        return tenderService.getUnits();
    }
}
