package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
@Data
@AllArgsConstructor
public class Genre {
    @NotBlank
    private int id;
    @NotBlank
    private String name;
}