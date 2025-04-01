package com.example.parquiatenov10

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class Localizacion : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var usuario: FusedLocationProviderClient
    private lateinit var VolverButton: Button

    data class UbicacionDetallada(
        val coordinates: LatLng,
        val title: String,
        val description: String,
        val zoom: Float
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localizacion)
        overridePendingTransition(0, 0)

        VolverButton = findViewById(R.id.Volver_BTN)
        usuario = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        VolverButton.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val locations = listOf(
            UbicacionDetallada(
                LatLng(4.57659138470627, -74.2240896601396),
                "Soacha",
                "Centro regional Soacha",
                0f
            ),
            UbicacionDetallada(
                LatLng(4.593701120910692, -74.16766795674489),
                "Perdomo",
                "Sede perdomo",
                0f
            ),
            UbicacionDetallada(
                LatLng(4.592124767752068, -74.08401575355045),
                "San camilo",
                "Sede San camilo",
                0f
            ),
            UbicacionDetallada(
                LatLng(4.699314309392525, -74.08915049599695),
                "Sede 80",
                "Sede 80",
                0f
            ),
            UbicacionDetallada(
                LatLng(4.711247338118655, -74.09691817352969),
                "Sede 90",
                "Sede 90",
                0f
            )
        )

        // Agregar marcadores en el mapa
        for (ubicacion in locations) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(ubicacion.coordinates)
                    .title(ubicacion.description)
            )
            marker?.tag = ubicacion.zoom
        }

        val initialLocation = LatLng(4.7103897, -74.1224939)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 17f))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        mMap.isMyLocationEnabled = true

        usuario.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLocation = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                mMap.addMarker(MarkerOptions().position(userLocation).title("Ubicación actual"))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap)
            }
        }
    }
}
