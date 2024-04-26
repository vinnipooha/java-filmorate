package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectObjectStructureException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> filmsStorage = new HashMap<>();
    private final UserStorage userStorage;

    @Override
    public Film addFilm(Film film) {
        log.trace("Получаем id фильма name={}", film.getName());
        film.setId(getNextId());
        filmsStorage.put(film.getId(), film);
        log.info("Новый фильм сохранён в памяти приложения id={}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new IncorrectObjectStructureException("Id фильма не указан.");
        }
        log.info("Проверяем по id наличие фильма в памяти приложения");
        validateId(film.getId());
        filmsStorage.put(film.getId(), film);
        log.info("Фильм обновлён в памяти приложения id={}", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(Long id) {
        validateId(id);
        return filmsStorage.get(id);
    }

    @Override
    public Film deleteFilm(Long id) {
        validateId(id);
        Film filmForDelete = filmsStorage.get(id);
        filmsStorage.remove(id);
        return filmForDelete;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return filmsStorage.values();
    }

    @Override
    public Film likeIt(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);
        film.getLikes().add(userId);
        return film;
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userStorage.getUserById(userId);
        film.getLikes().remove(userId);
        return film;
    }

    @Override
    public Collection<Film> getPopularFilms(int size) {
        Comparator<Film> comparator = Comparator.comparing(x -> x.getLikes().size());
        comparator = comparator.reversed();
        return getAllFilms().stream().filter(film -> !film.getLikes().isEmpty())
                .sorted(comparator).limit(size).collect(Collectors.toList());
    }

    private long getNextId() {
        long currentMaxId = filmsStorage.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    protected void validateId(Long id) {
        if (!filmsStorage.containsKey(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
    }
}
