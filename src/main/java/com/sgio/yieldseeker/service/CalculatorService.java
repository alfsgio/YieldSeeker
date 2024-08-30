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

    /**
     * Calculate monthly loan.
     *
     * @param  amount   the base amount to calculate the loan
     * @return          the monthly loan amount
     */
    public Float calculateLoanMonthly(Float amount) {
        final Float amountWithNotaryTax = amount * 1.08f;
        final Float amountWithoutInput = amountWithNotaryTax - 20000f;

        // (Puissance({1+(taux annuel/100)}^{année de prêt*12}) - 1
        final Float tauxP = (float) (Math.pow(1 + (3.8/100), 1.0/12) - 1);

        // [ montantEmprunté * tauxP * Puissance({1+tauxP}^{année de prêt*12}) ] / [ Puissance({1+tauxP}^{année de prêt*12}) - 1 ]
        final Float monthlyLoan = (float) ((amountWithoutInput * tauxP * Math.pow(1 + tauxP, 20*12)) / (Math.pow(1 + tauxP, 20*12) - 1));

        return monthlyLoan;
    }

    /**
     * Calculate the monthly property tax of a given Apartment.
     * To do this, it uses data provided by a file of real estate
     * data analysis of the previous year.
     *
     * @param  apartment    the apartment
     * @return              the monthly tax amount
     */
    public Float calculatePropertyTaxMonthly(Apartment apartment){
        // surface pondérée = surface réelle + modificateurs
        // valeur locative cadastrale = ( surface pondérée * tarif au m² )*12
        // revenu cadastral = valeur locative cadastrale/2
        // taxe foncière = revenu cadastral * taux fixés par les collectivités locales
        // mensualisé = taxe foncière /12

        CityData cityData = dataLoaderService .getCities().stream()
                .filter(city -> Objects.equals(city.getPostalCode(), apartment.getPostalCode()))
                .findFirst().orElse(null);

        Float priceBySquareMeter = cityData != null ? cityData.getPriceBySquareMeter() : -1f;
        Float valeurLocativeCadastrale = priceBySquareMeter > -1f ? (( apartment.getSurfaceArea() * priceBySquareMeter ) * 12f) : -1f;
        Float revenuCadastrale = valeurLocativeCadastrale > -1f ? (valeurLocativeCadastrale/2f) : -1;
        Float taxeFonciere = revenuCadastrale > -1f ? ( revenuCadastrale * (cityData.getTauxFonciere()/100f) ) : -1f;

        return taxeFonciere/12f;
    }

    /**
     * Calculate the monthly management tax.
     *
     * @param  amount   the base amount to calculate the tax
     * @return          the monthly tax amount
     */
    public Float calculateManagementTaxMonthly(Float amount){
        return amount*0.1f;
    }
}