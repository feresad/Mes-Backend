package com.example.Produit.controller;

import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
