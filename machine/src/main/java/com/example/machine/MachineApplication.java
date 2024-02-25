package com.example.machine;

import com.example.machine.entity.Machine;
import com.example.machine.repository.MachineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableDiscoveryClient
public class MachineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MachineApplication.class, args);
	}

	@Bean
	CommandLineRunner start(MachineRepository machineRepository) {
		return args -> {
			machineRepository.save(new Machine(null, "premier machine", true, "la machine est en marche"));
			machineRepository.save(new Machine(null, "deuxieme machine", false, "la machine est en panne"));
			machineRepository.findAll().forEach(System.out::println);
		};
	}
}
