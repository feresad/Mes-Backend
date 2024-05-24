package com.example.machine.Controller;

import com.example.machine.Service.EmailService;
import com.example.machine.entity.Machine;
import com.example.machine.entity.Panne;
import com.example.machine.entity.UserInfoResponse;
import com.example.machine.repository.MachineRepository;
import com.example.machine.repository.PanneRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


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
    @Value("${produit-service.url}")
    private String produitServiceUrl;
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
        machine.setDate(LocalDateTime.now());
        return MachineRepository.save(machine);
    }
    @GetMapping("/enpanne")
    public List<Machine> getMachinesEnPanne() {
        // Obtenir les machines avec un tri par date décroissant
        return MachineRepository.findByEtatFalse(Sort.by(Sort.Direction.DESC, "date"));
    }
    @DeleteMapping("/{id}")
    public void deleteMachine(@PathVariable(name = "id") Long id){
        MachineRepository.deleteById(id);
    }
    @PutMapping("/{id}")
    public Machine updateMachine(@PathVariable(name = "id") Long id, @RequestBody Machine machine){
        Optional<Machine> optionalMachine = MachineRepository.findById(id);
        Machine machineexistant = MachineRepository.findById(id).get();
        machineexistant.setName(machine.getName());

        if(optionalMachine.isPresent()) {
            Machine existingMachine = optionalMachine.get();
            boolean previousState = existingMachine.isEtat(); // Obtenez l'état précédent
            existingMachine.setEtat(machine.isEtat());
            existingMachine.setDate(LocalDateTime.now());
            if (!existingMachine.isEtat() && machine.isEtat()) {
                existingMachine.getPannes().clear();
            }
            Machine updatedMachine = MachineRepository.save(existingMachine);

            // Vérifiez si l'état a changé de true à false
            if (previousState && !machine.isEtat()) {
                if (machine.getPannes() != null) {
                    Set<Panne> pannes = new HashSet<>();
                    for (Panne panneInput : machine.getPannes()) {
                        // Fetch the actual Panne entity from the database
                        Optional<Panne> panneOptional = panneRepository.findById(panneInput.getId());
                        if (panneOptional.isPresent()) {
                            pannes.add(panneOptional.get());
                        }
                    }
                    existingMachine.setPannes(pannes);
                }
                // Appel REST vers le service Produit pour mettre à jour l'état de l'ordre de fabrication
               String updateOrdreFabricationUrl = produitServiceUrl + "/ordreFabrication/updateByMachineId/" + id;
                try {
                    restTemplate.put(updateOrdreFabricationUrl, null);
                } catch (HttpClientErrorException e) {
                    System.err.println("Erreur lors de la mise à jour de l'ordre de fabrication : " + e.getMessage());
                }
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String formattedDate = updatedMachine.getDate().format(dateFormatter);
                String formattedTime = updatedMachine.getDate().format(timeFormatter);

                List<String> adminEmails = getAllAdminEmails();
                String subject = "Panne de machine: " + updatedMachine.getName();

                for (String adminEmail : adminEmails) {
                    emailService.sendMachinePanneEmail(adminEmail, subject, updatedMachine.getName(), formattedDate, formattedTime, updatedMachine.getPannes());
                }
            }
            return updatedMachine;
        } else {
            // Gérer le cas où la machine n'est pas trouvée
            return null;
        }
    }
    @PutMapping("/add-panne/{machineId}")
    public ResponseEntity<Machine> addPannesToMachine(
            @PathVariable("machineId") Long machineId,
            @RequestBody Map<String, Object> requestData
    ) {
        Optional<Machine> optionalMachine = MachineRepository.findById(machineId);

        if (optionalMachine.isPresent()) {
            Machine machine = optionalMachine.get();

            // Extract panneIds and username
            List<Long> panneIds = (List<Long>) requestData.get("panneIds");
            String username = (String) requestData.get("username");

            // Fetch Pannes
            Set<Panne> pannes = panneRepository.findAllById(panneIds).stream().collect(Collectors.toSet());

            machine.getPannes().addAll(pannes);
            machine.setUsername(username);
            machine = MachineRepository.save(machine);

            return ResponseEntity.ok(machine);
        } else {
            return ResponseEntity.notFound().build();
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
            // Gérer les erreurs de désérialisation ou de réponse de l'API
            System.err.println("Erreur lors de la récupération des utilisateurs admin : " + response.getStatusCode());
            return Collections.emptyList();
        }
    }

    @GetMapping("/statistiques")
    public Map<String, Long> getMachineStatistiques() {
        Map<String, Long> statistiques = new HashMap<>();
        long totalMachines = MachineRepository.count();
        long machinesEnMarche = MachineRepository.findByEtat(true).spliterator().getExactSizeIfKnown();
        long machinesEnPanne = MachineRepository.findByEtat(false).spliterator().getExactSizeIfKnown();

        statistiques.put("total", totalMachines);
        statistiques.put("enMarche", machinesEnMarche);
        statistiques.put("enPanne", machinesEnPanne);

        return statistiques;
    }
}
