package com.tmt.ecommerce.identity.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProfileUpdateRequest {
    @NotBlank @Size(max = 100)
    private String fullName;
    @Size(max = 20)
    private String phone;

    @JsonSetter("fullName")
    public void setFullName(Object value) { fullName = text(value); }
    @JsonSetter("phone")
    public void setPhone(Object value) {
        phone = text(value);
        if (phone != null && phone.isEmpty()) phone = null;
    }
    @JsonAnySetter
    public void rejectUnknown(String name, Object value) {
        throw new IllegalArgumentException("Unknown profile field");
    }
    private static String text(Object value) {
        if (value == null) return null;
        if (!(value instanceof String text)) throw new IllegalArgumentException("Profile fields must be strings");
        return text.trim();
    }
}
