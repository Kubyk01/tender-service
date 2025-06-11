package com.tender_service.feature.file.service;

import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.file_service.FileManagerService;
import com.tender_service.core.configuration.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public ResponseEntity<?> saveFile(String auth, Long id, MultipartFile file){
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            boolean tender = tenderRepo.existsByUserAndId(user, id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            return ResponseEntity.ok(fileManagerService.saveFile(id, file));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt save file");
        }
    }

    public ResponseEntity<?> loadFile(String auth, Long id, String filePathName) {
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            boolean tender = tenderRepo.existsByUserAndId(user, id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            Resource resource = fileManagerService.loadFileAsResource(id, filePathName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt download file");
        }
    }

    public ResponseEntity<?> deleteFile(String auth, Long id, String filePathName){
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            boolean tender = tenderRepo.existsByUserAndId(user, id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            fileManagerService.deleteFile(id, filePathName);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt delete file");
        }
    }

    public ResponseEntity<?> saveFileByAdmin(Long id, MultipartFile file){
        try {
            boolean tender = tenderRepo.existsById(id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            return ResponseEntity.ok(fileManagerService.saveFile(id, file));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt save file");
        }
    }

    public ResponseEntity<?> loadFileByAdmin(Long id, String filePathName) {
        try {
            boolean tender = tenderRepo.existsById(id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            Resource resource = fileManagerService.loadFileAsResource(id, filePathName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt download file");
        }
    }

    public ResponseEntity<?> deleteFileByAdmin(Long id, String filePathName){
        try {
            boolean tender = tenderRepo.existsById(id);

            if (!tender){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt find tender");
            }

            fileManagerService.deleteFile(id, filePathName);
            return ResponseEntity.ok().build();
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt delete file");
        }
    }

}
