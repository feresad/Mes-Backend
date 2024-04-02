package com.example.consommation.Repository;

import com.example.consommation.entity.consommation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ConsoRepository extends JpaRepository<consommation,Long> {
    @Query("select c from consommation c where c.idMachine = ?1")
    public Iterable<consommation> findByMachineId(Long idMachine);
}
