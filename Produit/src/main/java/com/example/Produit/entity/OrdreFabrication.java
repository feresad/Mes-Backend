package com.example.Produit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "ordre_fabrication")
public class OrdreFabrication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long idProduitFini;

    private Long  idmachine;

    private int quantite;

    private Float quantiteRebut;

    private int etat;
    private LocalDate dateFin;
}
