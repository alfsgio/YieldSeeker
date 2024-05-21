package com.sgio.yieldseeker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
