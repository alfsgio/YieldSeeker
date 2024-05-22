package com.sgio.yieldseeker.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.IOException;

public class YieldService {

    public String startScrapper() {
        String URL = "https://www.seloger.com/list.htm?projects=2&types=2,1&places=[{%22inseeCodes%22:[910161]}]&surface=30/NaN&rooms=1&epc=A,B,C,D&qsVersion=1.0";

        Document doc = null;
        try {
            doc = Jsoup.connect(URL).get();
            Elements allApartmentCard = doc.getElementsByClass("sc-hkbPbT");
            for(Element e : allApartmentCard){
//                if("data-testid")
//                faire comparaison avec code Insee
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            return "";
        }
    }

    private void scrapIt(String url) {

    }
}