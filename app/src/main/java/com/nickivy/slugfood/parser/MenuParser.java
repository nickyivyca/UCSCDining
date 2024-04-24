package com.nickivy.slugfood.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nickivy.slugfood.R;
import com.nickivy.slugfood.util.MenuItem;
import com.nickivy.slugfood.util.Util;

import android.content.Context;
import android.util.Log;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

//import okhttp3.OkHttpClient;

import nl.altindag.ssl.util.CertificateUtils;
import nl.altindag.ssl.SSLFactory;
import okhttp3.OkHttpClient;

//import com.squareup.okhttp3.OkHttpClient;
//import com.squareup.okhttp.*;
//import okhttp3.OkHttpClient;
//import okhttp3.OkHttpClient.Builder;



/**
 * Parses the incoming file.
 * 
 * <p>Data is stored in the static fullMenu arraylist of 
 * CollegeMenu objects.
 *
 * <p>Released under GNU GPL v2 - see doc/LICENCES.txt for more info.
 * 
 * @author Nicky Ivy parkedraccoon@gmail.com
 */

public class MenuParser {

    public static final String URLPart1 = "https://nutrition.sa.ucsc.edu/longmenu.aspx?sName=UC+Santa+Cruz+Dining&locationNum=";

    public static final String[] URLPart2s = {
            "05&locationName=Cowell+Stevenson+Dining+Hall&dtdate=",
            "20&locationName=+Crown+Merrill&dtdate=",
            "25&locationName=Porter&dtdate=",
            "30&locationName=+Rachel+Carson+Oakes+Dining+Hall&dtdate=",
            "40&locationName=College+Nine+%26+Ten&dtdate="
    };

    private static final String URLPart3 = "&naFlag=1&WeeksMenus=UCSC+-+This+Week%27s+Menus&mealName=";

    private static final String icalurl = "https://calendar.google.com/calendar/ical/ucsc.edu_t59u0f85lnvamgj30m22e3fmgo%40group.calendar.google.com/public/basic.ics";

    public static boolean manualRefresh = false;

    /*
     * Cowell and 9/10 have late night and post nutrition info for all meals. Crown and Porter do
     * not have late night. Rachel Carson has late night but does not post nutrition info for the
     * late night meal itself. This pulls in the empty late night info for Crown and Porter, then
     * uses a separate method (the old method I used to use, before I would show nutrition info)
     * to get the menu items for Rachel Carson dining hall's late night menu.
     */

