package com.example.Rebut.controller;


import com.example.Rebut.Repository.RebutRepository;
import com.example.Rebut.entity.Rebut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/rebut")

public class RebutController {
    @Autowired
    private RebutRepository rebutRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${produit-service.url}")
    private String produitServiceUrl;

    @GetMapping("/all")
    public Iterable<Rebut> getAllRebut(){
        return rebutRepository.findAll();
    }

    @PostMapping("/add")
    public Rebut addRebut(@RequestBody Rebut rebut){
        rebut.setDate(LocalDateTime.now());

        Rebut savedRebut = rebutRepository.save(rebut);


        // Envoi de la requête au microservice "Produit"
        String produitService = produitServiceUrl +"/ordreFabrication/addRebut/"
                + rebut.getIdProduitFini() + "/"
                + rebut.getQuantite();

        restTemplate.put(produitService, null); // appel de l'endpoint pour mettre à jour la quantité de rebut
        String produitService2 = produitServiceUrl + "/produits/soustraireQuantites";
        restTemplate.put(produitService2, rebut);

        return savedRebut;
    }
    @DeleteMapping("/{id}")
    public void deleteRebut(@PathVariable(name = "id") Long id) {
        Rebut existingRebut = rebutRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Rebut not found")
        );


        try {
            // Envoi de la requête au microservice "Produit" pour soustraire la quantité du rebut supprimé
            String produitService = produitServiceUrl + "/ordreFabrication/addRebut/"
                    + existingRebut.getIdProduitFini() + "/"
                    + (-existingRebut.getQuantite()); // Soustraire la quantité

            restTemplate.put(produitService, null); // appel de l'endpoint pour mettre à jour la quantité de rebut
        } catch (Exception e) {
            // Log de l'exception, mais continuer à supprimer le rebut
            System.err.println("Failed to update ordre fabrication: " + e.getMessage());
        }

        rebutRepository.deleteById(id);
    }
    @GetMapping("/search")
    public Iterable<Rebut> searchRebut(@RequestParam(name = "idProduit") Long idProduit){
        return rebutRepository.findByProduitId(idProduit);
    }
    @PutMapping("/{id}")
    public Rebut updateRebut(
            @PathVariable(name = "id") Long id,
            @RequestBody Rebut rebutDetails
    ) {
        Rebut existingRebut = rebutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rebut not found"));

        int ancienneQuantite = existingRebut.getQuantite();
        int nouvelleQuantite = rebutDetails.getQuantite();
        int differenceQuantite = nouvelleQuantite - ancienneQuantite;

        // 1. Mise à jour de l'ordre de fabrication (comme avant)
        String produitService = produitServiceUrl + "/ordreFabrication/updateRebut/"
                + existingRebut.getIdProduitFini() + "/"
                + nouvelleQuantite + "/"
                + ancienneQuantite;

        restTemplate.put(produitService, null);

        // 2. Mise à jour du rebut (comme avant)
        existingRebut.setIdProduitFini(rebutDetails.getIdProduitFini());
        existingRebut.setIdMachine(rebutDetails.getIdMachine());
        existingRebut.setQuantite(nouvelleQuantite);
        existingRebut.setDate(LocalDateTime.now());

        Rebut updatedRebut = rebutRepository.save(existingRebut);

        // 3. Mise à jour des quantités de ProduitConso
        if (differenceQuantite != 0) { // Vérifier s'il y a un changement de quantité
            String produitServiceUrlRecalculation = produitServiceUrl + "/produits/recalculerQuantites"
                    + "?ancienneQuantite=" + ancienneQuantite;
            restTemplate.put(produitServiceUrlRecalculation, updatedRebut);
        }

        return updatedRebut;
    }
    @GetMapping("/{id}")
    public Rebut getRebutById(@PathVariable Long id) {
        return rebutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rebut not found with id: " + id));
    }
    @GetMapping("/produit/{idProduit}")
    public Iterable<Rebut> getRebutByProduit(@PathVariable(name = "idProduit") Long idProduit){
        return rebutRepository.findByProduitId(idProduit);
    }
}
