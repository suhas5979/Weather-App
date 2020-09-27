package com.scube.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.scube.weatherapp.network.WeatherService
import com.weatherapp.models.WeatherResponse
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog :Dialog? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(!isLocationEnabled()){
            Toast.makeText(this,
                "your location provider is turned off. please turn it on"
            ,Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            Log.i("debug"," dexter")
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if(report!!.areAllPermissionsGranted()){
                            Log.i("debug"," data")
                            requestLocationData()
                        }
                        if(report.isAnyPermissionPermanentlyDenied){
                            Log.i("debug"," denied")
                            Toast.makeText(this@MainActivity,
                                "your have denied location permission"
                                ,Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread()
                .check()
        }
    }
    private fun getLocationWeatherDetails(latitude:Double,longitude:Double){
        if(Constants.isNetworkAvailable(this)){
            val retrofit : Retrofit =Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service : WeatherService = retrofit
                .create<WeatherService>(WeatherService::class.java)
            val listCall : Call<WeatherResponse> = service.getWeather(
                latitude,longitude,Constants.METRIC_UNIT,
                Constants.APP_ID
            )
            showProgressDialog()
            listCall.enqueue(object :Callback<WeatherResponse>{
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable?) {
                     Log.e("Errorrr",t!!.message.toString())
                }

                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if(response!!.isSuccessful){
                        hideProgressDialog()
                        val weatherList: WeatherResponse? = response.body()
                        if (weatherList != null) {
                            setupUI(weatherList)
                        }
                        Log.i("response","$weatherList")
                    }else{
                        val rc = response.code()
                        when(rc){
                            400->{
                                Log.e("Error 400","Bad connection")
                            }
                        404->{
                            Log.e("Error 404","Not found")
                        }
                        else->{
                              Log.e("Error","Errorrrrr")
                            }
                        }
                    }
                }
            })
        }else{
            Toast.makeText(this@MainActivity,
                "do not have internet"
                ,Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("MissingPermission")
    private fun requestLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        Log.i("debug"," location client")
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,mLocationCallback, Looper.myLooper()
        )
    }
    private var mLocationCallback = object :LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            Log.i("debug"," location update")
            val mLastLocation : Location = locationResult!!.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("current latitude"," $latitude")
            val longitude = mLastLocation.longitude
            Log.i("current latitude"," $longitude")
            getLocationWeatherDetails(latitude,longitude)
        }
    }
    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private  fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions")
            .setPositiveButton("GO TO SETTINGS"){_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data =uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }

            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()

            }
            .show()
    }
    private fun showProgressDialog(){
        mProgressDialog = Dialog(this)

        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)

        mProgressDialog!!.show()
    }
    private fun hideProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }

    private fun setupUI(weatherList:WeatherResponse){
        for(i in weatherList.weather.indices){
            Log.i("weather name",weatherList.weather.toString())
        }

    }
}