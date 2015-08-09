package co.com.tappsi.pruebatappsi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import co.com.tappsi.controller.ControllerPosition;
public class MainActivity extends Activity implements OnMapReadyCallback {
	private String message="";
	private ControllerPosition mlocListener;
	static final int ZOOM = 13;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.mlocListener = new ControllerPosition();
		this.mlocListener.setMainActivity(this);
		mlocManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) mlocListener);
        
        //Mapeo el fragment del mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap map) {
    	//obtengo las coordenadas del objeto Position (model) instanciado en el Listener (ControllerPosition)
        LatLng ubicacion = new LatLng(this.mlocListener.getPosition().getLatitude(),this.mlocListener.getPosition().getLongitude());

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, ZOOM));
        //marco con un pin mi posición en el mapa
        map.addMarker(new MarkerOptions()
                .title("Ubicación Actual")//titulo del globo
                .snippet(""+this.message)//contenido del globo
                .position(ubicacion));//posicion para el pin en el mapa
        //recorro el arraylist obtenido de la lectura del JSon para ubicar los clientes en el mapa
        for(int i=0; i < this.mlocListener.getClientes().size(); i++){
        	LatLng ubcClient = new LatLng(this.mlocListener.getClientes().get(i).getLatitude(),this.mlocListener.getClientes().get(i).getLongitude());
            
        	//marco con un pin la posición de los clientes en el mapa
            map.addMarker(new MarkerOptions()
                    .title("Ubicación Cliente "+i)//titulo del globo
                    .snippet(""+"ID Cliente: "+this.mlocListener.getClientes().get(i).getBooking_id()
                    		+"\nDirección Cliente: "+this.mlocListener.getClientes().get(i).getAddress()
                    		+"\nBarrio: "+this.mlocListener.getClientes().get(i).getNeighborhood()
                    		+"\nLatitud: "+this.mlocListener.getClientes().get(i).getLatitude()
                    		+"\nLongitud: "+this.mlocListener.getClientes().get(i).getLongitude()
                    		)//contenido del globo
                    .position(ubcClient));//posicion para el pin en el mapa
        }
        //ahora se ubica el pin del centroide en el mapa:
        LatLng ubcCentroid = new LatLng(this.mlocListener.getCentroid().getLatitude(),this.mlocListener.getCentroid().getLongitude());
        map.addMarker(new MarkerOptions()
        .title("Ubicación Centroide")//titulo del globo
        .snippet(""+"Este punto es el centroide")//contenido del globo
        .position(ubcCentroid));//posicion para el pin en el mapa
        
    }
    public void setLocation(Location loc) {
		if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
			try {
				Geocoder geocoder = new Geocoder(this, Locale.getDefault());
				List<Address> list = geocoder.getFromLocation(
						loc.getLatitude(), loc.getLongitude(), 1);
				if (!list.isEmpty()) {
					Address address = list.get(0);
					this.message="Su dirección es: \n"
							+ address.getAddressLine(0);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}