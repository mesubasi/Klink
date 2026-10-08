package com.urlshortener;

import com.urlshortener.model.UserAccount;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.AuthService;
import com.urlshortener.service.AuthTokenService;
import com.urlshortener.service.MessageService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LogoutEverywhereTest {

    @Test
    void bumpsTheVersionAndSavesTheAccount() {
        UserRepository userRepository = mock(UserRepository.class);
        UserAccount user = UserAccount.builder().username("ayse").build();
        when(userRepository.findByUsername("ayse")).thenReturn(Optional.of(user));
        AuthService service = new AuthService(userRepository, null, mock(MessageService.class), null, null, null, mock(AuthTokenService.class));

        service.logoutEverywhere("ayse");

        assertEquals(1, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void unknownUserIsAnError() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        AuthService service = new AuthService(userRepository, null, mock(MessageService.class), null, null, null, mock(AuthTokenService.class));

        assertThrows(IllegalArgumentException.class, () -> service.logoutEverywhere("ghost"));
    }
}
