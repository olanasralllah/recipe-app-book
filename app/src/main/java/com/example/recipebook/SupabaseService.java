package com.example.recipebook;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SupabaseService {
    @POST("storage/v1/object/recipe-images/{folder}/{image_name}")
    Call<ResponseBody> uploadImage(
            @Header("Authorization") String authorization,
            @Header("apikey") String apiKey,
            @Header("Content-Type") String contentType,
            @Path("folder") String folder,
            @Path("image_name") String imageName,
            @Body RequestBody image
    );
}
