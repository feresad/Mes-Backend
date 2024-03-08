package com.example.Produit.Repository;

import com.example.Produit.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface
ProduitRepository extends JpaRepository<Produit,Long> {
    // search by name with query
    @Query("select p from Produit p where p.Name like %?1%")
    public List<Produit> findByNameContains(String name);
}
