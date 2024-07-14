package com.alura.literalura.principal;

import com.alura.literalura.model.DTO.*;
import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
private Scanner teclado = new Scanner(System.in);
private ConsumoAPI consumoAPI = new ConsumoAPI();
private ConvierteDatos conversor = new ConvierteDatos();
private String URL_BASE = "https://gutendex.com/books/";
private AutorRepository repository;

public Principal(AutorRepository repository){
    this.repository = repository;
}


public void mostrarMenu() {
    var opcion = -1;
    var menu = """
            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            ⭐ ─ ✰ ──『 Bienvenido(a) a Literalura 』── ✰ ─ ⭐
            _____________________________________________
				MENU
            _____________________________________________
            1 - Buscar Libros por TÍtulo
            2 - Buscar Autor por Nombre
            3 - Listar Libros Registrados
            4 - Listar Autores Registrados
            5 - Listar Autores Vivos
            6 - Listar Libros por Idioma
            7 - Listar Autores por Año
            8 - Top 10 Libros más Buscados
            9 - Generar Estadísticas
            _____________________________________________
            0 - SALIR
            _____________________________________________
            Elija una opción:
            """;

    while (opcion != 0) {
        System.out.println(menu);
        try {
            opcion = Integer.valueOf(teclado.nextLine());
            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    buscarAutorPorNombre();
                    break;
                case 3:
                    listarLibrosRegistrados();
                    break;
                case 4:
                    listarAutoresRegistrados();
                    break;
                case 5:
                    listarAutoresVivos();
                    break;
                case 6:
                    listarLibrosPorIdioma();
                    break;
                case 7:
                    listarAutoresPorAnio();
                    break;
                case 8:
                    top10Libros();
                    break;
                case 9:
                    generarEstadisticas();
                    break;
                case 0:
                    System.out.println("Gracias por utilizar Literalura ✔\uFE0F");
                    System.out.println("Cerrando la aplicacion Literalura \uD83D\uDCD3 ...");
                    break;
                default:
                    System.out.println("Opción no válida!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());

        }
    }
}
public void buscarLibroPorTitulo() {
    System.out.println("""
            ______________________________
               BUSCAR LIBROS POR TÍTULO 
            ______________________________
             """);
    System.out.println("Introduzca el nombre del libro que desea buscar:");
    var nombre = teclado.nextLine();
    var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

    // Check if JSON is empty
    if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
        var datos = conversor.obtenerDatos(json, StatisticsDTO.class);

        // Process valid data
        Optional<LibroDTO> libroBuscado = datos.libros().stream()
                .findFirst();
        if (libroBuscado.isPresent()) {
            System.out.println(
                    "\n------------- LIBRO \uD83D\uDCD9  --------------" +
                            "\nTítulo: " + libroBuscado.get().titulo() +
                            "\nAutor: " + libroBuscado.get().autores().stream()
                            .map(a -> a.nombre()).limit(1).collect(Collectors.joining()) +
                            "\nIdioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                            "\nNúmero de descargas: " + libroBuscado.get().descargas() +
                            "\n_____________________________________________\n"
            );

            try {
                List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
                Autor autorAPI = libroBuscado.stream().
                        flatMap(l -> l.autores().stream()
                                .map(a -> new Autor(a)))
                        .collect(Collectors.toList()).stream().findFirst().get();
                Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                        .map(a -> a.nombre())
                        .collect(Collectors.joining()));
                Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
                if (libroOptional.isPresent()) {
                    System.out.println("El libro ya está guardado en la BD.");
                } else {
                    Autor autor;
                    if (autorBD.isPresent()) {
                        autor = autorBD.get();
                        System.out.println("EL autor ya esta guardado en la BD");
                    } else {
                        autor = autorAPI;
                        repository.save(autor);
                    }
                    autor.setLibros(libroEncontrado);
                    repository.save(autor);
                }
            } catch (Exception e) {
                System.out.println("Warning! " + e.getMessage());
            }
        } else {
            System.out.println("Libro no encontrado!");
        }
    }
}

    public void buscarAutorPorNombre () {
            System.out.println("""
                    ___________________________
                      BUSCAR AUTOR POR NOMBRE 
                    ___________________________
                    """);
            System.out.println("Ingrese el nombre del autor:");
            var nombre = teclado.nextLine();
            Optional<Autor> autor = repository.buscarAutorPorNombre(nombre);
            if (autor.isPresent()) {
                System.out.println(
                        "\nAutor: " + autor.get().getNombre() +
                                "\nFecha de Nacimiento: " + autor.get().getNacimiento() +
                                "\nFecha de Fallecimiento: " + autor.get().getFallecimiento() +
                                "\nLibros: " + autor.get().getLibros().stream()
                                .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                );
            } else {
                System.out.println("El autor no existe en la BD");
            }
        }

        public void listarLibrosRegistrados () {
            System.out.println("""
                    _____________________________
                      LISTAR LIBROS REGISTRADOS 
                    _____________________________
                     """);
            List<Libro> libros = repository.buscarTodosLosLibros();
            libros.forEach(l -> System.out.println(
                    "-------------- LIBRO \uD83D\uDCD9  -----------------" +
                            "\nTítulo: " + l.getTitulo() +
                            "\nAutor: " + l.getAutor().getNombre() +
                            "\nIdioma: " + l.getIdioma().getIdioma() +
                            "\nNúmero de descargas: " + l.getDescargas() +
                            "\n----------------------------------------\n"
            ));
        }

        public void listarAutoresRegistrados () {
            System.out.println("""
                    _________________________________
                      LISTAR AUTORES REGISTRADOS  
                    _________________________________
                     """);
            List<Autor> autores = repository.findAll();
            System.out.println();
            autores.forEach(l -> System.out.println(
                    "Autor: " + l.getNombre() +
                            "\nFecha de Nacimiento: " + l.getNacimiento() +
                            "\nFecha de Fallecimiento: " + l.getFallecimiento() +
                            "\nLibros: " + l.getLibros().stream()
                            .map(t -> t.getTitulo()).collect(Collectors.toList()) + "\n"
            ));
        }

        public void listarAutoresVivos () {
            System.out.println("""
                    ___________________________
                       LISTAR AUTORES VIVOS 
                    ___________________________
                     """);
            System.out.println("Consultar Año:");
            try {
                var fecha = Integer.valueOf(teclado.nextLine());
                List<Autor> autores = repository.buscarAutoresVivos(fecha);
                if (!autores.isEmpty()) {
                    System.out.println();
                    autores.forEach(a -> System.out.println(
                            "Autor: " + a.getNombre() +
                                    "\nFecha de Nacimiento: " + a.getNacimiento() +
                                    "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                                    "\nLibros: " + a.getLibros().stream()
                                    .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                    ));
                } else {
                    System.out.println("No existen autores vivos para el año consultado");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un año válido " + e.getMessage());
            }
        }

    public void listarLibrosPorIdioma() {
        System.out.println("""
                ____________________________
                  LISTAR LIBROS POR IDIOMA 
                ____________________________
                """);
        var menu = """
                    ___________________________________________________
                    Seleccione el idioma del libro que desea encontrar:
                    ___________________________________________________
                    1 - Español
                    2 - Francés
                    3 - Inglés
                    4 - Portugués
                    ___________________________________________________
                    """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1:
                    buscarLibrosPorIdioma("es");
                    break;
                case 2:
                    buscarLibrosPorIdioma("fr");
                    break;
                case 3:
                    buscarLibrosPorIdioma("en");
                    break;
                case 4:
                    buscarLibrosPorIdioma("pt");
                    break;
                default:
                    System.out.println("Opción inválida!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción no válida: " + e.getMessage());
        }
    }

    private void buscarLibrosPorIdioma(String idioma) {
        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repository.buscarLibrosPorIdioma(idiomaEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados en ese idioma");
            } else {
                System.out.println();
                libros.forEach(l -> System.out.println(
                        "----------- LIBRO \uD83D\uDCD9  --------------" +
                                "\nTítulo: " + l.getTitulo() +
                                "\nAutor: " + l.getAutor().getNombre() +
                                "\nIdioma: " + l.getIdioma().getIdioma() +
                                "\nNúmero de descargas: " + l.getDescargas() +
                                "\n----------------------------------------\n"
                ));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Introduce un idioma válido en el formato especificado.");
        }
    }

        public void listarAutoresPorAnio () {
            System.out.println("""
                    __________________________
                      LISTAR AUTORES POR AÑO 
                    __________________________
                     """);
            var menu = """
                    __________________________________________
                    Ingresa una opción para listar los autores
                    __________________________________________
                    1 - Listar autor por Año de Nacimiento
                    2 - Listar autor por año de Fallecimiento
                    __________________________________________
                    """;
            System.out.println(menu);
            try {
                var opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        ListarAutoresPorNacimiento();
                        break;
                    case 2:
                        ListarAutoresPorFallecimiento();
                        break;
                    default:
                        System.out.println("Opción inválida!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: " + e.getMessage());
            }
        }

        public void ListarAutoresPorNacimiento () {
            System.out.println("""
                    __________________________________________
                      BUSCAR AUTOR POR SU AÑO DE NACIMIENTO 
                    __________________________________________
                    """);
            System.out.println("Consultar año de nacimiento:");
            try {
                var nacimiento = Integer.valueOf(teclado.nextLine());
                List<Autor> autores = repository.listarAutoresPorNacimiento(nacimiento);
                if (autores.isEmpty()) {
                    System.out.println("No existen autores con año de nacimiento igual a " + nacimiento);
                } else {
                    System.out.println();
                    autores.forEach(a -> System.out.println(
                            "Autor: " + a.getNombre() +
                                    "\nFecha de Nacimiento: " + a.getNacimiento() +
                                    "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                                    "\nLibros: " + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                    ));
                }
            } catch (NumberFormatException e) {
                System.out.println("Año no válido: " + e.getMessage());
            }
        }

        public void ListarAutoresPorFallecimiento () {
            System.out.println("""
                    __________________________________________________________
                     📖  BUSCAR LIBROS POR AÑO DE FALLECIMIENTO DEL AUTOR 📖
                    __________________________________________________________
                     """);
            System.out.println("Consultar año de fallecimiento:");
            try {
                var fallecimiento = Integer.valueOf(teclado.nextLine());
                List<Autor> autores = repository.listarAutoresPorFallecimiento(fallecimiento);
                if (autores.isEmpty()) {
                    System.out.println("No existen autores con año de fallecimiento igual a " + fallecimiento);
                } else {
                    System.out.println();
                    autores.forEach(a -> System.out.println(
                            "Autor: " + a.getNombre() +
                                    "\nFecha de Nacimiento: " + a.getNacimiento() +
                                    "\nFecha de Fallecimeinto: " + a.getFallecimiento() +
                                    "\nLibros: " + a.getLibros().stream().map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                    ));
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción no válida: " + e.getMessage());
            }
        }
        public void top10Libros () {
            System.out.println("""
                    _______________________________
                        TOP 10 LIBROS MÁS BUSCADOS 
                    _______________________________
                     """);
            List<Libro> libros = repository.top10Libros();
            System.out.println();
            libros.forEach(l -> System.out.println(
                    "----------------- LIBRO \uD83D\uDCD9  ----------------" +
                            "\nTítulo: " + l.getTitulo() +
                            "\nAutor: " + l.getAutor().getNombre() +
                            "\nIdioma: " + l.getIdioma().getIdioma() +
                            "\nNúmero de descargas: " + l.getDescargas() +
                            "\n-------------------------------------------\n"
            ));
        }
        public void generarEstadisticas () {
            System.out.println("""
                    ___________________________
                     📊 GENERAR ESTADÍSTICAS 📊
                    ___________________________
                     """);
            var json = consumoAPI.obtenerDatos(URL_BASE);
            var datos = conversor.obtenerDatos(json, StatisticsDTO.class);
            IntSummaryStatistics est = datos.libros().stream()
                    .filter(l -> l.descargas() > 0)
                    .collect(Collectors.summarizingInt(LibroDTO::descargas));
            Integer media = (int) est.getAverage();
            System.out.println("\n--------- ESTADÍSTICAS \uD83D\uDCCA ------------");
            System.out.println("Media de descargas: " + media);
            System.out.println("Máxima de descargas: " + est.getMax());
            System.out.println("Mínima de descargas: " + est.getMin());
            System.out.println("Total registros para calcular las estadísticas: " + est.getCount());
            System.out.println("---------------------------------------------------\n");
        }
    }
