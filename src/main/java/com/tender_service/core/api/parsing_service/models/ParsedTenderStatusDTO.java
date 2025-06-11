package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ParsedTenderStatusDTO {

    @JsonProperty("StatusTitle")
    private String StatusTitle;

    @JsonProperty("ImportantDates")
    private ImportantDates importantDates;

    public String getStatusTitle() {
        return StatusTitle;
    }

    public void setStatusTitle(String statusTitle) {
        StatusTitle = statusTitle;
    }

    public ImportantDates getImportantDates() {
        return importantDates;
    }

    public void setImportantDates(ImportantDates importantDates) {
        this.importantDates = importantDates;
    }
}
