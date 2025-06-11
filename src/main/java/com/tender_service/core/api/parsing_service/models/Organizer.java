package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Organizer {

    @JsonProperty("Id")
    private Long Id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Usreou")
    private String usreou;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsreou() {
        return usreou;
    }

    public void setUsreou(String usreou) {
        this.usreou = usreou;
    }
}
