package com.jesussoto.android.rappimovies.api

import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.jesussoto.android.rappimovies.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

object WebServiceUtils {

    @Volatile
    private var sWebService: WebService? = null

    @JvmStatic
    @Synchronized
    fun getWebService(): WebService {
        if (sWebService == null) {
            val dateDeserializer = object : JsonDeserializer<Date?> {
                override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    return try {
                        format.parse(json?.asString);
                    } catch (t: Throwable) {
                        null
                    }
                }
            }

            val gson = GsonBuilder()
                    .registerTypeAdapter(Date::class.java, dateDeserializer)
                    .create()

            val clientBuilder = OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor())

            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor()
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                clientBuilder.addInterceptor(loggingInterceptor)
            }

            sWebService = Retrofit.Builder()
                    .baseUrl(BuildConfig.TMDB_API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build()
                    .create<WebService>(WebService::class.java)
        }

        return sWebService!!
    }

    @JvmStatic
    fun buildMoviePosterUri(moviePosterPath: String): Uri {
        return Uri.parse(BuildConfig.TMDB_IMAGE_BASE_URL).buildUpon()
                .appendPath(WebService.PATH_POSTER_SIZE)
                .appendPath(moviePosterPath.substring(1))
                .build()
    }

    @JvmStatic
    fun buildMovieBackdropUri(movieBackdropPath: String): Uri {
        return Uri.parse(BuildConfig.TMDB_IMAGE_BASE_URL).buildUpon()
                .appendPath(WebService.PATH_BACKDROP_SIZE)
                .appendPath(movieBackdropPath.substring(1))
                .build()
    }

    @JvmStatic
    fun buildYoutubeThumbUri(videoKey: String): Uri {
        return Uri.parse("https://img.youtube.com/vi").buildUpon()
                .appendPath(videoKey)
                .appendPath("default.jpg")
                .build()
    }
}
