package com.alura.literalura.model.DTO;

import com.alura.literalura.model.DTO.LibroDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record StatisticsDTO(
        @JsonAlias("count") Integer total,
        @JsonAlias("results") List<LibroDTO> libros) {
}