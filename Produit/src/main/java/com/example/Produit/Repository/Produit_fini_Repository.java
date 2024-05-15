package com.example.Produit.Repository;

import com.example.Produit.entity.ProduitFini;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Produit_fini_Repository extends JpaRepository<ProduitFini,Long> {
    Long countByEtat(int etat);
}
