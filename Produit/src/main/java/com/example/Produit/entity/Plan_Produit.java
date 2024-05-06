package com.example.Produit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plan_produit")
public class Plan_Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matierePremierName;

    private float quantiteTotal;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_fini_id")
    private ProduitFini produitFini;
}