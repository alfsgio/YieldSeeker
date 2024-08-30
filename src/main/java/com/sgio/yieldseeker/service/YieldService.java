package com.sgio.yieldseeker.service;

import com.sgio.yieldseeker.model.CityStats;
import com.sgio.yieldseeker.model.Purchase;
import com.sgio.yieldseeker.model.PurchaseStats;
import com.sgio.yieldseeker.model.Rental;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class YieldService {

    private static final Logger logger = LoggerFactory.getLogger(YieldService.class);

    @Autowired
    private CollectorService collectorService;

    /**
     * The main method, call the collector service and the methods used to calculate the stats.
     * Make a map of the final stats by city.
     *
     * @return  a map containing all purchase stats by city
     */
    public Map<Integer, List<PurchaseStats>> getYield(){
        logger.info("Collect : start");
        final Map<String, Map<Integer, List<?>>> allRealEstateAds = collectorService.collectAll();
        logger.info("Collect : end");

        logger.info("Yield : start");
        final Map<Integer, List<PurchaseStats>> yieldPurchases = new HashMap<>();
        if(allRealEstateAds.get("Rentals") != null && allRealEstateAds.get("Purchases") != null){
            allRealEstateAds.get("Purchases").forEach( (cityCode, appList) -> {
                final Map<Integer, CityStats> cityStatsMap = getRentalsStats(allRealEstateAds.get("Rentals"));
                final CityStats cityStats = cityStatsMap.get(cityCode) != null ? cityStatsMap.get(cityCode) : new CityStats(0f, 0f, 0f);
                yieldPurchases.put(cityCode, getPurchasesStatsByCity(cityStats, appList));
            });
        }
        logger.info("Yield : end");

        return yieldPurchases;
    }

    /**
     * Calculating the stats of each city rental market.
     *
     * @param  rentalMarket     a map of the rentalMarket
     * @return                  the city stats calculated
     */
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

    /**
     * Calculating the yield of the purchases for a given city.
     * The data returned contains also the citystats and the purchase ads itself.
     *
     * @param  cityStats    the city stats
     * @param  purchaseList the purchase list
     * @return              the purchases stats calulated
     */
    private List<PurchaseStats> getPurchasesStatsByCity(CityStats cityStats, List<?> purchaseList){
        final List<PurchaseStats> purchaseStatsList = new ArrayList<>();
        purchaseList.stream().map(purchase -> (Purchase) purchase).toList()
                .forEach(purchase -> {
                    final Float yieldRatio = cityStats.getAvgPrice() > 0f ?
                            purchase.getMonthlyCost() / cityStats.getAvgPrice()
                            : -1f;
                    final PurchaseStats purchaseStats = PurchaseStats.builder().purchase(purchase).yieldRatio(yieldRatio).cityStats(cityStats).build();
                    purchaseStatsList.add(purchaseStats);
                });
        return purchaseStatsList;
    }

    /**
     * Retrieving the rental ads for a given city (represented by postal code).
     *
     * @param  cityCode the city code
     * @return          a list of rentals
     */
    public List<Rental> getRentalsFromCityCode(Integer cityCode) {
        logger.info("Collect : start");
        final Map<String, Map<Integer, List<?>>> allRealEstateAds = collectorService.collectAll();
        logger.info("Collect : end");

        return allRealEstateAds.get("Rentals").get(cityCode)
                .stream().map(rental -> (Rental) rental)
                .toList();
    }
}