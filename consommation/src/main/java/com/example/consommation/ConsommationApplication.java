package com.example.consommation;

import com.example.consommation.Repository.ConsoRepository;
import com.example.consommation.entity.consommation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ConsommationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsommationApplication.class, args);
	}
	/*@Bean
	CommandLineRunner start(ConsoRepository ConsomRepository) {
		return args -> {
			ConsomRepository.save(new consommation(null, "1 kg courant , 2 kg matiere Premier",1L,1L));
			ConsomRepository.save(new consommation(null, "24 kg courant , 03 kg matiere Premier",2L,2L));
			ConsomRepository.findAll().forEach(System.out::println);
		};
	}*/
}
