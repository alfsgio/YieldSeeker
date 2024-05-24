package com.sgio.yieldseeker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase extends Apartment {
    private float priceNotary;
    private float priceCharges;
    private float priceEnergy;
    private boolean rented;
    private boolean procedureInProgress;
    private float propertyTax;
    private float credit;
    private float management;
}
