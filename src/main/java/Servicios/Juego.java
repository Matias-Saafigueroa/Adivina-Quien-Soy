package Servicios;

import DataSystem.GuardarMarcadorTXT;
import DataSystem.RepositorioPersonajes;
import Modelos.ColorPelo;
import Modelos.Filtro;
import Modelos.Genero;
import Modelos.Personaje;
import Modelos.RegistroMarcador;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Motor del juego. Coordina dos modalidades: jugador contra las dos máquinas
 * en secuencia, y máquina contra máquina en modo espectador.
 *
 * El personaje elegido por el jugador se guarda en una variable local de este
 * motor: las máquinas nunca reciben una referencia a él.
 */
public class Juego {

    private final RepositorioPersonajes repositorioPersonajes;
    private final GuardarMarcadorTXT marcador;
    private final Scanner teclado;

    public Juego(String archivoPersonajes, String archivoMarcador, Scanner teclado) {
        this.repositorioPersonajes = new RepositorioPersonajes(archivoPersonajes);
        this.marcador = new GuardarMarcadorTXT(archivoMarcador);
        this.teclado = teclado;
    }

    // ---------------------------------------------------------------
    // Humano vs las dos máquinas (en secuencia, mismo personaje elegido)
    // ---------------------------------------------------------------

    /**
     * El jugador elige su personaje una única vez y lo mantiene durante todo
     * el desafío. Primero enfrenta a Máquina 1; si le gana, pasa
     * automáticamente a Máquina 2, que arranca heredando las preguntas y
     * respuestas que Máquina 1 ya obtuvo sobre ese mismo personaje (por eso
     * nunca las repite y empieza con el espacio de búsqueda ya recortado).
     * Sólo se registra una victoria en el marcador si le gana a las dos.
     */
    public void jugarContraLasMaquinas(String nombreUsuario) {
        List<Personaje> personajes = repositorioPersonajes.cargar();
        Personaje secretoHumano = elegirPersonajeHumano(personajes);

        MaquinaBasica maquina1 = new MaquinaBasica(personajes);
        maquina1.elegirSecreto(List.of(secretoHumano));
        System.out.println("\n=== Ronda 1: contra " + maquina1.getNombre() + " ===");
        System.out.println(maquina1.getNombre() + " ya eligió su personaje en secreto. ¡Empieza el duelo!");
        System.out.println("Ojo: si arriesgás una adivinanza directa y le errás, perdés el desafío en el acto.");

        List<PreguntaRespuesta> historialMaquina1 = new ArrayList<>();
        ResultadoEtapa etapa1 = jugarEtapa(personajes, secretoHumano, maquina1, historialMaquina1);

        if (!etapa1.humanoGanoLaEtapa()) {
            mostrarResultadoDerrota(etapa1, maquina1, secretoHumano);
            marcador.sumarDerrota(nombreUsuario);
            mostrarMarcador();
            return;
        }

        System.out.println("\n¡Le ganaste a " + maquina1.getNombre() + " en " + etapa1.turnos() + " intento(s)!");
        marcador.sumarVictoriaMaquina1(nombreUsuario);

        MaquinaAvanzada maquina2 = new MaquinaAvanzada(personajes);
        maquina2.elegirSecreto(List.of(secretoHumano, maquina1.getSecreto()));
        for (PreguntaRespuesta pr : historialMaquina1) {
            maquina2.registrarRespuesta(pr.pregunta(), pr.respuesta());
        }

        System.out.println("\n=== Ronda 2: contra " + maquina2.getNombre() + " ===");
        System.out.println(maquina2.getNombre() + " ya escuchó las " + historialMaquina1.size()
                + " pregunta(s) que te hizo " + maquina1.getNombre() + ", así que arranca sabiendo eso y no las repite.");

        ResultadoEtapa etapa2 = jugarEtapa(personajes, secretoHumano, maquina2, null);
        int intentosTotales = etapa1.turnos() + etapa2.turnos();

        if (etapa2.humanoGanoLaEtapa()) {
            System.out.println("\n¡Ganaste el desafío completo, " + nombreUsuario + "! En total te llevó " + intentosTotales + " intento(s).");
            marcador.sumarVictoriaCompleta(nombreUsuario, intentosTotales);
        } else {
            mostrarResultadoDerrota(etapa2, maquina2, secretoHumano);
            marcador.sumarDerrota(nombreUsuario);
        }

        mostrarMarcador();
    }

