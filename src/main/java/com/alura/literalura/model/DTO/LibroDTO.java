package com.alura.literalura.model.DTO;

import com.alura.literalura.model.DTO.AutorDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record LibroDTO(
        @JsonAlias("id") long id,
        @JsonAlias("title") String titulo,
        @JsonAlias("authors") List<AutorDTO> autores,
        @JsonAlias("languages") List<String> idiomas,
        @JsonAlias("copyright") String copyright,
        @JsonAlias("download_count") Integer descargas) {
}