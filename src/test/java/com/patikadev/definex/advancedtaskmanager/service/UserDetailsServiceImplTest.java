package com.patikadev.definex.advancedtaskmanager.service;

import com.patikadev.definex.advancedtaskmanager.model.entity.Role;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import com.patikadev.definex.advancedtaskmanager.model.enums.UserRole;
import com.patikadev.definex.advancedtaskmanager.repository.UserRepository;
import com.patikadev.definex.advancedtaskmanager.security.CustomUserDetails;
import com.patikadev.definex.advancedtaskmanager.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;
    private Role adminRole;
    private Role teamMemberRole;
    private String userEmail;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test.user@example.com";

        adminRole = createRole(1L, UserRole.ADMIN);
        teamMemberRole = createRole(2L, UserRole.TEAM_MEMBER);
        user = createUser();
    }

    @Test
    @DisplayName("Load User By Username - Success")
    void loadUserByUsername_Success() {
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        assertEquals(userEmail, userDetails.getUsername());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_TEAM_MEMBER")));

        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());

        verify(userRepository).findByEmailAndIsActiveTrue(userEmail);
    }

    @Test
    @DisplayName("Load User By Username - User Not Found")
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(userEmail));

        assertTrue(exception.getMessage().contains(userEmail));
        verify(userRepository).findByEmailAndIsActiveTrue(userEmail);
    }

    @Test
    @DisplayName("Load User By Username - User Is Inactive")
    void loadUserByUsername_UserIsInactive() {
        User inactiveUser = createUser();
        inactiveUser.setIsActive(false);

        when(userRepository.findByEmailAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(userEmail));

        assertTrue(exception.getMessage().contains(userEmail));
        verify(userRepository).findByEmailAndIsActiveTrue(userEmail);
    }

    private User createUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        roles.add(teamMemberRole);

        User user = User.builder()
                .id(userId)
                .firstName("Test")
                .lastName("User")
                .email(userEmail)
                .password("Password123!")
                .roles(roles)
                .build();
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        return user;
    }

    private Role createRole(Long id, UserRole roleName) {
        Role role = Role.builder()
                .id(id)
                .name(roleName)
                .build();
        role.setIsActive(true);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }
} 