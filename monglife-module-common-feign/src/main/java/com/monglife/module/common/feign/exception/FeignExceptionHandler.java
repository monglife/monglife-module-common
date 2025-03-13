//package com.monglife.common.module.feign.exception;
//
//import com.monglife.core.dto.response.ResponseDto;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.Map;
//
//@Slf4j
//@RestControllerAdvice
//public class FeignExceptionHandler {
//
//    /**
//     * exception bean
//     * @param e 예외 객체
//     * @return 에러 응답 객체
//     */
//    @ExceptionHandler(FeignClientException.class)
//    public ResponseEntity<ResponseDto<Map<String, Object>>> handleFeignClientException(FeignClientException e) {
//
//        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>(e.getCode(), e.getMessage(), e.getHttpStatus(), e.getResult());
//
//        return ResponseEntity
//                .status(e.getHttpStatus())
//                .body(responseDto);
//    }
//}
