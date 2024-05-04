package com.example.Produit.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@DiscriminatorValue("Fini")
public class ProduitFini extends Produit{
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_fini_id")
    private List<MatierePremier> matieresPremieres;
    private int etat;

    public List<MatierePremier> getMatieresPremieres() {
        return matieresPremieres;
    }

    public void setMatieresPremieres(List<MatierePremier> matieresPremieres) {
        this.matieresPremieres = matieresPremieres;
    }

    public int getEtat() {
        return etat;
    }

    public void setEtat(int etat) {
        this.etat = etat;
    }
}
