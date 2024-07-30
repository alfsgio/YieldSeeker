package com.sgio.yieldseeker.builder;

import com.google.gson.JsonObject;
import com.sgio.yieldseeker.model.Apartment;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PurchaseBuilder {

    @Autowired
    private CalculatorService calculatorService;

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

    public PurchaseBuilder from(JsonObject jsonDatas) {
        this.apartment = parseApartment(jsonDatas);
        this.price = parsePrice(jsonDatas);
        this.agencyFeePercentage = parseAgencyFeePercentage(jsonDatas);
        this.annualCondominiumFees = parseAnnualCondominiumFees(jsonDatas);
        this.rented = parseRented(jsonDatas);
        this.procedureInProgress = parseProcedureInProgress(jsonDatas);
        this.calculatedAttributes();
        return this;
    }

    private Apartment parseApartment(JsonObject jsonDatas) {
        return new ApartmentBuilder().from(jsonDatas).build();
    }

    private Float parsePrice(JsonObject jsonDatas) {
        return jsonDatas.has("price") ? jsonDatas.get("price").getAsFloat() : -1f;
    }

    private Float parseAgencyFeePercentage(JsonObject jsonDatas) {
        return jsonDatas.has("agencyFeePercentage") ? jsonDatas.get("agencyFeePercentage").getAsFloat() : 0f;
    }

    private Float parseAnnualCondominiumFees(JsonObject jsonDatas) {
        return jsonDatas.has("annualCondominiumFees") ? jsonDatas.get("annualCondominiumFees").getAsFloat() : 0f;
    }

    private Boolean parseRented(JsonObject jsonDatas) {
        final Boolean isRented = jsonDatas.get("description").getAsString().toLowerCase().contains("vendu loué");
        this.apartment.addToScore(isRented ? 0.5f : 0f);
        return isRented;
    }

    private Boolean parseProcedureInProgress(JsonObject jsonDatas) {
        final Boolean hasProcedure = jsonDatas.has("isCondominiumInProcedure") && jsonDatas.get("isCondominiumInProcedure").getAsBoolean();
        this.apartment.addToScore(hasProcedure ? -1f : 0f);
        return hasProcedure;
    }

    private void calculatedAttributes(){
        this.monthlyLoan = calculatorService.calculateLoanMonthly(this.price);
        this.propertyTax = calculatorService.calculatePropertyTax(this.apartment);
        this.managementTax = calculatorService.calculateManagementTaxMonthly(this.monthlyLoan + this.propertyTax);
    }

    public Purchase build() {
       return Purchase.builder()
                .apartment(apartment)
                .price(price)
                .agencyFeePercentage(agencyFeePercentage)
                .annualCondominiumFees(annualCondominiumFees)
                .rented(rented)
                .procedureInProgress(procedureInProgress)
                .monthlyLoan(monthlyLoan)
                .propertyTax(propertyTax)
                .managementTax(managementTax)
                .build();
    }
}
