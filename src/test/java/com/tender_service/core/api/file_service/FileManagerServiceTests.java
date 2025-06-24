package com.tender_service.core.api.file_service;

import com.tender_service.core.api.database.entity.FileCwk;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.FileRepository;
import com.tender_service.core.api.database.repository.TenderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileManagerServiceTests {

    @TempDir
    Path tempDir;

    @Mock
    private FileRepository fileRepo;

    @Mock
    private TenderRepository tenderRepo;

    @Mock
    private MultipartFile multipartFile;

    private FileManagerService fileManagerService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        fileManagerService = new FileManagerService();

        setPrivateField(fileManagerService, "fileRepo", fileRepo);
        setPrivateField(fileManagerService, "tenderRepo", tenderRepo);
        setPrivateField(fileManagerService, "baseUploadDir", tempDir.toString());
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = FileManagerService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSaveFileSuccessfully() throws Exception {
        Long tenderId = 1L;
        String originalFileName = "test.txt";
        String fileContent = "test content";

        TenderCwk tender = new TenderCwk();
        when(tenderRepo.findById(tenderId)).thenReturn(Optional.of(tender));
        when(multipartFile.getOriginalFilename()).thenReturn(originalFileName);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length());

        // Simulate file saving
        doAnswer(invocation -> {
            Path target = invocation.getArgument(0);
            Files.write(target, fileContent.getBytes());
            return null;
        }).when(multipartFile).transferTo(any(Path.class));

        FileCwk savedFile = fileManagerService.saveFile(tenderId, multipartFile);

        assertEquals(originalFileName, savedFile.getFileName());
        assertEquals(fileContent.length(), savedFile.getFileSize());
        assertTrue(Files.exists(Path.of(savedFile.getFilePath())));
        verify(fileRepo).save(any(FileCwk.class));
    }

    @Test
    void testLoadFileAsResource_success() throws Exception {
        Long tenderId = 1L;
        String fileName = UUID.randomUUID() + "_test.txt";
        Path filePath = tempDir.resolve(tenderId.toString()).resolve(fileName);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "test");

        setPrivateField(fileManagerService, "baseUploadDir", tempDir.toString());

        Resource resource = fileManagerService.loadFileAsResource(tenderId, fileName);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void testDeleteFile_success() throws Exception {
        Long tenderId = 1L;
        String fileName = UUID.randomUUID() + "_delete.txt";
        Path filePath = tempDir.resolve(tenderId.toString()).resolve(fileName);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "to delete");

        setPrivateField(fileManagerService, "baseUploadDir", tempDir.toString());

        fileManagerService.deleteFile(tenderId, fileName);

        assertFalse(Files.exists(filePath));
        verify(fileRepo).deleteByTenderIdAndFilePathName(tenderId, fileName);
    }

    @Test
    void testLoadFileAsResource_fileNotFound() {
        Long tenderId = 99L;
        String fileName = "missing.txt";

        Exception exception = assertThrows(RuntimeException.class, () ->
                fileManagerService.loadFileAsResource(tenderId, fileName));

        assertTrue(exception.getMessage().contains("File not found"));
    }

    @Test
    void testDeleteFile_fileDoesNotExist() {
        Long tenderId = 42L;
        String fileName = "nonexistent.txt";

        assertThrows(RuntimeException.class, () ->
                fileManagerService.deleteFile(tenderId, fileName));
    }

    @Test
    void testDeleteFolderWithFiles_success() throws Exception {
        Long tenderId = 123L;

        Path tenderDir = tempDir.resolve(tenderId.toString());
        Files.createDirectories(tenderDir);
        Path file1 = Files.writeString(tenderDir.resolve("file1.txt"), "File 1");
        Path file2 = Files.writeString(tenderDir.resolve("file2.txt"), "File 2");

        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));
        assertTrue(Files.exists(tenderDir));

        fileManagerService.deleteFolderWithFiles(tenderId);

        assertFalse(Files.exists(file1));
        assertFalse(Files.exists(file2));
        assertFalse(Files.exists(tenderDir));

        verify(fileRepo).deleteByTenderId(tenderId);
    }
}