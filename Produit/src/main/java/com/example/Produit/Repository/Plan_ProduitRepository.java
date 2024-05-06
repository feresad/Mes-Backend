package com.example.Produit.Repository;

import com.example.Produit.entity.Plan_Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Plan_ProduitRepository extends JpaRepository<Plan_Produit, Long> {
        List<Plan_Produit> findByMatierePremierName(String matierePremierName);
    List<Plan_Produit> findByProduitFiniId(Long produitFiniId);
}