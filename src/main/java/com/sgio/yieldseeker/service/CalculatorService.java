package com.sgio.yieldseeker.service;

import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public Double calculateMonthly(Integer amount) {
        // (Puissance({1+(taux annuel/100)}^{année de prêt*12}) - 1
        Double tauxP = Math.pow(1 + (3.8/100), 1.0/12) - 1;

        // [ montantEmprunté * tauxP * Puissance({1+tauxP}^{année de prêt*12}) ] / [ Puissance({1+tauxP}^{année de prêt*12}) - 1 ]
        Double monthly = (amount * tauxP * Math.pow(1 + tauxP, 20*12)) / (Math.pow(1 + tauxP, 20*12) - 1);

        return monthly;
    }

}
