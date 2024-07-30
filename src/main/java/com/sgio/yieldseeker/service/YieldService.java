package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.CityStats;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.PurchaseStats;
import com.sgio.yieldseeker.model.Rental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class YieldService {

    @Autowired
    CollectorService collectorService;

    @Autowired
    CalculatorService calculatorService;

    public Map<Integer, List<PurchaseStats>> getYield(){
        final Map<String, Map<Integer, List<?>>> allRealEstateAds = collectorService.collectAll();

        final Map<Integer, List<PurchaseStats>> yieldPurchases = new HashMap<>();

        if(allRealEstateAds.get("Rentals") != null && allRealEstateAds.get("Purchases") != null){
            allRealEstateAds.get("Purchases").forEach( (cityCode, appList) -> {
                final Map<Integer, CityStats> cityStatsMap = getRentalsStats(allRealEstateAds.get("Rentals"));
                final CityStats cityStats = cityStatsMap.get(cityCode) != null ? cityStatsMap.get(cityCode) : new CityStats(0f, 0f, 0f);
                yieldPurchases.put(cityCode, getPurchasesStatsByCity(cityStats, appList));
            });
        }

        return yieldPurchases;
    }

    private Map<Integer, CityStats> getRentalsStats(Map<Integer, List<?>> rentalMarket){
        final Map<Integer, CityStats> citiesStats = new HashMap<>();

        rentalMarket.forEach((cityCode, appList) -> {
            final List<Rental> rentals = appList.stream().map(app -> (Rental) app).toList();
            final Float avgPrice = rentals.stream().collect(Collectors.averagingDouble(Rental::getPriceTotal)).floatValue();
            final Float minPrice = rentals.stream().min(Comparator.comparing(Rental::getPriceTotal))
                    .map(Rental::getPriceTotal)
                    .orElse(-1f);
            final Float maxPrice = rentals.stream().max(Comparator.comparing(Rental::getPriceTotal))
                    .map(Rental::getPriceTotal)
                    .orElse(-1f);

            final CityStats cityStat = CityStats.builder().avgPrice(avgPrice).minPrice(minPrice).maxPrice(maxPrice).build();

            citiesStats.put(cityCode, cityStat);
        });

        return citiesStats;
    }

    private List<PurchaseStats> getPurchasesStatsByCity(CityStats cityStats, List<?> purchaseList){
        final List<PurchaseStats> purchaseStatsList = new ArrayList<>();
        purchaseList.stream().map(purchase -> (Purchase) purchase).toList()
                .forEach(purchase -> {
                    final Float yieldRatio = cityStats.getAvgPrice() > 0f ?
                            purchase.getMonthlyCost() / cityStats.getAvgPrice()
                            : -1f;
                    final PurchaseStats purchaseStats = PurchaseStats.builder().purchase(purchase).yield(yieldRatio).cityStats(cityStats).build();
                    purchaseStatsList.add(purchaseStats);
                });
        return purchaseStatsList;
    }
}