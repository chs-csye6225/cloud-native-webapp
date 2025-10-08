package com.chs.webapp.service;

import com.chs.webapp.dto.UserCreateRequest;
import com.chs.webapp.dto.UserResponse;
import com.chs.webapp.dto.UserUpdateRequest;
import com.chs.webapp.entity.User;
import com.chs.webapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User savedUser = userRepository.saveAndFlush(user); // Timestamp 在資料庫 INSERT 操作時才執行，所以要 flush
        User refreshedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return mapToResponse(refreshedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request, String authenticatedEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (!user.getEmail().equals(authenticatedEmail)) {
            throw new IllegalArgumentException("Users can only update their own account information");
        }

        boolean updated = false;
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
            updated = true;
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
            updated = true;
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            updated = true;
        }
        if (!updated) {
            throw new IllegalArgumentException("No valid fields provided for update");
        }

        User savedUser = userRepository.saveAndFlush(user); // Timestamp 在資料庫 INSERT 操作時才執行，所以要 flush
        User refreshedUser = userRepository.findById(savedUser.getId()).orElse(savedUser);

        log.info("User updated successfully with ID: {}", savedUser.getId());
        return mapToResponse(refreshedUser);
    }


    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accountCreated(user.getAccountCreated())
                .accountUpdated(user.getAccountUpdated())
                .build();
    }
}
