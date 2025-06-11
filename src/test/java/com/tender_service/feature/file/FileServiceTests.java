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
import org.mockito.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

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
    void saveFile_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test-content".getBytes());
        UserCwk user = new UserCwk();
        FileCwk fileCwk = new FileCwk();
        fileCwk.setFileName(fileName);

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);
        when(fileManagerService.saveFile(tenderId, file)).thenReturn(fileCwk);

        ResponseEntity<?> response = fileService.saveFile(token, tenderId, file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(fileCwk, response.getBody());
    }

    @Test
    void saveFile_UserNotFound() throws Exception {
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileService.saveFile(token, tenderId, null);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Couldnt save file", response.getBody());
    }

    @Test
    void loadFile_Success() throws Exception {
        UserCwk user = new UserCwk();
        Resource resource = new ByteArrayResource("content".getBytes());

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);
        when(fileManagerService.loadFileAsResource(tenderId, savedFileName)).thenReturn(resource);

        ResponseEntity<?> response = fileService.loadFile(token, tenderId, savedFileName);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Resource);
    }

    @Test
    void deleteFile_Success() throws ParseException {
        UserCwk user = new UserCwk();

        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(tenderRepo.existsByUserAndId(user, tenderId)).thenReturn(true);

        ResponseEntity<?> response = fileService.deleteFile(token, tenderId, savedFileName);

        verify(fileManagerService).deleteFile(tenderId, savedFileName);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void saveFileByAdmin_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", fileName, "application/pdf", "test".getBytes());
        FileCwk fileCwk = new FileCwk();
        fileCwk.setFileName(fileName);

        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(fileManagerService.saveFile(tenderId, file)).thenReturn(fileCwk);

        ResponseEntity<?> response = fileService.saveFileByAdmin(tenderId, file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(fileCwk, response.getBody());
    }

    @Test
    void loadFileByAdmin_FileFound() {
        Resource resource = new ByteArrayResource("test".getBytes());

        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(fileManagerService.loadFileAsResource(tenderId, savedFileName)).thenReturn(resource);

        ResponseEntity<?> response = fileService.loadFileByAdmin(tenderId, savedFileName);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(resource, response.getBody());
    }

    @Test
    void deleteFileByAdmin_FileFound() {
        when(tenderRepo.existsById(tenderId)).thenReturn(true);

        ResponseEntity<?> response = fileService.deleteFileByAdmin(tenderId, savedFileName);

        verify(fileManagerService).deleteFile(tenderId, savedFileName);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void deleteFileByAdmin_TenderNotFound() {
        when(tenderRepo.existsById(tenderId)).thenReturn(false);

        ResponseEntity<?> response = fileService.deleteFileByAdmin(tenderId, savedFileName);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Couldnt find tender", response.getBody());
    }
}
