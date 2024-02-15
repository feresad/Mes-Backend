package com.example.machine.repository;

import com.example.machine.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface MachineRepository extends JpaRepository<Machine,Long>{
}
