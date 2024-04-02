package com.example.machine.repository;

import com.example.machine.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface MachineRepository extends JpaRepository<Machine,Long>{
    @Query("select m from Machine m where m.etat = ?1")
    public Iterable<Machine> findByEtat(boolean etat);
    @Query("select m from Machine m where m.name like %?1%")
    public Iterable<Machine> searchMachineByName(String nom);
}
