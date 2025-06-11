package com.tender_service.core.schedulingtasks;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ParsedTenderStatusDTO;
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

    @Scheduled(cron = "0 0 2 * * *") // At 2:00 AM every day
    public void updateTenderStatusAndAuctionStart() throws InterruptedException {
        List<TenderCwk> tenders = tenderRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (TenderCwk tender : tenders) {
            boolean updated = false;

            if (!Objects.equals(tender.getStatusTitle(), "Пропозиції розглянуті") || tender.getAuctionStart() == null) {
                ParsedTenderStatusDTO parsed = parsingService.getStatusTitleById(tender.getId());

                if (!Objects.equals(tender.getStatusTitle(), parsed.getStatusTitle())) {
                    tender.setStatusTitle(parsed.getStatusTitle());
                    updated = true;
                }

                if (tender.getAuctionStart() == null && parsed.getImportantDates().getAuctionStart() != null) {
                    tender.setAuctionStart(LocalDateTime.parse(parsed.getImportantDates().getAuctionStart(), formatter));
                    updated = true;
                }

                if (updated) {
                    tenderRepository.save(tender);
                    Thread.sleep(100); // Rate-limiting API calls
                }
            }
        }
    }
}
