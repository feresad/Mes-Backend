package com.example.Produit.controller;

import com.example.Produit.ProduitApplication;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("produits")
public class ProduitController {
    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping("/all")
    public Iterable<Produit> getAllProduits(){
        return produitRepository.findAll();
    }
    @GetMapping("/{id}")
    Produit getProduitbyId(@PathVariable(name = "id") Long id){
        return produitRepository.findById(id).get();
    }
    @PostMapping("/add")
    Produit addProduit(@RequestBody Produit produit){
        return produitRepository.save(produit);
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

}
