package com.myproductions.easyweatherpro

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.json.JSONObject
import java.lang.NumberFormatException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AdapterView




class MainActivity : AppCompatActivity() {

    var CITY : String = "Delhi,in"
    val API : String = "009945dbc069272c6d92b03024af761f"

    lateinit var spinner : Spinner

    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val network = checkNetwork()

        if (network) {
            process()
        }else{
            Toast.makeText(this,"No internet Connection",Toast.LENGTH_SHORT).show()
        }
    }

    fun process(){
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        spinner = findViewById(R.id.locationSpinner)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // your code here
                CITY = "${spinner.selectedItem}, in"
                weatherTask().execute()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

        weatherTask().execute()
    }
    private fun checkNetwork(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    inner class weatherTask() : AsyncTask<String, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: String?): String? {
            var response : String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=$API")
                    .readText(Charsets.UTF_8)
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            findViewById<ProgressBar>(R.id.loader).visibility = View.INVISIBLE
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val temp = main.getString("temp")
            try {
                val temperature = (temp.substring(0,3).toInt()-273).toString()+"°C"
                val min_temp = "MIN Temp : "+(main.getString("temp_min").substring(0,3).toInt()-273).toString()+"°C"
                val max_temp = "MAX Temp : "+(main.getString("temp_max").substring(0,3).toInt()-273).toString()+"°C"
                val wind_speed = wind.getString("speed")+" km/h"
                val pressure = main.getString("pressure")+" mbar"
                val humidity = main.getString("humidity")+" %"
                val address = jsonObj.getString("name")+", "+sys.getString("country")
                val sunrise = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    .format(Date(sys.getLong("sunrise")*1000))
                val sunset = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    .format(Date(sys.getLong("sunset")*1000))

                findViewById<TextView>(R.id.temp).text = temperature
                findViewById<TextView>(R.id.status).text = weather.getString("description")
                findViewById<TextView>(R.id.min_temp).text = min_temp
                findViewById<TextView>(R.id.max_temp).text = max_temp
                findViewById<TextView>(R.id.sunrise).text = sunrise
                findViewById<TextView>(R.id.sunset).text = sunset
                findViewById<TextView>(R.id.wind).text = wind_speed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.humidity).text = humidity
            }catch (e: NumberFormatException){
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}