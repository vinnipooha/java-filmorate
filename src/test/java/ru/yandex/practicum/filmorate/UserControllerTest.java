package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    UserController userController;

    @BeforeEach
    public void beforeEach() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
    }

    @Test
    void addValidFriend() {
        User user1 = User.builder()
                .email("test1@ya.ru")
                .login("login1")
                .name("Test Name1")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        User user2 = User.builder()
                .email("test2@ya.ru")
                .login("login2")
                .name("Test Name2")
                .birthday(LocalDate.of(1981, 1, 1))
                .build();
        User addUser1 = userController.createUser(user1);
        User addUser2 = userController.createUser(user2);
        User addFriend = userController.addFriend(1L, 2L);
        assertTrue(addFriend.getFriends().contains(2));
    }

    @Test
    void postAndGetValidUser() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        User addUser = userController.createUser(user);
        Collection<User> listUsers = userController.findAllUsers();
        assertEquals(1, listUsers.size(), "GET-запрос работает некорректно");
        assertEquals(1, addUser.getId(), "Генерация id работает некорректно");
        assertEquals(user.getLogin(), addUser.getLogin(), "POST-запрос работает некорректно");
    }

    @Test
    void testExceptionByPostWithEmptyLogin() {
        User user = User.builder()
                .email("ya@ya.ru")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя без логина должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithLoginContainsWhitespace() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя с логином, содержащим пробел(-ы) должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithoutEmail() {
        User user = User.builder()
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя без электронной почты должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithEmptyEmail() {
        User user = User.builder()
                .email("")
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя с пустой электронной почтой должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithInvalidEmail() {
        User user = User.builder()
                .email("yaya.ru@")
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя с некорректной электронной почтой должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithBirthdayInNextYear() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.now().plusYears(1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя с датой рождения в будущем должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithBirthdayInNextDay() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login test")
                .name("Test Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();
        Assertions.assertThrows(ValidationException.class, () -> userController.createUser(user),
                "Загрузка пользователя с датой рождения завтра должна привести к исключению");
    }

    @Test
    void testByPostWithBirthdayToday() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .name("Test Name")
                .birthday(LocalDate.now())
                .build();
        User addUser = userController.createUser(user);
        Collection<User> listUsers = userController.findAllUsers();
        assertEquals(1, listUsers.size(), "GET-запрос работает некорректно");
        assertEquals(user.getLogin(), addUser.getLogin(), "POST-запрос работает некорректно");
    }

    @Test
    void testByPostWithEmptyName() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .birthday(LocalDate.now())
                .build();
        User addUser = userController.createUser(user);
        Collection<User> listUsers = userController.findAllUsers();
        assertEquals(1, listUsers.size(), "GET-запрос работает некорректно");
        assertEquals(user.getLogin(), addUser.getName(), "Пустому полю имени должно присваиваться значение логина");
    }

    @Test
    void testByPostWithIsBlankName() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .name("  ")
                .birthday(LocalDate.now())
                .build();
        User addUser = userController.createUser(user);
        Collection<User> listUsers = userController.findAllUsers();
        assertEquals(1, listUsers.size(), "GET-запрос работает некорректно");
        assertEquals(user.getLogin(), addUser.getName(),
                "Полю имени, состоящему только из пробелов, должно присваиваться значение логина");
    }

    @Test
    void testExceptionByPutNonexistentUser() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        User addUser = userController.createUser(user);
        addUser.setId(2L);
        Assertions.assertThrows(NotFoundException.class, () -> userController.updateUser(addUser),
                "Обновление несуществующего пользователя должно привести к исключению");
    }

    @Test
    void testByPutValidUser() {
        User user = User.builder()
                .email("ya@ya.ru")
                .login("login")
                .name("Test Name")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        User addUser = userController.createUser(user);
        Collection<User> listUsers = userController.findAllUsers();
        assertEquals(1, listUsers.size(), "POST-запрос работает некорректно");
        addUser.setLogin("newlogin");
        User updUser = userController.updateUser(addUser);
        Collection<User> updListUsers = userController.findAllUsers();
        assertEquals(1, updListUsers.size(),
                "После PUT-запроса размер списка пользователей должен остаться равным 1");
        assertNotEquals("login", updUser.getLogin(),
                "Логины добавленного и обновленного пользователя должны отличаться");
    }

}