package com.example.Produit.Repository;

import com.example.Produit.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface
ProduitRepository extends JpaRepository<Produit,Long> {

}
