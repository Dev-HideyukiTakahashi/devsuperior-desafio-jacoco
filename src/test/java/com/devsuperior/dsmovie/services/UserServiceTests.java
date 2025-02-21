package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserUtil customUserUtil;

    private String existingUsername;
    private String notExistsUsername;
    private UserEntity userEntity;
    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setUp() throws Exception {
        existingUsername = "maria@gmail.com";
        notExistsUsername = "alex@gmail.com";
        userEntity = UserFactory.createUserEntity();
        userDetails = UserDetailsFactory.createCustomClientUser(existingUsername);
    }

    @Test
    public void authenticatedShouldReturnUserEntityWhenUserExists() {

        when(customUserUtil.getLoggedUsername()).thenReturn(existingUsername);
        when(userRepository.findByUsername(existingUsername)).thenReturn(Optional.of(userEntity));

        UserEntity result = userService.authenticated();

        assertNotNull(result);
        assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        when(customUserUtil.getLoggedUsername()).thenReturn(notExistsUsername);
        when(userRepository.findByUsername(notExistsUsername)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.authenticated();
        });
    }

    @Test
    public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

        when(userRepository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetails);

        UserDetails result = userService.loadUserByUsername(existingUsername);

        assertNotNull(result);
        assertEquals(result.getUsername(), existingUsername);
    }

    @Test
    public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

        when(userRepository.searchUserAndRolesByUsername(notExistsUsername))
                .thenReturn(new ArrayList<>());
        userDetails.clear();
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(notExistsUsername);
        });
    }
}