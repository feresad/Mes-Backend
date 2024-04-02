package com.example.Produit.controller;

import com.example.Produit.ProduitApplication;
import com.example.Produit.Repository.ProduitRepository;
import com.example.Produit.entity.Produit;
import javax.servlet.http.HttpServletRequest;
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
import java.util.List;

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
    public Produit addProduit(@RequestBody Produit produit){
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
    // search by name
    @GetMapping("/search")
    public List<Produit> searchProduit(@RequestParam(name = "name") String name){
        return produitRepository.findByNameContains(name);
    }

}
