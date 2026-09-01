package com.urlshortener.service;

import com.urlshortener.dto.BioLinkItemDto;
import com.urlshortener.dto.BioPageDto;
import com.urlshortener.dto.BioPageUpdateRequest;
import com.urlshortener.model.BioLinkItem;
import com.urlshortener.model.BioPage;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.BioLinkItemRepository;
import com.urlshortener.repository.BioPageRepository;
import com.urlshortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BioPageService {

    private static final Logger log = LoggerFactory.getLogger(BioPageService.class);
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");

    private final BioPageRepository bioPageRepository;
    private final BioLinkItemRepository bioLinkItemRepository;
    private final UserRepository userRepository;

    public BioPageService(BioPageRepository bioPageRepository,
                          BioLinkItemRepository bioLinkItemRepository,
                          UserRepository userRepository) {
        this.bioPageRepository = bioPageRepository;
        this.bioLinkItemRepository = bioLinkItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BioPageDto getBioPageByUsername(String username, boolean recordView) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Kullanıcı adı boş olamaz.");
        }

        BioPage bioPage = bioPageRepository.findByUsernameIgnoreCase(username.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Bio sayfası bulunamadı: @" + username));

        if (recordView) {
            try {
                bioPageRepository.incrementViewCount(bioPage.getId());
            } catch (Exception e) {
                log.warn("Bio görüntülenme sayısı artırılamadı: {}", e.getMessage());
            }
        }

        return toDto(bioPage, false); // public view: only active links
    }

    @Transactional(readOnly = true)
    public BioPageDto getBioPageForCurrentUser() {
        UserAccount user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Giriş yapmış bir kullanıcı bulunamadı.");
        }

        BioPage bioPage = bioPageRepository.findByUserId(user.getId()).orElse(null);
        if (bioPage == null) {
            // Bio sayfası henüz oluşturulmamışsa draft bir yapı döndür
            return BioPageDto.builder()
                    .username(user.getUsername())
                    .displayName(user.getUsername())
                    .bioDescription("Klink Bio sayfama hoş geldiniz!")
                    .theme("classic_dark")
                    .verified(false)
                    .viewCount(0L)
                    .links(new ArrayList<>())
                    .build();
        }

        return toDto(bioPage, true); // owner view: all links
    }

    @Transactional
    public BioPageDto createOrUpdateBioPage(BioPageUpdateRequest request) {
        UserAccount user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Giriş yapmanız gerekmektedir.");
        }

        String username = request.getUsername() != null ? request.getUsername().trim().toLowerCase() : user.getUsername().toLowerCase();
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Geçersiz kullanıcı adı formatı (3-30 karakter, harf, rakam, alt çizgi ve tire kullanılabilir).");
        }

        BioPage existingByUsername = bioPageRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (existingByUsername != null && !existingByUsername.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bu kullanıcı adı (@" + username + ") başka bir kullanıcı tarafından alınmış.");
        }

        BioPage bioPage = bioPageRepository.findByUserId(user.getId()).orElse(null);
        if (bioPage == null) {
            bioPage = BioPage.builder()
                    .user(user)
                    .username(username)
                    .displayName(request.getDisplayName() != null ? request.getDisplayName().trim() : user.getUsername())
                    .bioDescription(request.getBioDescription())
                    .avatarUrl(request.getAvatarUrl())
                    .theme(request.getTheme() != null ? request.getTheme() : "classic_dark")
                    .socialLinks(request.getSocialLinks())
                    .verified(false)
                    .viewCount(0L)
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .links(new ArrayList<>())
                    .build();
        } else {
            bioPage.setUsername(username);
            bioPage.setDisplayName(request.getDisplayName() != null ? request.getDisplayName().trim() : user.getUsername());
            bioPage.setBioDescription(request.getBioDescription());
            bioPage.setAvatarUrl(request.getAvatarUrl());
            bioPage.setTheme(request.getTheme() != null ? request.getTheme() : "classic_dark");
            bioPage.setSocialLinks(request.getSocialLinks());
            bioPage.setUpdatedAt(System.currentTimeMillis());
        }

        // Link itemlerini güncelle
        bioPage.getLinks().clear();

        if (request.getLinks() != null) {
            int order = 0;
            for (BioLinkItemDto itemDto : request.getLinks()) {
                if (itemDto.getTitle() == null || itemDto.getTitle().trim().isEmpty() ||
                    itemDto.getUrl() == null || itemDto.getUrl().trim().isEmpty()) {
                    continue;
                }

                BioLinkItem linkItem = BioLinkItem.builder()
                        .title(itemDto.getTitle().trim())
                        .url(itemDto.getUrl().trim())
                        .icon(itemDto.getIcon() != null ? itemDto.getIcon() : "Globe")
                        .highlighted(itemDto.isHighlighted())
                        .active(itemDto.isActive())
                        .sortOrder(order++)
                        .clickCount(itemDto.getClickCount() != null ? itemDto.getClickCount() : 0L)
                        .bioPage(bioPage)
                        .build();

                bioPage.getLinks().add(linkItem);
            }
        }

        BioPage saved = bioPageRepository.save(bioPage);
        log.info("✨ Bio sayfası kaydedildi: @{}", saved.getUsername());
        return toDto(saved, true);
    }

    @Transactional
    public void recordLinkClick(UUID linkId) {
        if (linkId == null) return;
        try {
            bioLinkItemRepository.incrementClickCount(linkId);
            log.info("🖱️ Bio link tıklaması kaydedildi: {}", linkId);
        } catch (Exception e) {
            log.warn("Bio link tıklaması kaydedilemedi: {}", e.getMessage());
        }
    }

    private BioPageDto toDto(BioPage page, boolean includeInactive) {
        List<BioLinkItemDto> linkDtos = page.getLinks().stream()
                .filter(l -> includeInactive || l.isActive())
                .map(l -> BioLinkItemDto.builder()
                        .id(l.getId())
                        .title(l.getTitle())
                        .url(l.getUrl())
                        .icon(l.getIcon())
                        .highlighted(l.isHighlighted())
                        .active(l.isActive())
                        .sortOrder(l.getSortOrder())
                        .clickCount(l.getClickCount())
                        .build())
                .collect(Collectors.toList());

        return BioPageDto.builder()
                .id(page.getId())
                .username(page.getUsername())
                .displayName(page.getDisplayName())
                .bioDescription(page.getBioDescription())
                .avatarUrl(page.getAvatarUrl())
                .theme(page.getTheme())
                .socialLinks(page.getSocialLinks())
                .verified(page.isVerified())
                .viewCount(page.getViewCount())
                .createdAt(page.getCreatedAt())
                .updatedAt(page.getUpdatedAt())
                .links(linkDtos)
                .build();
    }

    private UserAccount getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
