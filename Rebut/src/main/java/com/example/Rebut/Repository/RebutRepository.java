package com.example.Rebut.Repository;

import com.example.Rebut.entity.Rebut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface RebutRepository extends JpaRepository<Rebut, Long>{
    @Query("select r from Rebut r where r.idProduitFini = ?1")
    public Iterable<Rebut> findByProduitId(Long produitId);

}
