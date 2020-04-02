package com.example.myapplication.Retrofit;

import com.example.myapplication.Model.RootObject;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface revolutcodesInterface {

    @GET("api/android/latest?base=EUR")
    Observable<RootObject> getCurrency();
}
