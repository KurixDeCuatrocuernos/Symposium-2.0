package com.sympos2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.sympos2.dto.RespuestaComentario;
import com.sympos2.models.Comentario;
import com.sympos2.models.Obra;
import com.sympos2.models.Usuario;
import com.sympos2.repositories.ComentarioRepository;
import com.sympos2.repositories.ObraRepository;
import com.sympos2.repositories.UserRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackages="com.sympos2.repositories")
public class Application {
	
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(Application.class, args);
		
		// Test for Usuario insertion in DB 
		
		var userRepo = context.getBean(UserRepository.class);
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		userRepo.deleteAll();
		// Usuario(String id, String name, LocalDate fechaNac, String email, String password, String avatar, String role, String studies, String school)
		var user1 = new Usuario(null, "Juan", LocalDate.of(1998, 3, 4), "juan@correo.com", encoder.encode("12345"), null, "student", "estudiantePrueba", "escuelaPrueba");
		var user2 = new Usuario(null, "Elena", LocalDate.of(2002, 2, 6), "elena@correo.com", encoder.encode("12345"), null, "TITLED", "estudiantePrueba", "escuelaPrueba");
		var user3 = new Usuario(null, "Lucas", LocalDate.of(2980, 2, 9), "lucas@correo.com", encoder.encode("12345"), null, "STUDENT", "estudiantePrueba", "escuelaPrueba");
		var user4 = new Usuario(null, "Adam", LocalDate.of(1996, 7, 9), "adam@correo.com", encoder.encode("12345"), null, "titled", "estudiantePrueba", "escuelaPrueba");
		// String id, String name, LocalDate fechaNac, String email, String password, String avatar, String role, Long phone
		var user5 = new Usuario(null, "UsuarioAdmin", LocalDate.of(1998, 2, 1), "admin@correo.com", encoder.encode("admin"),null, "ADmin", 555555555L);
		
		userRepo.saveAll(List.of(user1, user2, user3, user4, user5));
		
		System.out.println("Usuarios:");
		userRepo.findAll().forEach(System.out::println);

		
		// Test for Obra insertion in DB
		
		var obraRepo = context.getBean(ObraRepository.class);
		
