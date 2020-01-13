package com.example.offlinemovies.data;

import androidx.room.Room;

import com.example.offlinemovies.app.MyApp;
import com.example.offlinemovies.data.local.MovieRoomDatabase;
import com.example.offlinemovies.data.local.dao.MovieDAO;
import com.example.offlinemovies.data.remote.ApiConstants;
import com.example.offlinemovies.data.remote.MovieApiService;
import com.example.offlinemovies.data.remote.RequestInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {
    private final MovieApiService movieApiService;
    private final MovieDAO movieDAO;
    public MovieRepository(){
        //Local > ROOM
        MovieRoomDatabase movieRoomDatabase = Room.databaseBuilder(
                MyApp.getContext(),
                MovieRoomDatabase.class,
                "db_movies"
        ).build();
        movieDAO = movieRoomDatabase.getmovieDAO();

        //RequestInterceptor: incluir  en la cabecera (URL) de la
        //peticion el TOKEN o API_KEY que autoriza al usuario
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new RequestInterceptor());
        OkHttpClient cliente = okHttpClientBuilder.build();

        //Remote Retrofit
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        movieApiService=retrofit.create(MovieApiService.class);
    }
}
