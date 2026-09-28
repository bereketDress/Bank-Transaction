package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.util.PasswordHasher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit: Java testing library/framework
 *
 * @Test           -> marks a method as a test
 * @BeforeEach     -> runs before every @Test
 * assertEquals()  -> compares expected and actual values
 * assertTrue()    -> expects true
 * assertFalse()   -> expects false
 * assertNull()    -> expects null
 * assertNotNull() -> expects non-null
 * assertThrows()  -> expects an exception
 *
 * Mockito: Java mocking library
 *
 * mock()        -> creates a fake/mock object
 * when()        -> defines behavior of a mock(prepare)
 * thenReturn()  -> returns a fixed value from a mock
 * thenAnswer()  -> dynamically decides what the mock returns
 * getArgument() -> gets an argument passed to the mocked method by index
 * verify()      -> checks whether a mock method was called(check)
 * any()         -> matches any argument of the specified type
 */
class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private UserServiceImpl userService;
    // @BeforeEach: creates fresh mocks and a new test object before every test.
    // This keeps tests independent so one test does not affect another.
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);

        userService = new UserServiceImpl(userRepository, passwordHasher);
    }

    // Positive
    @Test
    void registerUserShouldWork() {
        // when control fake behavior: preparation
        when(passwordHasher.hash("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(i -> i.getArgument(0));

        User user = userService.registerUser(
                "Bereket",
                "bereket@gmail.com",
                "password123"
        );
        assertEquals("Bereket", user.getUserName());
        assertEquals("bereket@gmail.com", user.getUserEmail());
        assertEquals("hashedPassword", user.getPasswordHash());
        // verify:check whether the behavior happened:checking
        verify(userRepository).save(any(User.class));
    }
    // Negative
    @Test
    void registerUserShouldRejectShortPassword() {

        assertThrows(
                BankException.class,
                () -> userService.registerUser(
                        "Bereket",
                        "bereket@gmail.com",
                        "123"
                )
        );
    }

    // Positive
    @Test
    void loginUserShouldWork() {

        User user = new User(
                1L,
                "Bereket",
                "bereket@gmail.com",
                "hashedPassword"
        );

        when(userRepository.findByEmail("bereket@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches(
                "password123",
                "hashedPassword"
        )).thenReturn(true);

        User result = userService.loginUser(
                "bereket@gmail.com",
                "password123"
        );

        assertEquals(user, result);
    }

    // Negative: invalid or error situation
    @Test
    void getUserShouldThrowWhenUserDoesNotExist() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                BankException.class,
                () -> userService.getUser(1L)
        );
    }
}
