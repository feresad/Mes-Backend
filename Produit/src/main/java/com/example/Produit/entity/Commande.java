package com.example.Produit.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long  idProduitFini;
    private int quantite;
}
