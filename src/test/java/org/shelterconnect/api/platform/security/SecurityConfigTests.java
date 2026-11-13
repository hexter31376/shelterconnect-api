package org.shelterconnect.api.platform.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfigTests.ProbeController.class)
@Import({SecurityConfig.class, SecurityConfigTests.ProbeController.class})
class SecurityConfigTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void anonymousApiRequestRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/probe")).andExpect(status().isUnauthorized());
    }

    @Test
    void documentationIsProtectedByDefault() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void authenticatedReadIsAllowed() throws Exception {
        mockMvc.perform(get("/api/probe")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void writesRequireCsrfToken() throws Exception {
        mockMvc.perform(post("/api/probe")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/probe").with(csrf())).andExpect(status().isOk());
    }

    @RestController
    static class ProbeController {

        @GetMapping("/api/probe")
        String read() {
            return "ok";
        }

        @PostMapping("/api/probe")
        String write() {
            return "ok";
        }
    }
}
