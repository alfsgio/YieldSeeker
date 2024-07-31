package com.sgio.yieldseeker.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    private Apartment apartment;
    private Float price;
    private Float agencyFeePercentage;
    private Float annualCondominiumFees;
    private Boolean rented;
    private Boolean procedureInProgress;

    // Calculated
    private Float monthlyLoan;
    private Float propertyTax;
    private Float managementTax;

    public Float getMonthlyCost(){
        return this.monthlyLoan + this.propertyTax + this.managementTax;
    }
}