		obraRepo.deleteAll();
		//Obra(long isbn, LocalDate fecha_publicacion, String titulo, String autor, String tipo, String abstracto, String lugar_publicacion, String temas, String editorialOrPage)
		var Obra1 = new Obra(9788430607099L, LocalDate.of(1781,01,01), "Crítica de la razón pura", "Immanuel Kant", "BOOK", "Si hay un filósofo que representa lo que ha sido la Ilustración, este es sin duda Kant y, concretamente, su obra más universal, la Critica de la razón pura. Con este libro emprende una crítica tan sistemática como demoledora a la metafísica o, para mayor exactitud, a los argumentos con que se sostiene la metafísica tradicional. En realidad, la Crítica de la razón pura se presenta como preparación de un riguroso sistema filosófico que Kant no llegó a elaborar. Pero el ejercicio crítico contenido en este libro es una cumbre del esfuerzo intelectual por el rigor, un ejercicio en el que son tan impresionantes los derribos que lleva a cabo como los caminos que abre a la reflexión filosófica. De ahí que siga siendo una permanente e indispensable fuente de ideas que explorar y debatir.", "Germany", List.of("Epistemología"), "Taurus Editorial");
		var Obra2 = new Obra(9788467028126L, LocalDate.of(1790,01,01), "Crítica del juicio", "Immanuel Kant", "BOOK", "Dos ámbitos de razones convierten a la Crítica del Juicio en una obra actual y de lectura ineludible. El primero recoge razones fundamentalmente académicas. Por ejemplo, qué posición ocupa la noción de juicio reflexivo en el sistema filosófico kantiano. En las Críticas anteriores, Kant ha probado la posibilidad de los juicios sintéticos a priori para el conocimiento y la moralidad; ahora trata de probar la posibilidad de probar los juicios sintéticos a priori en la esfera del sentimiento. Esta clase de juicios reflexivos sólo intenta estimar los objetos según leyes de libertad y bajo criterio de fin. Por este camino, la Crítica del juicio pretende ser un puente entre la Crítica de la Razón Pura y la Crítica de la Razón Pública. El segundo tipo de razones que hacen estimulante la lectura de esta obra desbordan el campo académico y hacen de la Crítica del juicio uno de los referentes teóricos más decisivos en la discusión sobre los orígenes y diversos desarrollos de la modernidad.", "Germany", List.of("Estética", "Epistemología"), "Austral Editorial");
		var Obra3 = new Obra(9788420676111L, LocalDate.of(1788,01,01), "Crítica de la razón práctica","Immanuel Kant", "BOOK", "El hecho de que todas las teorías morales contemporáneas continúen dialogando aún hoy con las premisas y planteamientos formulados por Immanuel Kant (1724-1804) permite hablar, en la historia de la ética, de un antes y un después del filósofo de Königsberg, cesura que viene marcada por el carácter de punto de inflexión que, para la filosofía moral, representa su formalismo ético. En este sentido, cabe calificar la \"Crítica de la razón práctica\" (1788) -uno de los textos kantianos capitales- como una verdadera biblia por lo que atañe al pensamiento moral de la modernidad.", "Germany", List.of("Ética"), "Alianza Editorial" );
		var Obra4 = new Obra(9788491043409L, LocalDate.of(1793,01,01), "La Religión dentro de los límites de la mera razón", "Immanuel Kant", "BOOK", "Si dentro de las cuatro preguntas que según el autor (1724-1804) delimitan el campo de la Filosofía ¿qué puedo saber?, ¿qué debo hacer?, ¿qué me está permitido esperar? y ¿qué es el hombre?, la \"Crítica de la razón pura\" contesta a la primera y la \"Crítica de la razón práctica\" a la segunda, mientras que la cuarta abre el camino a la reflexión antropológica, este libro es la obra destinada a dar respuesta, dentro de su ambicioso proyecto filosófico, a la tercera de ellas. Obra tardía de la producción kantiana, este tratado culmina el proceso de pensamiento del filósofo alemán y arroja una luz peculiar sobre la totalidad de su gran hazaña intelectual en el campo de la reflexión humana, que modificó las coordenadas de la ciencia y la moral en el mundo moderno.", "Germany", List.of("Antropología filosófica", "Filosofía de la Religión"), "Alianza Editorial");
		var Obra5 = new Obra(9788491040781L, LocalDate.of(1798,01,01), "Antropología en sentido pragmático", "Immanuel Kant", "BOOK", "La \"Antropología\" de Immanuel Kant, más que en la diversidad y relatividad de las culturas, se centra en los rasgos específicos de la especie humana, si bien para «ensanchar su volumen» recomiende viajar ­o al menos leer libros de viajes­ y prestar atención a las obras literarias y a las biografías, pues aunque en ellas la ficción invente y acuse ciertos rasgos, vienen a ser un extracto de la observación de lo que los hombres hacen debido a personas de inteligencia penetrante. En la primera parte se estudian sucesivamente las facultades intelectuales -conocimiento y sensibilidad- y las dependientes de la afectividad -las pasiones-. En la segunda se describen los caracteres y su reflejo fisionómico en rasgos y gestos, terminando con observaciones en torno al carácter de la especie, de las razas y de los pueblos.", "Germany", List.of("Antropología filosófica"), "Alianza Editorial");
		var Obra6 = new Obra(9788498790474L, LocalDate.of(1927,01,01), "Ser y Tiempo", "Martin Heidegger", "BOOK", "La presente traducción de Ser y tiempo es el fruto de veintitrés años de trabajo. El traductor tuvo sus primeros contactos con Martin Heidegger en 1961, permaneciendo en estrecha relación con él. Entre 1973 y 1975 concluyó la primera versión del texto, que el propio Heidegger conoció y aprobó. En 1988 preparó una segunda versión; esta vez en reuniones semanales con el editor de Heidegger en alemán, Friedrich-Wilhelm von Herrmann, y el apoyo de Hans-Georg Gadamer y el profesor Max Müller. Finalmente, en 1991 inició una tarea de cinco años con un equipo multidisciplinario de especialistas, que daría como resultado, en 1995, la tercera y definitiva versión. Con todo, y más allá de la historia, la traducción de Ser y tiempo de Jorge Eduardo Rivera C. constituye un hito para la filosofía actual.", "Germany", List.of("Metafísica"), "Trotta Editorial");
		var Obra7 = new Obra(9788498797107L, LocalDate.of(2018,01,01), "Filosofía de la Religión","Georg Wilhelm Friedrich Hegel", "BOOK", "Georg Wilhelm Friedrich Hegel ofrece en sus últimas lecciones berlinesas de filosofía de la religión la exposición más sistemática, clara y accesible del concepto especulativo de Dios como Espíritu, objetivado en su representación e interiorizado en su culto. Concepto, representación y culto son los tres momentos en los que se ordena el análisis de cada grupo de religiones determinadas hasta llegar a la religión cristiana como religión consumada.", "Germany", List.of("Filosofía de la religión"), "Trotta Editorial");
		var Obra8 = new Obra(9788420678818L, LocalDate.of(2013,01,01), "La República", "Platón", "BOOK", "En el período que transcurrió desde su infancia hasta su muerte, Platón (ca. 428-ca. 347 a.C.) conoció la decadencia de la grandeza ateniense, jalonada por numerosos y señalados episodios históricos que, junto con su reiterado fracaso político en Siracusa, influyeron poderosamente tanto en su actividad política como en su trabajo intelectual. \"La república\" presenta el modelo de ciudad donde la justicia prevalece frente al desorden, la confusión y la perversión; sin embargo, como señala Manuel Fernández-Galiano en la introducción al volumen, el diálogo no apunta a la construcción ideal de una sociedad perfecta de hombres perfectos, sino que es un \"tratado de medicina política\" con aplicación a los regímenes existentes en su tiempo.", "Greek", List.of("Filosofía política", "Diálogo platónico"), "Alianza Editorial");
		var Obra9 = new Obra(9788424928377L, LocalDate.of(2014,01,01), "Apología de Sócrates", "Platón", "BOOK", "En Apología de Sócrates, diálogo compuesto entre 393 y 389 a. C., Platón (c. 427 - 347 a. C.) da una versión de la defensa del mismo Sócrates tras ser acusado de corromper a los jóvenes y despreciar a los dioses. La apología refleja la defensa de Sócrates frente al jurado de Atenas, de manera que apología tiene el significado original de defensa formal de las opiniones de uno. El diálogo, perteneciente al ciclo platónico de obras socráticas o de juventud, rescata el texto de la apología (defensa) de Sócrates ante el tribunal, así como constituye la apología (elogio) que hace Platón de su maestro.", "Greek", List.of("Filosofía Política", "Diálogo Platónico"), "Gredos Editorial");
		var Obra10 = new Obra(9788424926373L, LocalDate.of(2014,01,01), "El Banquete", "Platón", "BOOK", "Entre la reflexión filosófica y la teoría psicológica, Platón (c. 427 – 347 a. C.) nos ofrece con el Banquete\" una brillante y elaborada exposición de su teoría de los afectos. El diálogo, perteneciente al período de madurez, en el que el filósofo ateniense se interesa por la esencia ontológica de diversas ideas, presenta una estructura sencilla. A traves de seis discursos que encuentran su eje en este sentimiento, el autor desmenuza sus múltiples facetas: la naturaleza divina de Eros, sus diversas formas y manifestaciones, sus designios y anhelos, o su incidencia en la vida humana. Por estas páginas desfila, en suma, la consistente doctrina del amor platónica que constituye en sí misma su exaltación.", "Greek", List.of("Diálogo platónico"), "Gredos Editorial");
		var Obra11 = new Obra(9788424926366L, LocalDate.of(2014,01,01), "Ética a Nicómaco", "Aristóteles", "BOOK", "Parece claro que la felicidad es el fin último al que aspira la vida humana. Pero ¿cuál es la verdadera esencia de la felicidad? A esta espinosa cuestión se enfrenta Aristóteles (384 – 322 a. C.) en la Ética a Nicómaco. Resultado de la selección realizada por su hijo Nicómaco con las notas que el propio autor utilizaba para sus lecciones en el Liceo, la obra resume las claves de la reflexión moral de su autor. Y aún más meritorio es el hecho de haber sido él quien, por vez primera en la literatura universal, aborda la disciplina como rama filosófica independiente. Para Aristóteles, la ética, ciencia de los hábitos y el carácter, no es un saber meramente teórico, sino que despliega una dimensión práctica en la búsqueda de la virtud, el bien más preciado por ser patrimonio del alma.", "Greek", List.of("Ética"),"Gredos Editorial");
		var Obra12 = new Obra(9788424929060L, LocalDate.of(2014,01,01), "La Metafísica", "Aristóteles", "BOOK", "La Metafísica, uno de los tratados fundamentales de Aristóteles (384-322 a. C.), tuvo como genesis un conjunto de escritos independientes, cuya finalidad original era sobre todo educativa. Así pues, por su naturaleza, la Metafísica no puede considerarse tanto la exposición de un sistema perfectamente acabado como una obra que aborda diferentes temas a lo largo de catorce libros. No obstante, su concepción integral acaba dando a luz la ciencia \"más allá de la física\", es decir, la filosofía primera. La Metafísica no solo es un libro pionero, sino que se trata una de las obras capitales de la filosofía, cuyo peso e influencia son inmensos e inabarcables.", "Greek", List.of("Metafísica"), "Gredos Editorial");
		var Obra13 = new Obra(9788430946082L, LocalDate.of(1975,01,01), "Tratado contra el método", "Paul Feyerabend", "BOOK", "La moderna filosofía de la ciencia ha prestado gran atención al entendimiento de la práctica científica, a diferencia de su anterior concentración en el \"método científico\". Los trabajos de Karl Popper, Thomas Kuhn e Imre Lakatos han aportado una diversidad de planteamientos sobre lo que es la práctica. Paul Feyerabend supera esta posición: sostiene que la mayor parte de las investigaciones científicas de éxito nunca se han desarrollado siguiendo un método racional. Examina en detalle los argumentos que utilizó Galileo para defender la revolución copernicana en el campo de la física, y muestra que semejante éxito no depende de un argumento racional, sino de una mezcla de subterfugio, retórica y propaganda. Y llega a una conclusión: \"Galileo hizo tampas\". Afirmando que el anarquismo debe reemplazar ahora al racionalismo en la teoría del conocimiento, Feyerabend arguye que el progreso intelectual sólo puede alcanzarse poniendo el acento en la creatividad y en los deseos del científico más que en el método y la autoridad de la ciencia. En la segunda mitad del libro examina el \"racionalismo crítico\" de Popper y el intento de Lakatos de construir una metodología que reconozca al científico su libertad sin amenazar \"la ley y el orden\" científicos. Descartando ambas tentativas de apuntalamiento del racionalismo, pone toda su esperanza en el «arrollador alejamiento de la razón» y mantiene que \"el único principio que no inhibe el progreso es el de todo pasa\".", "United Kingdom", List.of("Filosofía de la ciencia"), "Tecnos Editorial");
		var Obra14 = new Obra(9788420675978L, LocalDate.of(1995,01,01), "Caminos de Bosque", "Martin Heidegger", "BOOK", "Caminos de bosque nos lleva por seis sendas. El primer camino da vueltas en torno a la esencia y origen de la obra de arte; el segundo atiende a los fundamentos metafísicos de las concepciones del mundo; los dos siguientes versan sobre Hegel y Nietzsche; el quinto se plantea la pregunta de Hölderlin acerca de la razón de ser de la poesía en tiempos de tribulación y el último desciende a los orígenes del pensamiento primordial acerca del Ser.", "Germany", List.of("Estética"), "Alianza Editorial");
		var Obra15 = new Obra(9788420647500L, LocalDate.of(2005,01,01), "Aclaraciones a la poesía de Hölderlin", "Martin Heidegger", "BOOK", "Las Aclaraciones a la poesía de Hölderlin fueron escritas entre 1936 y 1968. En el prólogo a la segunda edición escribió Heidegger: \"Dichas aclaraciones forman parte de un diálogo entre un pensar y un poetizar cuya singularidad histórica nunca podrá ser demostrada por la historia de la literatura, pero sí por ese diálogo pensante\". Este diálogo pensante con la poesía de Hölderlin se inició a principios de los años treinta, cuando Heidegger comenzó el pensar de la historia del ser, en cuya formulación no dejó de acompañarle Hölderlin.", "Germany", List.of("Metafísica", "Estética"), "Alianza Editorial");
		var Obra16 = new Obra(9788491484813L, LocalDate.of(2017,01,01), "Ser en el tiempo: Desde el claro del ser", "Marco A. Arévalo", "ARTICLE", "La filosofía siempre es pregunta y enigma. Aquí nuestro pensar buscó una respuesta a algo con lo que el hombre convive, de lo que usa a diario, y de lo que reúsa preguntar, algo simple y banal, cercano y conocido, a la vez que misterio e intimidad; nuestro pensar se preguntó por el tiempo. Eso que lleva nuestros quehaceres, en lo que nos desenvolvemos, con lo que operamos, lo que medimos y aplicamos, lo que condiciona nuestras vidas… ¿ sabemos realmente si es? Es decir,¿ acaso hay un ens que vista el ropaje del tiempo?", "Hermenéuticas del Cuidado del Sí: Cuerpo Alma Mente Mundo", List.of("Hermenéutica", "Metafísica"), "Dykinson", 35, 86);
		var Obra17 = new Obra(9788499402147L, LocalDate.of(2011,01,01), "Lectura antropológica del mito de Aristófanes en el diálogo \"El banquete\" de Platón'", "Ivan Dragoev", "ARTICLE", "Sin Abstracto", "La imagen del ser humano: historia, literatura y hermenéutica", List.of("Hermenéutica", "Literatura", "Historia"), "Biblioteca Nueva", 150, 158);
		var Obra18 = new Obra(9788499200231L, LocalDate.of(2010,01,01), "La religión de Kant", "Leonardo Rodriguez Duplá", "ARTICLE", "Sin Abstracto", "De nobis ipsis silemus: homenaje a Juan Miguel Palacio", List.of("Filosofia de la Religion", "Idealismo Trascendental", "Metafísica"), "Encuentro", 101, 129);
		var Obra19 = new Obra(23862491L, LocalDate.of(2014,01,01), "El Gorgias como precedente a la República de Platón", "Alberto Medina González", "ARTICLE", "En este artículo consideramos el diálogo platónico Gorgias un antecedente de la República. A ese respecto analizamos los textos del Gorgias que critican la retórica aduladora, la injusticia tiránica y a los principales políticos de la democracia ateniense. Estos temas son desarrollados y ampliados por Platón en la República.", "La Albolafia: Revista de Humanidades y Cultura", List.of("Hermenéutica", "Filosofia política", "Epistemología"), "Instituto de Humanidades: La Albolafia: Asociación de Humanidades y Cultura", 155, 170);
		
