package com.example.Produit.controller;


import com.example.Produit.Repository.OrdreFabricationRepository;
import com.example.Produit.entity.OrdreFabrication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ordreFabrication")
public class OrdreFabricationController {

    @Autowired
    private OrdreFabricationRepository ordreFabricationRepository;

    @GetMapping("/all")
    public Iterable<OrdreFabrication> getAllOrdresFabrication() {
        return ordreFabricationRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<OrdreFabrication> addOrdreFabrication(@RequestBody OrdreFabrication ordreFabrication) {
        OrdreFabrication savedOrdreFabrication = ordreFabricationRepository.save(ordreFabrication);
        return ResponseEntity.ok(savedOrdreFabrication);
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
        ordreFabrication.setIdmachine(ordreFabricationDetails.getIdmachine());
        ordreFabrication.setQuantite(ordreFabricationDetails.getQuantite());
        ordreFabrication.setQuantiteRebut(ordreFabricationDetails.getQuantiteRebut());
        ordreFabrication.setEtat(ordreFabricationDetails.getEtat());
        ordreFabrication.setDateFin(ordreFabricationDetails.getDateFin());

        OrdreFabrication updatedOrdreFabrication = ordreFabricationRepository.save(ordreFabrication);
        return ResponseEntity.ok(updatedOrdreFabrication);
    }


    @DeleteMapping("/{id}")
    public void deleteOrdreFabrication(@PathVariable Long id) {
        ordreFabricationRepository.deleteById(id);
    }
}
