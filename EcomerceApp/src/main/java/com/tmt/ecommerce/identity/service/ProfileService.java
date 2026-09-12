package com.tmt.ecommerce.identity.service;

import com.tmt.ecommerce.common.exception.*;
import com.tmt.ecommerce.common.security.CurrentUserPrincipal;
import com.tmt.ecommerce.identity.dto.*;
import com.tmt.ecommerce.identity.repository.UserRepository;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileService {
    private final UserRepository users;
    private final Validator validator;

    private Long currentId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CurrentUserPrincipal user) || user.getId() == null)
            throw new AccessDeniedException("Missing user principal");
        return user.getId();
    }
    @Transactional(readOnly = true)
    public ProfileResponse get() {
        return users.findProfileById(currentId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    @Transactional
    public ProfileResponse update(ProfileUpdateRequest request) {
        Long id = currentId();
        if (request == null) throw new AppException(ErrorCode.INVALID_INPUT);
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        int changed = users.updateProfile(id, request.getFullName(), request.getPhone());
        if (changed == 0) throw new AppException(ErrorCode.USER_NOT_FOUND);
        if (changed != 1) throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);

        return users.findProfileById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
