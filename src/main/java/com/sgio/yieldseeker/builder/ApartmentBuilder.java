package com.sgio.yieldseeker.builder;

import com.google.gson.JsonObject;
import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import com.sgio.yieldseeker.model.Apartment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApartmentBuilder {
    private String city;
    private Integer postalCode;
    private Float surfaceArea;
    private DPE dpe;
    private Heating heating;
    private Boolean parking;
    private List<ExtraSpace> extraSpaces = new ArrayList<>();
    private List<Convenience> convenience = new ArrayList<>();
    private Float score = 0f;

    public ApartmentBuilder from(JsonObject jsonDatas) {
        this.city = parseCity(jsonDatas);
        this.postalCode = parsePostalCode(jsonDatas);
        this.surfaceArea = parseSurfaceArea(jsonDatas);
        this.dpe = parseDPE(jsonDatas);
        this.heating = parseHeating(jsonDatas);
        this.parking = parseParking(jsonDatas);
        this.extraSpaces = parseExtraSpaces(jsonDatas);
        this.convenience = parseConvenience(jsonDatas);
        return this;
    }

    private String parseCity(JsonObject jsonDatas) {
        return jsonDatas.get("city") != null ? jsonDatas.get("city").getAsString() : "";
    }

    private Integer parsePostalCode(JsonObject jsonDatas) {
        return jsonDatas.get("postalCode").getAsInt();
    }

    private Float parseSurfaceArea(JsonObject jsonDatas) {
        return jsonDatas.get("surfaceArea").getAsFloat();
    }

    private DPE parseDPE(JsonObject jsonDatas) {
        final String stringDPE = jsonDatas.get("energyClassification").getAsString();
        score += "A".equalsIgnoreCase(stringDPE) ? 1f : "B".equalsIgnoreCase(stringDPE) ? 0.5f : 0f;
        return DPE.valueOf(stringDPE);
    }

    private Heating parseHeating(JsonObject jsonDatas) {
        String heatingString = jsonDatas.has("heating") ? jsonDatas.get("heating").getAsString() : "";
        return heatingString.contains("collectif") ? Heating.valueOf("collectif") : Heating.valueOf("individuel");
    }

    private Boolean parseParking(JsonObject jsonDatas) {
        final Boolean hasParking = jsonDatas.has("parkingPlacesQuantity") && jsonDatas.get("parkingPlacesQuantity").getAsInt() > 0;
        final Boolean hasEnclosedParking = jsonDatas.has("enclosedParkingQuantity") && jsonDatas.get("enclosedParkingQuantity").getAsInt() > 0;
        score += hasEnclosedParking ? 1f : hasParking ? 0.5f : 0f;
        return hasParking || hasEnclosedParking;
    }

    private List<ExtraSpace> parseExtraSpaces(JsonObject jsonDatas) {
        List<ExtraSpace> extraSpaces = new ArrayList<>();
        if ((jsonDatas.has("hasBalcony") && jsonDatas.get("hasBalcony").getAsBoolean())
                || (jsonDatas.has("balconyQuantity") && jsonDatas.get("balconyQuantity").getAsInt() > 0)) {
            extraSpaces.add(ExtraSpace.valueOf("balcon"));
            score += 1f;
        }
        if ((jsonDatas.has("hasTerrace") && jsonDatas.get("hasTerrace").getAsBoolean())
                || (jsonDatas.has("terracesQuantity") && jsonDatas.get("terracesQuantity").getAsInt() > 0)) {
            extraSpaces.add(ExtraSpace.valueOf("terrasse"));
            score += 1f;
        }
        if ((jsonDatas.has("hasCellar") && jsonDatas.get("hasCellar").getAsBoolean())
                || (jsonDatas.has("cellarsOrUndergroundsQuantity") && jsonDatas.get("cellarsOrUndergroundsQuantity").getAsInt() > 0)) {
            extraSpaces.add(ExtraSpace.valueOf("cave"));
            score += 0.5f;
        }
        return extraSpaces;
    }

    private List<Convenience> parseConvenience(JsonObject jsonDatas) {
        List<Convenience> convenience = new ArrayList<>();
        if (jsonDatas.has("hasElevator") && jsonDatas.get("hasElevator").getAsBoolean()) {
            convenience.add(Convenience.valueOf("ascenseur"));
            score += 0.5f;
        }
        if (jsonDatas.has("hasIntercom") && jsonDatas.get("hasIntercom").getAsBoolean()) {
            convenience.add(Convenience.valueOf("interphone"));
            score += 0.5f;
        }
        if (jsonDatas.has("hasDoorCode") && jsonDatas.get("hasDoorCode").getAsBoolean()) {
            convenience.add(Convenience.valueOf("digicode"));
            score += 0.5f;
        }
        return convenience;
    }

    public Apartment build() {
        return Apartment.builder()
                .city(city)
                .postalCode(postalCode)
                .surfaceArea(surfaceArea)
                .dpe(dpe)
                .heating(heating)
                .parking(parking)
                .extraSpaces(extraSpaces)
                .convenience(convenience)
                .score(score)
                .build();
    }
}