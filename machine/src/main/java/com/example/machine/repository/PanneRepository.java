package com.example.machine.repository;

import com.example.machine.entity.Panne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface PanneRepository extends JpaRepository<Panne,Long> {
}
