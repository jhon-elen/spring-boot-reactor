package com.bolsadeideas.springboot.reactor.app;

import com.bolsadeideas.springboot.reactor.app.models.Comentarios;
import com.bolsadeideas.springboot.reactor.app.models.Usuario;
import com.bolsadeideas.springboot.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploContraPresionFormaDos();

		// Prueba();
	}

	public void ejemploContraPresionFormaDos(){
		Flux.range(1, 10)
				.log()
				.limitRate(2)
				.subscribe();
	}

	public void ejemploContraPresion() {
		Flux.range(1, 10)
				.log()
				.subscribe(new Subscriber<>() {
					private Subscription s;

					private Integer limite = 5;
					private Integer consumido = 0;

					@Override
                    public void onSubscribe(Subscription s) {
						this.s = s;
						s.request(limite);
                    }

                    @Override
                    public void onNext(Integer o) {
                        log.info(o.toString());
						consumido++;
						if (consumido == limite) {
							consumido = 0;
							s.request(limite);
						}
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error(t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        log.info("Hemos terminado");
                    }
				});
	}

	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if(contador == 10) {
						timer.cancel();
                        emitter.complete();
					}
					if(contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos terminado"));
	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(() -> latch.countDown())
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Solo hasta 5!"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(log::info, e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElementsOtraForma() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.subscribe();

		Thread.sleep(13000);
	}

	public void ejemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.blockLast();
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ran, ret) -> ran)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast();
	}

	public void ejemploZipWithRangos() {
		Flux.just(1, 2, 3, 4)
				.map(i -> i*2)
				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(texto -> log.info(texto));
	}

	public void ejemploUsuarioComentariosZipwithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Andrés", "Guzman"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> {
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipwith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Andrés", "Guzman"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) -> new UsuarioComentarios(usuario, comentariosUsuario));

		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Andrés", "Guzman"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andrés", "Guzman"));
		usuariosList.add(new Usuario("David", "Fulano"));
		usuariosList.add(new Usuario("John", "Fulano"));
		usuariosList.add(new Usuario("Juan", "Sultano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
				});
	}

	public void ejemploToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andrés", "Guzman"));
		usuariosList.add(new Usuario("David", "Fulano"));
		usuariosList.add(new Usuario("John", "Fulano"));
		usuariosList.add(new Usuario("Juan", "Sultano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase() + " " + usuario.getApellido().toUpperCase())
				.flatMap(nombre -> {
					if (nombre.contains("BRUCE")) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> nombre.toLowerCase())
				.subscribe(valor -> log.info(valor.toString()));
	}

	public void ejemploFlatMap() throws Exception {

		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andrés Guzman");
		nombresList.add("David Fulano");
		nombresList.add("John Fulano");
		nombresList.add("Juan Sultano");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");

		Flux.fromIterable(nombresList)
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			.flatMap(usuario -> {
				if (usuario.getNombre().equalsIgnoreCase("bruce")) {
					return Mono.just(usuario);
				} else {
					return Mono.empty();
				}
			})
			.map(usuario -> {
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			})
			.subscribe(valor -> log.info(valor.toString()));
	}

	public void ejemploIterable() throws Exception {

		List<String> nombresList = new ArrayList<>();
		nombresList.add("Andrés Guzman");
		nombresList.add("David Fulano");
		nombresList.add("John Fulano");
		nombresList.add("Juan Sultano");
		nombresList.add("Bruce Lee");
		nombresList.add("Bruce Willis");

		// Flux<String> nombres = Flux.just("Andrés Guzman", "David Fulano", "John Fulano", "Juan Sultano", "Bruce Lee", "Bruce Willis");
		Flux<String> nombres = Flux.fromIterable(nombresList);

		Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(nombre -> nombre.getNombre().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(usuario);
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});


		usuarios.subscribe(
				valor -> log.info(valor.getNombre() + " " + valor.getApellido()),
				error -> log.error(error.getMessage()),
				() -> log.info("El observable ha finalizado la ejecución con éxito.")
		);
	}
}
