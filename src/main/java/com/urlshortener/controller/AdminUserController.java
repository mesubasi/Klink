package com.urlshortener.controller;

import com.urlshortener.dto.UserDto;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin Kullanıcı Yönetimi", description = "Sistemdeki tüm kayıtlı kullanıcıları listeleme, rol değiştirme ve silme servisleri")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Tüm Kullanıcıları Listele (Admin)", description = "Sistemde kayıtlı tüm kullanıcıları döndürür.")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{userId}/role")
    @Transactional
    @Operation(summary = "Kullanıcı Rolünü Değiştir (Admin)", description = "Kullanıcının rolünü ROLE_ADMIN veya ROLE_USER olarak günceller.")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable UUID userId,
            @RequestParam String role,
            Authentication authentication) {

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + userId));

        String targetRole = role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();

        // Eğer son admin'in rolü düşürülmeye çalışılıyorsa engelle
        if (user.getRole().equals("ROLE_ADMIN") && !targetRole.equals("ROLE_ADMIN")) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Sistemdeki son yöneticinin (Admin) rolü düşürülemez!");
            }
        }

        user.setRole(targetRole);
        userRepository.save(user);

        return ResponseEntity.ok(toDto(user));
    }

    @DeleteMapping("/{userId}")
    @Transactional
    @Operation(summary = "Kullanıcıyı Sil (Admin)", description = "Belirtilen kullanıcıyı sistemden kalıcı olarak siler.")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            Authentication authentication) {

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı: " + userId));

        if (authentication != null && authentication.getName().equals(user.getUsername())) {
            throw new IllegalArgumentException("Kendi hesabınızı silemezsiniz!");
        }

        if ("ROLE_ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Sistemdeki son yönetici silinemez!");
            }
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    private UserDto toDto(UserAccount user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
