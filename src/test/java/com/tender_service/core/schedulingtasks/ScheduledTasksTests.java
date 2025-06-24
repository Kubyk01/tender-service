package com.tender_service.core.schedulingtasks;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.repository.TenderRepository;
import com.tender_service.core.api.parsing_service.ParsingService;
import com.tender_service.core.api.parsing_service.models.ImportantDates;
import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    public void testUpdateTender_updatesIfMissing() throws InterruptedException {
        TenderCwk tender = new TenderCwk();
        tender.setId(1L);
        tender.setStatusTitle("Очікує розгляду");
        tender.setAuctionStart(null);

        ParsedTenderDTO parsedDTO = new ParsedTenderDTO();
        parsedDTO.setStatusTitle("Завершено");

        ImportantDates datesDTO = new ImportantDates();
        datesDTO.setAuctionStart("01.01.2025 14:00");
        parsedDTO.setImportantDates(datesDTO);

        when(tenderRepository.findAll()).thenReturn(List.of(tender));
        when(parsingService.getTenderById(1L)).thenReturn(parsedDTO);

        scheduledTasks.updateTender();

        verify(parsingService, times(1)).getTenderById(1L);
        verify(tenderRepository, times(1)).save(tender);

        assert tender.getStatusTitle().equals("Завершено");
        assert tender.getAuctionStart().equals(LocalDateTime.parse("01.01.2025 14:00", formatter));
    }

    @Test
    public void testUpdateTenderIfStillNull() throws InterruptedException {
        TenderCwk tender = new TenderCwk();
        tender.setId(3L);
        tender.setStatusTitle("Пропозиції розглянуті");
        tender.setAuctionStart(null);

        ParsedTenderDTO parsedDTO = new ParsedTenderDTO();
        ImportantDates datesDTO = new ImportantDates();
        datesDTO.setAuctionStart("05.05.2025 12:30");
        parsedDTO.setImportantDates(datesDTO);

        when(tenderRepository.findAll()).thenReturn(List.of(tender));
        when(parsingService.getTenderById(3L)).thenReturn(parsedDTO);

        scheduledTasks.updateTender();

        verify(parsingService, times(1)).getTenderById(3L);
        verify(tenderRepository, times(1)).save(tender);

        assert tender.getAuctionStart().equals(LocalDateTime.parse("05.05.2025 12:30", formatter));
    }

    @Test
    public void testNoUpdateWhenAllFieldsPresent() throws InterruptedException {
        TenderCwk tender = new TenderCwk();
        tender.setId(6L);
        tender.setStatusTitle("Пропозиції розглянуті");
        tender.setAuctionStart(LocalDateTime.now());
        tender.setQualificationDate(LocalDateTime.now());
        tender.setIdDeal("456");
        tender.setDateDeal(LocalDateTime.now());
        tender.setAmountDeal(20000L);
        tender.setUrlDeal("http://existing-contract");

        when(tenderRepository.findAll()).thenReturn(List.of(tender));

        scheduledTasks.updateTender();

        verify(parsingService, never()).getTenderById(anyLong());
        verify(tenderRepository, never()).save(any());
    }

}