    private void mostrarResultadoDerrota(ResultadoEtapa etapa, MaquinaJugadora maquina, Personaje secretoHumano) {
        switch (etapa.fin()) {
            case HUMANO_SE_RINDIO -> System.out.println("\nTe rendiste. El personaje de " + maquina.getNombre()
                    + " era " + maquina.getSecreto().getNombre() + ".");
            case HUMANO_FALLO_ADIVINANZA -> System.out.println("\nArriesgaste una adivinanza y le erraste: ¡perdiste el desafío de una!"
                    + " El personaje de " + maquina.getNombre() + " era " + maquina.getSecreto().getNombre() + ".");
            case MAQUINA_GANO -> System.out.println("\n" + maquina.getNombre() + " adivinó tu personaje ("
                    + secretoHumano.getNombre() + "). ¡Perdiste el desafío!");
            default -> throw new IllegalStateException("Resultado inesperado: " + etapa.fin());
        }
    }

    /** Cómo terminó una ronda: quién ganó, o quién arriesgó una adivinanza y falló (pierde en el acto). */
    private enum FinDeEtapa { HUMANO_GANO, HUMANO_SE_RINDIO, HUMANO_FALLO_ADIVINANZA, MAQUINA_GANO, MAQUINA_FALLO_ADIVINANZA }

    private record ResultadoEtapa(FinDeEtapa fin, int turnos) {
        boolean humanoGanoLaEtapa() {
            return fin == FinDeEtapa.HUMANO_GANO || fin == FinDeEtapa.MAQUINA_FALLO_ADIVINANZA;
        }
    }

    private enum ResultadoTurnoHumano { GANO, RINDIO, FALLO_ADIVINANZA, SIGUE }

    private enum ResultadoTurnoMaquina { MAQUINA_GANO, MAQUINA_FALLO_ADIVINANZA, SIGUE }

    private ResultadoEtapa jugarEtapa(List<Personaje> personajes, Personaje secretoHumano, MaquinaJugadora maquina,
                                       List<PreguntaRespuesta> historialParaRegistrar) {
        Set<String> preguntasHumanoHechas = new HashSet<>();
        List<Personaje> candidatosHumano = new ArrayList<>(personajes);
        candidatosHumano.removeIf(p -> p.getId() == secretoHumano.getId());

        int turno = 1;
        while (true) {
            System.out.println("\n--- Turno " + turno + " ---");

            ResultadoTurnoHumano resultadoHumano = turnoHumano(secretoHumano, maquina, candidatosHumano, preguntasHumanoHechas);
            switch (resultadoHumano) {
                case GANO -> {
                    return new ResultadoEtapa(FinDeEtapa.HUMANO_GANO, turno);
                }
                case RINDIO -> {
                    return new ResultadoEtapa(FinDeEtapa.HUMANO_SE_RINDIO, turno);
                }
                case FALLO_ADIVINANZA -> {
                    return new ResultadoEtapa(FinDeEtapa.HUMANO_FALLO_ADIVINANZA, turno);
                }
                case SIGUE -> {
                }
            }

            ResultadoTurnoMaquina resultadoMaquina = turnoMaquina(maquina, secretoHumano, historialParaRegistrar);
            switch (resultadoMaquina) {
                case MAQUINA_GANO -> {
                    return new ResultadoEtapa(FinDeEtapa.MAQUINA_GANO, turno);
                }
                case MAQUINA_FALLO_ADIVINANZA -> {
                    return new ResultadoEtapa(FinDeEtapa.MAQUINA_FALLO_ADIVINANZA, turno);
                }
                case SIGUE -> {
                }
            }

            pausa("(Enter para ver el turno siguiente) ");
            turno++;
        }
    }

    private Personaje elegirPersonajeHumano(List<Personaje> personajes) {
        Personaje elegido = null;
        while (elegido == null) {
            mostrarPersonajes(personajes);
            System.out.print("Elegí tu personaje secreto por id (no vas a poder cambiarlo): ");
            int id = leerEntero();
            elegido = buscarPorId(personajes, id);
            if (elegido == null) {
                System.out.println("Ese id no existe, probá de nuevo.");
            }
        }
        System.out.println("Tu personaje secreto es " + elegido.getNombre() + ". ¡Que no se entere la máquina!");
        return elegido;
    }

    private ResultadoTurnoHumano turnoHumano(Personaje secretoHumano, MaquinaJugadora maquina,
                                              List<Personaje> candidatosHumano, Set<String> preguntasHechas) {
        System.out.println("\nTu turno. Personajes que todavía podrían ser el secreto de " + maquina.getNombre() + ":");
        mostrarPersonajes(candidatosHumano);
        System.out.println("(" + candidatosHumano.size() + " personaje(s) posible(s))");
        System.out.println("1) Preguntar por un filtro   2) Adivinar directamente   3) Rendirme");
        System.out.print("Elegí una opción: ");
        int opcion = leerEntero();

        if (opcion == 2) {
            System.out.print("¿Quién creés que es? Ingresá el id (¡si le errás, perdés de una!): ");
            int idAdivinado = leerEntero();
            if (maquina.getSecreto().getId() == idAdivinado) {
                return ResultadoTurnoHumano.GANO;
            }
            System.out.println("No, no es ese personaje.");
            return ResultadoTurnoHumano.FALLO_ADIVINANZA;
        }

        if (opcion == 3) {
            return ResultadoTurnoHumano.RINDIO;
        }

        Pregunta pregunta = pedirPreguntaAlHumano(preguntasHechas);
        boolean respuesta = maquina.responder(pregunta);
        System.out.println("Le preguntás a " + maquina.getNombre() + ": ¿su personaje " + pregunta.getClausula() + "? → " + (respuesta ? "Sí" : "No"));
        preguntasHechas.add(pregunta.getClave());
        candidatosHumano.removeIf(p -> pregunta.evaluar(p) != respuesta);
        System.out.println("Con esa pista, te quedan " + candidatosHumano.size() + " personajes posibles.");
        return ResultadoTurnoHumano.SIGUE;
    }


