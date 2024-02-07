package com.example.machine;

import com.example.machine.entity.Machine;
import com.example.machine.repository.MachineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MachineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MachineApplication.class, args);
	}

	@Bean
	CommandLineRunner start(MachineRepository machineRepository) {
		return args -> {
			machineRepository.save(new Machine(null, "premier machine", true, "la machine est en marche"));
			machineRepository.save(new Machine(null, "deuxieme machine", true, "la machine est en marche"));
			machineRepository.findAll().forEach(System.out::println);
		};
	}
}
