package com.example.Produit.controller;

import com.example.Produit.ProduitApplication;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import javax.servlet.http.HttpServletRequest;

import com.example.Produit.entity.ProduitConso;
import com.example.Produit.entity.ProduitFini;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @GetMapping("/produitConso")
    public List<ProduitConso> getProduitConso(){
        return produitRepository.findAllProduitConso();
    }

    @GetMapping("/{id}")
    Produit getProduitbyId(@PathVariable(name = "id") Long id){
        return produitRepository.findById(id).get();
    }
    @PostMapping("/addFini")
    public ProduitFini addProduitFini(@RequestBody ProduitFini produitFini){
        produitFini.setEtat(0);
        if (produitFini.getMatieresPremieres() == null) {
            produitFini.setMatieresPremieres(new ArrayList<>());
        }
        return produitRepository.save(produitFini);
    }

    @PostMapping("/addConso")
    public ProduitConso addProduitConso(@RequestBody ProduitConso produitConso){
        produitConso.setDate(LocalDateTime.now());
        return produitRepository.save(produitConso);
    }
    @DeleteMapping("/{id}")
    public void deleteProduit(@PathVariable(name = "id") Long id){
        produitRepository.deleteById(id);
    }
    @PutMapping("/{id}")
    public Produit updateProduit(@PathVariable(name = "id") Long id, @RequestBody Produit produit){
        produit.setId(id);
        return produitRepository.save(produit);
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
    public ResponseEntity<ProduitFini> updateProduitFini(@PathVariable(name = "id") Long id,
                                                         @RequestBody ProduitFini produitFini) {
        // Ensure the ID in the URL matches the product being updated
        produitFini.setId(id);

        // Handle potential errors (e.g., product not found)
        ProduitFini existingProduitFini = (ProduitFini) produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFini not found with id: " + id));

        // Update fields and associated entities (matieresPremieres)
        existingProduitFini.setName(produitFini.getName());
        existingProduitFini.setQuantite(produitFini.getQuantite());
        existingProduitFini.setEtat(produitFini.getEtat());
        existingProduitFini.setMatieresPremieres(produitFini.getMatieresPremieres());

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