    private ResultadoTurnoMaquina turnoMaquina(MaquinaJugadora maquina, Personaje secretoHumano, List<PreguntaRespuesta> historialParaRegistrar) {
        System.out.println(maquina.getNombre() + " está pensando...");

        if (maquina.debeArriesgar()) {
            Personaje adivinanza = maquina.elegirAdivinanza();
            System.out.println(maquina.getNombre() + " arriesga: ¿sos " + adivinanza.getNombre() + "?");
            if (adivinanza.getId() == secretoHumano.getId()) {
                return ResultadoTurnoMaquina.MAQUINA_GANO;
            }
            System.out.println("No, falló su intento. ¡Eso le hace perder el desafío a " + maquina.getNombre() + "!");
            return ResultadoTurnoMaquina.MAQUINA_FALLO_ADIVINANZA;
        }

        Pregunta pregunta = maquina.elegirPregunta();
        if (pregunta == null) {
            Personaje adivinanza = maquina.elegirAdivinanza();
            System.out.println(maquina.getNombre() + " ya no tiene más preguntas útiles y arriesga: ¿sos " + adivinanza.getNombre() + "?");
            if (adivinanza.getId() == secretoHumano.getId()) {
                return ResultadoTurnoMaquina.MAQUINA_GANO;
            }
            System.out.println("No, falló su intento. ¡Eso le hace perder el desafío a " + maquina.getNombre() + "!");
            return ResultadoTurnoMaquina.MAQUINA_FALLO_ADIVINANZA;
        }

        boolean respuesta = pregunta.evaluar(secretoHumano);
        System.out.println(maquina.getNombre() + " pregunta: " + pregunta.getDescripcion() + " → " + (respuesta ? "Sí" : "No"));
        maquina.registrarRespuesta(pregunta, respuesta);
        if (historialParaRegistrar != null) {
            historialParaRegistrar.add(new PreguntaRespuesta(pregunta, respuesta));
        }
        return ResultadoTurnoMaquina.SIGUE;
    }

    private Pregunta pedirPreguntaAlHumano(Set<String> preguntasHechas) {
        while (true) {
            System.out.println("Filtros: 1) Género  2) Calvicie  3) Lentes  4) Color de pelo");
            System.out.print("Elegí sobre qué filtro querés preguntarle a la máquina: ");
            int filtroOpcion = leerEntero();

            Pregunta pregunta = switch (filtroOpcion) {
                case 1 -> new Pregunta(Filtro.GENERO, pedirValorGenero());
                case 2 -> new Pregunta(Filtro.CALVICIE, Boolean.TRUE);
                case 3 -> new Pregunta(Filtro.LENTES, Boolean.TRUE);
                case 4 -> new Pregunta(Filtro.COLOR_PELO, pedirValorColorPelo());
                default -> null;
            };

            if (pregunta == null) {
                System.out.println("Opción inválida.");
                continue;
            }
            if (preguntasHechas.contains(pregunta.getClave())) {
                System.out.println("Ya preguntaste eso, probá con otro filtro.");
                continue;
            }
            return pregunta;
        }
    }

    private Genero pedirValorGenero() {
        System.out.print("¿Sobre qué género querés preguntar? (m = masculino / f = femenino): ");
        String r = teclado.nextLine().trim().toLowerCase();
        return r.startsWith("m") ? Genero.MASCULINO : Genero.FEMENINO;
    }

    private ColorPelo pedirValorColorPelo() {
        System.out.print("¿Sobre qué color de pelo querés preguntar? (c = colorado / n = negro / a = amarillo): ");
        String r = teclado.nextLine().trim().toLowerCase();
        char primerCaracter = r.isEmpty() ? ' ' : r.charAt(0);
        return switch (primerCaracter) {
            case 'n' -> ColorPelo.NEGRO;
            case 'a' -> ColorPelo.AMARILLO;
            default -> ColorPelo.COLORADO;
        };
    }

