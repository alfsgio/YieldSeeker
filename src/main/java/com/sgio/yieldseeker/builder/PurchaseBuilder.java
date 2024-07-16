package com.sgio.yieldseeker.builder;

import com.google.gson.JsonObject;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;

public class PurchaseBuilder {
    private Apartment apartment;
    private Float price;
    private Float agencyFeePercentage;
    private Float annualCondominiumFees;
    private Boolean rented;
    private Boolean procedureInProgress;

    public PurchaseBuilder from(JsonObject jsonDatas) {
        this.apartment = new ApartmentBuilder().from(jsonDatas).build();
        this.price = jsonDatas.get("price").getAsFloat();
        this.agencyFeePercentage = jsonDatas.has("agencyFeePercentage") ? jsonDatas.get("agencyFeePercentage").getAsFloat() : 0;
        this.annualCondominiumFees = jsonDatas.has("annualCondominiumFees") ? jsonDatas.get("annualCondominiumFees").getAsFloat() : 0;
        this.rented = jsonDatas.get("description").getAsString().toLowerCase().contains("vendu loué");
        this.procedureInProgress = jsonDatas.has("isCondominiumInProcedure") && jsonDatas.get("isCondominiumInProcedure").getAsBoolean();
        return this;
    }

    public Purchase build() {
        return Purchase.builder()
                .apartment(apartment)
                .price(price)
                .agencyFeePercentage(agencyFeePercentage)
                .annualCondominiumFees(annualCondominiumFees)
                .rented(rented)
                .procedureInProgress(procedureInProgress)
                .build();
    }
}
