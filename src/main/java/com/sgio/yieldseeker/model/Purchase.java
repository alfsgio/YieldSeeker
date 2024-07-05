package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    private String purchaseAdLink;
    private Apartment apartment;
    private Float price;
    private Float agencyFeePercentage;
    private Float annualCondominiumFees;
    private Boolean rented;
    private Boolean procedureInProgress;

    // Calculated
    private Float priceNotary;
    private Float propertyTax;
    private Float credit;
    private Float management;
}
