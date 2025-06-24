package com.tender_service.core.schedulingtasks;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;


@Component
public class ScheduledTasks {

    @Autowired
    private ParsingService parsingService;
    @Autowired
    private TenderRepository tenderRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void updateTender() throws InterruptedException {
        List<TenderCwk> tenders = tenderRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (TenderCwk tender : tenders) {
            boolean updated = false;

            if (!Objects.equals(tender.getStatusTitle(), "Пропозиції розглянуті")
                    || tender.getAuctionStart() == null
                    || tender.getQualificationDate() == null
                    || tender.getIdDeal() == null
                    || tender.getDateDeal() == null
                    || tender.getAmountDeal() == null
                    || tender.getUrlDeal() == null) {
                ParsedTenderDTO parsed = parsingService.getTenderById(tender.getId());

                if (!Objects.equals(tender.getStatusTitle(), parsed.getStatusTitle())) {
                    tender.setStatusTitle(parsed.getStatusTitle());
                    updated = true;
                }

                if (tender.getAuctionStart() == null && parsed.getImportantDates().getAuctionStart() != null) {
                    tender.setAuctionStart(LocalDateTime.parse(parsed.getImportantDates().getAuctionStart(), formatter));
                    updated = true;
                }

                if (tender.getQualificationDate() == null
                        && parsed.getAwards() != null
                        && !parsed.getAwards().isEmpty()
                        && parsed.getAwards().getFirst().getComplaintPeriodStart() != null) {
                    tender.setQualificationDate(parsed.getAwards().getFirst().getComplaintPeriodStart().plusDays(4));
                    updated = true;
                }

                boolean shouldUpdateDealFields = tender.getIdDeal() == null
                        || tender.getDateDeal() == null
                        || tender.getAmountDeal() == null
                        || tender.getUrlDeal() == null;

                if (shouldUpdateDealFields
                        && parsed.getParticipantContracts() != null
                        && !parsed.getParticipantContracts().isEmpty()) {

                    var participantContract = parsed.getParticipantContracts().getFirst();

                    if (participantContract.getParticipantTitle().contains(tender.getParticipant().getName())
                            && participantContract.getContracts() != null
                            && !participantContract.getContracts().isEmpty()) {

                        var contract = participantContract.getContracts().getFirst();
                        if (contract.getStatus() != null && "Підписаний".equals(contract.getStatus().getTitle())
                                && contract.getDocuments() != null && !contract.getDocuments().isEmpty()) {
                            try {
                                if (tender.getIdDeal() == null) {
                                    tender.setIdDeal(String.valueOf(contract.getDocuments().getFirst().getId()));
                                    updated = true;
                                }
                            } catch (Exception ignored) {
                            }

                            try {
                                if (tender.getDateDeal() == null) {
                                    tender.setDateDeal(LocalDateTime.parse(contract.getDocuments().getFirst().getDateModified(), formatter));
                                    updated = true;
                                }
                            } catch (Exception ignored) {
                            }

                            if (tender.getAmountDeal() == null) {
                                tender.setAmountDeal(contract.getAmount());
                                updated = true;
                            }

                            try {
                                if (tender.getUrlDeal() == null) {
                                    tender.setUrlDeal(contract.getDocuments().getFirst().getViewUrl());
                                    updated = true;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }

                if (updated) {
                    tenderRepository.save(tender);
                    Thread.sleep(100);
                }
            }
        }
    }
}