package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectStructureException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> usersStorage = new HashMap<>();

    @Override
    public User addUser(User user) {
        log.trace("Получаем id пользователя login={}", user.getLogin());
        user.setId(getNextId());
        usersStorage.put(user.getId(), user);
        log.info("Новый пользователь сохранён в памяти приложения id={}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == 0) {
            throw new IncorrectObjectStructureException("Id пользователя не указан.");
        }
        log.info("Проверяем по id наличие пользователя в памяти приложения");
        validateId(user.getId());
        usersStorage.put(user.getId(), user);
        log.info("Данные пользователя обновлены в памяти приложения id={}", user.getId());
        return user;
    }

    @Override
    public User getUserById(Long id) {
        validateId(id);
        return usersStorage.get(id);
    }

    @Override
    public User deleteUser(Long id) {
        validateId(id);
        User userForDelete = usersStorage.get(id);
        usersStorage.remove(id);
        return userForDelete;
    }

    @Override
    public Collection<User> getAllUsers() {
        return usersStorage.values();
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        log.info("Проверяем по id наличие пользователей в памяти приложения");
        validateId(userId);
        validateId(friendId);
        usersStorage.get(userId).getFriends().add(friendId);
        log.info("Добавили в список друзей пользователя с id = {}", friendId);
        usersStorage.get(friendId).getFriends().add(userId);
        log.info("Провели симметричное добавление в список друзей");
        return getUserById(userId);
    }

    @Override
    public User deleteFriend(Long userId, Long friendId) {
        validateId(userId);
        validateId(friendId);
        User user = usersStorage.get(userId);
        if (!user.getFriends().contains(friendId)) {
            return user;
        }
        user.getFriends().remove(friendId);
        log.info("Удалили из списка друзей пользователя с id = {}", friendId);
        usersStorage.get(friendId).getFriends().remove(userId);
        log.info("Провели симметричное удаление из списка друзей");
        return user;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        validateId(userId);
        validateId(otherId);
        Set<Long> userFriends = getUserById(userId).getFriends();
        Set<Long> otherUserFriends = getUserById(otherId).getFriends();
        return userFriends.stream().filter(otherUserFriends::contains)
                .map(this::getUserById).collect(Collectors.toList());
    }

    @Override
    public Collection<User> getUserFriends(Long userId) {
        validateId(userId);
        return getUserById(userId).getFriends().stream().map(this::getUserById).collect(Collectors.toList());
    }

    private Long getNextId() {
        long currentMaxId = usersStorage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    protected void validateId(Long id) {
        if (!usersStorage.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }
}