		obraRepo.saveAll(List.of(Obra1, Obra2, Obra3, Obra4, Obra5, Obra6, Obra7, Obra8, Obra9, Obra10, Obra11, Obra12, Obra13, Obra14, Obra15, Obra16, Obra17, Obra18, Obra19));
		
		System.out.println("Obras:");
		obraRepo.findAll().forEach(System.out::println);

		
		// test for Comentario
		
		var commentRepo = context.getBean(ComentarioRepository.class);
		
		commentRepo.deleteAll();
		
		var com1 = new Comentario(null, "Me encanta este libro", "Qué bueno es El Banquete de Platón, es el mejor libro que he leído", LocalDateTime.now(), 100, "COMMENT", 9788424926373L, "67aa2b4b445b6647f1a2dad4");
		var com2 = new Comentario(null, "Está bien este libro", "Propone perspectivas interesantes respecto al amor y sus aplicaciones en otros campos", LocalDateTime.now(), 80, "COMMENT", 9788424926373L,"67aa2b4b445b6647f1a2dad6");
		var com3 = new Comentario(null, "Es una obra famosa y ya", "Su fama se debe a que unos pedantes les gusta y ya, es aburrido y monótono, no entiendo quién querría leerlo", LocalDateTime.now(), 0, "Comment", 9788424926373L, "67aa2b4b445b6647f1a2dad7");
		var com4 = new Comentario(null, "Un clásico, eso es exactamente", "Sin duda fue importante y abrió camino a otras obras futuras, pero hay autres que han cambiado las perspectivas que propone, como Herbert Marcuse en Eros y civilización (si bien debe su origen a esta obra, claro)", LocalDateTime.now(), 65, "COmmenT", 9788424926373L, "67aa2b4b445b6647f1a2dad5");
		
