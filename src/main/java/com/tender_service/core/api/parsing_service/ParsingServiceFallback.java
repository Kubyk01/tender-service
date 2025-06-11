package com.tender_service.core.api.parsing_service;

import org.springframework.cloud.openfeign.FallbackFactory;

public class ParsingServiceFallback implements FallbackFactory<ParsingServiceFeignClient> {

    @Override
    public ParsingServiceFeignClient create(Throwable cause) {
        throw new ParsingServiceException("Error while parsing tender" ,cause);
    }
}
