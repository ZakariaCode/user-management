package com.hendisantika.usermanagement;

import com.hendisantika.usermanagement.entity.Role;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserDetailsServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        // Préparation des rôles
        Role adminRole = new Role();
        adminRole.setId(Long.valueOf(1));
        adminRole.setName("ADMIN");
        adminRole.setDescription("ROLE_ADMIN");

        Role userRole = new Role();
        userRole.setId(Long.valueOf(2));
        userRole.setName("USER");
        userRole.setDescription("ROLE_USER");

        roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(userRole);

        // Préparation de l'utilisateur de test
        testUser = new User();
        testUser.setId(Long.valueOf(1));
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword123");
        testUser.setEmail("test@example.com");
        testUser.setRoles(roles);
    }

    @Test
    @DisplayName("Doit charger un utilisateur avec succès par son username")
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword123", userDetails.getPassword());
        assertEquals(2, userDetails.getAuthorities().size());

        // Vérifier que les autorités contiennent les bons rôles
        Set<String> authorities = new HashSet<>();
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_USER"));

        // Vérifier que la méthode du repository a été appelée une fois
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Doit lancer UsernameNotFoundException quand l'utilisateur n'existe pas")
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknownuser")
        );

        assertEquals("Login Username Invalid.", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("unknownuser");
    }

    @Test
    @DisplayName("Doit charger un utilisateur sans rôles")
    void testLoadUserByUsername_UserWithoutRoles() {
        // Arrange
        testUser.setRoles(new HashSet<>()); // Utilisateur sans rôles
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals(0, userDetails.getAuthorities().size());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Doit charger un utilisateur avec un seul rôle")
    void testLoadUserByUsername_UserWithSingleRole() {
        // Arrange
        Role singleRole = new Role();
        singleRole.setId(Long.valueOf(1));
        singleRole.setName("USER");
        singleRole.setDescription("ROLE_USER");

        Set<Role> singleRoleSet = new HashSet<>();
        singleRoleSet.add(singleRole);
        testUser.setRoles(singleRoleSet);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Doit gérer les username null")
    void testLoadUserByUsername_NullUsername() {
        // Arrange
        when(userRepository.findByUsername((String) null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername((String) null)
        );
        verify(userRepository, times(1)).findByUsername((String) null);
    }

    @Test
    @DisplayName("Doit gérer les username vides")
    void testLoadUserByUsername_EmptyUsername() {
        // Arrange
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("")
        );
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    @DisplayName("Doit charger un utilisateur avec des rôles ayant des caractères spéciaux")
    void testLoadUserByUsername_RolesWithSpecialCharacters() {
        // Arrange
        Role specialRole = new Role();
        specialRole.setId(Long.valueOf(1));
        specialRole.setName("SUPER_ADMIN");
        specialRole.setDescription("ROLE_SUPER_ADMIN");

        Set<Role> specialRoles = new HashSet<>();
        specialRoles.add(specialRole);
        testUser.setRoles(specialRoles);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Doit vérifier que UserDetails retourné est une instance de User")
    void testLoadUserByUsername_ReturnsUserInstance() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertInstanceOf(org.springframework.security.core.userdetails.User.class, userDetails);
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}