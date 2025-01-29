package com.example.backgroundlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Em Compose, você NÃO usa R.layout, e sim "setContent { }"
        setContent {
            // Aqui é sua raiz de Compose
            MyComposeScreen()
        }

        // Inicializa o FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura as requisições de localização
        locationRequest = LocationRequest.create().apply {
            interval = 10_000L       // Intervalo desejado (10s neste exemplo)
            fastestInterval = 5_000L // Intervalo mais rápido possível
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        // Define como tratar as atualizações de localização
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Toast.makeText(
                        this@MainActivity,
                        "Lat: $latitude, Lng: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Verifica permissões de localização
        checkLocationPermissions()
    }

    /**
     * Verifica se as permissões de localização foram concedidas.
     * Caso não, pede ao usuário.
     */
    private fun checkLocationPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Verifica ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Verifica ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Se rodar em Android 10 (Q) ou superior, verifica ACCESS_BACKGROUND_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }

        // Se a lista não estiver vazia, solicitamos as permissões
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    /**
     * Retorno do pedido de permissões ao usuário.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Verifica se todas as permissões foram concedidas
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Permissões de localização negadas.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Inicia a obtenção de localização caso tenha as permissões necessárias.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Toast.makeText(
                this,
                "Sem permissão para obter localização.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Exemplo de quando parar de receber updates ao sair do foreground
     */
    override fun onPause() {
        super.onPause()
        // fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}

/**
 * Exemplo de Composable raiz simples.
 */
@Composable
fun MyComposeScreen() {
    Text("Olá, Jetpack Compose + Localização!")
}
