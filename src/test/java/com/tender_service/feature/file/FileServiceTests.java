package com.tender_service.feature.file;

import com.tender_service.core.api.database.entity.FileCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.file_service.FileManagerService;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.file.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTests {

    @InjectMocks
    private FileService fileService;

    @Mock
    private FileManagerService fileManagerService;
    @Mock
    private TenderRepository tenderRepo;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private final String token = "Bearer test.jwt.token";
    private final String email = "user@example.com";
    private final Long tenderId = 1L;
    private final String fileName = "document.pdf";
    private final String savedFileName = "uuid_document.pdf";

    @Test
    void saveFile_Success() throws IOException, ParseException {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test-content".getBytes());
        UserCwk user = new UserCwk();
        FileCwk fileCwk = new FileCwk();
        fileCwk.setFileName(fileName);

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);
        when(fileManagerService.saveFile(tenderId, file)).thenReturn(fileCwk);

        FileCwk result = fileService.saveFile(token, tenderId, file);
        assertEquals(fileCwk, result);
    }

    @Test
    void saveFile_UserNotFound() throws ParseException {
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                fileService.saveFile(token, tenderId, null)
        );
    }

    @Test
    void saveFile_AccessDenied() throws ParseException {
        UserCwk user = new UserCwk();
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test-content".getBytes());

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                fileService.saveFile(token, tenderId, file)
        );
    }

    @Test
    void loadFile_Success() throws ParseException {
        UserCwk user = new UserCwk();
        Resource resource = new ByteArrayResource("content".getBytes());

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);
        when(fileManagerService.loadFileAsResource(tenderId, savedFileName)).thenReturn(resource);

        ResponseEntity<Resource> response = fileService.loadFile(token, tenderId, savedFileName);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(resource, response.getBody());
    }

    @Test
    void deleteFile_Success() throws ParseException {
        UserCwk user = new UserCwk();

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);

        assertDoesNotThrow(() -> fileService.deleteFile(token, tenderId, savedFileName));
        verify(fileManagerService).deleteFile(tenderId, savedFileName);
    }

    @Test
    void saveFileByAdmin_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test".getBytes());
        FileCwk fileCwk = new FileCwk();
        fileCwk.setFileName(fileName);

        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(fileManagerService.saveFile(tenderId, file)).thenReturn(fileCwk);

        FileCwk result = fileService.saveFileByAdmin(tenderId, file);
        assertEquals(fileCwk, result);
    }

    @Test
    void saveFileByAdmin_TenderNotFound() {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test".getBytes());
        when(tenderRepo.existsById(tenderId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                fileService.saveFileByAdmin(tenderId, file)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tender not found", exception.getReason());
    }

    @Test
    void loadFileByAdmin_Success() {
        Resource resource = new ByteArrayResource("test".getBytes());

        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(fileManagerService.loadFileAsResource(tenderId, savedFileName)).thenReturn(resource);

        ResponseEntity<Resource> response = fileService.loadFileByAdmin(tenderId, savedFileName);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(resource, response.getBody());
    }

    @Test
    void loadFileByAdmin_TenderNotFound() {
        when(tenderRepo.existsById(tenderId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                fileService.loadFileByAdmin(tenderId, savedFileName)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tender not found", exception.getReason());
    }

    @Test
    void deleteFileByAdmin_Success() {
        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        assertDoesNotThrow(() -> fileService.deleteFileByAdmin(tenderId, savedFileName));
        verify(fileManagerService).deleteFile(tenderId, savedFileName);
    }

    @Test
    void deleteFileByAdmin_TenderNotFound() {
        when(tenderRepo.existsById(tenderId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                fileService.deleteFileByAdmin(tenderId, savedFileName)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tender not found", exception.getReason());
    }
}