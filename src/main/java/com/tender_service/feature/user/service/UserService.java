package com.tender_service.feature.user.service;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.user.model.AuthorizationDTO;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.utils.UserCwkSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.*;

import static com.tender_service.utils.BeanCopyUtils.getNullPropertyNames;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserCwk getUsersInfo(String auth) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public List<UserCwk> getUsersInfoForRoleByAdmin(
            Integer pageNumber, Integer pageSize, String sortBy,
            String sortDirection, Map<String, String> allParams, String role) {
        Specification<UserCwk> spec = Specification.where(null);

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();
            spec = spec.and(UserCwkSpecification.fieldStartsWith(field, value));
        }

        if (role != null) {
            spec = spec.and((root, query, cb) ->
                    root.join("roles").in(Role.valueOf(role.toUpperCase())));
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        return userRepo.findAll(spec, pageable).getContent();
    }

    public Map<String, String> updateUserInfo(String auth, UserCWKDTO updatedUserCwk) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        BeanUtils.copyProperties(updatedUserCwk, user, getNullPropertyNames(updatedUserCwk));

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepo.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return Map.of("accessToken", token, "refreshToken", refreshToken);
    }

    public UserCwk updateUserInfoForIdByAdmin(String auth, Long id, UserCwk updatedUserCwk) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk admin = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserCwk user = (id == 0) ?
                admin :
                userRepo.findById(id).orElseThrow(() ->
                        new ResponseStatusException(NOT_FOUND, "User not found with id: " + id));

        Set<Role> requestedRoles = new HashSet<>(updatedUserCwk.getRoles());

        if (user.getRoles().contains(Role.ADMIN)) {
            if (!admin.getId().equals(user.getId())) {
                throw new AccessDeniedException("Cannot access");
            }
            requestedRoles.add(Role.ADMIN);
        } else {
            requestedRoles.remove(Role.ADMIN);
        }

        BeanUtils.copyProperties(updatedUserCwk, user, getNullPropertyNames(updatedUserCwk));
        user.setRoles(requestedRoles);

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepo.save(user);
    }

    public String deleteUserForIdByAdmin(Long id) {
        UserCwk user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id: " + id));
        if(user.getRoles().contains(Role.ADMIN)){
            throw new AccessDeniedException("Cannot access");
        }
        userRepo.delete(user);
        return "Successfully deleted";
    }

    public String registration(AuthorizationDTO request) {
        String email = request.getEmail();
        if (userRepo.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already in use");
        }

        if (userRepo.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(CONFLICT, "Username already in use");
        }

        UserCwk user = new UserCwk();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setUserStatus(request.getUserStatus());

        Set<Role> roles = new HashSet<>(request.getRoles() != null ?
                request.getRoles() : Collections.emptySet());
        roles.remove(Role.ADMIN);
        user.setRoles(roles);

        userRepo.save(user);
        return "User created";
    }
}