    // ---------------------------------------------------------------
    // Máquina vs Máquina (espectador)
    // ---------------------------------------------------------------

    public void jugarMaquinaVsMaquina() {
        List<Personaje> personajes = repositorioPersonajes.cargar();

        MaquinaBasica maquina1 = new MaquinaBasica(personajes);
        MaquinaAvanzada maquina2 = new MaquinaAvanzada(personajes);

        maquina1.elegirSecreto(null);
        maquina2.elegirSecreto(List.of(maquina1.getSecreto()));

        System.out.println("\n=== Modo espectador: Máquina vs Máquina ===");
        System.out.println("Acá las dos parten en igualdad de condiciones (cada una busca un personaje distinto),");
        System.out.println("para poder comparar en igualdad de condiciones sus dos formas de jugar.");
        System.out.println("(Secreto de " + maquina1.getNombre() + ": " + maquina1.getSecreto().getNombre() + " — oculto para su rival)");
        System.out.println("(Secreto de " + maquina2.getNombre() + ": " + maquina2.getSecreto().getNombre() + " — oculto para su rival)");

        int turno = 1;
        while (true) {
            System.out.println("\n--- Turno " + turno + " ---");
            pausa("(Enter para continuar) ");
            if (turnoEspectador(maquina1, maquina2)) {
                break;
            }
            pausa("(Enter para continuar) ");
            if (turnoEspectador(maquina2, maquina1)) {
                break;
            }
            turno++;
        }
    }

    /** @return true si la partida terminó en este turno (por acierto o por un intento fallido). */
    private boolean turnoEspectador(MaquinaJugadora activo, MaquinaJugadora rival) {
        System.out.println(activo.getNombre() + " le quedan " + activo.getCandidatos().size() + " candidato(s) para el secreto de " + rival.getNombre() + ".");

        if (activo.debeArriesgar()) {
            Personaje adivinanza = activo.elegirAdivinanza();
            System.out.println(activo.getNombre() + " arriesga: ¿es " + adivinanza.getNombre() + "?");
            if (rival.esMiPersonaje(adivinanza)) {
                System.out.println("¡Correcto! " + activo.getNombre() + " gana la partida.");
            } else {
                System.out.println("Falló. No era " + adivinanza.getNombre() + ". ¡Eso le hace perder la partida a "
                        + activo.getNombre() + "! Gana " + rival.getNombre() + ".");
            }
            return true;
        }

        Pregunta pregunta = activo.elegirPregunta();
        if (pregunta == null) {
            Personaje adivinanza = activo.elegirAdivinanza();
            System.out.println(activo.getNombre() + " ya no tiene preguntas útiles y arriesga: ¿es " + adivinanza.getNombre() + "?");
            if (rival.esMiPersonaje(adivinanza)) {
                System.out.println("¡Correcto! " + activo.getNombre() + " gana la partida.");
            } else {
                System.out.println("Falló. ¡Eso le hace perder la partida a " + activo.getNombre() + "! Gana " + rival.getNombre() + ".");
            }
            return true;
        }

        boolean respuesta = rival.responder(pregunta);
        System.out.println(activo.getNombre() + " pregunta a " + rival.getNombre() + ": " + pregunta.getDescripcion()
                + " → " + (respuesta ? "Sí" : "No"));
        activo.registrarRespuesta(pregunta, respuesta);
        return false;
    }

    private void pausa(String mensaje) {
        System.out.print(mensaje);
        teclado.nextLine();
    }

    // ---------------------------------------------------------------
    // Marcador
    // ---------------------------------------------------------------

    public void mostrarMarcador() {
        Map<String, RegistroMarcador> tabla = marcador.leer();
        System.out.println("\n=== Marcador de récords ===");
        if (tabla.isEmpty()) {
            System.out.println("Todavía no hay partidas registradas.");
            return;
        }
        tabla.forEach((nombre, registro) -> {
            String mejor = registro.tieneVictoriasCompletas() ? (registro.getMejorIntentos() + " intento(s)") : "sin victorias completas";
            System.out.println(nombre + " -> le ganó a Máquina 1: " + registro.getVictoriasMaquina1()
                    + " | ganó el desafío completo (las 2): " + registro.getVictoriasCompletas()
                    + " | derrotas: " + registro.getDerrotas()
                    + " | mejor desafío completo: " + mejor);
        });
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private void mostrarPersonajes(List<Personaje> personajes) {
        System.out.println("Personajes disponibles:");
        for (Personaje p : personajes) {
            System.out.println(p);
        }
    }

    private Personaje buscarPorId(List<Personaje> personajes, int id) {
        for (Personaje p : personajes) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private int leerEntero() {
        while (!teclado.hasNextInt()) {
            System.out.print("Ingresá un número válido: ");
            teclado.nextLine();
        }
        int valor = teclado.nextInt();
        teclado.nextLine();
        return valor;
    }
}
