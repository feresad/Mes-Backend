package com.example.Produit.Repository;

import com.example.Produit.entity.Produit;
import com.example.Produit.entity.ProduitConso;
import com.example.Produit.entity.ProduitFini;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface ProduitRepository extends JpaRepository<Produit,Long> {
    @Query("select p from Produit p where p.Name like %?1%")
    public List<Produit> findByNameContains(String name);

    public List<ProduitFini> findAllBy();

    @Query("SELECT p FROM ProduitConso p")
    List<ProduitConso> findAllProduitConso();

    @Query("select pc from ProduitConso pc where pc.Name = :name")
    public ProduitConso findProduitConsoByName(@Param("name") String name);


}
