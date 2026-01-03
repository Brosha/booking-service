package org.example.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.booking.dto.AuthRequest;
import org.example.booking.entity.User;
import org.example.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody AuthRequest request,
                                           @RequestParam(defaultValue = "USER") String role) {
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (payload.containsKey("password")) {
            user.setPasswordHash(passwordEncoder.encode(payload.get("password")));
        }
        if (payload.containsKey("role")) {
            user.setRole(payload.get("role"));
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

