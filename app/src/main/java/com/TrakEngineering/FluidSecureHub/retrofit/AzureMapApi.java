package com.TrakEngineering.FluidSecureHub.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AzureMapApi {

    // For Get Address By Lat-Lng:
    //https://atlas.microsoft.com/search/address/reverse/json?api-version=1.0&subscription-key=FJ29LaayVFiy20Hp29hEe5mG7F6QTbhfyV6wuWwG7Sg&query=17.5205731,73.5416914&language=/

    // For Get Address Suggestions By entered data:
    //https://atlas.microsoft.com/search/address/json?typeahead=true&subscription-key={subscription-key}&api-version=1&query={query}&language={language}&lon={lon}&lat={lat}&countrySet={countrySet}&view=Auto

    @GET("/search/address/reverse/json")
    Call<JsonObject> GetAddressByLatLng(
            @Query(value = "api-version")  String apiVersion,
            @Query(value = "subscription-key")   String subscriptionKey,
            @Query(value = "query")   String query,
            @Query(value = "language") String language
    );

    @GET("/search/address/json")
    Call<JsonObject> GetAddressSuggestions(
            @Query(value = "typeahead")  String typeAhead,
            @Query(value = "subscription-key")   String subscriptionKey,
            @Query(value = "api-version")  String apiVersion,
            @Query(value = "query")   String query,
            @Query(value = "language") String language,
            @Query(value = "lon") String lon,
            @Query(value = "lat") String lat,
            @Query(value = "countrySet") String countrySet,
            @Query(value = "view") String view
    );
}
