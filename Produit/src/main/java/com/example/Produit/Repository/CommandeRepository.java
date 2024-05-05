package com.example.Produit.Repository;

import com.example.Produit.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommandeRepository extends JpaRepository<Commande,Long> {
    Commande findByIdProduitFini(Long id);

}
