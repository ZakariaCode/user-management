package com.hendisantika.usermanagement;

import com.hendisantika.usermanagement.config.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(WebSecurityConfig.class)
class WebSecurityConfigTest {

    @MockBean
    private PasswordEncoder bCryptPasswordEncoder;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private WebSecurityConfig webSecurityConfig;

    @Autowired
    private ApplicationContext context;

    @Test
    void authenticationProvider_isConfiguredWithMocks() throws Exception {
        DaoAuthenticationProvider provider = webSecurityConfig.authenticationProvider();
        assertNotNull(provider, "DaoAuthenticationProvider should not be null");

        // access protected getUserDetailsService() via reflection
        Method getUds = DaoAuthenticationProvider.class.getDeclaredMethod("getUserDetailsService");
        getUds.setAccessible(true);
        Object uds = getUds.invoke(provider);
        assertSame(userDetailsService, uds, "UserDetailsService should be the mocked instance");

        // verify password encoder wiring via reflection (field may be non-public)
        Field passwordEncoderField = DaoAuthenticationProvider.class.getDeclaredField("passwordEncoder");
        passwordEncoderField.setAccessible(true);
        Object encoder = passwordEncoderField.get(provider);
        assertSame(bCryptPasswordEncoder, encoder, "PasswordEncoder should be the mocked instance");
    }

    @Test
    void securityFilterChain_methodExistsAndReturnsSecurityFilterChain() throws Exception {
        Method method = WebSecurityConfig.class.getMethod("securityFilterChain", org.springframework.security.config.annotation.web.builders.HttpSecurity.class);
        assertEquals(SecurityFilterChain.class, method.getReturnType(), "securityFilterChain should return SecurityFilterChain");
    }
}