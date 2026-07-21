package com.example.recipebook;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiRequests {
    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    @Headers({"Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJpbHpkdXZrcXlzc29mYnp1Z3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyMDQwMDEsImV4cCI6MjA5OTc4MDAwMX0.f0pyZUAdQDan1Jt9Tc3sSiWbFOuhFFFMZfApgEy0O_Y"})
    Call<ResponseBody> uploadFile(
            @Path("bucket") String bucket,
            @Path("path") String path,
            @Part MultipartBody.Part file
    );
}
