package com.kotlin_maps

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.multidex.MultiDex
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_gps.*


class GpsActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

        private val TAG = "GpsActivity"
        private lateinit var mGoogleApiClient: GoogleApiClient
        private var mLocationManager: LocationManager? = null
        lateinit var mLocation: Location
        //private var mLocationRequest: LocationRequest? = null
        //private val listener: com.google.android.gms.location.LocationListener? = null
        //private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
        //private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

        lateinit var locationManager: LocationManager



        override fun onStart() {
            super.onStart();
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        }

        override fun onStop() {
            super.onStop();
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

        override fun onConnectionSuspended(p0: Int) {

            Log.i(TAG, "Connection Suspended");
            mGoogleApiClient.connect();
        }

        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
        }

        /*override fun onLocationChanged(location: Location) {


            var msg = "Updated Location: Latitude " + location.longitude.toString() + location.longitude;
            txt_latitude.setText(""+location.latitude);
            txt_longitude.setText(""+location.longitude);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();


        }*/

        override fun onConnected(p0: Bundle?) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            var fusedLocationProviderClient :
                    FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationProviderClient .getLastLocation()
                .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mLocation = location;
                        txt_latitude.setText("" + mLocation.latitude)
                        txt_longitude.setText("" + mLocation.longitude)
                    }
                })
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_gps)

            MultiDex.install(this)

            mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

            mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            checkLocation()
        }

        private fun checkLocation(): Boolean {
            if(!isLocationEnabled())
                showAlert();
            return isLocationEnabled();
        }

        private fun isLocationEnabled(): Boolean {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        private fun showAlert() {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings", DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { paramDialogInterface, paramInt -> })
            dialog.show()
        }

       /* protected fun startLocationUpdates() {

            // Create the location request
            mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
            // Request location updates
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        }*/
    }


