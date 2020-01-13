package com.example.offlinemovies.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.offlinemovies.app.MyApp;
import com.example.offlinemovies.data.local.MovieRoomDatabase;
import com.example.offlinemovies.data.local.dao.MovieDAO;
import com.example.offlinemovies.data.local.entity.MovieEntity;
import com.example.offlinemovies.data.network.NetworkBoundResource;
import com.example.offlinemovies.data.network.Resource;
import com.example.offlinemovies.data.remote.ApiConstants;
import com.example.offlinemovies.data.remote.MovieApiService;
import com.example.offlinemovies.data.remote.RequestInterceptor;
import com.example.offlinemovies.data.remote.model.MoviesResponse;

import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {
    private final MovieApiService movieApiService;
    private final MovieDAO movieDAO;

    public MovieRepository() {
        //Local > ROOM
        MovieRoomDatabase movieRoomDatabase = Room.databaseBuilder(
                MyApp.getContext(),
                MovieRoomDatabase.class,
                "/storage/emulated/0/InecMovil/db_movies.db"
        ).build();
        movieDAO = movieRoomDatabase.getmovieDAO();

        //RequestInterceptor: incluir  en la cabecera (URL) de la
        //peticion el TOKEN o API_KEY que autoriza al usuario
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.addInterceptor(new RequestInterceptor());
        OkHttpClient cliente = okHttpClientBuilder.build();

        //Remote Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConstants.BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        movieApiService = retrofit.create(MovieApiService.class);
    }

    public LiveData<Resource<List<MovieEntity>>> getPopularMovies() {
        //Tipo que devuelve ROOM (BD local), Tipo que devuelve la API con Retrofit
        return new NetworkBoundResource<List<MovieEntity>, MoviesResponse>() {

            @Override
            protected void saveCallResult(@NonNull MoviesResponse item) {
                //Se encarga de almacenar la respuesta del servidor en la BD local,
                // para futuras ocasiones si no tenemos conexion a internet podamos optar
                //por recibir la informacion de ese recurso local
                movieDAO.saveMovies(item.getResults());
            }

            @NonNull
            @Override
            protected LiveData<List<MovieEntity>> loadFromDb() {
                //Los datos que dispongamos en ROOm, em la BD local
                return movieDAO.loadMovies();
            }

            @NonNull
            @Override
            protected Call<MoviesResponse> createCall() {
                // Si hay acceso a internet, obtenemos los datos de la API remota
                return movieApiService.loadPopularMovies();
            }
        }.getAsLiveData();
    }
}
