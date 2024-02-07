package com.example.Produit;

import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProduitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProduitApplication.class, args);
	}
	@Bean
	CommandLineRunner start(ProduitRepository produitRepository){
		return args ->{
			produitRepository.save(new Produit(null,"Cache","1kg coulourant"));
			produitRepository.save(new Produit(null,"Bouchon du Lait","1L"));
			produitRepository.findAll().forEach(System.out::println);
		};
	}
}
