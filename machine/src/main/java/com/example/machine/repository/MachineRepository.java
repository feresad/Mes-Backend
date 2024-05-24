package com.example.machine.repository;

import com.example.machine.entity.Machine;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;


@RepositoryRestResource
public interface MachineRepository extends JpaRepository<Machine,Long>{
    @Query("select m from Machine m where m.etat = ?1")
    public Iterable<Machine> findByEtat(boolean etat);
    @Query("SELECT m FROM Machine m WHERE m.name LIKE %:nom%")
    public Iterable<Machine> searchMachineByName(@Param("nom") String nom);
    List<Machine> findByEtatFalse();
    List<Machine> findByEtatFalse(Sort sort);




}
