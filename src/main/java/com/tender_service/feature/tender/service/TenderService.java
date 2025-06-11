package com.tender_service.feature.tender.service;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.utils.TenderCwkSpecification;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.tender_service.utils.BeanCopyUtils.getNullPropertyNames;

@Service
@RequiredArgsConstructor
public class TenderService {

    @Autowired
    private TenderRepository tenderRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ParsingService parsingService;

    public ResponseEntity<?> getTenderById(String auth, Long id) {
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            if (tenderRepo.existsById(id)){
                TenderCwk res = tenderRepo.findByIdAndUserInAnyRole(id, user);
                if (res == null){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }

                return ResponseEntity.ok(res);
            }

            ParsedTenderDTO parsed = parsingService.getTenderById(id);

            if (parsed == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found tender by id: " + id);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            DateTimeFormatter formatterWithoutTime = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            TenderCwk newtender = new TenderCwk();
            newtender.setUser(user);
            newtender.setId(id);
            newtender.setProzorroNumber(parsed.getProzorroNumber());
            newtender.setProcedureType(parsed.getProcedureType());
            newtender.setOrganizerName(parsed.getOrganizer().getName());
            newtender.setTitle(parsed.getTitle());
            newtender.setCategoryId(parsed.getCategory().getId());
            newtender.setCategoryCode(parsed.getCategory().getCode());
            newtender.setCategoryTitle(parsed.getCategory().getTitle());
            newtender.setStatusTitle(parsed.getStatusTitle());
            newtender.setBudgetAmount(parsed.getBudget().getAmount());
            newtender.setBudgetAmountTitle(parsed.getBudget().getAmountTitle());
            newtender.setWithVat(parsed.getBudget().isWithVat());
            newtender.setVatTitle(parsed.getBudget().getVatTitle());
            newtender.setCurrencyTitle(parsed.getBudget().getCurrencyTitle());
            newtender.setCurrencyHtmlTitle(parsed.getBudget().getCurrencyHtmlTitle());
            newtender.setCurrencyId(parsed.getBudget().getCurrencyId());
            newtender.setParticipantionCost(parsed.getParticipationCost());
            newtender.setDeliveryAddress(parsed.getNomenclaturesList().getFirst().getDeliveryAdress());

            try{
                newtender.setPaymentTermsDay(parsed.getPaymentTerms().getFirst().getDays());
            } catch (Exception _) {}
            try{
                newtender.setGuaranteeBank(parsed.getGuarantee().getAmountTitle());
            } catch (Exception _) {}
            try{
                newtender.setEnquiryPeriodStart(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodStart(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setEnquiryPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodEnd(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setTenderingPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getTenderingPeriodEnd(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setAuctionStart(LocalDateTime.parse(parsed.getImportantDates().getAuctionStart(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setDeliveryPeriodTo(LocalDate.parse(parsed.getNomenclaturesList().getFirst().getDeliveryPeriodTo() ,formatterWithoutTime));
            } catch (Exception _) {}

            tenderRepo.save(newtender);
            return ResponseEntity.ok(newtender);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt parse id: " + id);
        }
    }

    public ResponseEntity<?> getTenderForJWT(String auth, Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, String role, Map<String, String> allParams){
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            Specification<TenderCwk> spec;

            switch (Role.valueOf(role)) {
                case USER: {
                    spec = TenderCwkSpecification.hasUserId(user.getId());
                    allParams.remove("userId");
                    break;
                }
                case SUPPLIER: {
                    spec = TenderCwkSpecification.hasSupplierId(user.getId());
                    allParams.remove("supplierId");
                    break;
                }
                case TENDERER: {
                    spec = TenderCwkSpecification.hasTendererId(user.getId());
                    allParams.remove("tendererId");
                    break;
                }
                default:{
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found role");
                }
            }

            Set<String> processedKeys = new HashSet<>();

            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                if (processedKeys.contains(key)) continue;

                if (key.endsWith("_start") || key.endsWith("_stop")) {
                    String baseKey = key.replaceAll("_(start|stop)$", "");
                    String startVal = allParams.get(baseKey + "_start");
                    String stopVal = allParams.get(baseKey + "_stop");

                    spec = spec.and(TenderCwkSpecification.fieldInRange(baseKey, startVal, stopVal));

                    processedKeys.add(baseKey + "_start");
                    processedKeys.add(baseKey + "_stop");
                } else if (!processedKeys.contains(key + "_start") && !processedKeys.contains(key + "_stop")) {
                    spec = spec.and(TenderCwkSpecification.fieldStartsWith(key, entry.getValue()));
                    processedKeys.add(key);
                }
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

            List<TenderCwk> list = tenderRepo.findAll(spec, pageable).getContent();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not get tenders");
        }
    }

    public ResponseEntity<?> getAllTenders(Integer pageNumber, Integer pageSize, String sortBy, String sortDirection, Map<String, String> allParams){
            try {
                Specification<TenderCwk> spec = Specification.where(null);

                Set<String> processedKeys = new HashSet<>();

                for (Map.Entry<String, String> entry : allParams.entrySet()) {
                    String key = entry.getKey();
                    if (processedKeys.contains(key)) continue;

                    if (key.endsWith("_start") || key.endsWith("_stop")) {
                        String baseKey = key.replaceAll("_(start|stop)$", "");
                        String startVal = allParams.get(baseKey + "_start");
                        String stopVal = allParams.get(baseKey + "_stop");

                        spec = spec.and(TenderCwkSpecification.fieldInRange(baseKey, startVal, stopVal));

                        processedKeys.add(baseKey + "_start");
                        processedKeys.add(baseKey + "_stop");
                    } else if (!processedKeys.contains(key + "_start") && !processedKeys.contains(key + "_stop")) {
                        spec = spec.and(TenderCwkSpecification.fieldStartsWith(key, entry.getValue()));
                        processedKeys.add(key);
                    }
                }

                Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
                PageRequest pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

                List<TenderCwk> list = tenderRepo.findAll(spec, pageable).getContent();
                List<Map<String, Object>> responseList = new ArrayList<>();

                for (TenderCwk tender : list) {
                    Map<String, Object> tenderUserPair = new HashMap<>();

                    if (tender.getUser() != null) {
                        Hibernate.initialize(tender.getUser());
                        UserCwk user = (UserCwk) Hibernate.unproxy(tender.getUser());
                        tenderUserPair.put("user", user);
                    }

                    tenderUserPair.put("tender", tender);

                    responseList.add(tenderUserPair);
                }

                return ResponseEntity.ok(responseList);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt get tenders");
            }
    }

    public ResponseEntity<?> updateByIdAndJWT(String auth, TenderCwk updatedFields){
        try{
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            TenderCwk res = tenderRepo.findByIdAndUserInAnyRole(updatedFields.getId(), user);

            if (res == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            BeanUtils.copyProperties(updatedFields, res, getNullPropertyNames(updatedFields));

            tenderRepo.save(res);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt update tender");
        }
    }

    public ResponseEntity<?> updateById(TenderCwk updatedFields, Long tendererId, Long supplierId, Long userId){
        try{
            TenderCwk res = tenderRepo.findTenderCwkById(updatedFields.getId());

            if (res == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            }

            updatedFields.setFiles(null);

            if (tendererId != null){
                try{
                    UserCwk user = userRepo.findUserCwkById(tendererId);

                    if (user.getRoles().contains(Role.TENDERER)){
                        res.setTenderer(user);
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("User isnt tenderer");
                    }
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Error getting user for tenderer");
                }
            }

            if (supplierId != null) {
                try {
                    UserCwk user = userRepo.findUserCwkById(supplierId);

                    if (user.getRoles().contains(Role.SUPPLIER)){
                        res.setSupplier(user);
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("User isnt supplier");
                    }
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Error getting user for provider");
                }
            }

            if (userId != null) {
                try{
                    UserCwk user = userRepo.findUserCwkById(userId);

                    res.setUser(user);
                } catch (Exception e){
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Error getting user");
                }
            }

            BeanUtils.copyProperties(updatedFields, res, getNullPropertyNames(updatedFields));

            tenderRepo.save(res);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt update tender");
        }
    }

    public ResponseEntity<?> deleteById(Long id) {
        try {
            TenderCwk res = tenderRepo.findTenderCwkById(id);

            if (res == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            }

            tenderRepo.delete(res);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt delete tender");
        }
    }

    public ResponseEntity<?> deleteByJWTandId(String auth, Long id) {
        try {
            String email = jwtService.getEmailFromToken(auth.substring(7));
            UserCwk user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            TenderCwk res = tenderRepo.findTenderCwkByUserAndId(user, id);

            if (res == null){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            tenderRepo.delete(res);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt delete tender");
        }
    }

    public ResponseEntity<?> addTenderForIdByAdmin(Long id, Long tenderId) {
        try {
            UserCwk user = userRepo.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

            if(tenderRepo.existsById(tenderId)){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Tender already exist");
            }

            ParsedTenderDTO parsed = parsingService.getTenderById(tenderId);

            if (parsed == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found tender by id: " + id);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            DateTimeFormatter formatterWithoutTime = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            TenderCwk newtender = new TenderCwk();
            newtender.setUser(user);
            newtender.setId(id);
            newtender.setProzorroNumber(parsed.getProzorroNumber());
            newtender.setProcedureType(parsed.getProcedureType());
            newtender.setOrganizerName(parsed.getOrganizer().getName());
            newtender.setTitle(parsed.getTitle());
            newtender.setCategoryId(parsed.getCategory().getId());
            newtender.setCategoryCode(parsed.getCategory().getCode());
            newtender.setCategoryTitle(parsed.getCategory().getTitle());
            newtender.setStatusTitle(parsed.getStatusTitle());
            newtender.setBudgetAmount(parsed.getBudget().getAmount());
            newtender.setBudgetAmountTitle(parsed.getBudget().getAmountTitle());
            newtender.setWithVat(parsed.getBudget().isWithVat());
            newtender.setVatTitle(parsed.getBudget().getVatTitle());
            newtender.setCurrencyTitle(parsed.getBudget().getCurrencyTitle());
            newtender.setCurrencyHtmlTitle(parsed.getBudget().getCurrencyHtmlTitle());
            newtender.setCurrencyId(parsed.getBudget().getCurrencyId());
            newtender.setParticipantionCost(parsed.getParticipationCost());
            newtender.setDeliveryAddress(parsed.getNomenclaturesList().getFirst().getDeliveryAdress());

            try{
                newtender.setPaymentTermsDay(parsed.getPaymentTerms().getFirst().getDays());
            } catch (Exception _) {}
            try{
                newtender.setGuaranteeBank(parsed.getGuarantee().getAmountTitle());
            } catch (Exception _) {}
            try{
                newtender.setEnquiryPeriodStart(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodStart(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setEnquiryPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodEnd(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setTenderingPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getTenderingPeriodEnd(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setAuctionStart(LocalDateTime.parse(parsed.getImportantDates().getAuctionStart(), formatter));
            } catch (Exception _) {}
            try{
                newtender.setDeliveryPeriodTo(LocalDate.parse(parsed.getNomenclaturesList().getFirst().getDeliveryPeriodTo() ,formatterWithoutTime));
            } catch (Exception _) {}

            tenderRepo.save(newtender);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Couldnt add tender");
        }
    }

    public ResponseEntity<?> getUnits() {
        try{
            return ResponseEntity.ok(tenderRepo.findAllDistinctUnits());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Couldnt add tender");

        }
    }
}
