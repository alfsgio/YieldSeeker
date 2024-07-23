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
        this.apartment = parseApartment(jsonDatas);
        this.price = parsePrice(jsonDatas);
        this.agencyFeePercentage = parseAgencyFeePercentage(jsonDatas);
        this.annualCondominiumFees = parseAnnualCondominiumFees(jsonDatas);
        this.rented = parseRented(jsonDatas);
        this.procedureInProgress = parseProcedureInProgress(jsonDatas);
        return this;
    }

    private Apartment parseApartment(JsonObject jsonDatas) {
        return new ApartmentBuilder().from(jsonDatas).build();
    }

    private Float parsePrice(JsonObject jsonDatas) {
        return jsonDatas.get("price").getAsFloat();
    }

    private Float parseAgencyFeePercentage(JsonObject jsonDatas) {
        return jsonDatas.has("agencyFeePercentage") ? jsonDatas.get("agencyFeePercentage").getAsFloat() : 0;
    }

    private Float parseAnnualCondominiumFees(JsonObject jsonDatas) {
        return jsonDatas.has("annualCondominiumFees") ? jsonDatas.get("annualCondominiumFees").getAsFloat() : 0;
    }

    private Boolean parseRented(JsonObject jsonDatas) {
        return jsonDatas.get("description").getAsString().toLowerCase().contains("vendu loué");
    }

    private Boolean parseProcedureInProgress(JsonObject jsonDatas) {
        return jsonDatas.has("isCondominiumInProcedure") && jsonDatas.get("isCondominiumInProcedure").getAsBoolean();
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
