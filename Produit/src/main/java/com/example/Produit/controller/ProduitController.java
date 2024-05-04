package com.example.Produit.controller;

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
        produitFini.setEtat(0);

        ProduitFini savedProduitFini = produitRepository.save(produitFini);

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

        // Vérifiez que l'ID correspond
        if (!id.equals(produitFini.getId())) {
            return ResponseEntity.badRequest().body(null);
        }

        // Récupérer le ProduitFini existant
        ProduitFini existingProduitFini = (ProduitFini) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + id));

        // Récupérer la quantité globale du produit fini
        float quantiteGlobale = produitFini.getQuantite();

        // Cartographier les anciennes et nouvelles quantités des matières premières
        Map<String, Float> anciennesQuantites = existingProduitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        Map<String, Float> nouvellesQuantites = produitFini.getMatieresPremieres().stream()
                .collect(Collectors.toMap(MatierePremier::getName, MatierePremier::getQuantite));

        // Traitez les ajustements de produits consommables en fonction des différences de quantité
        for (Map.Entry<String, Float> entry : anciennesQuantites.entrySet()) {
            String matiereName = entry.getKey();
            Float ancienneQuantite = entry.getValue();

            if (nouvellesQuantites.containsKey(matiereName)) {
                Float nouvelleQuantite = nouvellesQuantites.get(matiereName);

                // Calculer la différence multipliée par la quantité globale du produit fini
                float difference = (nouvelleQuantite - ancienneQuantite) * quantiteGlobale;

                // Trouver le ProduitConso correspondant
                ProduitConso produitConso = produitRepository.findProduitConsoByName(matiereName);

                if (produitConso != null) {
                    float quantiteConso = produitConso.getQuantite();

                    if (difference < 0) {
                        // Si la différence est négative, ajouter la différence (augmentation de la quantité consommable)
                        quantiteConso += Math.abs(difference);
                    } else if (difference > 0) {
                        // Si la différence est positive, soustraire la différence (diminution de la quantité consommable)
                        quantiteConso -= Math.abs(difference);
                    }

                    // Mettre à jour la quantité du ProduitConso
                    produitConso.setQuantite(quantiteConso);
                    produitRepository.save(produitConso);
                }
            }
        }

        // Mettre à jour les détails du ProduitFini avec les nouvelles données
        existingProduitFini.setName(produitFini.getName());
        existingProduitFini.setQuantite(produitFini.getQuantite());
        existingProduitFini.setEtat(produitFini.getEtat());
        existingProduitFini.setMatieresPremieres(produitFini.getMatieresPremieres());

        // Sauvegarder le ProduitFini mis à jour
        ProduitFini updatedProduitFini = produitRepository.save(existingProduitFini);

        return ResponseEntity.ok(updatedProduitFini);
    }
    @PutMapping("/conso/{id}")
    public ResponseEntity<ProduitConso> updateProduitConso(@PathVariable(name = "id") Long id,
                                                           @RequestBody ProduitConso produitConso) {
        // Ensure the ID in the URL matches the product being updated
        produitConso.setId(id);

        // Handle potential errors (e.g., product not found)
        ProduitConso existingProduitConso = (ProduitConso) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitConso not found with id: " + id));

        // Update fields
        existingProduitConso.setDate(LocalDateTime.now());
        existingProduitConso.setName(produitConso.getName());
        existingProduitConso.setQuantite(produitConso.getQuantite());

        ProduitConso updatedProduitConso = produitRepository.save(existingProduitConso);
        return ResponseEntity.ok(updatedProduitConso);
    }

}