		var com5 = new Comentario(null, "Un artículo clave para entender la obra original", "Expone una interpretación nueva respecto a la intervención de Aristófanes, el enemigo de Platón, en principio, mostrando que eso no es exactamente así, de hecho abre una nueva mirada respecto al diálogo precisamente a partir de la intervención de Aristófanes en el diálogo, un 10", LocalDateTime.now(), 100, "comment", 9788499402147L, "67aa2b4b445b6647f1a2dad4");
		var com6 = new Comentario(null, "Una ayuda para entender El Banquete", "Una ayuda tanto para entender la obra original, como para entendernos a nosotros mismos", LocalDateTime.now(), 90, "comment", 9788499402147L, "67aa2b4b445b6647f1a2dad6");
		var com7 = new Comentario(null, "Es un Plomazo", "Me pidieron leerlo y aún no sé por qué", LocalDateTime.now(), 0, "comment", 9788499402147L, "67aa2b4b445b6647f1a2dad7");
		var com8 = new Comentario(null, "Muy útil y estimulante", "Aporta una vision diferente respecto al Banquete de Platón, aporta una interpretación antropológica al mito que cuenta el personaje Aristófanes y explica el por qué Platón lo dibuja de la manera en que lo hace y aporta otras posibilidades interpretativas al mismo tiempo, si buscas entender por qué Platón nos muestra al poeta Aristófanes de la manera en que lo hace, te será muy útil", LocalDateTime.now(), 95, "COMMENT", 9788499402147L, "67aa2b4b445b6647f1a2dad5");
		
