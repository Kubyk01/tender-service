package com.tender_service.feature.tender;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.tender.service.TenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private final String jwtToken = "Bearer valid.jwt.token";
    private final String email = "test@example.com";

    private UserCwk mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new UserCwk();
        mockUser.setId(1L);
        mockUser.setEmail(email);
        mockUser.setRoles(Set.of(Role.TENDERER));
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

        ResponseEntity<?> response = tenderService.getTenderById(jwtToken, tenderId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockTender, response.getBody());
    }

    @Test
    void testGetTenderById_TenderExistsButNotOwned() throws ParseException {
        Long tenderId = 124L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(true);
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        ResponseEntity<?> response = tenderService.getTenderById(jwtToken, tenderId);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testGetTenderById_ParsingFails() throws ParseException {
        Long tenderId = 126L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.existsById(tenderId)).thenReturn(false);
        when(parsingService.getTenderById(tenderId)).thenReturn(null);

        ResponseEntity<?> response = tenderService.getTenderById(jwtToken, tenderId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetTenderById_UserNotFound() throws ParseException {
        Long tenderId = 37312021L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = tenderService.getTenderById(jwtToken, tenderId);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Couldnt parse id: 37312021", response.getBody());
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

        ResponseEntity<?> response = tenderService.updateByIdAndJWT(jwtToken, updatedFields);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Title", ((TenderCwk) response.getBody()).getTitle());
    }

    @Test
    void testUpdateByIdAndJWT_TenderNotOwned() throws ParseException {
        Long tenderId = 129L;
        TenderCwk updatedFields = new TenderCwk();
        updatedFields.setId(tenderId);

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        ResponseEntity<?> response = tenderService.updateByIdAndJWT(jwtToken, updatedFields);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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

        ResponseEntity<?> response = tenderService.deleteByJWTandId(jwtToken, tenderId);

        verify(tenderRepo, times(1)).delete(tender);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteTenderById_TenderNotOwnedByUser() throws ParseException {
        Long tenderId = 201L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        ResponseEntity<?> response = tenderService.deleteByJWTandId(jwtToken, tenderId);

        verify(tenderRepo, never()).delete((TenderCwk) any());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody());
    }

    @Test
    void testDeleteTenderById_UserNotFound() throws ParseException {
        Long tenderId = 202L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = tenderService.deleteByJWTandId(jwtToken, tenderId);

        verify(tenderRepo, never()).delete((TenderCwk) any());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Couldnt delete tender", response.getBody());
    }

    @Test
    void testDeleteTenderById_TenderNotFound() throws ParseException {
        Long tenderId = 203L;

        when(jwtService.getEmailFromToken("valid.jwt.token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(tenderRepo.findByIdAndUserInAnyRole(tenderId, mockUser)).thenReturn(null);

        ResponseEntity<?> response = tenderService.deleteByJWTandId(jwtToken, tenderId);

        verify(tenderRepo, never()).delete((TenderCwk) any());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody());
    }
}