package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAllUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.trace("Проверяем выполнение необходимых условий");
        validation(user);
        log.trace("Получаем id пользователя");
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Новый пользователь сохранён в памяти приложения id={}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.trace("Проверяем выполнение необходимых условий");
        validation(user);
        if (user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.trace("Проверяем по id наличие пользователя в памяти приложения");
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Пользователь обновлён в памяти приложения id={}", user.getId());
            return user;
        }
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    void validation(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна быть указана");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
