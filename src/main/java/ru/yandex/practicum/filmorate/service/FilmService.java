package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private static final int MAX_NAME_LENGTH = 200;

    public Film createFilm(Film film) {
        log.info("Проверяем выполнение необходимых условий");
        validate(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Проверяем выполнение необходимых условий");
        validate(film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Collection<Film> getPopularFilms(int size) {
        return filmStorage.getPopularFilms(size);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film deleteFilm(Long id) {
        return filmStorage.deleteFilm(id);
    }

    public Film likeIt(Long filmId, Long userId) {
        return filmStorage.likeIt(filmId, userId);
    }

    public Film deleteLike(Long filmId, Long userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Mаксимальная длина описания — 200 символов, а у вас: "
                    + (film.getDescription() != null ? film.getDescription().length() : 0));
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года, а у вас : "
                    + film.getReleaseDate());
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
