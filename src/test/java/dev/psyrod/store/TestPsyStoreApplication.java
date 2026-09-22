package dev.psyrod.store;

import org.springframework.boot.SpringApplication;

public class TestPsyStoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(PsyStoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
