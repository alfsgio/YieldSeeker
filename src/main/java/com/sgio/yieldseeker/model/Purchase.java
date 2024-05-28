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
    private Float priceNotary;
    private Float priceCharges;
    private Float priceEnergy;
    private Float priceFees;
    private boolean rented;
    private boolean procedureInProgress;
    private Float propertyTax;
    private Float credit;
    private Float management;
}
