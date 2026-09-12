package com.tmt.ecommerce.common;

import com.tmt.ecommerce.common.dto.ApiResponse;
import com.tmt.ecommerce.common.exception.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ErrorResponseTest {
    private MockMvc mvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new Probe()).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private JsonNode assertError(MvcResult result, int status, String code) throws Exception {
        assertEquals(status, result.getResponse().getStatus());
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertEquals(status, body.get("status").asInt());
        assertFalse(body.get("success").asBoolean());
        assertEquals(code, body.get("errorCode").asText());
        assertTrue(body.has("data"));
        assertTrue(body.get("data").isNull());
        return body;
    }

    @Test void domainNotFoundKeepsCustomMessage() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/probe/missing")).andReturn(), 404, "SHOP_NOT_FOUND");
        assertEquals("Custom message", body.get("message").asText());
    }
    @Test void defaultDomainMessageComesFromCode() {
        var response = new GlobalExceptionHandler().handleAppException(new AppException(ErrorCode.SHOP_NOT_FOUND));
        assertEquals(404, response.getStatusCode().value());
        assertEquals(ErrorCode.SHOP_NOT_FOUND.getDefaultMessage(), response.getBody().getMessage());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
    }
    @Test void successIsDerivedForExistingBuilders() throws Exception {
        MvcResult result = mvc.perform(get("/probe/ok")).andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertTrue(body.get("success").asBoolean());
        assertEquals("ok", body.get("data").asText());
        assertFalse(body.has("errorCode"));
    }
    @Test void validationUsesFieldErrorsAndNullData() throws Exception {
        JsonNode body = assertError(mvc.perform(post("/probe/input").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}")).andReturn(), 400, "INVALID_INPUT");
        assertTrue(body.get("fieldErrors").has("name"));
    }
    @Test void objectLevelValidationDoesNotCastToFieldError() throws Exception {
        var binding = new BeanPropertyBindingResult(new Input(""), "input");
        binding.addError(new ObjectError("input", "Invalid combination"));
        var parameter = new MethodParameter(Probe.class.getDeclaredMethod("input", Input.class), 0);
        var response = new GlobalExceptionHandler().handleValidationExceptions(new MethodArgumentNotValidException(parameter, binding));
        assertEquals("Invalid combination", response.getBody().getFieldErrors().get("_global"));
        assertNull(response.getBody().getData());
    }
    @Test void malformedJsonIsBadRequest() throws Exception {
        assertError(mvc.perform(post("/probe/input").contentType(MediaType.APPLICATION_JSON).content("{")).andReturn(), 400, "INVALID_INPUT");
    }
    @Test void wrongParameterIsBadRequest() throws Exception {
        assertError(mvc.perform(get("/probe/number").param("count", "not-a-number")).andReturn(), 400, "INVALID_INPUT");
    }
    @Test void missingParameterIsBadRequest() throws Exception {
        assertError(mvc.perform(get("/probe/number")).andReturn(), 400, "INVALID_INPUT");
    }
    @Test void wrongMethodPreservesAllowHeader() throws Exception {
        var result = mvc.perform(post("/probe/ok")).andReturn();
        assertError(result, 405, "METHOD_NOT_ALLOWED");
        assertNotNull(result.getResponse().getHeader("Allow"));
    }
    @Test void unsupportedMediaIsNotServerError() throws Exception {
        assertError(mvc.perform(post("/probe/input").contentType(MediaType.TEXT_PLAIN).content("hello")).andReturn(), 415, "UNSUPPORTED_MEDIA_TYPE");
    }
    @Test void unexpectedFailureDoesNotLeakDetails() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/probe/crash")).andReturn(), 500, "INTERNAL_SERVER_ERROR");
        assertFalse(body.toString().contains("private-database-detail"));
    }
    @Test void methodAccessDeniedHasEnvelope() throws Exception {
        assertError(mvc.perform(get("/probe/denied")).andReturn(), 403, "ACCESS_DENIED");
    }
    @Test void missingRouteHasEnvelope() throws Exception {
        assertError(mvc.perform(get("/absent-route")).andReturn(), 404, "NOT_FOUND");
    }

    record Input(@NotBlank String name) {}
    @RestController
    @org.springframework.context.annotation.Profile("standalone-error-probe")
    static class Probe {
        @GetMapping("/probe/missing") Object missing() { throw new AppException(ErrorCode.SHOP_NOT_FOUND, "Custom message"); }
        @GetMapping("/probe/ok") ApiResponse<String> ok() { return ApiResponse.<String>builder().status(200).data("ok").build(); }
        @PostMapping("/probe/input") Object input(@Valid @RequestBody Input input) { return input; }
        @GetMapping("/probe/number") int number(@RequestParam int count) { return count; }
        @GetMapping("/probe/crash") Object crash() { throw new RuntimeException("private-database-detail"); }
        @GetMapping("/probe/denied") Object denied() { throw new AccessDeniedException("private details"); }
    }
}
