package com.tender_service.core.api.parsing_service;

import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParsingService {

    @Autowired
    private ParsingServiceFeignClient parsingServiceFeignClient;

    public ParsedTenderDTO getTenderById(Long Id){
        return parsingServiceFeignClient.getTenderById(Id);
    }
}
