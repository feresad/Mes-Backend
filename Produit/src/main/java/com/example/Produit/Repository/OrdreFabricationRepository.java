package com.example.Produit.Repository;

import com.example.Produit.entity.OrdreFabrication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface OrdreFabricationRepository extends JpaRepository<OrdreFabrication, Long> {
}
