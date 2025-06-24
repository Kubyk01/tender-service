package com.tender_service.feature.file.controller;

import com.tender_service.core.api.database.entity.FileCwk;
import com.tender_service.feature.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Parameter;

import java.text.ParseException;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "File service api", description = "File logic")
public class FileController {

    @Autowired
    private FileService fileService;

    @Operation(summary = "Save file for tenderID", description = "Save file")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileCwk> saveFile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam("tenderId") Long id,
            @RequestPart("file") MultipartFile file
    ) throws ParseException {
        return ResponseEntity.ok(fileService.saveFile(auth, id, file));
    }

    @Operation(summary = "Download file", description = "Download a file for a given tender ID and file name")
    @GetMapping()
    public ResponseEntity<Resource> downloadFile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam("tenderId") Long id,
            @RequestParam("filePathName") String filePathName
    ) throws ParseException {
        return fileService.loadFile(auth, id, filePathName);
    }

    @Operation(summary = "Delete file", description = "Delete a file for a given tender ID and file name")
    @DeleteMapping()
    public ResponseEntity<Void> deleteFile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam("tenderId") Long id,
            @RequestParam("filePathName") String filePathName
    ) throws ParseException {
        fileService.deleteFile(auth, id, filePathName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Save file for tenderID by Admin", description = "Save file")
    @PostMapping(value = "/admin", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<FileCwk> saveFileByAdmin(
            @RequestParam("tenderId") Long id,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(fileService.saveFileByAdmin(id, file));
    }

    @Operation(summary = "Download file by Admin", description = "Download a file for a given tender ID and file name")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Resource> downloadFileByAdmin(
            @RequestParam("tenderId") Long id,
            @RequestParam("filePathName") String filePathName
    ) {
        return fileService.loadFileByAdmin(id, filePathName);
    }

    @Operation(summary = "Delete file by Admin", description = "Delete a file for a given tender ID and file name")
    @DeleteMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteFileByAdmin(
            @RequestParam("tenderId") Long id,
            @RequestParam("filePathName") String filePathName
    ) {
        fileService.deleteFileByAdmin(id, filePathName);
        return ResponseEntity.ok().build();
    }
}