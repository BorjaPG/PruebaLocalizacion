package com.bp.pruebalocalizacion;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private static final String LOGTAG = "android-localizacion";
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //API client, necesario para acceder a los servicios de Google Play Services.
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /* Gestiona el mapa. Se pueden añadir marcadores y mover la cámara.
     * Si el dispositivo no tiene instalado Google Play Services se solicitará instalarlo. */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /* Se conecta el apiClient y se realizan las operaciones de onConnected(). */
        apiClient.connect();

    }

    /* Representa en un mapa las coordenadas relativas a la ubicación del dispositivo. */
    private void updateLocation(Location loc) {
        if (loc != null) {
            LatLng myLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    //Se puede personalizar el marcador del mapa.
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.nombreMarcador))
                    .position(myLoc)
                    .title("Tú"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));

        } else {
            Toast.makeText(this, "Sin coordenadas", Toast.LENGTH_SHORT).show();
        }
    }

    /* Gestiona la correcta conexión con Google Play Services. */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        /* Si la conexión es correcta se procede a solicitar permisos al usuario. */
        //Primero comprueba si existe permiso para localización.
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Si no lo hay se pide.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        } else { //Si lo hay se obtiene la ubicación.

            /* Se obtiene la última localización conocida con getLastLocation. */
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            /* updateLocation añade un marcador con las coordenadas de la ubicación. */
            updateLocation(lastLocation);
        }

    }

    /* Controla que sucede si se suspende la conexión con Google Play Services. */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Conexión suspendida", Toast.LENGTH_SHORT).show();
    }

    /* Gestiona los errores en la conexión con Google Play Services. */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Se ha producido un error en la conexión con los Google Play Services.
        Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show();
        Log.e(LOGTAG, "Error al conectar con Google Play Services");
    }

    /* Gestiona la respuesta del usuario a la solicitud del permiso. */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            //Si permiso concedido...
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Se activa la localización y se obtiene la ubicación.
                @SuppressWarnings("MissingPermission")
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
                /* updateLocation añade un marcador con las coordenadas de la ubicación. */
                updateLocation(lastLocation);
                
            //Si permiso denegado...
            } else {
                //Deshabilita la funcionalidad relativa a la localización.
                Log.e(LOGTAG, "Permiso denegado");
            }
        }
    }
}
