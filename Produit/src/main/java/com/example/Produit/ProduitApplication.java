package com.example.Produit;

import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
public class ProduitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProduitApplication.class, args);
	}
	/*@Bean
	CommandLineRunner start(ProduitRepository produitRepository){
		return args ->{
			produitRepository.save(new Produit(null,"Cache",0,"1kg coulourant",2));
			produitRepository.save(new Produit(null,"Bouchon du Lait",5,"1L matiere",1));
			produitRepository.save(new Produit(null,"Bouchon du Lait",50,"1L col",0));
			produitRepository.findAll().forEach(System.out::println);
		};*/
}
