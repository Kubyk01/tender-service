package com.tender_service.feature.user.service;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.utils.UserCwkSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.tender_service.utils.BeanCopyUtils.getNullPropertyNames;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> getUsersInfo(String auth){
        try{
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error during getting user");
        }
    }

    public ResponseEntity<?> getUsersInfoForRoleByAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Map<String, String> allParams, String role){
        try{
            Specification<UserCwk> spec = Specification.where(null);

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String field = entry.getKey();
                String value = entry.getValue();
                spec = spec.and(UserCwkSpecification.fieldStartsWith(field, value));
            }

            if(role != null) {
                spec = spec.and((root, query, cb) -> root.join("roles").in(Role.valueOf(role.toUpperCase())));
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

            List<UserCwk> list = userRepo.findAll(spec, pageable).getContent();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error during getting user");
        }
    }

    public ResponseEntity<?> updateUserInfo(String auth, UserCWKDTO updatedUserCwk){
        try{
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            BeanUtils.copyProperties(updatedUserCwk, user, getNullPropertyNames(updatedUserCwk));

            if(user.getPassword() != null){
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            userRepo.save(user);

            String token = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());
            return ResponseEntity.ok(Map.of("accessToken", token,"refreshToken", refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error during updating user");
        }
    }

    public ResponseEntity<?> updateUserInfoForIdByAdmin(Long id, UserCwk updatedUserCwk){
        try{
            UserCwk user = userRepo.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            BeanUtils.copyProperties(updatedUserCwk, user, getNullPropertyNames(updatedUserCwk));

            if(user.getPassword() != null){
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            userRepo.save(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error during updating user");
        }
    }

    public ResponseEntity<?> deleteUserForIdByAdmin(Long id){
        try{
            UserCwk user = userRepo.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            userRepo.delete(user);
            return ResponseEntity.ok("Succefully deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error during deleting user");
        }
    }
}
