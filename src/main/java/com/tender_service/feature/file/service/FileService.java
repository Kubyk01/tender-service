package com.tender_service.feature.file.service;

import com.tender_service.core.api.database.entity.FileCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.file_service.FileManagerService;
import com.tender_service.core.configuration.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.FileSystemException;
import java.text.ParseException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    @Autowired
    private FileManagerService fileManagerService;
    @Autowired
    private TenderRepository tenderRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;

    public FileCwk saveFile(String auth, Long id, MultipartFile file) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!tenderRepo.existsByUserAndId(user, id)) {
            throw new AccessDeniedException("Access denied to tender");
        }

        try {
            return fileManagerService.saveFile(id, file);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not save file", e);
        }
    }

    public ResponseEntity<Resource> loadFile(String auth, Long id, String filePathName) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!tenderRepo.existsByUserAndId(user, id)) {
            throw new AccessDeniedException("Access denied to tender");
        }

        try {
            Resource resource = fileManagerService.loadFileAsResource(id, filePathName);
            return createFileResponse(resource);
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof FileSystemException) {
                throw new ResponseStatusException(NOT_FOUND, "File not found", e);
            }
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not load file", e);
        }
    }

    public void deleteFile(String auth, Long id, String filePathName) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!tenderRepo.existsByUserAndId(user, id)) {
            throw new AccessDeniedException("Access denied to tender");
        }

        try {
            fileManagerService.deleteFile(id, filePathName);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not delete file", e);
        }
    }

    public FileCwk saveFileByAdmin(Long id, MultipartFile file) {
        if (!tenderRepo.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Tender not found");
        }
        try {
            return fileManagerService.saveFile(id, file);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not save file", e);
        }
    }

    public ResponseEntity<Resource> loadFileByAdmin(Long id, String filePathName) {
        if (!tenderRepo.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Tender not found");
        }
        try {
            Resource resource = fileManagerService.loadFileAsResource(id, filePathName);
            return createFileResponse(resource);
        } catch (Exception e) {
            if (e.getCause() != null && e.getCause() instanceof FileSystemException) {
                throw new ResponseStatusException(NOT_FOUND, "File not found", e);
            }
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not load file", e);
        }
    }

    public void deleteFileByAdmin(Long id, String filePathName) {
        if (!tenderRepo.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Tender not found");
        }
        try {
            fileManagerService.deleteFile(id, filePathName);
        } catch (Exception e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Could not delete file", e);
        }
    }

    private ResponseEntity<Resource> createFileResponse(Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}