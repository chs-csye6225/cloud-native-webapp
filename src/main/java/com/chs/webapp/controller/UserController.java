package com.chs.webapp.controller;

import com.chs.webapp.dto.UserCreateRequest;
import com.chs.webapp.dto.UserResponse;
import com.chs.webapp.dto.UserUpdateRequest;
import com.chs.webapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Create user with email: {}", request.getEmail());

        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id, Authentication authentication) {
        log.info("Getting user info for ID: {}", id);

        String authenticatedEmail = authentication.getName();
        UserResponse userResponse = userService.getUserById(id);

        if (!userResponse.getEmail().equals(authenticatedEmail)) {
            throw new IllegalArgumentException("Access denied: Users can only view their own information");
        }
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request, Authentication authentication) {
        log.info("Updating user with ID: {}", id);

        String authenticatedEmail = authentication.getName();
        UserResponse userResponse = userService.updateUser(id, request, authenticatedEmail);
        return ResponseEntity.ok(userResponse);
    }
}
