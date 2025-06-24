package com.tender_service.core.api.parsing_service;

import com.tender_service.core.api.parsing_service.models.ParsedTenderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tenderClient", url = "${tender.api.url}", fallbackFactory = ParsingServiceFallback.class)
public interface ParsingServiceFeignClient {

    @GetMapping("/uk/PurchaseDetail/GetTenderModel/")
    ParsedTenderDTO getTenderById(@RequestParam("tenderId") long tenderId);
}
