package com.example.Produit.controller;


import com.example.Produit.Repository.CommandeRepository;
import com.example.Produit.Repository.OrdreFabricationRepository;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.Repository.Produit_fini_Repository;
import com.example.Produit.entity.*;
import com.example.consommation.entity.QuantiteMatiereConso;
import com.example.consommation.entity.consommation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("ordreFabrication")
public class OrdreFabricationController {

    @Autowired
    private OrdreFabricationRepository ordreFabricationRepository;
    @Autowired
    private CommandeRepository commandeRepository;
    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private Produit_fini_Repository produitFiniRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${consommation-service.url}")
    private String consoServiceUrl;

    @GetMapping("/all")
    public Iterable<OrdreFabrication> getAllOrdresFabrication() {
        return ordreFabricationRepository.findAll();
    }
    @GetMapping("/commande/{id}")
    public Commande getCommande(@PathVariable Long id){
        return commandeRepository.findByIdProduitFini(id);
    }
    @PostMapping("/add")
    public OrdreFabrication addOrdreFabrication(@RequestBody OrdreFabrication ordreFabrication) {
        Commande existanceCommande = commandeRepository.findByIdProduitFini(ordreFabrication.getIdProduitFini());
        if (existanceCommande == null) {
            throw new ResourceNotFoundException("Commande not found with id: " + ordreFabrication.getIdProduitFini());
        }
        ordreFabrication.setIdCommande(existanceCommande.getId());
        ordreFabrication.setIdProduitFini(existanceCommande.getIdProduitFini());
        existanceCommande.setQuantiteRestante(existanceCommande.getQuantiteRestante() - ordreFabrication.getQuantite());
        if (ordreFabrication.getEtat() == 2) {
            createConsommationForOrdreFabrication(ordreFabrication);
        }
        Optional<ProduitFini> produitFini = produitFiniRepository.findById(ordreFabrication.getIdProduitFini());
        if (existanceCommande.getQuantiteRestante() == 0) {
            produitFini.get().setEtat(2);
        } else if (existanceCommande.getQuantiteRestante() > 0) {
            produitFini.get().setEtat(1);
        }

        return ordreFabricationRepository.save(ordreFabrication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdreFabrication> getOrdreFabricationById(@PathVariable Long id) {
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdreFabrication not found with id: " + id));
        return ResponseEntity.ok(ordreFabrication);
    }
    @PutMapping("/{id}")
    public ResponseEntity<OrdreFabrication> updateOrdreFabrication(
            @PathVariable Long id,
            @RequestBody OrdreFabrication ordreFabricationDetails) {

        OrdreFabrication existingOrdreFabrication = ordreFabricationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdreFabrication not found with id: " + id));
        Commande associatedCommande = commandeRepository.findById(existingOrdreFabrication.getIdCommande())
                .orElseThrow(() -> new ResourceNotFoundException("Commande not found with id: " + existingOrdreFabrication.getIdCommande()));
        associatedCommande.setId(existingOrdreFabrication.getIdCommande());
        int difference = ordreFabricationDetails.getQuantite() - existingOrdreFabrication.getQuantite();
        if (difference < 0) {
            // Si la quantité diminue, ajouter la différence (valeur absolue) à la quantité restante
            associatedCommande.setQuantiteRestante(associatedCommande.getQuantiteRestante() + Math.abs(difference));
        } else if (difference > 0) {
            associatedCommande.setQuantiteRestante(associatedCommande.getQuantiteRestante() - difference);
        }
        commandeRepository.save(associatedCommande);

        // Mise à jour des propriétés de l'ordre de fabrication
        existingOrdreFabrication.setIdProduitFini(ordreFabricationDetails.getIdProduitFini());
        existingOrdreFabrication.setIdCommande(associatedCommande.getId());
        existingOrdreFabrication.setIdmachine(ordreFabricationDetails.getIdmachine());
        existingOrdreFabrication.setQuantite(ordreFabricationDetails.getQuantite());
        existingOrdreFabrication.setQuantiteRebut(ordreFabricationDetails.getQuantiteRebut());
        existingOrdreFabrication.setEtat(ordreFabricationDetails.getEtat());
        existingOrdreFabrication.setDateDebut(ordreFabricationDetails.getDateDebut());
        existingOrdreFabrication.setDateFin(ordreFabricationDetails.getDateFin());

        OrdreFabrication updatedOrdreFabrication = ordreFabricationRepository.save(existingOrdreFabrication);
        Commande Commande = commandeRepository.findById(existingOrdreFabrication.getIdCommande())
                .orElseThrow(() -> new ResourceNotFoundException("Commande not found with id: " + existingOrdreFabrication.getIdCommande()));
        Optional<ProduitFini> produitFini = produitFiniRepository.findById(updatedOrdreFabrication.getIdProduitFini());
        if (Commande.getQuantiteRestante() == 0) {
            produitFini.get().setEtat(2);
        } else if (Commande.getQuantiteRestante() > 0) {
            produitFini.get().setEtat(1);
        }
        produitFiniRepository.save(produitFini.get());

        if (updatedOrdreFabrication.getEtat() == 2) {
            createConsommationForOrdreFabrication(updatedOrdreFabrication);
        }

        return ResponseEntity.ok(updatedOrdreFabrication);
    }

    private void createConsommationForOrdreFabrication(OrdreFabrication ordreFabrication) {
        consommation newConsommation = new consommation();
        newConsommation.setIdProduitFini(ordreFabrication.getIdProduitFini());
        newConsommation.setIdMachine(ordreFabrication.getIdmachine());

        List<QuantiteMatiereConso> quantiteMatiereConsoList = new ArrayList<>();

        // Obtenir le produit fini associé à l'ordre de fabrication
        ProduitFini produitFini = (ProduitFini) produitRepository.findById(ordreFabrication.getIdProduitFini())
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + ordreFabrication.getIdProduitFini()));

        // Si quantiteRebut est 0, utilisez les quantités des matières premières multipliées par la quantité du produit fini
        if (ordreFabrication.getQuantiteRebut() == 0) {
            for (MatierePremier matiere : produitFini.getMatieresPremieres()) {
                QuantiteMatiereConso qmc = new QuantiteMatiereConso();
                qmc.setNomMatiere(matiere.getName());
                qmc.setQuantite(matiere.getQuantite() * ordreFabrication.getQuantite());
                quantiteMatiereConsoList.add(qmc);
            }
        } else { // Si l'ordre de fabrication est terminé, ajoutez également les quantités de rebut
            for (MatierePremier matiere : produitFini.getMatieresPremieres()) {
                QuantiteMatiereConso qmc = new QuantiteMatiereConso();
                qmc.setNomMatiere(matiere.getName());
                // Calculer la quantité totale en ajoutant la quantité normale et la quantité de rebut
                qmc.setQuantite((matiere.getQuantite() * ordreFabrication.getQuantite()) +
                        (matiere.getQuantite() * ordreFabrication.getQuantiteRebut()));
                quantiteMatiereConsoList.add(qmc);
            }
        }

        newConsommation.setQuantiteMatiereConso(quantiteMatiereConsoList);

        String createConsommationUrl = consoServiceUrl + "/consommations/add";
        restTemplate.postForEntity(createConsommationUrl, newConsommation, consommation.class);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdreFabrication(@PathVariable Long id) {
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdreFabrication not found with id: " + id));

        if (ordreFabrication.getEtat() == 0) {
            Commande commande = commandeRepository.findById(ordreFabrication.getIdCommande())
                    .orElseThrow(() -> new ResourceNotFoundException("Commande not found with id: " + ordreFabrication.getIdCommande()));
            commande.setQuantiteRestante(commande.getQuantiteRestante() + ordreFabrication.getQuantite());
            commandeRepository.save(commande);
        }
        Commande commande = commandeRepository.findById(ordreFabrication.getIdCommande())
                .orElseThrow(() -> new ResourceNotFoundException("Commande not found with id: " + ordreFabrication.getIdCommande()));
        Optional<ProduitFini> produitFini = produitFiniRepository.findById(ordreFabrication.getIdProduitFini());
        if (commande.getQuantiteRestante() == 0) {
            produitFini.get().setEtat(2);
        } else if (commande.getQuantiteRestante() == produitFini.get().getQuantite()) {
            produitFini.get().setEtat(0);
        }
        else{
            produitFini.get().setEtat(1);
        }
        produitFiniRepository.save(produitFini.get());

        ordreFabricationRepository.delete(ordreFabrication);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/updateByMachineId/{machineId}")
    public ResponseEntity<Void> updateOrdreFabricationByMachineId(@PathVariable("machineId") Long machineId) {
        List<OrdreFabrication> ordres = ordreFabricationRepository.findByIdmachine(machineId);

        // Mettre à jour l'état de tous les ordres de fabrication liés à la machine
        for (OrdreFabrication ordre : ordres) {
            if (ordre.getEtat() != 2) {
                ordre.setEtat(3); // 3 indiquant une panne de machine
                ordreFabricationRepository.save(ordre);
            }
        }

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/addRebut/{idProduitFini}/{quantite}")
    public ResponseEntity<Void> addRebut(@PathVariable Long idProduitFini, @PathVariable int quantite) {
        // Rechercher l'ordre de fabrication avec idProduitFini
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findByIdProduitFini(idProduitFini);

        if (ordreFabrication == null) {
            return ResponseEntity.notFound().build(); // si aucun ordre de fabrication trouvé
        }

        // Ajouter la quantité au quantiteRebut
        ordreFabrication.setQuantiteRebut(ordreFabrication.getQuantiteRebut() + quantite);
        if (quantite != 0) {
            // Récupération du produit fini associé à l'ordre de fabrication
            ProduitFini produitFini = (ProduitFini) produitRepository.findById(idProduitFini)
                    .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + idProduitFini));

            // Calcul de la quantité de matières premières des rebut
            List<MatierePremier> matieresPremieres = produitFini.getMatieresPremieres();
            for (MatierePremier matiere : matieresPremieres) {
                // Soustraction de la quantité de matière première des rebut du ProduitConso
                ProduitConso produitConso = produitRepository.findProduitConsoByName(matiere.getName());
                if (produitConso != null) {
                    float quantiteRequise = matiere.getQuantite() * quantite;

                    produitConso.setQuantite(produitConso.getQuantite() - quantiteRequise);
                    produitRepository.save(produitConso);
                }
            }
        }

        ordreFabricationRepository.save(ordreFabrication);

        return ResponseEntity.ok().build(); // retourner une réponse appropriée
    }
    @PutMapping("/updateRebut/{idProduitFini}/{nouvelleQuantite}/{ancienneQuantite}")
    public ResponseEntity<Void> updateQuantiteRebut(
            @PathVariable Long idProduitFini,
            @PathVariable int nouvelleQuantite,
            @PathVariable int ancienneQuantite
    ) {
        // Rechercher l'ordre de fabrication par ID du produit fini
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findByIdProduitFini(idProduitFini);

        if (ordreFabrication == null) {
            return ResponseEntity.notFound().build();
        }

        // Calculer la différence de quantités
        int difference = nouvelleQuantite - ancienneQuantite;

        // Mettre à jour la quantité de rebut
        ordreFabrication.setQuantiteRebut(ordreFabrication.getQuantiteRebut() + difference);

        // Sauvegarder l'ordre de fabrication mis à jour
        ordreFabricationRepository.save(ordreFabrication);
        if (nouvelleQuantite != 0) {
            // Récupération du produit fini associé à l'ordre de fabrication
            ProduitFini produitFini = (ProduitFini) produitRepository.findById(idProduitFini)
                    .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + idProduitFini));

            // Calcul de la quantité de matières premières des rebut
            List<MatierePremier> matieresPremieres = produitFini.getMatieresPremieres();
            for (MatierePremier matiere : matieresPremieres) {
                // Soustraction de la quantité de matière première des rebut du ProduitConso
                ProduitConso produitConso = produitRepository.findProduitConsoByName(matiere.getName());
                if (produitConso != null) {
                    float quantiteRequise = matiere.getQuantite() * nouvelleQuantite;

                    produitConso.setQuantite(produitConso.getQuantite() - quantiteRequise);
                    produitRepository.save(produitConso);
                }
            }
        }

        return ResponseEntity.ok().build();
    }

}
