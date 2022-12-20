package com.example.myapplication

import com.example.myapplication.databinding.ActivityMainBinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*
import androidx.databinding.DataBindingUtil
import org.json.JSONObject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

data class Weather(var city : String = "NoNe",
                   var temp : String = "0",
                   var temp_like_as : String = "0",
                   var wind_speed : String = "0",
                   var wind_degrees : String = "0",
                   var humidity : String = "0",
                   var day_duration : String = "0");

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var _city = "Irkutsk"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.weather = Weather()

        setContentView(binding.root)

        binding.btnReq.setOnClickListener { onClick(false) }
        binding.btnUpdate.setOnClickListener { onClick(true) }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun loadWeather(is_update : Boolean = false) {
        val API_KEY = resources.getString(R.string.API_KEY)

        if (!is_update)
        {
            var city = binding.cityName.text.toString()
            this._city = if (city == "") { "Irkutsk" } else { city }
        }

        val weatherURL = "https://api.openweathermap.org/data/2.5/weather?q=${this._city}&appid=${API_KEY}&units=metric";
        var data = ""

        try {
            val stream = URL(weatherURL).getContent() as InputStream

            data = Scanner(stream).nextLine() ?: ""
        } catch (e : IOException){
            print("Error Internet connection!")
        }

        var weather = Weather()

        weather.city = this._city

        val jsonObj = JSONObject(data)
        weather.temp = jsonObj.getJSONObject("main").getString("temp") ?: "Error"
        weather.temp_like_as = jsonObj.getJSONObject("main").getString("feels_like") ?: "Error"
        weather.wind_speed = jsonObj.getJSONObject("wind").getString("speed") ?: "Error"
        weather.wind_degrees = jsonObj.getJSONObject("wind").getString("deg") ?: "Error"
        weather.humidity = jsonObj.getJSONObject("main").getString("humidity") ?: "Error"
        val temp = (jsonObj.getJSONObject("sys").getString("sunset") ?: "0").toLong() -
                (jsonObj.getJSONObject("sys").getString("sunrise") ?: "0").toLong()
        weather.day_duration = "${(Duration.convert(temp.toDouble(), DurationUnit.SECONDS, DurationUnit.HOURS)).toInt().toString()} ч. : " +
                               "${(Duration.convert(temp.toDouble() % 3600, DurationUnit.SECONDS, DurationUnit.MINUTES)).toInt().toString()} м."

        binding.weather = weather

        Log.d("mytag", data)
        Log.d("mytag", temp.toString())
    }

    public fun onClick(is_update : Boolean = false) {
        // Используем IO-диспетчер вместо Main (основного потока)
        GlobalScope.launch (Dispatchers.IO) {
            loadWeather(is_update)
        }
    }
}