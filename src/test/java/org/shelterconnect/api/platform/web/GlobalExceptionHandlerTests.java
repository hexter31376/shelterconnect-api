package org.shelterconnect.api.platform.web;

import jakarta.validation.Valid;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTests {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void returnsSuccessPayload() throws Exception {
        mvc.perform(get("/success"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void mapsBusinessCodeToHttpStatus() throws Exception {
        mvc.perform(get("/business"))
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

    @RestController
    static class ProbeController {
        @GetMapping("/success")
        CommonResponse<String> success() {
            return CommonResponse.success("ok");
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("private database details");
        }

        @PostMapping("/input")
        CommonResponse<Input> input(@Valid @RequestBody Input input) {
            return CommonResponse.success(input);
        }
    }

    record Input(@NotBlank String name) {
    }
}
