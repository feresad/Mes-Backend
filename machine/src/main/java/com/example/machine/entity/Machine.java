package com.example.machine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "machines")
public class Machine {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private boolean etat;

    private String username;
    private LocalDateTime date;
    @ManyToMany
    @JoinTable(
            name = "machine_panne",
            joinColumns = @JoinColumn(name = "machine_id"),
            inverseJoinColumns = @JoinColumn(name = "panne_id")
    )
    private Set<Panne> pannes = new HashSet<>();

    @PreUpdate
    public void preUpdate() {
        if (this.isEtat()) {
            this.pannes.clear();
        }
    }
}
