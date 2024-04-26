package com.example.machine.Controller;

import com.example.machine.Service.EmailService;
import com.example.machine.entity.Machine;
import com.example.machine.entity.Panne;
import com.example.machine.entity.UserInfoResponse;
import com.example.machine.repository.MachineRepository;
import com.example.machine.repository.PanneRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("machines")
public class MachineController {
     @Autowired
    private MachineRepository MachineRepository;
     @Autowired
     private PanneRepository panneRepository;
     @Autowired
     private EmailService emailService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${auth-service.url}")
    private String authServiceUrl;

    @GetMapping("/all")
    public Iterable<Machine> getAllMachines(){
        return MachineRepository.findAll();
    }
    @GetMapping("/{id}")
    public Machine getMachinebyId(@PathVariable(name = "id") Long id){
        return MachineRepository.findById(id).get();
    }
    @PostMapping("/add")
    public Machine addMachine(@RequestBody Machine machine){
        machine.setEtat(true);
        return MachineRepository.save(machine);
    }
    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable(name = "id") Long id){
        MachineRepository.deleteById(id);
    }
    @PutMapping("/{id}")
    public Machine updateMachine(@PathVariable(name = "id") Long id, @RequestBody Machine machine){
        Optional<Machine> optionalMachine = MachineRepository.findById(id);
        if(optionalMachine.isPresent()) {
            Machine existingMachine = optionalMachine.get();
            boolean previousState = existingMachine.isEtat(); // Obtenez l'état précédent
            existingMachine.setEtat(machine.isEtat());
            Machine updatedMachine = MachineRepository.save(existingMachine);
            // Vérifiez si l'état a changé de true à false
            if (previousState && !machine.isEtat()) {
                // Envoie d'e-mail à tous les administrateurs
                List<String> adminEmails = getAllAdminEmails();
                String subject = "Changement d'état de la machine";
                String body = "la machine '" + updatedMachine.getName() + "' est tombée en panne.";
                for (String adminEmail : adminEmails) {
                    emailService.sendSimpleEmail(adminEmail, subject, body);
                }
            }
            return updatedMachine;
        } else {
            // Gérer le cas où la machine n'est pas trouvée
            return null;
        }
    }
    @GetMapping("/count")
    public Long countMachine(){
        return MachineRepository.count();
    }
    @GetMapping("/search")
    public Iterable<Machine> searchMachine(@RequestParam(name = "etat") boolean etat){
        return MachineRepository.findByEtat(etat);
    }
    @GetMapping("/searchMachine")
    public Iterable<Machine> searchMachine(@RequestParam(name = "name") String name){
        return MachineRepository.searchMachineByName(name);
    }
    @GetMapping("/pannes")
    public List<Panne> getAllPannes(){
        return panneRepository.findAll();
    }
    @GetMapping("/pannes/{id}")
    public Panne getPanneById(@PathVariable(name = "id") Long id){
        return panneRepository.findById(id).get();
    }

    private List<String> getAllAdminEmails() {
        ResponseEntity<List<UserInfoResponse>> response = restTemplate.exchange(
                authServiceUrl + "/users/admin",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserInfoResponse>>() {});

        if (response.getStatusCode() == HttpStatus.OK) {
            List<UserInfoResponse> adminUsers = response.getBody();
            List<String> adminEmails = new ArrayList<>();
            for (UserInfoResponse user : adminUsers) {
                adminEmails.add(user.getEmail());
            }
            return adminEmails;
        } else {
            // Gérer les erreurs
            return Collections.emptyList();
        }
    }

}
