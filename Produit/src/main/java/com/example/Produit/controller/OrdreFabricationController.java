package com.example.Produit.controller;


import com.example.Produit.Repository.CommandeRepository;
import com.example.Produit.Repository.OrdreFabricationRepository;
import com.example.Produit.entity.Commande;
import com.example.Produit.entity.OrdreFabrication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("ordreFabrication")
public class OrdreFabricationController {

    @Autowired
    private OrdreFabricationRepository ordreFabricationRepository;
    @Autowired
    private CommandeRepository commandeRepository;

    @GetMapping("/all")
    public Iterable<OrdreFabrication> getAllOrdresFabrication() {
        return ordreFabricationRepository.findAll();
    }

    @PostMapping("/add")
    public OrdreFabrication addOrdreFabrication(@RequestBody OrdreFabrication ordreFabrication) {
        Commande existanceCommande = commandeRepository.findByIdProduitFini(ordreFabrication.getIdProduitFini());
        if(existanceCommande == null){
            throw new ResourceNotFoundException("Commande not found with id: " + ordreFabrication.getIdProduitFini());
        }
        ordreFabrication.setIdCommande(existanceCommande.getId());
        ordreFabrication.setIdProduitFini(existanceCommande.getIdProduitFini());
        ordreFabrication.setQuantite(existanceCommande.getQuantite());
        return ordreFabricationRepository.save(ordreFabrication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdreFabrication> getOrdreFabricationById(@PathVariable Long id) {
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdreFabrication not found with id: " + id));
        return ResponseEntity.ok(ordreFabrication);
    }
    @PutMapping("/{id}")
    public ResponseEntity<OrdreFabrication> updateOrdreFabrication(@PathVariable Long id, @RequestBody OrdreFabrication ordreFabricationDetails) {
        OrdreFabrication ordreFabrication = ordreFabricationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdreFabrication not found with id: " + id));

        ordreFabrication.setIdProduitFini(ordreFabricationDetails.getIdProduitFini());
        ordreFabrication.setIdCommande(ordreFabricationDetails.getIdCommande());
        ordreFabrication.setIdmachine(ordreFabricationDetails.getIdmachine());
        ordreFabrication.setQuantite(ordreFabricationDetails.getQuantite());
        ordreFabrication.setQuantiteRebut(ordreFabricationDetails.getQuantiteRebut());
        ordreFabrication.setEtat(ordreFabricationDetails.getEtat());
        ordreFabrication.setDateDebut(ordreFabricationDetails.getDateDebut());
        ordreFabrication.setDateFin(ordreFabricationDetails.getDateFin());

        OrdreFabrication updatedOrdreFabrication = ordreFabricationRepository.save(ordreFabrication);
        return ResponseEntity.ok(updatedOrdreFabrication);
    }


    @DeleteMapping("/{id}")
    public void deleteOrdreFabrication(@PathVariable Long id) {
        ordreFabricationRepository.deleteById(id);
    }
    @PutMapping("/updateByMachineId/{machineId}")
    public ResponseEntity<Void> updateOrdreFabricationByMachineId(@PathVariable("machineId") Long machineId) {
        List<OrdreFabrication> ordres = ordreFabricationRepository.findByIdmachine(machineId);

        // Mettre à jour l'état de tous les ordres de fabrication liés à la machine
        for (OrdreFabrication ordre : ordres) {
            ordre.setEtat(3); // 3 indiquant une panne de machine
            ordreFabricationRepository.save(ordre);
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

        return ResponseEntity.ok().build();
    }

}
