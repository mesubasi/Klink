package com.urlshortener;

import com.urlshortener.exception.MaliciousUrlException;
import com.urlshortener.service.MessageService;
import com.urlshortener.service.UrlSecurityScannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UrlSecurityScannerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private UrlSecurityScannerService urlSecurityScannerService;

    @BeforeEach
    public void setup() {
        given(messageService.getMessage(any())).willAnswer(inv -> inv.getArgument(0));
        ReflectionTestUtils.setField(urlSecurityScannerService, "enabled", true);
    }

    @Test
    public void testSafeUrlPassesScan() {
        assertDoesNotThrow(() -> {
            urlSecurityScannerService.checkUrlSafety("https://google.com");
        });
    }

    @Test
    public void testKnownThreatUrlThrowsMaliciousUrlException() {
        assertThrows(MaliciousUrlException.class, () -> {
            urlSecurityScannerService.checkUrlSafety("http://testsafebrowsing.appspot.com/s/phishing.html");
        });
    }

    @Test
    public void testPhishingDomainThrowsMaliciousUrlException() {
        assertThrows(MaliciousUrlException.class, () -> {
            urlSecurityScannerService.checkUrlSafety("https://phishing.test/login");
        });
    }
}
