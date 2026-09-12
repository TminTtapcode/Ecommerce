package com.tmt.ecommerce.common.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtErrorResponseTest {
    private final JsonMapper mapper = JsonMapper.builder().build();
    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void infrastructureFailureIs500AndDoesNotContinueChain() throws Exception {
        JwtService jwt = mock(JwtService.class);
        UserDetailsService users = mock(UserDetailsService.class);
        when(jwt.extractUsername("test-token")).thenReturn("user@example.test");
        when(users.loadUserByUsername("user@example.test")).thenThrow(new org.springframework.dao.DataAccessResourceFailureException("private SQL details"));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");
        var response = new MockHttpServletResponse();
        var chain = mock(FilterChain.class);
        new JwtAuthenticationFilter(jwt, users, new RestSecurityExceptionHandler(mapper)).doFilter(request, response, chain);
        assertEquals(500, response.getStatus());
        var body = mapper.readTree(response.getContentAsString());
        assertEquals("INTERNAL_SERVER_ERROR", body.get("errorCode").asText());
        assertTrue(body.get("data").isNull());
        assertFalse(body.get("success").asBoolean());
        assertFalse(body.toString().contains("SQL"));
        verifyNoInteractions(chain);
    }

    @Test void accessDeniedHandlerWrites403Envelope() throws Exception {
        var response = new MockHttpServletResponse();
        new RestSecurityExceptionHandler(mapper).handle(new MockHttpServletRequest(), response, new AccessDeniedException("private"));
        assertEquals(403, response.getStatus());
        var body = mapper.readTree(response.getContentAsString());
        assertEquals("ACCESS_DENIED", body.get("errorCode").asText());
        assertTrue(body.get("data").isNull());
        assertFalse(body.toString().contains("private"));
    }
}
