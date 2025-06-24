package com.tender_service.feature.tender;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.file_service.FileManagerService;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.tender.service.TenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenderServiceTests {

    @InjectMocks
    private TenderService tenderService;

    @Mock
    private TenderRepository tenderRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private JwtService jwtService;

    @Mock
    private ParsingService parsingService;

    @Mock
    private FileManagerService fileManagerService;

    private final String jwtToken = "Bearer valid.jwt.token";
    private final String email = "test@example.com";

    private UserCwk mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new UserCwk();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setRoles(Set.of(Role.TENDERER, Role.USER));
    }

    @Test
    void testGetTenderById_TenderExistsAndOwnedByUser() throws ParseException {
        Long tenderId = 123L;
        TenderCwk mockTender = new TenderCwk();
        mockTender.setId(tenderId);
        mockTender.setUser(mockUser);

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(mockTender);

        TenderCwk result = tenderService.getTenderById(jwtToken, tenderId);
        assertEquals(mockTender, result);
    }

    @Test
    void testGetTenderById_TenderExistsButNotOwned() throws ParseException {
        Long tenderId = 124L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () ->
                tenderService.getTenderById(jwtToken, tenderId)
        );
    }

    @Test
    void testGetTenderById_ParsingFails() throws ParseException {
        Long tenderId = 126L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(false);
        when(parsingService.getTenderById(tenderId)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                tenderService.getTenderById(jwtToken, tenderId)
        );
    }

    @Test
    void testGetTenderById_UserNotFound() throws ParseException {
        Long tenderId = 37312021L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                tenderService.getTenderById(jwtToken, tenderId)
        );
    }

    @Test
    void testUpdateByIdAndJWT_TenderOwnedByUser_Success() throws ParseException {
        Long tenderId = 128L;
        TenderCwk tenderInDb = new TenderCwk();
        tenderInDb.setId(tenderId);
        tenderInDb.setTitle("Old Title");
        tenderInDb.setUser(mockUser);

        TenderCwk updatedFields = new TenderCwk();
        updatedFields.setId(tenderId);
        updatedFields.setTitle("Updated Title");

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(tenderInDb);
        when(tenderRepo.save(any(TenderCwk.class))).thenAnswer(i -> i.getArguments()[0]);

        TenderCwk result = tenderService.updateByIdAndJWT(jwtToken, updatedFields);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void testUpdateByIdAndJWT_TenderNotOwned() throws ParseException {
        Long tenderId = 129L;
        TenderCwk updatedFields = new TenderCwk();
        updatedFields.setId(tenderId);

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () ->
                tenderService.updateByIdAndJWT(jwtToken, updatedFields)
        );
    }

    @Test
    void testDeleteTenderById_TenderOwnedByUser_Success() throws ParseException {
        Long tenderId = 200L;
        TenderCwk tender = new TenderCwk();
        tender.setId(tenderId);
        tender.setUser(mockUser);

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findTenderCwkByUserAndId(mockUser, tenderId)).thenReturn(tender);

        assertDoesNotThrow(() -> tenderService.deleteByJWTandId(jwtToken, tenderId));
        verify(tenderRepo, times(1)).delete(tender);
    }

    @Test
    void testDeleteTenderById_TenderNotOwnedByUser() throws ParseException {
        Long tenderId = 201L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findTenderCwkByUserAndId(mockUser, tenderId)).thenReturn(null);

        assertThrows(AccessDeniedException.class, () ->
                tenderService.deleteByJWTandId(jwtToken, tenderId)
        );
        verify(tenderRepo, never()).delete((TenderCwk) any());
    }

    @Test
    void testDeleteTenderById_UserNotFound() throws ParseException {
        Long tenderId = 202L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                tenderService.deleteByJWTandId(jwtToken, tenderId)
        );
        verify(tenderRepo, never()).delete((TenderCwk) any());
    }

    @Test
    void testAddTenderForIdByAdmin_TenderAlreadyExists() {
        Long userId = 1L;
        Long tenderId = 300L;

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                tenderService.addTenderForIdByAdmin(userId, tenderId)
        );
    }

    @Test
    void testAddTenderForIdByAdmin_TenderNotFound() {
        Long userId = 1L;
        Long tenderId = 301L;

        when(userRepo.findById(userId)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(false);
        when(parsingService.getTenderById(tenderId)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                tenderService.addTenderForIdByAdmin(userId, tenderId)
        );
    }

    @Test
    void testUpdateById_UserNotTenderer() {
        Long tenderId = 400L;
        TenderCwk tender = new TenderCwk();
        tender.setId(tenderId);

        UserCwk nonTenderer = new UserCwk();
        nonTenderer.setRoles(Set.of(Role.USER));

        when(tenderRepo.findTenderCwkById(tenderId)).thenReturn(tender);
        when(userRepo.findUserCwkById(1L)).thenReturn(nonTenderer);

        assertThrows(IllegalArgumentException.class, () ->
                tenderService.updateById(tender, 1L, null, null, null)
        );
    }

    @Test
    void testDeleteById_TenderNotFound() {
        Long tenderId = 500L;

        when(tenderRepo.findTenderCwkById(tenderId)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                tenderService.deleteById(tenderId)
        );
    }

    @Test
    void testGetTenderForJWT_InvalidRole() throws ParseException {
        Map<String, String> params = Map.of();

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));

        assertThrows(IllegalArgumentException.class, () ->
                tenderService.getTenderForJWT(jwtToken, 0, 10, "id", "asc", "INVALID_ROLE", params)
        );
    }
}