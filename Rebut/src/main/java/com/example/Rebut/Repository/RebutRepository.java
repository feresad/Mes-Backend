package com.example.Rebut.Repository;

import com.example.Rebut.entity.Rebut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface RebutRepository extends JpaRepository<Rebut, Long>{

}
