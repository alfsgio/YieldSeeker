package com.sgio.yieldseeker.model;

import com.sgio.yieldseeker.enumerations.Convenience;
import com.sgio.yieldseeker.enumerations.DPE;
import com.sgio.yieldseeker.enumerations.ExtraSpace;
import com.sgio.yieldseeker.enumerations.Heating;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Apartment {
    private String city;
    private Integer postalCode;
    private Float surfaceArea;
    private DPE dpe;
    private Heating heating;
    private Boolean parking;
    private List<ExtraSpace> extraSpaces;
    private List<Convenience> conveniences;
    private List<String> photoLinks;
    private Float score;

    public Apartment() {
        this.extraSpaces = new ArrayList<>();
        this.conveniences = new ArrayList<>();
        this.photoLinks = new ArrayList<>();
    }

    public void addConvenience(Convenience convenience) {
        this.conveniences.add(convenience);
    }

    public void addExtraSpace(ExtraSpace extraSpaces) {
        this.extraSpaces.add(extraSpaces);
    }

    public void addPhotoLink(String photoLink) { this.photoLinks.add(photoLink); }

    public void addToScore(Float scoreToAdd){ this.score += scoreToAdd; }
}