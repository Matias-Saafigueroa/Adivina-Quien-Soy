package Servicios;

/**
 * Una pregunta que formuló una máquina junto con la respuesta verdadera que
 * obtuvo. Se usa para transferirle a Máquina 2 lo que Máquina 1 ya averiguó
 * sobre el mismo personaje secreto.
 */
public record PreguntaRespuesta(Pregunta pregunta, boolean respuesta) {
}
