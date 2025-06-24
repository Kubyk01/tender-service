package com.tender_service.feature.tender.controller;

import com.tender_service.core.api.database.entity.Participant;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.feature.tender.model.TenderDTO;
import com.tender_service.feature.tender.service.TenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tender")
@RequiredArgsConstructor
public class TenderController {

    @Autowired
    private TenderService tenderService;

    @Operation(summary = "get Parsed Tender for User", description = "Get Parsed Tender by its id for user")
    @GetMapping()
    public ResponseEntity<TenderCwk> getTender(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam Long id
    ) throws ParseException {
        return ResponseEntity.ok(tenderService.getTenderById(auth, id));
    }

    @Operation(summary = "get Tenders for Role", description = "Get Tenders for specified role")
    @PreAuthorize("hasAuthority(#role)")
    @GetMapping("/{role}")
    public ResponseEntity<List<TenderDTO>> getTenderForJWT(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @PathVariable String role,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ) throws ParseException {
        return ResponseEntity.ok(tenderService.getTenderForJWT(
                auth, pageNumber, pageSize, sortBy, sortDirection, role, allParams
        ));
    }

    @Operation(summary = "update Tender for User", description = "Update tender")
    @PreAuthorize("hasAuthority('USER')")
    @PatchMapping("")
    public ResponseEntity<TenderCwk> updateByIdAndJWT(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestBody TenderCwk tender
    ) throws ParseException {
        return ResponseEntity.ok(tenderService.updateByIdAndJWT(auth, tender));
    }

    @Operation(summary = "Delete tender by tenderID for user", description = "Delete tender by tenderID and jwt")
    @PreAuthorize("hasAuthority('USER')")
    @DeleteMapping("")
    public ResponseEntity<Void> deleteTender(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam Long id
    ) throws ParseException {
        tenderService.deleteByJWTandId(auth, id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "get all tenders in database", description = "Get all tenders (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<Map<String, Object>>> getAllTenderByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ){
        return ResponseEntity.ok(tenderService.getAllTenders(
                pageNumber, pageSize, sortBy, sortDirection, allParams
        ));
    }

    @Operation(summary = "Update tender By Admin", description = "Update tender (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/admin")
    public ResponseEntity<TenderCwk> updateById(
            @RequestParam(required = false) Long tendererId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long participantId,
            @RequestParam(required = false) Long userId,
            @RequestBody TenderCwk tender
    ){
        return ResponseEntity.ok(tenderService.updateById(
                tender, tendererId, supplierId, participantId, userId
        ));
    }

    @Operation(summary = "Delete tender by tenderID", description = "Delete tender by ID (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin")
    public ResponseEntity<Void> deleteById(
            @RequestParam Long id
    ){
        tenderService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "add parsed tenders by id to UserID", description = "Add tender to user (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<Void> addTender(
            @RequestParam Long id,
            @RequestParam Long tenderId
    ){
        tenderService.addTenderForIdByAdmin(id, tenderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "get all units", description = "Get all units from database")
    @GetMapping("/units")
    public ResponseEntity<List<String>> getUnits(){
        return ResponseEntity.ok(tenderService.getUnits());
    }

    @Operation(summary = "get all participants", description = "Get all participants (admin only)")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/participants")
    public ResponseEntity<List<Participant>> getParticipants(){
        return ResponseEntity.ok(tenderService.getParticipants());
    }
}
