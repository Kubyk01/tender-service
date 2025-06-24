package com.tender_service.feature.tender.service;

import com.tender_service.core.api.database.entity.*;
import com.tender_service.core.api.database.repository.ParticipantRepository;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.api.file_service.FileManagerService;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.tender.model.TenderDTO;
import com.tender_service.utils.TenderCwkSpecification;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.tender_service.utils.BeanCopyUtils.getNullPropertyNames;

@Service
@Transactional
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
    @Autowired
    private ParticipantRepository participantRepo;
    private final ModelMapper modelMapper;
    @Autowired
    private FileManagerService fileManagerService;

    public TenderCwk getTenderById(String auth, Long id) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (tenderRepo.existsById(id)) {
            TenderCwk res = tenderRepo.findByIdAndUserInAnyRole(id, user);
            if (res == null) {
                throw new AccessDeniedException("Access denied");
            }
            return res;
        }

        if (!user.getRoles().contains(Role.USER)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found");
        }

        ParsedTenderDTO parsed = parsingService.getTenderById(id);
        if (parsed == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found tender by id: " + id);
        }

        return save(user, id, parsed);
    }

    public List<TenderDTO> getTenderForJWT(String auth, Integer pageNumber, Integer pageSize, String sortBy,
                                           String sortDirection, String role, Map<String, String> allParams) throws ParseException {
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
            default: {
                throw new IllegalArgumentException("Not found role");
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

        return tenderRepo.findAll(spec, pageable).getContent().stream()
                .map(e -> modelMapper.map(e, TenderDTO.class))
                .toList();
    }

    public List<Map<String, Object>> getAllTenders(Integer pageNumber, Integer pageSize, String sortBy,
                                                   String sortDirection, Map<String, String> allParams) {
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

            tenderUserPair.put("tender", modelMapper.map(tender, TenderDTO.class));
            responseList.add(tenderUserPair);
        }

        return responseList;
    }

    public TenderCwk updateByIdAndJWT(String auth, TenderCwk updatedFields) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        TenderCwk res = tenderRepo.findByIdAndUserInAnyRole(updatedFields.getId(), user);

        if (res == null) {
            throw new AccessDeniedException("Access denied");
        }

        if (updatedFields.getItemsAndParticipants() != null) {
            res.getItemsAndParticipants().clear();

            for (ItemsAndParticipants newItem : updatedFields.getItemsAndParticipants()) {
                newItem.setTender(res);
                res.getItemsAndParticipants().add(newItem);
            }
        }

        updatedFields.setItemsAndParticipants(null);

        BeanUtils.copyProperties(updatedFields, res, getNullPropertyNames(updatedFields));

        return tenderRepo.save(res);
    }

    public TenderCwk updateById(TenderCwk updatedFields, Long tendererId, Long supplierId, Long participantId, Long userId) {
        TenderCwk res = tenderRepo.findTenderCwkById(updatedFields.getId());

        if (res == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        updatedFields.setFiles(null);

        if (tendererId != null) {
            UserCwk user = userRepo.findUserCwkById(tendererId);
            if (user.getRoles().contains(Role.TENDERER)) {
                res.setTenderer(user);
            } else {
                throw new IllegalArgumentException("User isn't tenderer");
            }
        }

        if (supplierId != null) {
            UserCwk user = userRepo.findUserCwkById(supplierId);
            if (user.getRoles().contains(Role.SUPPLIER)) {
                res.setSupplier(user);
            } else {
                throw new IllegalArgumentException("User isn't supplier");
            }
        }

        if (userId != null) {
            UserCwk user = userRepo.findUserCwkById(userId);
            res.setUser(user);
        }

        if (participantId != null) {
            Participant participant = participantRepo.getParticipantById(participantId);
            res.setParticipant(participant);
        }

        if (updatedFields.getItemsAndParticipants() != null) {
            res.getItemsAndParticipants().clear();

            for (ItemsAndParticipants newItem : updatedFields.getItemsAndParticipants()) {
                newItem.setTender(res);
                res.getItemsAndParticipants().add(newItem);
            }
        }

        updatedFields.setItemsAndParticipants(null);

        BeanUtils.copyProperties(updatedFields, res, getNullPropertyNames(updatedFields));

        return tenderRepo.save(res);
    }

    public void deleteById(Long id) {
        TenderCwk res = tenderRepo.findTenderCwkById(id);

        if (res == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }

        fileManagerService.deleteFolderWithFiles(id);
        tenderRepo.delete(res);
    }

    public void deleteByJWTandId(String auth, Long id) throws ParseException {
        String email = jwtService.getEmailFromToken(auth.substring(7));
        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        TenderCwk res = tenderRepo.findTenderCwkByUserAndId(user, id);

        if (res == null) {
            throw new AccessDeniedException("Access denied");
        }

        fileManagerService.deleteFolderWithFiles(id);
        tenderRepo.delete(res);
    }

    public void addTenderForIdByAdmin(Long id, Long tenderId) {
        UserCwk user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (tenderRepo.existsById(tenderId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tender already exists");
        }

        ParsedTenderDTO parsed = parsingService.getTenderById(tenderId);
        if (parsed == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found tender by id: " + tenderId);
        }

        save(user, tenderId, parsed);
    }

    public List<String> getUnits() {
        return tenderRepo.findAllDistinctUnits();
    }

    public List<Participant> getParticipants() {
        return participantRepo.findAll();
    }

    public TenderCwk save(UserCwk user, Long id, ParsedTenderDTO parsed) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        DateTimeFormatter formatterWithoutTime = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        TenderCwk newtender = new TenderCwk();
        newtender.setUser(user);
        newtender.setId(id);

        if (parsed != null) {
            newtender.setProzorroNumber(parsed.getProzorroNumber());
            newtender.setProcedureType(parsed.getProcedureType());

            if (parsed.getOrganizer() != null) {
                newtender.setOrganizerName(parsed.getOrganizer().getName());
                newtender.setOrganizerUsreou(parsed.getOrganizer().getUsreou());
                newtender.setOrganizerAddress(parsed.getOrganizer().getAddress());

                if (parsed.getOrganizer().getContactPerson() != null) {
                    newtender.setContactPersonEmail(parsed.getOrganizer().getContactPerson().getEmail());
                    newtender.setContactPersonPhone(parsed.getOrganizer().getContactPerson().getPhone());
                    newtender.setContactPersonName(parsed.getOrganizer().getContactPerson().getName());
                }
            }

            newtender.setTitle(parsed.getTitle());

            if (parsed.getCategory() != null) {
                newtender.setCategoryId(parsed.getCategory().getId());
                newtender.setCategoryCode(parsed.getCategory().getCode());
                newtender.setCategoryTitle(parsed.getCategory().getTitle());
            }

            newtender.setStatusTitle(parsed.getStatusTitle());

            if (parsed.getBudget() != null) {
                newtender.setBudgetAmount(parsed.getBudget().getAmount());
                newtender.setBudgetAmountTitle(parsed.getBudget().getAmountTitle());
                newtender.setWithVat(parsed.getBudget().isWithVat());
                newtender.setVatTitle(parsed.getBudget().getVatTitle());
                newtender.setCurrencyTitle(parsed.getBudget().getCurrencyTitle());
                newtender.setCurrencyHtmlTitle(parsed.getBudget().getCurrencyHtmlTitle());
                newtender.setCurrencyId(parsed.getBudget().getCurrencyId());
            }

            newtender.setParticipantCost(parsed.getParticipationCost());

            if (parsed.getNomenclaturesList() != null && !parsed.getNomenclaturesList().isEmpty()) {
                newtender.setDeliveryAddress(parsed.getNomenclaturesList().getFirst().getDeliveryAdress());

                try {
                    newtender.setDeliveryPeriodTo(LocalDate.parse(
                            parsed.getNomenclaturesList().getFirst().getDeliveryPeriodTo(), formatterWithoutTime));
                } catch (Exception ignored) {}
            }

            if (parsed.getAwards() != null && !parsed.getAwards().isEmpty() &&
                    parsed.getAwards().getFirst().getComplaintPeriodStart() != null) {
                try {
                    newtender.setQualificationDate(
                            parsed.getAwards().getFirst().getComplaintPeriodStart().plusDays(4));
                } catch (Exception ignored) {}
            }

            if (parsed.getParticipantContracts() != null && !parsed.getParticipantContracts().isEmpty()) {
                var contract = parsed.getParticipantContracts().getFirst().getContracts();
                if (contract != null && !contract.isEmpty()) {
                    var document = contract.getFirst().getDocuments();
                    if (document != null && !document.isEmpty()) {
                        try {
                            newtender.setIdDeal(String.valueOf(document.getFirst().getId()));
                            newtender.setDateDeal(LocalDateTime.parse(document.getFirst().getDateModified(), formatter));
                            newtender.setUrlDeal(document.getFirst().getViewUrl());
                        } catch (Exception ignored) {}
                    }
                    newtender.setAmountDeal(contract.getFirst().getAmount());
                }
            }

            if (parsed.getNomenclaturesList() != null) {
                List<ItemCwk> items = parsed.getNomenclaturesList().stream()
                        .map(n -> {
                            ItemCwk item = new ItemCwk();
                            item.setTender(newtender);
                            item.setCount(n.getCount());
                            item.setTitle(n.getTitle());
                            return item;
                        }).collect(Collectors.toList());

                newtender.setItems(items);
            }

            if (parsed.getPaymentTerms() != null && !parsed.getPaymentTerms().isEmpty()) {
                newtender.setPaymentTermsDay(parsed.getPaymentTerms().getFirst().getDays());
            }

            if (parsed.getGuarantee() != null) {
                try {
                    newtender.setGuaranteeBank(parsed.getGuarantee().isAmountTitle());
                } catch (Exception ignored) {}
            }

            if (parsed.getImportantDates() != null) {
                try {
                    if (parsed.getImportantDates().getEnquiryPeriodStart() != null)
                        newtender.setEnquiryPeriodStart(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodStart(), formatter));
                    if (parsed.getImportantDates().getEnquiryPeriodEnd() != null)
                        newtender.setEnquiryPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getEnquiryPeriodEnd(), formatter));
                    if (parsed.getImportantDates().getTenderingPeriodEnd() != null)
                        newtender.setTenderingPeriodEnd(LocalDateTime.parse(parsed.getImportantDates().getTenderingPeriodEnd(), formatter));
                    if (parsed.getImportantDates().getAuctionStart() != null)
                        newtender.setAuctionStart(LocalDateTime.parse(parsed.getImportantDates().getAuctionStart(), formatter));
                } catch (Exception ignored) {}
            }
        }

        return tenderRepo.save(newtender);
    }
}