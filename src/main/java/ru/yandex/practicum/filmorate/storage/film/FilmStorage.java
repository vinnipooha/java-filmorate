package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(Long id);

    Film deleteFilm(Long id);

    Collection<Film> getAllFilms();

    Film likeIt(Long filmId, Long userId);

    Film deleteLike(Long filmId, Long userId);

    Collection<Film> getPopularFilms(int size);


}
