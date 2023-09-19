package com.vsnitko.oauth2.service.impl;

import static com.vsnitko.oauth2.service.impl.BasicAuthServiceImpl.DEFAULT_USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vsnitko.oauth2.model.entity.User;
import com.vsnitko.oauth2.model.entity.UserDetailsImpl;
import com.vsnitko.oauth2.model.payload.SignInRequest;
import com.vsnitko.oauth2.model.payload.SignInResponse;
import com.vsnitko.oauth2.model.payload.SignUpRequest;
import com.vsnitko.oauth2.service.TokenManager;
import com.vsnitko.oauth2.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceImplTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    TokenManager tokenManager;

    @Mock
    UserService userService;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    BasicAuthServiceImpl basicAuthService;

    @Test
    void basicSignIn() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(new UserDetailsImpl(new User()), null);
        String expectedToken = "anyToken";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
            authentication);
        when(tokenManager.createToken(any(User.class))).thenReturn(expectedToken);

        SignInResponse signInResponse = basicAuthService.basicSignIn(new SignInRequest());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenManager, times(1)).createToken(any(User.class));

        assertEquals(expectedToken, signInResponse.getToken());
    }

    @Test
    void basicSignUp() {
        String expectedToken = "anyToken";

        when(tokenManager.createToken(any(User.class))).thenReturn(expectedToken);

        SignInResponse signInResponse = basicAuthService.basicSignUp(new SignUpRequest());

        verify(userService, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(any());
        verify(tokenManager, times(1)).createToken(any(User.class));

        assertEquals(expectedToken, signInResponse.getToken());
    }

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Test
    void basicSignUp_when_usernameIsEmpty_then_useDefaultUsername() {
        final String name = "anyName";

        basicAuthService.basicSignUp(new SignUpRequest().setName(name));

        verify(userService, times(1)).save(userCaptor.capture());
        assertEquals(name, userCaptor.getValue().getName());
    }

    @Test
    void basicSignUp_when_usernameNotEmpty_then_useThisUsername() {
        basicAuthService.basicSignUp(new SignUpRequest());

        verify(userService, times(1)).save(userCaptor.capture());
        assertEquals(DEFAULT_USERNAME, userCaptor.getValue().getName());
    }
}
