package com.example.Produit;

import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import com.example.Produit.entity.ProduitConso;
import com.example.Produit.entity.ProduitFini;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;
import java.util.Date;

@SpringBootApplication
public class ProduitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProduitApplication.class, args);
	}
	/*@Bean
	CommandLineRunner start(ProduitRepository produitRepository){
		return args ->{
			ProduitFini pf1 = new ProduitFini();
			pf1.setName("Cache");
			pf1.setQuantite(1000);
			pf1.setMatiere_Premier("0.2 colorant");
			pf1.setEtat(1);
			produitRepository.save(pf1);
			ProduitConso pc1 = new ProduitConso();
			pc1.setName("colorant");
			pc1.setQuantite(1000);
			pc1.setDate(LocalDateTime.now());
			produitRepository.save(pc1);


			produitRepository.findAll().forEach(System.out::println);
		};
	}*/

}