    public static int getSingleMealList(int k, int month, int day, int year, boolean collegeNight,
                                           boolean otherCollegeforCNight, boolean healthyMonday,
                                           boolean farmFriday, Context context) throws IOException {
        Elements breakfastNutIds = null,
                breakfastFoodNames = null,
                lunchNutIds = null,
                lunchFoodNames = null,
                dinnerNutIds = null,
                dinnerFoodNames = null,
                latenightNutIds = null,
                latenightFoodNames = null;

        Document breakfastDoc = fetchDocumentwithCookie(URLPart1 + URLPart2s[k] + month + "%2F" +
                day + "%2F" + year + URLPart3 + Util.meals[0], Util.CookieLocations[k], context);
        Document lunchDoc = fetchDocumentwithCookie(URLPart1 + URLPart2s[k] + month + "%2F" +
                day + "%2F" + year + URLPart3 + Util.meals[1], Util.CookieLocations[k], context);
        Document dinnerDoc = fetchDocumentwithCookie(URLPart1 + URLPart2s[k] + month + "%2F" +
                day + "%2F" + year + URLPart3 + Util.meals[2], Util.CookieLocations[k], context);
        Document latenightDoc = null;
        if (k != 1 && k != 2 ) {
            latenightDoc = fetchDocumentwithCookie(URLPart1 + URLPart2s[k] + month + "%2F" +
                    day + "%2F" + year + URLPart3 + "Late+Night", Util.CookieLocations[k], context);
        }

        breakfastFoodNames = breakfastDoc.select("div[class=\"longmenucoldispname\"],div[class^=\"longmenucolmenucat\"]");
        breakfastNutIds = breakfastDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        lunchFoodNames = lunchDoc.select("div[class=\"longmenucoldispname\"],div[class^=\"longmenucolmenucat\"]");
        lunchNutIds = lunchDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        dinnerFoodNames = dinnerDoc.select("div[class=\"longmenucoldispname\"],div[class^=\"longmenucolmenucat\"]");
        dinnerNutIds = dinnerDoc.select("INPUT[TYPE=\"CHECKBOX\"]");

        if (k != 1 && k != 2 ){
            latenightFoodNames = latenightDoc.select("div[class=\"longmenucoldispname\"],div[class^=\"longmenucolmenucat\"]");
            latenightNutIds = latenightDoc.select("INPUT[TYPE=\"CHECKBOX\"]");
        }

        ArrayList<MenuItem> breakfastList = new ArrayList<MenuItem>(),
                lunchList = new ArrayList<MenuItem>(),
                dinnerList = new ArrayList<MenuItem>(),
                latenightList = new ArrayList<MenuItem>();

        //Catch if the dining hall is closed for that day
        if(breakfastFoodNames != null && breakfastFoodNames.size() > 0){
            int findex = 0;
            for(int i = 0; i < breakfastFoodNames.size(); i++){
                if (!breakfastFoodNames.get(i).text().startsWith("-- ")) {
                    String nutid = breakfastNutIds.get(findex).attr("VALUE");
                    breakfastList.add(new MenuItem(breakfastFoodNames.get(i).text(),
                            nutid.substring(0, nutid.length()-3)));
                    findex++;
                } else {
                    if (breakfastFoodNames.get(i).text().contains("-- Cereal --")) {
                        break;
                    }
                }
            }
        }
        //Catch if the dining hall is closed for that day
        if(lunchFoodNames != null && lunchFoodNames.size() > 0){
            int findex = 0;
            for(int i = 0; i < lunchFoodNames.size(); i++){
                if (!lunchFoodNames.get(i).text().startsWith("-- ")) {
                    String nutid = lunchNutIds.get(findex).attr("VALUE");
                    lunchList.add(new MenuItem(lunchFoodNames.get(i).text(),
                            nutid.substring(0, nutid.length() - 3)));
                    findex++;
                } else {
                    if (lunchFoodNames.get(i).text().contains("-- Cereal --")) {
                        break;
                    }
                }
            }
        }
        //Catch if the dining hall is closed for that day
        if(dinnerFoodNames != null && dinnerFoodNames.size() > 0){
            int findex = 0;
            for(int i = 0; i < dinnerFoodNames.size(); i++){
                if (!dinnerFoodNames.get(i).text().startsWith("-- ")) {
                    String nutid = dinnerNutIds.get(findex).attr("VALUE");
                    dinnerList.add(new MenuItem(dinnerFoodNames.get(i).text(),
                            nutid.substring(0, nutid.length()-3)));
                    findex++;
                } else {
                    if (dinnerFoodNames.get(i).text().contains("-- Cereal --")) {
                        break;
                    }
                }
            }
        }

        if (k != 1 && k != 2 ){
            //Catch if the dining hall is closed for that day
            if(latenightFoodNames != null && latenightFoodNames.size() > 0){
                int findex = 0;
                for(int i = 0; i < latenightFoodNames.size(); i++){
                    if (!latenightFoodNames.get(i).text().startsWith("-- ")) {
                        String nutid = latenightNutIds.get(findex).attr("VALUE");
                        latenightList.add(new MenuItem(latenightFoodNames.get(i).text(),
                                nutid.substring(0, nutid.length()-3)));
                        findex++;
                    } else {
                        if (latenightFoodNames.get(i).text().contains("-- Cereal --")) {
                            break;
                        }
                    }
                }
            }
        }

        Util.fullMenuObj.get(k).setBreakfast(breakfastList);
        Util.fullMenuObj.get(k).setLunch(lunchList);
        Util.fullMenuObj.get(k).setDinner(dinnerList);
        Util.fullMenuObj.get(k).setLateNight(latenightList);

        /*
         * Dining halls seem to post breakfast menus for weekend brunch now, so no real need for
         * this code anymore.
         */
        if(Util.fullMenuObj.get(k).getBreakfast().isEmpty() &&
                (!(Util.fullMenuObj.get(k).getLunch().isEmpty()) &&
                        !(Util.fullMenuObj.get(k).getDinner().isEmpty()))){
            ArrayList<MenuItem> breakfastMessage = new ArrayList<MenuItem>();
            breakfastMessage.add(new MenuItem(Util.brunchMessage, "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfastMessage);
        }

        // If events are on Calendar but not on menu, insert them.

        if (collegeNight) {
            // Whenever it's college night the rest of the list should be empty
            ArrayList<MenuItem> dinner = new ArrayList<MenuItem>();
            String college = "";
            switch (k) {
                case 0:
                    college = otherCollegeforCNight? "Stevenson " : "Cowell ";
                    break;
                case 1:
                    college = otherCollegeforCNight? "Merrill " : "Crown ";
                    break;
                case 2:
                    college = otherCollegeforCNight? "Kresge " : "Porter ";
                    break;
                case 3:
                    college = "Rachel Carson/Oakes";
                    break;
                case 4:
                    college = "College 9/10 ";
                    break;
            }

            dinner.add(new MenuItem(college + "College Night", "-1"));
            Util.fullMenuObj.get(k).setDinner(dinner);
        }

        // Dining halls don't even seem to have healthy mondays anymore...
        if (healthyMonday && !Util.fullMenuObj.get(k).getIsHealthyMonday()) {
            ArrayList<MenuItem> breakfast = Util.fullMenuObj.get(k).getBreakfast();
            ArrayList<MenuItem> lunch = Util.fullMenuObj.get(k).getLunch();
            ArrayList<MenuItem> dinner = Util.fullMenuObj.get(k).getDinner();
            breakfast.add(0, new MenuItem("Healthy Mondays", "-1"));
            lunch.add(0, new MenuItem("Healthy Mondays", "-1"));
            dinner.add(0, new MenuItem("Healthy Mondays", "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfast);
            Util.fullMenuObj.get(k).setDinner(lunch);
            Util.fullMenuObj.get(k).setDinner(dinner);
        }

        if (farmFriday && !Util.fullMenuObj.get(k).getIsFarmFriday()) {
            ArrayList<MenuItem> breakfast = Util.fullMenuObj.get(k).getBreakfast();
            ArrayList<MenuItem> lunch = Util.fullMenuObj.get(k).getLunch();
            ArrayList<MenuItem> dinner = Util.fullMenuObj.get(k).getDinner();
            breakfast.add(0, new MenuItem("Farm Fridays", "-1"));
            lunch.add(0, new MenuItem("Farm Fridays", "-1"));
            dinner.add(0, new MenuItem("Farm Fridays", "-1"));
            Util.fullMenuObj.get(k).setBreakfast(breakfast);
            Util.fullMenuObj.get(k).setLunch(lunch);
            Util.fullMenuObj.get(k).setDinner(dinner);
        }
        return Util.GETLIST_SUCCESS;
    }

    /**
     * Puts downloaded data from specified date (instead of today) into the full menu object.
     */
    public static int getMealList(int month, int day, int year, Context context) throws IOException {
        /**
         * outer array is colleges, inner is [0]
         * college night, [1] if college night is 'secondary' college, [2]healthy monday,
         * [3] farm friday
         */
        boolean[][] eventBools = new boolean[5][4];
        Document icaldoc = fetchDocument(icalurl);

        icaldoc.outputSettings(new Document.OutputSettings().prettyPrint(false));

        String icalstring = icaldoc.body().html();

        String dayStr = (day > 9)? "" + day : "0" + day;
        String monthStr = (month > 9)? "" + month : "0" + month;

        String date = "" + year + monthStr + dayStr;

        int indexof = icalstring.indexOf("DTSTART;VALUE=DATE:" + date);

        while (indexof != -1) {
            String desc = icalstring.substring(icalstring.indexOf("SUMMARY", indexof),
                    icalstring.indexOf("END:VEVENT", indexof))
                    .replace("\n", "").replace("\r", "");
            if (desc.contains("College Night")) {
                if (desc.contains("Cowell")) {
                    eventBools[0][0] = true;
                    eventBools[0][1] = false;
                } else if (desc.contains("Stevenson")) {
                    eventBools[0][0] = true;
                    eventBools[0][1] = true;
                } else if (desc.contains("Crown")) {
                    eventBools[1][0] = true;
                    eventBools[1][1] = false;
                } else if (desc.contains("Merrill")) {
                    eventBools[1][0] = true;
                    eventBools[1][1] = true;
                } else if (desc.contains("Porter")) {
                    eventBools[2][0] = true;
                    eventBools[2][1] = false;
                } else if (desc.contains("Kresge")) {
                    eventBools[2][0] = true;
                    eventBools[2][1] = true;
                } else if (desc.contains("Eight") || desc.contains("College 8") ||
                        desc.contains("Carson") || desc.contains("Oakes")) {
                    // Rachel Carson and Oakes seem to be having their college nights together now
                    eventBools[3][0] = true;
                    eventBools[3][1] = false;
                } else if (desc.contains("Nine") || desc.contains("College 9") ||
                        desc.contains("Ten")) {
                    // Nine/Ten have their college nights together
                    eventBools[4][0] = true;
                    eventBools[4][1] = false;
                }
            }
            if (desc.contains("Healthy Monday")) {
                if (desc.contains("Cowell")) {
                    eventBools[0][2] = true;
                } else if (desc.contains("Crown")) {
                    eventBools[1][2] = true;
                } else if (desc.contains("Porter")) {
                    eventBools[2][2] = true;
                } else if (desc.contains("Eight") || desc.contains("College 8")) {
                    eventBools[3][2] = true;
                } else if (desc.contains("Nine") || desc.contains("College 9")) {
                    eventBools[4][2] = true;
                }
            }
            if (desc.contains("Farm Friday") || desc.contains("FARM FRIDAY")) {
                if (desc.contains("Cowell")) {
                    eventBools[0][3] = true;
                } else if (desc.contains("Crown")) {
                    eventBools[1][3] = true;
                } else if (desc.contains("Porter")) {
                    eventBools[2][3] = true;
                } else if (desc.contains("Eight") || desc.contains("College 8") ||
                        desc.contains("Rachel Carson") || desc.contains("Oakes")) {
                    eventBools[3][3] = true;
                } else if (desc.contains("Nine") || desc.contains("College 9")) {
                    eventBools[4][3] = true;
                }
            }
            indexof = icalstring.indexOf("DTSTART;VALUE=DATE:" + date, indexof + 1);
        }


        for (int i = 0; i < 5; i++) {
            int res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                    eventBools[i][2], eventBools[i][3], context);
                /*
                 * For some stupid reason, it throws these stupid unexpected status line errors half the
                 * time on mobile data. So we have to intercept those somehow. getsinglemeallist returns
                 * okhttp failure if it gets one - and getsinglemeallist also tries multiple times
                 * before returning the error. It will only try once for lost internet connection,
                 * though.
                 */
            if (res == Util.GETLIST_OKHTTP_FAILURE) {
                res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                        eventBools[i][2], eventBools[i][3], context);
                if (res == Util.GETLIST_OKHTTP_FAILURE) {
                    res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                            eventBools[i][2], eventBools[i][3], context);
                    if (res == Util.GETLIST_OKHTTP_FAILURE) {
                        res = getSingleMealList(i, month, day, year, eventBools[i][0], eventBools[i][1],
                                eventBools[i][2], eventBools[i][3], context);
                        if (res == Util.GETLIST_OKHTTP_FAILURE) {
                            return Util.GETLIST_INTERNET_FAILURE;
                        }
                    }
                }
            } else if (res != Util.GETLIST_SUCCESS) {
                return res;
            }
    	}

        return Util.GETLIST_SUCCESS;
    }

    public static Document fetchDocument(String url)
            throws IOException{
        Document ret;
        try {
            ret = Jsoup.connect(url).get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                ret = Jsoup.connect(url).get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    ret = Jsoup.connect(url).get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    throw e2;
                }
            }
        }
        return ret;
    }
    public static Document fetchDocumentwithCookie(String url, String cookieVal, Context context)
            throws IOException{

        InputStream trustedCertificatesAsInputStream = context.getResources().openRawResource(R.raw.cert);
        List<Certificate> trustedCertificates = CertificateUtils.loadCertificate(trustedCertificatesAsInputStream);

        SSLFactory sslFactory = SSLFactory.builder()
                .withTrustMaterial(trustedCertificates)
                .build();

        SSLSocketFactory sslSocketFactory = sslFactory.getSslSocketFactory();
        X509ExtendedTrustManager trustManager = sslFactory.getTrustManager().orElseThrow();

//        OkHttpClient cleint = OkHttpClient.Builder();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .build();

        Document ret;
        Log.v(Util.LOGTAG, "Fetching " + url);
        try {
            ret = Jsoup.connect(url).cookie("WebInaCartLocation", cookieVal).cookie("WebInaCartDates", "")
                    .cookie("WebInaCartMeals", "").cookie("WebInaCartQtys", "")
                    .cookie("WebInaCartRecipes", "").get();
        } catch (UnknownHostException e) {
            // Internet connection completely missing is a separate error from okhttp
            Log.v(Util.LOGTAG, Util.LOGMSG_INTERNETERROR);
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
            try {
                ret = Jsoup.connect(url).cookie("WebInaCartLocation", cookieVal).cookie("WebInaCartDates", "")
                        .cookie("WebInaCartMeals", "").cookie("WebInaCartQtys", "")
                        .cookie("WebInaCartRecipes", "").get();
            } catch (IOException e1) {
                Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                try {
                    ret = Jsoup.connect(url).cookie("WebInaCartLocation", cookieVal).cookie("WebInaCartDates", "")
                            .cookie("WebInaCartMeals", "").cookie("WebInaCartQtys", "")
                            .cookie("WebInaCartRecipes", "").get();
                } catch (IOException e2) {
                    Log.w(Util.LOGTAG, Util.LOGMSG_OKHTTP);
                    // Give up after three times
                    throw e2;
                }
            }
        }
        return ret;
    }
}
