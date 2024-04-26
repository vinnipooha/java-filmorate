package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    User getUserById(Long id);

    User deleteUser(Long id);

    Collection<User> getAllUsers();

    User addFriend(Long id, Long friendId);

    User deleteFriend(Long id, Long friendId);

    Collection<User> getCommonFriends(Long id, Long otherId);

    Collection<User> getUserFriends(Long id);

}
