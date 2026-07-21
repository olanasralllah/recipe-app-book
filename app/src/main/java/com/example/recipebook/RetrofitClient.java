package com.example.recipebook;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://bilzduvkqyssofbzugve.supabase.co/";
    public static final String SUPABASE_KEY = "sb_publishable_SCbXsxZmpeqOI7Nu6dbc-w_BOxZc1p4";    private static Retrofit retrofit = null;

    public static SupabaseService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseService.class);
    }
}
