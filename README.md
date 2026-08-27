Personaje(int id; String nombre; RangoEdad rangoEdad; boolean anteojos; boolean gorro; ColorDePelo colorDePelo, Genero genero)
Hombre(boolean calvo; boolean barba)
Mujer(boolean peloLargo; boolean peloAtado)
<<enumeration>>RangoEdad(NIÑO, ADULTO,ABUELO)
<<enumeration>>Genero(HOMBRE, MUJER)
<<enumeration>>ColorDePelo(COLORADO, RUBIO, CASTAÑO, GRIS)

Juego(List<Personaje>; List<Jugadores>)
Jugadores(String User; int score)

<<asbtract>>Maquina(Personaje personajeElegido; List<Personaje> candiadtos; List<Pregunta>historial,String nombre)
MaquinaBasica(Random random)
MaquinaAvanzada(List<Pregunta>historialMaquinaBasica;)


Pregunta(String descripción; TipoFiltro tipo; Object valorConsultado, boolean respuesta)
TipoFiltro(GENERO,ANTEOJOS,CALVICIE,COLOR_DE_PELO,BARBA,PELO_LARGO,PELO_ATADO)
GerneradoDePreguntas(List<Pregunta> preguntas)

Preguntas Genericas(¿Es hombre?; ¿Es mujer?; ¿Tiene anteojos?; ¿Usa lentes?; ¿Usa gorro?; ¿El color de pelo es ColorDePelo?)
Preguntas Hombre(¿Es calvo?; ¿Tiene barba?)
Preguntas Mujer(¿Tiene el pelo atado?; ¿Tiene pelo largo?)
