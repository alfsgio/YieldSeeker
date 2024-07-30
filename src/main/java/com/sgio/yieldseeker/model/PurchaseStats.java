package com.sgio.yieldseeker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PurchaseStats {
    private Purchase purchase;
    private Float yield;
    private CityStats cityStats;
}
