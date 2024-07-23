package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.CityData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CalculatorService {

    @Autowired
    private DataLoaderService dataLoaderService;

    public Float calculateLoan(Integer amount) {
        // (Puissance({1+(taux annuel/100)}^{année de prêt*12}) - 1
        Float tauxP = (float) (Math.pow(1 + (3.8/100), 1.0/12) - 1);

        // [ montantEmprunté * tauxP * Puissance({1+tauxP}^{année de prêt*12}) ] / [ Puissance({1+tauxP}^{année de prêt*12}) - 1 ]
        Float monthly = (float) ((amount * tauxP * Math.pow(1 + tauxP, 20*12)) / (Math.pow(1 + tauxP, 20*12) - 1));

        return monthly;
    }

    public Float calculatePropertyTaxe(Apartment apartment){
        // surface pondérée = surface réelle + modificateurs
        // valeur locative cadastrale = ( surface pondérée * tarif au m² )*12
        // revenu cadastral = valeur locative cadastrale/2
        // taxe foncière = revenu cadastral * taux fixés par les collectivités locales
        // mensualisé = taxe foncière /12

        CityData cityData = dataLoaderService .getCities().stream()
                .filter(city -> Objects.equals(city.getPostalCode(), apartment.getPostalCode()))
                .findFirst()
                .orElse(null);

        Float priceBySquareMeter = cityData != null ? cityData.getPriceBySquareMeter() : -1;
        Float valeurLocativeCadastrale = priceBySquareMeter > -1 ? (( apartment.getSurfaceArea() * priceBySquareMeter ) * 12) : -1;
        Float revenuCadastrale = valeurLocativeCadastrale > -1 ? (valeurLocativeCadastrale/2) : -1;
        Float taxeFonciere = revenuCadastrale > -1 ? ( revenuCadastrale * (cityData.getTauxFonciere()/100) ) : -1;

        return taxeFonciere/12;
    }
}