package com.tender_service.core.schedulingtasks;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ImportantDates;
import com.tender_service.core.api.parsing_service.models.ParsedTenderStatusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class ScheduledTasksTests {

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    @Mock
    private ParsingService parsingService;

    @Mock
    private TenderRepository tenderRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateTenderStatusAndAuctionStart_updatesStatusAndSetsAuctionStartIfMissing() throws InterruptedException {
        TenderCwk tender = new TenderCwk();
        tender.setId(1L);
        tender.setStatusTitle("Очікує розгляду");
        tender.setAuctionStart(null);

        ParsedTenderStatusDTO parsedDTO = new ParsedTenderStatusDTO();
        parsedDTO.setStatusTitle("Завершено");

        ImportantDates datesDTO = new ImportantDates();
        datesDTO.setAuctionStart("01.01.2025 14:00");
        parsedDTO.setImportantDates(datesDTO);

        when(tenderRepository.findAll()).thenReturn(List.of(tender));
        when(parsingService.getStatusTitleById(1L)).thenReturn(parsedDTO);

        scheduledTasks.updateTenderStatusAndAuctionStart();

        verify(parsingService, times(1)).getStatusTitleById(1L);
        verify(tenderRepository, times(1)).save(tender);

        assert tender.getStatusTitle().equals("Завершено");
        assert tender.getAuctionStart().equals(LocalDateTime.parse("01.01.2025 14:00", formatter));
    }

    @Test
    public void testUpdateTenderStatusAndAuctionStart_fetchesAuctionStartIfStillNull() throws InterruptedException {
        TenderCwk tender = new TenderCwk();
        tender.setId(3L);
        tender.setStatusTitle("Пропозиції розглянуті");
        tender.setAuctionStart(null); // Should still trigger auctionStart update

        ParsedTenderStatusDTO parsedDTO = new ParsedTenderStatusDTO();
        ImportantDates datesDTO = new ImportantDates();
        datesDTO.setAuctionStart("05.05.2025 12:30");
        parsedDTO.setImportantDates(datesDTO);

        when(tenderRepository.findAll()).thenReturn(List.of(tender));
        when(parsingService.getStatusTitleById(3L)).thenReturn(parsedDTO);

        scheduledTasks.updateTenderStatusAndAuctionStart();

        verify(parsingService, times(1)).getStatusTitleById(3L);
        verify(tenderRepository, times(1)).save(tender);

        assert tender.getAuctionStart().equals(LocalDateTime.parse("05.05.2025 12:30", formatter));
    }
}
