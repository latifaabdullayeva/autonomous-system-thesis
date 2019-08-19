package com.example.autonomoussystemthesis.network.api.personality;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PersonalityRepository {

    private final PersonalityService personalityService;

    public PersonalityRepository() {
        Retrofit retrofit;
        retrofit = new Retrofit.Builder()
                .baseUrl("https://d8a27c3f.ngrok.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        personalityService = retrofit.create(PersonalityService.class);
    }

    public void sendNetworkRequestPers(Integer per_id, String personality_name, String hue_color, Integer bri, Integer hue, Integer sat, String screen_color, Integer vibration_level, String music_genre) {
        Personality personalityRequest = new Personality(null, personality_name, hue_color, bri, hue, sat, screen_color, vibration_level, music_genre);

        personalityService.createPersonality(personalityRequest)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call,
                                           @NonNull Response<ResponseBody> response) {
                        Log.d("PersonalityRepository", "Response: " + response.body());
                        try {
                            if (response.body() != null) {
                                Log.d("PersonalityRepository", "success! \n"
                                        + response.body().string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e("PersonalityRepository", "failure :(", t);
                    }
                });
    }

    public void getNetworkRequest(Callback<ApiPersonalityResponse> callback) {
        personalityService.getPersonality().enqueue(callback);
    }
}
