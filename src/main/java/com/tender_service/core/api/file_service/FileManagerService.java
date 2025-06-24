package com.tender_service.core.api.file_service;

import com.tender_service.core.api.database.entity.FileCwk;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.FileRepository;
import com.tender_service.core.api.database.repository.TenderRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileManagerService {

    @Autowired
    private FileRepository fileRepo;
    @Autowired
    private TenderRepository tenderRepo;

    @Value("${file.upload-dir:uploads}")
    private String baseUploadDir;

    public FileCwk saveFile(Long tenderId, MultipartFile multipartFile) throws IOException {
        TenderCwk tender = tenderRepo.findById(tenderId)
                .orElseThrow(() -> new RuntimeException("Tender not found: " + tenderId));

        Path tenderDir = Paths.get(baseUploadDir, tenderId.toString());
        Files.createDirectories(tenderDir);

        String originalFileName = multipartFile.getOriginalFilename();
        String savedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = tenderDir.resolve(savedFileName);

        multipartFile.transferTo(filePath);

        FileCwk fileCwk = new FileCwk();
        fileCwk.setFileName(originalFileName);
        fileCwk.setFilePathName(savedFileName);
        fileCwk.setFilePath(filePath.toString());
        fileCwk.setFileSize((int) multipartFile.getSize());
        fileCwk.setTender(tender);
        fileRepo.save(fileCwk);

        return fileCwk;
    }

    public Resource loadFileAsResource(Long tenderId, String savedFileName) {
        try {
            Path filePath = Paths.get(baseUploadDir, tenderId.toString(), savedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or unreadable: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File path is invalid for tenderId: " + tenderId + ", fileName: " + savedFileName, e);
        }
    }

    @Transactional
    public void deleteFile(Long tenderId, String savedFileName) {
        try {
            Path filePath = Paths.get(baseUploadDir, tenderId.toString(), savedFileName).normalize();

            fileRepo.deleteByTenderIdAndFilePathName(tenderId, savedFileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + savedFileName + " for tenderId: " + tenderId, e);
        }
    }

    @Transactional
    public void deleteFolderWithFiles(Long tenderId) {
        Path tenderDir = Paths.get(baseUploadDir, tenderId.toString()).normalize();

        fileRepo.deleteByTenderId(tenderId);

        if (Files.exists(tenderDir)) {
            try {
                Files.walk(tenderDir)
                        .sorted((p1, p2) -> p2.compareTo(p1))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete file or folder: " + path, e);
                            }
                        });
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete directory for tenderId: " + tenderId, e);
            }
        }
    }
}
