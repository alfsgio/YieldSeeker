package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase extends Apartment {
    private Float price;
    private Float priceFees;
    private Float priceCharges;
    private Float priceEnergy;
    private Float priceNotary;
    private Boolean rented;
    private Boolean procedureInProgress;
    private Float propertyTax;
    private Float credit;
    private Float management;
}
