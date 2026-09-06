package com.urlshortener;

import com.urlshortener.model.UrlMapping;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.service.LinkHealthMonitorService;
import com.urlshortener.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class LinkHealthMonitorServiceTest {

    @Mock
    private UrlMappingRepository urlMappingRepository;

    @Mock
    private MessageService messageService;

    @Mock
    private com.urlshortener.service.EmailService emailService;

    private LinkHealthMonitorService linkHealthMonitorService;

    @BeforeEach
    public void setup() {
        linkHealthMonitorService = new LinkHealthMonitorService(urlMappingRepository, messageService, emailService);
    }

    @Test
    public void testCheckUrlHealthWithInvalidHost() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("brokenTest")
                .originalUrl("https://this-domain-definitely-does-not-exist-xyz987654.com")
                .build();

        given(urlMappingRepository.save(any(UrlMapping.class))).willAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = linkHealthMonitorService.checkUrlHealth(mapping);

        assertNotNull(result);
        assertEquals("BROKEN", result.getHealthStatus());
        assertEquals(0, result.getHealthStatusCode());
        assertNotNull(result.getLastHealthCheck());
        assertNotNull(result.getHealthErrorMessage());
    }

    @Test
    public void testCheckHealthByShortCode() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("demoCode")
                .originalUrl("https://this-domain-definitely-does-not-exist-xyz987654.com")
                .build();

        given(urlMappingRepository.findByShortCode("demoCode")).willReturn(Optional.of(mapping));
        given(urlMappingRepository.save(any(UrlMapping.class))).willAnswer(invocation -> invocation.getArgument(0));

        UrlMapping result = linkHealthMonitorService.checkHealthByShortCode("demoCode");

        assertNotNull(result);
        assertEquals("BROKEN", result.getHealthStatus());
    }

    @Test
    public void testEmailAlertFiredOnTransitionToBroken() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("alertTest")
                .originalUrl("https://this-domain-definitely-does-not-exist-xyz987654.com")
                .healthStatus("HEALTHY")
                .build();

        given(urlMappingRepository.save(any(UrlMapping.class))).willAnswer(invocation -> invocation.getArgument(0));

        linkHealthMonitorService.checkUrlHealth(mapping);

        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.times(1))
                .sendBrokenLinkAlert(any(UrlMapping.class));
    }

    @Test
    public void testEmailAlertNotFiredIfAlreadyBroken() {
        UrlMapping mapping = UrlMapping.builder()
                .shortCode("alreadyBroken")
                .originalUrl("https://this-domain-definitely-does-not-exist-xyz987654.com")
                .healthStatus("BROKEN")
                .build();

        given(urlMappingRepository.save(any(UrlMapping.class))).willAnswer(invocation -> invocation.getArgument(0));

        linkHealthMonitorService.checkUrlHealth(mapping);

        // Link zaten BROKEN olduğu için tekrar mail ATILMAMALI (Spam engeli)
        org.mockito.Mockito.verify(emailService, org.mockito.Mockito.never())
                .sendBrokenLinkAlert(any(UrlMapping.class));
    }
}
