package com.hendisantika.usermanagement;

import com.hendisantika.usermanagement.dto.ChangePasswordForm;
import com.hendisantika.usermanagement.entity.Role;
import com.hendisantika.usermanagement.entity.User;
import com.hendisantika.usermanagement.exception.CustomFieldValidationException;
import com.hendisantika.usermanagement.exception.UsernameOrIdNotFound;
import com.hendisantika.usermanagement.repository.UserRepository;
import com.hendisantika.usermanagement.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour UserService
 * Couvre toutes les méthodes publiques et privées via leurs appels
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Set<Role> roles;

    @BeforeEach
    void setUp() {
        // Préparation des rôles
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");
        userRole.setDescription("ROLE_USER");

        roles = new HashSet<>();
        roles.add(userRole);

        // Préparation de l'utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setConfirmPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(roles);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== Tests pour getAllUsers() ====================

    @Test
    @DisplayName("getAllUsers - Doit retourner tous les utilisateurs")
    void testGetAllUsers_Success() {
        // Arrange
        List<User> users = Arrays.asList(testUser, new User());
        when(repository.findAll()).thenReturn(users);

        // Act
        Iterable<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, ((List<User>) result).size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Doit retourner une liste vide si aucun utilisateur")
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // Act
        Iterable<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(0, ((List<User>) result).size());
        verify(repository, times(1)).findAll();
    }

    // ==================== Tests pour createUser() ====================

    @Test
    @DisplayName("createUser - Doit créer un utilisateur avec succès")
    void testCreateUser_Success() throws Exception {
        // Arrange
        when(repository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(repository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(repository, times(1)).findByUsername("testuser");
        verify(bCryptPasswordEncoder, times(1)).encode("password123");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Doit lever CustomFieldValidationException si username existe déjà")
    void testCreateUser_UsernameNotAvailable() {
        // Arrange
        when(repository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        CustomFieldValidationException exception = assertThrows(
                CustomFieldValidationException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Username not available", exception.getMessage());
        assertEquals("username", exception.getFieldName());
        verify(repository, times(1)).findByUsername("testuser");
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Doit lever CustomFieldValidationException si confirmPassword est null")
    void testCreateUser_ConfirmPasswordNull() {
        // Arrange
        testUser.setConfirmPassword(null);
        when(repository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        CustomFieldValidationException exception = assertThrows(
                CustomFieldValidationException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Confirm Password is required", exception.getMessage());
        assertEquals("confirmPassword", exception.getFieldName());
        verify(repository, times(1)).findByUsername("testuser");
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Doit lever CustomFieldValidationException si confirmPassword est vide")
    void testCreateUser_ConfirmPasswordEmpty() {
        // Arrange
        testUser.setConfirmPassword("");
        when(repository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        CustomFieldValidationException exception = assertThrows(
                CustomFieldValidationException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Confirm Password is required", exception.getMessage());
        assertEquals("confirmPassword", exception.getFieldName());
        verify(repository, times(1)).findByUsername("testuser");
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - Doit lever CustomFieldValidationException si password et confirmPassword ne correspondent pas")
    void testCreateUser_PasswordsDoNotMatch() {
        // Arrange
        testUser.setConfirmPassword("differentPassword");
        when(repository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        CustomFieldValidationException exception = assertThrows(
                CustomFieldValidationException.class,
                () -> userService.createUser(testUser)
        );

        assertEquals("Password and Confirm Password are not the same", exception.getMessage());
        assertEquals("password", exception.getFieldName());
        verify(repository, times(1)).findByUsername("testuser");
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    // ==================== Tests pour getUserById() ====================

    @Test
    @DisplayName("getUserById - Doit retourner un utilisateur par son ID")
    void testGetUserById_Success() throws UsernameOrIdNotFound {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getUserById - Doit lever UsernameOrIdNotFound si l'utilisateur n'existe pas")
    void testGetUserById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameOrIdNotFound exception = assertThrows(
                UsernameOrIdNotFound.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("User id does not exist.", exception.getMessage());
        verify(repository, times(1)).findById(999L);
    }

    // ==================== Tests pour updateUser() ====================

    @Test
    @DisplayName("updateUser - Doit mettre à jour un utilisateur avec succès")
    void testUpdateUser_Success() throws Exception {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldusername");
        existingUser.setFirstName("OldFirst");
        existingUser.setLastName("OldLast");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword("oldPassword");
        existingUser.setRoles(roles);

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updatedusername");
        updatedUser.setFirstName("UpdatedFirst");
        updatedUser.setLastName("UpdatedLast");
        updatedUser.setEmail("updated@example.com");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");
        Set<Role> updatedRoles = new HashSet<>();
        updatedRoles.add(adminRole);
        updatedUser.setRoles(updatedRoles);

        when(repository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(updatedUser);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(User.class));

        // Vérifier que mapUser a été appelé via ArgumentCaptor
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Vérifier que les propriétés ont été mappées correctement
        assertEquals("updatedusername", savedUser.getUsername());
        assertEquals("UpdatedFirst", savedUser.getFirstName());
        assertEquals("UpdatedLast", savedUser.getLastName());
        assertEquals("updated@example.com", savedUser.getEmail());
        assertEquals(updatedRoles, savedUser.getRoles());
        // Le password ne doit pas être changé par mapUser
        assertEquals("oldPassword", savedUser.getPassword());
    }

    @Test
    @DisplayName("updateUser - Doit lever UsernameOrIdNotFound si l'utilisateur n'existe pas")
    void testUpdateUser_UserNotFound() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setId(999L);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameOrIdNotFound.class, () -> userService.updateUser(updatedUser));
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Doit mapper correctement avec des valeurs null (teste mapUser indirectement)")
    void testUpdateUser_WithNullValues() throws Exception {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldusername");
        existingUser.setPassword("oldPassword");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername(null);
        updatedUser.setFirstName(null);
        updatedUser.setLastName(null);
        updatedUser.setEmail(null);
        updatedUser.setRoles(null);

        when(repository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(repository.save(any(User.class))).thenReturn(existingUser);

        // Act
        userService.updateUser(updatedUser);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNull(savedUser.getUsername());
        assertNull(savedUser.getFirstName());
        assertNull(savedUser.getLastName());
        assertNull(savedUser.getEmail());
        assertNull(savedUser.getRoles());
        assertEquals("oldPassword", savedUser.getPassword()); // Le password ne change pas
    }

    // ==================== Tests pour deleteUser() ====================

    @Test
    @DisplayName("deleteUser - Doit supprimer un utilisateur avec succès")
    void testDeleteUser_Success() throws UsernameOrIdNotFound {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(repository).delete(any(User.class));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser - Doit lever UsernameOrIdNotFound si l'utilisateur n'existe pas")
    void testDeleteUser_UserNotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameOrIdNotFound.class, () -> userService.deleteUser(999L));
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).delete(any(User.class));
    }

    // ==================== Tests pour changePassword() ====================

    @Test
    @DisplayName("changePassword - Doit changer le mot de passe avec succès (utilisateur non-admin)")
    void testChangePassword_Success_NonAdmin() throws Exception {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("password123");
        form.setNewPassword("newPassword456");
        form.setConfirmPassword("newPassword456");

        testUser.setPassword("password123");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bCryptPasswordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword456");
        when(repository.save(any(User.class))).thenReturn(testUser);

        mockSecurityContextNonAdmin();

        // Act
        User result = userService.changePassword(form);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(bCryptPasswordEncoder, times(1)).encode("newPassword456");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit changer le mot de passe avec succès (utilisateur admin sans vérification du mot de passe actuel)")
    void testChangePassword_Success_Admin() throws Exception {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("wrongCurrentPassword"); // Mot de passe actuel incorrect
        form.setNewPassword("newPassword456");
        form.setConfirmPassword("newPassword456");

        testUser.setPassword("password123"); // Mot de passe réel différent

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bCryptPasswordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword456");
        when(repository.save(any(User.class))).thenReturn(testUser);

        mockSecurityContextAdmin(); // L'admin peut changer sans vérifier le mot de passe actuel

        // Act
        User result = userService.changePassword(form);

        // Assert
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(bCryptPasswordEncoder, times(1)).encode("newPassword456");
        verify(repository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit lever une exception si le mot de passe actuel est invalide (non-admin)")
    void testChangePassword_InvalidCurrentPassword_NonAdmin() {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("wrongPassword");
        form.setNewPassword("newPassword456");
        form.setConfirmPassword("newPassword456");

        testUser.setPassword("password123");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        mockSecurityContextNonAdmin();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> userService.changePassword(form));
        assertEquals("Current Password invalid.", exception.getMessage());
        verify(repository, times(1)).findById(1L);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit lever une exception si le nouveau mot de passe est identique à l'ancien")
    void testChangePassword_SameAsCurrentPassword() {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("password123");
        form.setNewPassword("password123"); // Même que l'ancien
        form.setConfirmPassword("password123");

        testUser.setPassword("password123");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        mockSecurityContextNonAdmin();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> userService.changePassword(form));
        assertEquals("New password must be different from the current password.", exception.getMessage());
        verify(repository, times(1)).findById(1L);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit lever une exception si newPassword et confirmPassword ne correspondent pas")
    void testChangePassword_NewPasswordMismatch() {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("password123");
        form.setNewPassword("newPassword456");
        form.setConfirmPassword("differentPassword"); // Différent de newPassword

        testUser.setPassword("password123");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        mockSecurityContextNonAdmin();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> userService.changePassword(form));
        assertEquals("New Password and Confirm Password do not match.", exception.getMessage());
        verify(repository, times(1)).findById(1L);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit lever UsernameOrIdNotFound si l'utilisateur n'existe pas")
    void testChangePassword_UserNotFound() {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(999L);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameOrIdNotFound.class, () -> userService.changePassword(form));
        verify(repository, times(1)).findById(999L);
        verify(bCryptPasswordEncoder, never()).encode(anyString());
        verify(repository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("changePassword - Doit vérifier l'ordre de validation")
    void testChangePassword_ValidationOrder() {
        // Arrange
        ChangePasswordForm form = new ChangePasswordForm();
        form.setId(1L);
        form.setCurrentPassword("password123");
        form.setNewPassword("password123"); // Même que l'actuel
        form.setConfirmPassword("password123");

        testUser.setPassword("password123");

        when(repository.findById(1L)).thenReturn(Optional.of(testUser));
        mockSecurityContextNonAdmin();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> userService.changePassword(form));
        assertEquals("New password must be different from the current password.", exception.getMessage());
    }

    // ==================== Méthodes utilitaires pour mocker le contexte de sécurité ====================

    // java
    // java
    @SuppressWarnings("unchecked")
    private void mockSecurityContextAdmin() {
        UserDetails userDetails = mock(UserDetails.class);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Use doReturn to avoid generics capture mismatch
        doReturn(authorities).when(userDetails).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @SuppressWarnings("unchecked")
    private void mockSecurityContextNonAdmin() {
        UserDetails userDetails = mock(UserDetails.class);
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Use doReturn here as well
        doReturn(authorities).when(userDetails).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}