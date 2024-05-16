package com.example.Produit.controller;

import com.example.Produit.Repository.CommandeRepository;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.Repository.Produit_fini_Repository;
import com.example.Produit.entity.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    @Autowired
    private Produit_fini_Repository produitFiniRepository;


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

        return ResponseEntity.noContent().build();
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
    public ResponseEntity<ProduitFini> addProduitFini(@RequestBody ProduitFini produitFini){
        // Vérifier si le stock est suffisant
        boolean stockSuffisant = checkStockSuffisant(produitFini);
        if (!stockSuffisant) {
            throw new ResourceNotFoundException("Stock insuffisant pour le produit fini.");
        }
        produitFini.setEtat(0);

        // Enregistrer le produit fini
        ProduitFini savedProduitFini = produitRepository.save(produitFini);

        // Créer une commande
        Commande commande = new Commande();
        commande.setIdProduitFini(savedProduitFini.getId());
        commande.setQuantite((int) savedProduitFini.getQuantite());
        commandeRepository.save(commande);

        // Soustraire les quantités des matières premières du ProduitConso
        soustraireQuantites(savedProduitFini);

        return ResponseEntity.ok(savedProduitFini);
    }

    private void soustraireQuantites(ProduitFini produitFini) {
        for (MatierePremier matiere : produitFini.getMatieresPremieres()) {
            float quantiteRequise = matiere.getQuantite() * produitFini.getQuantite();
            ProduitConso produitConso = produitRepository.findProduitConsoByName(matiere.getName());

            if (produitConso != null) {
                produitConso.setQuantite(produitConso.getQuantite() - quantiteRequise);
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

        // Assurez-vous que l'ID de la requête correspond à l'ID du produit fini
        if (!id.equals(produitFini.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        // Trouver le produit fini existant ou renvoyer une erreur si introuvable
        ProduitFini existingProduitFini = (ProduitFini) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + id));

        // Obtenez les anciennes et nouvelles quantités du produit fini
        float ancienneQuantiteProduitFini = existingProduitFini.getQuantite();
        float nouvelleQuantiteProduitFini = produitFini.getQuantite();
        float differenceProduitFini = nouvelleQuantiteProduitFini - ancienneQuantiteProduitFini;

        // Ajuster les quantités de matières premières et les commandes si nécessaire
        Map<String, Float> anciennesQuantitesMatierePremiere = existingProduitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        Map<String, Float> nouvellesQuantitesMatierePremiere = produitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        // Recalculer et ajuster Plan_Produit selon les nouvelles quantités
        for (String matiereNom : nouvellesQuantitesMatierePremiere.keySet()) {
            float ancienneQuantite = anciennesQuantitesMatierePremiere.getOrDefault(matiereNom, 0f);
            float nouvelleQuantite = nouvellesQuantitesMatierePremiere.get(matiereNom);
            float difference = nouvelleQuantite - ancienneQuantite;

            // Obtenez le produit consommable associé
            ProduitConso produitConso = produitRepository.findProduitConsoByName(matiereNom);

            if (produitConso != null) {
                // Ajuster le stock de ProduitConso en fonction de la différence
                if (difference > 0) {
                    produitConso.setQuantite(produitConso.getQuantite() - difference * nouvelleQuantiteProduitFini);
                } else if (difference < 0) {
                    produitConso.setQuantite(produitConso.getQuantite() + Math.abs(difference) * nouvelleQuantiteProduitFini);
                }

                // Vérifiez pour éviter des valeurs négatives
                if (produitConso.getQuantite() < 0) {
                    throw new ResourceNotFoundException("Stock insuffisant pour la matière première: " + matiereNom);
                }

                produitRepository.save(produitConso);
            }
        }

        // Mettre à jour le produit fini avec les nouvelles quantités et données
        existingProduitFini.setName(produitFini.getName());
        existingProduitFini.setQuantite(nouvelleQuantiteProduitFini);
        existingProduitFini.setMatieresPremieres(produitFini.getMatieresPremieres());
        existingProduitFini.setEtat(produitFini.getEtat());

        // Sauvegarder les mises à jour du produit fini
        ProduitFini updatedProduitFini = produitRepository.save(existingProduitFini);

        return ResponseEntity.ok(updatedProduitFini);
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
    @GetMapping("/statistiques")
    public Map<String, Long> getProduitFiniStatistiques() {
        Map<String, Long> statistiques = new HashMap<>();
        long totalProduitsFini = produitFiniRepository.count();
        long produitsFiniAucun = produitFiniRepository.countByEtat(0);
        long produitsFiniEnCours = produitFiniRepository.countByEtat(1);
        long produitsFiniTermine = produitFiniRepository.countByEtat(2);

        statistiques.put("total", totalProduitsFini);
        statistiques.put("Aucun traitement", produitsFiniAucun);
        statistiques.put("enCours", produitsFiniEnCours);
        statistiques.put("terminer",produitsFiniTermine);

        return statistiques;
    }
}