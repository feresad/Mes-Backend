package com.example.machine;

import com.example.machine.entity.Machine;
import com.example.machine.entity.Panne;
import com.example.machine.repository.MachineRepository;
import com.example.machine.repository.PanneRepository;
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

	/*@Bean
	CommandLineRunner start(MachineRepository machineRepository, PanneRepository panneRepository){
		return args -> {
			machineRepository.save(new Machine(null, "premier machine", true,1L));
			machineRepository.save(new Machine(null, "deuxieme machine", false,2L));
			machineRepository.findAll().forEach(System.out::println);
			panneRepository.save(new Panne(null, "Aucune Panne"));
			panneRepository.save(new Panne(null, "Arret Indetermine"));
			panneRepository.save(new Panne(null, "Maintenance moule"));
			panneRepository.save(new Panne(null, "Manque effectif"));
			panneRepository.save(new Panne(null, "Rupture Matiere"));
			panneRepository.save(new Panne(null, "Maintenance generale"));
			panneRepository.save(new Panne(null, "Panne Production"));
			panneRepository.save(new Panne(null, "Att decesion qualite"));
			panneRepository.save(new Panne(null, "Panne Servitude"));
			panneRepository.save(new Panne(null, "Essai et val. tech."));
			panneRepository.save(new Panne(null, "Maint. Prev. Moule"));
			panneRepository.save(new Panne(null, "Changement moule"));
			panneRepository.save(new Panne(null, "Reglage"));
			panneRepository.save(new Panne(null, "Redemarrage"));
			panneRepository.save(new Panne(null, "Manque peripherique"));
			panneRepository.save(new Panne(null, "Essai Production"));
			panneRepository.save(new Panne(null, "Retour Moule"));
			panneRepository.findAll().forEach(System.out::println);


		};
	}*/

}
