package org.shelterconnect.api.platform.web;

import jakarta.validation.Valid;
import org.shelterconnect.api.platform.web.dto.ErrorResponses;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shelterconnect.api.platform.web.dto.CommonResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionAdviceTests {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController(), new DomainController())
                .setControllerAdvice(new GlobalExceptionAdvice(), new DomainAdvice()).build();
    }

    @Test
    void returnsSuccessPayload() throws Exception {
        mvc.perform(get("/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void mapsMissingRouteToNotFound() throws Exception {
        mvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("G006"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        mvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("G001"))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @Test
    void rejectsInvalidInput() throws Exception {
        mvc.perform(post("/input").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("G004"));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/input").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("G003"));
    }

    @Test
    void preservesMethodNotAllowedHeader() throws Exception {
        mvc.perform(post("/success"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "GET"))
                .andExpect(jsonPath("$.error.code").value("G005"));
    }

    @Test
    void preservesUnsupportedMediaTypeStatus() throws Exception {
        mvc.perform(post("/input").contentType(MediaType.TEXT_PLAIN).content("hello"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("HTTP_415"));
    }

    @Test
    void domainAdviceTakesPrecedenceOverGlobalAdvice() throws Exception {
        mvc.perform(get("/domain/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("G003"));
    }

    @Test
    void securityExceptionsAreRethrown() {
        GlobalExceptionAdvice advice = new GlobalExceptionAdvice();
        var denied = new AccessDeniedException("접근이 거부되었습니다.");
        var unauthenticated = new BadCredentialsException("인증 정보가 올바르지 않습니다.");
        assertSame(denied,
                assertThrows(Exception.class,
                        () -> advice.handleUnexpectedException(denied)));
        assertSame(unauthenticated,
                assertThrows(Exception.class,
                        () -> advice.handleUnexpectedException(unauthenticated)));
    }

    static class DomainException extends RuntimeException {
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RestControllerAdvice(assignableTypes = DomainController.class)
    static class DomainAdvice {
        @ExceptionHandler(DomainException.class)
        ResponseEntity<Object> missing() {
            return ErrorResponses.from(ErrorCode.BAD_REQUEST);
        }
    }

    @RestController
    static class DomainController {
        @GetMapping("/domain/missing")
        void missing() {
            throw new DomainException();
        }
    }

    @RestController
    static class ProbeController {
        @GetMapping("/success")
        CommonResponse<String> success() {
            return CommonResponse.success("ok");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("데이터베이스의 비공개 오류 정보");
        }

        @PostMapping("/input")
        CommonResponse<Input> input(@Valid @RequestBody Input input) {
            return CommonResponse.success(input);
        }
    }

    record Input(@NotBlank String name) {
    }
}
