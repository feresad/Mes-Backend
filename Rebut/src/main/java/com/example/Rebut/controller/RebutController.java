package com.example.Rebut.controller;


import com.example.Rebut.Repository.RebutRepository;
import com.example.Rebut.entity.Rebut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rebut")

public class RebutController {
    @Autowired
    private RebutRepository rebutRepository;

    @GetMapping("/all")
    public Iterable<Rebut> getAllRebut(){
        return rebutRepository.findAll();
    }
    @PutMapping("/{id}")
    public Rebut updateRebut(@PathVariable(name = "id") Long id, @RequestBody Rebut rebut){
        rebut.setId(id);
        return rebutRepository.save(rebut);
    }
    @PostMapping("/add")
    public Rebut addRebut(@RequestBody Rebut rebut){
        return rebutRepository.save(rebut);
    }
    @DeleteMapping("/{id}")
    public void deleteRebut(@PathVariable(name = "id") Long id) {
        rebutRepository.deleteById(id);
    }
    //search by produitid
    @GetMapping("/search")
    public Iterable<Rebut> searchRebut(@RequestParam(name = "idProduit") Long idProduit){
        return rebutRepository.findByProduitId(idProduit);
    }

}
