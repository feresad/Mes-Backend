package com.example.Rebut;

import com.example.Rebut.Repository.RebutRepository;
import com.example.Rebut.entity.Rebut;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.Date;

@SpringBootApplication
@EnableDiscoveryClient
public class RebutApplication {

	public static void main(String[] args) {
		SpringApplication.run(RebutApplication.class, args);
	}
	/*@Bean
	CommandLineRunner start(RebutRepository rebutRepository){
		return args ->{
			rebutRepository.save(new Rebut(null,6L,2L,40, LocalDate.now()));
			rebutRepository.save(new Rebut(null,1L,10L,40,LocalDate.now()));
			rebutRepository.findAll().forEach(System.out::println);
		};
	}*/
}
