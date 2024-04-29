package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {
    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    public void beforeEach() {
        UserStorage userStorage = new InMemoryUserStorage();
        FilmStorage filmStorage = new InMemoryFilmStorage(userStorage);
        filmController = new FilmController(new FilmService(filmStorage));
        userController = new UserController(new UserService(userStorage));
    }

    @Test
    void postAndGetValidFilm() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        assertNotNull(addFilm.getLikes());
        Collection<Film> listFilms = filmController.findAllFilms();
        assertEquals(1, listFilms.size(), "GET-запрос работает некорректно");
        assertEquals(1, addFilm.getId(), "Генерация id работает некорректно");
        assertEquals(film.getName(), addFilm.getName(), "POST-запрос работает некорректно");
    }

    @Test
    void getPopularFilms() {
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
        User user3 = User.builder()
                .email("test3@ya.ru")
                .login("login3")
                .name("Test Name3")
                .birthday(LocalDate.of(1982, 1, 1))
                .build();
        User addUser1 = userController.createUser(user1);
        User addUser2 = userController.createUser(user2);
        User addUser3 = userController.createUser(user3);
        Film film1 = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film film2 = Film.builder()
                .name("Film2")
                .description("D_film2")
                .releaseDate(LocalDate.of(2020, 2, 1))
                .duration(60)
                .build();
        Film film3 = Film.builder()
                .name("Film3")
                .description("D_film3")
                .releaseDate(LocalDate.of(2020, 3, 1))
                .duration(60)
                .build();
        assertTrue(filmController.getPopularFilms(10).isEmpty(),
                "Список популярных фильмов должен быть пустым");
        filmController.createFilm(film1);
        filmController.createFilm(film2);
        filmController.createFilm(film3);

        assertEquals(3, filmController.findAllFilms().size(),
                "Размер списка фильмов должен быть равен 3");

        filmController.like(1L, 1L);
        filmController.like(1L, 2L);
        filmController.like(1L, 3L);
        filmController.like(2L, 1L);
        filmController.like(2L, 2L);
        filmController.like(3L, 1L);
        assertEquals(3, filmController.getPopularFilms(1000).size(),
                "Размер списка фильмов должен быть равен 3");
        assertEquals(2, filmController.getPopularFilms(2).size(),
                "Размер списка фильмов должен быть равен 2");
        assertFalse(filmController.getPopularFilms(2).contains(film3),
                "Список не должен содержать film3");
    }

    @Test
    void testExceptionByPostWithoutName() {
        Film film = Film.builder()
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма без названия должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithEmptyName() {
        Film film = Film.builder()
                .name("")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с пустым названием должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithIsBlankName() {
        Film film = Film.builder()
                .name("  ")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с названием, состоящим только из пробелов, должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithDescriptionSizeMoreThan200() {
        Film film = Film.builder()
                .name("Film1")
                .description("Ну ооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооооочень " +
                        "подробное описание фильма, в котором рассказывается обо всем очень детально, " +
                        "раскрываются все секреты, совсем все-все-все и обо всех   ")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с описанием более 200 знаков должна привести к исключению");
    }

    @Test
    void testByPostWithDescriptionSize200() {
        Film film = Film.builder()
                .name("Film1")
                .description("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaa")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        assertEquals(film.getName(), addFilm.getName(), "POST-запрос работает некорректно");
    }

    @Test
    void testByPostWithDescriptionSize199() {
        Film film = Film.builder()
                .name("Film1")
                .description("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaa")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        assertEquals(film.getName(), addFilm.getName(), "POST-запрос работает некорректно");
    }

    @Test
    void testExceptionByPostWithInvalidDate() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(60)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с датой релиза ранее 28.12.1895г. должна привести к исключению");
    }

    @Test
    void testByPostWithDateOf28December1895() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        assertEquals(film.getName(), addFilm.getName(), "POST-запрос работает некорректно");
    }

    @Test
    void testExceptionByPostWithNegativeDuration() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(-1)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с отрицательной продолжительностью должна привести к исключению");
    }

    @Test
    void testExceptionByPostWithZeroDuration() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(0)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmController.createFilm(film),
                "Загрузка фильма с нулевой продолжительностью должна привести к исключению");
    }

    @Test
    void testExceptionByPutNonexistentFilm() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        addFilm.setId(2L);
        Assertions.assertThrows(NotFoundException.class, () -> filmController.updateFilm(addFilm),
                "Обновление несуществующего фильма должно привести к исключению");
    }

    @Test
    void testExceptionByPutValidFilm() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        Collection<Film> listFilms = filmController.findAllFilms();
        assertEquals(1, listFilms.size(), "POST-запрос работает некорректно");
        addFilm.setName("updFilm1");
        Film updFilm = filmController.updateFilm(addFilm);
        Collection<Film> updListFilms = filmController.findAllFilms();
        assertEquals(1, updListFilms.size(),
                "После PUT-запроса размер списка фильмов должен остаться равным 1");
        assertNotEquals("Film1", updFilm.getName(),
                "Логины добавленного и обновленного пользователя должны отличаться");
    }

    @Test
    void testExceptionByPutWithBlankName() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        addFilm.setName("  ");
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(addFilm),
                "Обновление названия фильма на строку, состоящую только из пробелов, должно привести к исключению");
    }

    @Test
    void testExceptionByPutWithInvalidDate() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        addFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(addFilm),
                "Обновление даты релиза на дату ранее 28.12.1895 г. должно привести к исключению");
    }

    @Test
    void testExceptionByPutWitNegativeDuration() {
        Film film = Film.builder()
                .name("Film1")
                .description("D_film1")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(60)
                .build();
        Film addFilm = filmController.createFilm(film);
        addFilm.setDuration(-1);
        Assertions.assertThrows(ValidationException.class, () -> filmController.updateFilm(addFilm),
                "Обновление продолжительности фильма на отрицательное значение должно привести к исключению");
    }
}
