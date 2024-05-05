package com.example.Produit.controller;

import com.example.Produit.Repository.CommandeRepository;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.*;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("produits")
public class ProduitController {
    @Autowired
    private ProduitRepository produitRepository;
    @Autowired
    private CommandeRepository commandeRepository;


    @GetMapping("/all")
    public Iterable<Produit> getAllProduits(){
        return produitRepository.findAll();
    }

    @GetMapping("/produitFini")
    public List<ProduitFini> getProduitFini(){
        return produitRepository.findAllBy();
    }
    @GetMapping("/produitFini/{id}")
    public ProduitFini getProduitFiniById(@PathVariable(name = "id") Long id){
        return (ProduitFini) produitRepository.findById(id).get();
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduit(@PathVariable(name = "id") Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit not found with id: " + id));

        produitRepository.delete(produit);

        return ResponseEntity.noContent().build(); // Renvoie une réponse vide avec un statut 204
    }
    @GetMapping("/produitConso")
    public List<ProduitConso> getProduitConso(){
        return produitRepository.findAllProduitConso();
    }

    @GetMapping("/{id}")
    Produit getProduitbyId(@PathVariable(name = "id") Long id){
        return produitRepository.findById(id).get();
    }
    @PostMapping("/addFini")
    public ResponseEntity<ProduitFini> addProduitFini(@RequestBody ProduitFini produitFini) {
        boolean stockSuffisant = checkStockSuffisant(produitFini);

        if (!stockSuffisant) {
            throw new ResourceNotFoundException("Stock insuffisant pour le produit Consommable .");

        }
        produitFini.setEtat(0);

        ProduitFini savedProduitFini = produitRepository.save(produitFini);
        Commande commande = new Commande();
        commande.setIdProduitFini(savedProduitFini.getId());
        commande.setQuantite((int) savedProduitFini.getQuantite());

        // Sauvegarder la commande
        commandeRepository.save(commande);


        // Calculez les quantités requises pour les matières premières
        soustraireQuantites(savedProduitFini);

        return ResponseEntity.ok(savedProduitFini);
    }

    private void soustraireQuantites(ProduitFini produitFini) {
        // Calculez les quantités requises pour les matières premières
        for (MatierePremier matiere : produitFini.getMatieresPremieres()) {
            float quantiteRequise = matiere.getQuantite() * produitFini.getQuantite();
            ProduitConso produitConso = produitRepository.findProduitConsoByName(matiere.getName());

            if (produitConso != null) {
                float nouvelleQuantite = produitConso.getQuantite() - quantiteRequise;
                produitConso.setQuantite(nouvelleQuantite);
                produitRepository.save(produitConso);
            }
        }
    }

    @PostMapping("/addConso")
    public ProduitConso addProduitConso(@RequestBody ProduitConso produitConso){
        produitConso.setDate(LocalDateTime.now());
        return produitRepository.save(produitConso);
    }

    @GetMapping("/count")
    public Long countProduit(){
        return produitRepository.count();
    }
    // search by name
    @GetMapping("/search")
    public List<Produit> searchProduit(@RequestParam(name = "name") String name){
        return produitRepository.findByNameContains(name);
    }

    @PutMapping("/fini/{id}")
    public ResponseEntity<ProduitFini> updateProduitFini(
            @PathVariable Long id,
            @RequestBody ProduitFini produitFini) {

        if (!id.equals(produitFini.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        ProduitFini existingProduitFini = (ProduitFini) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + id));

        Commande existingCommande = commandeRepository.findByIdProduitFini(existingProduitFini.getId());

        if (existingCommande == null) {
            throw new ResourceNotFoundException("Commande not found for product: " + existingProduitFini.getName());
        }

        float ancienneQuantiteProduitFini = existingProduitFini.getQuantite();
        float nouvelleQuantiteProduitFini = produitFini.getQuantite();

        // Si la quantité du produit fini a changé, ajuster le produit consommable
        if (ancienneQuantiteProduitFini != nouvelleQuantiteProduitFini) {
            float differenceProduitFini = nouvelleQuantiteProduitFini - ancienneQuantiteProduitFini;

            // Ajuster les matières premières en fonction du changement de quantité
            Map<String, Float> anciennesQuantitesMatierePremiere = existingProduitFini.getMatieresPremieres().stream()
                    .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

            for (Map.Entry<String, Float> entry : anciennesQuantitesMatierePremiere.entrySet()) {
                String matiereNom = entry.getKey();
                float ancienneQuantiteMatiere = entry.getValue();
                ProduitConso produitConso = produitRepository.findProduitConsoByName(matiereNom);

                if (produitConso != null) {
                    float quantiteRequise = ancienneQuantiteMatiere * differenceProduitFini;
                    produitConso.setQuantite(produitConso.getQuantite() - quantiteRequise);
                    if(produitConso.getQuantite() < 0){
                        throw new ResourceNotFoundException("Stock insuffisant pour le produit Consommable .");
                    }else{
                        produitRepository.save(produitConso);
                    }
                }
            }

            // Mettre à jour la commande
            existingCommande.setQuantite((int) nouvelleQuantiteProduitFini);
            commandeRepository.save(existingCommande);
        }

        // Traiter les modifications des quantités des matières premières
        Map<String, Float> anciennesQuantitesMatierePremiere = existingProduitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        Map<String, Float> nouvellesQuantitesMatierePremiere = produitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        for (String matiereNom : nouvellesQuantitesMatierePremiere.keySet()) {
            float ancienneQuantite = anciennesQuantitesMatierePremiere.getOrDefault(matiereNom, 0f);
            float nouvelleQuantite = nouvellesQuantitesMatierePremiere.get(matiereNom);

            float difference = nouvelleQuantite - ancienneQuantite;

            ProduitConso produitConso = produitRepository.findProduitConsoByName(matiereNom);

            if (produitConso != null) {
                if (difference > 0) {
                    // Si la différence est positive, besoin de plus de matière première
                    produitConso.setQuantite(produitConso.getQuantite() - difference * nouvelleQuantiteProduitFini);
                } else if (difference < 0) {
                    // Si la différence est négative, moins de matière première requise
                    produitConso.setQuantite(produitConso.getQuantite() - difference * nouvelleQuantiteProduitFini);
                }
                if(produitConso.getQuantite() < 0){
                    throw new ResourceNotFoundException("Stock insuffisant pour le produit Consommable .");}
                else {
                    produitRepository.save(produitConso);
                }
                }
        }

        // Mettre à jour le produit fini avec les nouvelles valeurs
        existingProduitFini.setName(produitFini.getName());
        existingProduitFini.setQuantite(nouvelleQuantiteProduitFini);
        existingProduitFini.setEtat(produitFini.getEtat());
        existingProduitFini.setMatieresPremieres(produitFini.getMatieresPremieres());

        ProduitFini updatedProduitFini = produitRepository.save(existingProduitFini);

        return ResponseEntity.ok(updatedProduitFini);
    }
    @PutMapping("/conso/{id}")
    public ResponseEntity<ProduitConso> updateProduitConso(@PathVariable(name = "id") Long id,
                                                           @RequestBody ProduitConso produitConso) {
        produitConso.setId(id);
        // errors (ex: product not found)
        ProduitConso existingProduitConso = (ProduitConso) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitConso not found with id: " + id));

        // Update fields
        existingProduitConso.setDate(LocalDateTime.now());
        existingProduitConso.setName(produitConso.getName());
        existingProduitConso.setQuantite(produitConso.getQuantite());

        ProduitConso updatedProduitConso = produitRepository.save(existingProduitConso);
        return ResponseEntity.ok(updatedProduitConso);
    }
    private boolean checkStockSuffisant(ProduitFini produitFini) {
        for (MatierePremier matiere : produitFini.getMatieresPremieres()) {
            ProduitConso produitConso = produitRepository.findProduitConsoByName(matiere.getName());

            if (produitConso != null) {
                float quantiteRequise = matiere.getQuantite() * produitFini.getQuantite();

                if (produitConso.getQuantite() < quantiteRequise) {
                    return false; // Stock insuffisant
                }
            }
        }

        return true; // Stock suffisant
    }

}