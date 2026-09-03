package com.urlshortener;

import com.urlshortener.dto.AbTestConfigResponse;
import com.urlshortener.dto.UpdateAbTestConfigRequest;
import com.urlshortener.dto.UrlVariantRequest;
import com.urlshortener.model.UrlMapping;
import com.urlshortener.model.UrlVariant;
import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.repository.UrlVariantRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.repository.WorkspaceMemberRepository;
import com.urlshortener.service.AbTestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbTestServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private UrlVariantRepository urlVariantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private AbTestService abTestService;

    private UserAccount testUser;
    private UrlMapping testMapping;

    @BeforeEach
    void setUp() {
        testUser = new UserAccount(UUID.randomUUID(), "testuser", "encoded", "test@example.com", "ROLE_USER", true, null, System.currentTimeMillis());
        testMapping = UrlMapping.builder()
                .id(UUID.randomUUID())
                .shortCode("test-ab")
                .originalUrl("https://klink.co/default")
                .user(testUser)
                .abTestingEnabled(false)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldThrowWhenLessThanTwoVariantsProvided() {
        when(urlMappingRepository.findByShortCode("test-ab")).thenReturn(Optional.of(testMapping));

        UpdateAbTestConfigRequest request = new UpdateAbTestConfigRequest(true, List.of(
                new UrlVariantRequest("Varyant A", "https://sirket.com/a", 100)
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                abTestService.updateAbTestConfig("test-ab", request));

        assertTrue(ex.getMessage().contains("en az 2 hedef varyant"));
    }

    @Test
    void shouldThrowWhenWeightsDoNotSumToHundred() {
        when(urlMappingRepository.findByShortCode("test-ab")).thenReturn(Optional.of(testMapping));

        UpdateAbTestConfigRequest request = new UpdateAbTestConfigRequest(true, List.of(
                new UrlVariantRequest("Varyant A", "https://sirket.com/a", 60),
                new UrlVariantRequest("Varyant B", "https://sirket.com/b", 30) // Total: 90
        ));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                abTestService.updateAbTestConfig("test-ab", request));

        assertTrue(ex.getMessage().contains("tam olarak %100 olmalıdır"));
    }

    @Test
    void shouldConfigureAbTestSuccessfully() {
        when(urlMappingRepository.findByShortCode("test-ab")).thenReturn(Optional.of(testMapping));

        UpdateAbTestConfigRequest request = new UpdateAbTestConfigRequest(true, List.of(
                new UrlVariantRequest("Varyant A", "https://sirket.com/a", 60),
                new UrlVariantRequest("Varyant B", "https://sirket.com/b", 40)
        ));

        UrlVariant vA = UrlVariant.builder().id(UUID.randomUUID()).label("Varyant A").targetUrl("https://sirket.com/a").weightPercent(60).clickCount(10L).active(true).build();
        UrlVariant vB = UrlVariant.builder().id(UUID.randomUUID()).label("Varyant B").targetUrl("https://sirket.com/b").weightPercent(40).clickCount(5L).active(true).build();

        when(urlVariantRepository.findByUrlMappingId(testMapping.getId())).thenReturn(List.of(vA, vB));

        AbTestConfigResponse response = abTestService.updateAbTestConfig("test-ab", request);

        assertNotNull(response);
        assertTrue(response.isAbTestingEnabled());
        assertEquals(2, response.getVariants().size());
        assertEquals(15L, response.getTotalClicks());
        verify(urlVariantRepository, times(2)).save(any(UrlVariant.class));
    }

    @Test
    void shouldSelectVariantBasedOnStickyCookie() {
        UUID variantAId = UUID.randomUUID();
        UrlVariant vA = UrlVariant.builder().id(variantAId).label("Varyant A").targetUrl("https://sirket.com/a").weightPercent(50).clickCount(0L).active(true).build();
        UrlVariant vB = UrlVariant.builder().id(UUID.randomUUID()).label("Varyant B").targetUrl("https://sirket.com/b").weightPercent(50).clickCount(0L).active(true).build();

        when(urlVariantRepository.findByUrlMappingId(testMapping.getId())).thenReturn(List.of(vA, vB));

        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie abCookie = new Cookie("klink_ab_test-ab", variantAId.toString());
        when(request.getCookies()).thenReturn(new Cookie[]{abCookie});

        UrlVariant selected = abTestService.selectVariant(testMapping, request, null);

        assertNotNull(selected);
        assertEquals(variantAId, selected.getId());
        assertEquals("Varyant A", selected.getLabel());
        assertEquals(1L, selected.getClickCount());
        verify(urlVariantRepository).save(vA);
    }
}
