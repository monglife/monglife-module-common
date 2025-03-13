package com.monglife.module.common.feign.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monglife.module.common.feign.exception.FeignClientException;
import com.monglife.core.dto.response.ResponseDto;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.util.Map;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        ResponseDto<Map<String, Object>> responseDto;

        try {

            responseDto = objectMapper.readValue(response.body().asInputStream(), new TypeReference<>() {});

            throw new FeignClientException(responseDto);

        } catch (IOException exception) {

            throw new RuntimeException(exception);
        }
    }
}