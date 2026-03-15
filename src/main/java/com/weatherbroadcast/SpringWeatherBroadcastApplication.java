package com.weatherbroadcast;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringWeatherBroadcastApplication {

	public static void main(String[] args) {
		// Evita il caricamento OpenSSL nativo di Netty/gRPC, fragile su alcuni runtime container.
		System.setProperty("io.grpc.netty.shaded.io.netty.handler.ssl.noOpenSsl", "true");
		System.setProperty("io.netty.handler.ssl.noOpenSsl", "true");

		SpringApplication.run(SpringWeatherBroadcastApplication.class, args);
	}

}