		commentRepo.saveAll(List.of(com1, com2, com3, com4, com5, com6, com7, com8));
		
		//Comentario(String id, String texto, LocalDate fecha, String tipo, Long obra, String usuario, String comment)
		Optional<Comentario> commentId;
		Optional<RespuestaComentario> answerId;
		
		commentId = commentRepo.findByObraAndUsuario(9788424926373L, "67aa2b4b445b6647f1a2dad4");
		// Responde a com1
		var resp1 = new Comentario(null, "No estoy para nada de acuerdo con lo que dices respecto a este libro, es aburrido, si te gusta eres tonto", LocalDateTime.now(), "ANSWER", "67aa2b4b445b6647f1a2dad7", commentId.get().getId());
		commentRepo.save(resp1);
		
		answerId = commentRepo.findByUsuarioAndTipo("67aa2b4b445b6647f1a2dad7", "ANSWER");
		System.out.println("Comentario Recogido: "+commentId.get().toString());
		// Responde a resp1
		var resp2 = new Comentario(null, "Entiendo que no te guste, pero aquí no venimos a insultar a los demás, sino a hablar acerca de los libros y artículos, si no te gusta haz un comentario exponiendo tu opinión, no vengas a criticar las opiniones de los demás", LocalDateTime.now(), "Answer", "67aa2b4b445b6647f1a2dad4", commentId.get().getId());
		commentRepo.save(resp2);
		
		commentId = commentRepo.findByObraAndUsuario(9788499402147L, "67aa2b4b445b6647f1a2dad5");
		// Responde a com8
		var resp3 = new Comentario(null, "¿No crees que fuerza la interpretación del personaje de Aristófanes al modelo popular?", LocalDateTime.now(), "ANSWER", "67aa2b4b445b6647f1a2dad6", commentId.get().getId());
		commentRepo.save(resp3);
		
		answerId = commentRepo.findByUsuarioAndTipo("67aa2b4b445b6647f1a2dad6", "ANSWER");
		// Responde a resp3
		var resp4 = new Comentario(null, "Su interpretación quizá, pero sigue siendo útil para entender tanto el contexto de la obra, como para abrir una nueva interpretación, aunque, por supuesto, no está exento de crítica", LocalDateTime.now(), "ANSWER", "67aa2b4b445b6647f1a2dad5", commentId.get().getId());
		commentRepo.save(resp4);
		
		System.out.println("Comentarios:");
		commentRepo.findAll().forEach(System.out::println);
		
		System.out.println("Mostrando todos los comentarios del libro El Banquete: ");
		System.out.println(commentRepo.findAllByObra(9788424926373L).toString());
		
	}
	

}
