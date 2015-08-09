package co.com.tappsi.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import co.com.tappsi.model.MyPosition;
import co.com.tappsi.model.Centroid;
import co.com.tappsi.model.Client;
import co.com.tappsi.pruebatappsi.MainActivity;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ControllerPosition implements LocationListener {
	private MyPosition position;//instancio la clase MyPosition para llenarlo con la posición del usuario
	private Centroid centroid;
	private ArrayList<Client> clients;//Para los datos leidos del JSon
	private String text;//para obtener la información de la ejecución de los métodos
	MainActivity mainActivity;//para manipular los atributos de la vista (Activity) que mapea el XML
	public ControllerPosition(){
		this.clients = new ArrayList<Client>();//instancio un ArrayList de Clientes para cargar varios objetos Client
		this.position = new MyPosition();
		this.centroid = new Centroid();
		this.text = "";
	}
	public MainActivity getMainActivity() {
		return mainActivity;
	}
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	@Override
	public void onLocationChanged(Location loc) {
		//lleno el objeto position cada que cambie la psición
		this.position.setLatitude(loc.getLatitude());
		this.position.setLongitude(loc.getLongitude());
		this.text = "Su ubicación actual es: " + "\n Lat = "
				+ this.position.getLatitude() + "\n Long = " 
				+ this.position.getLongitude();
		this.mainActivity.setMessage(this.text);
		this.mainActivity.setLocation(loc);
	}
	@Override
	public void onProviderDisabled(String provider) {
		this.text ="GPS Desactivado";//se ejecuta cuando el GPS no este activo
		this.mainActivity.setMessage(this.text);
	}
	@Override
	public void onProviderEnabled(String provider) {
		this.text ="GPS Activado";//se ejecuta cuando el GPS este activo
		this.mainActivity.setMessage(this.text);
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}	
	public MyPosition getPosition() {
		return position;
	}
	public void setPosition(MyPosition position) {
		this.position = position;
	}
	public Centroid getCentroid() {
		return centroid;
	}
	public void setCentroid(Centroid centroid) {
		this.centroid = centroid;
	}
	@Override
	public String toString(){
		return this.text;
	}
	public ArrayList<Client> getClientes(){//método que obtiene los datos del JSon del link de Tappsi y retorna un ArrayList de Objetos Client
		Client objClient;//objeto que se llenará por cada iteración del JSon
		//consumo el servicio
		HttpGet httpGet = new HttpGet("https://raw.githubusercontent.com/tappsi/test_recruiting/master/sample_files/driver_info.json");
    	HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject = new JSONObject(stringBuilder.toString());
    		this.clients.clear();//limpio el arreglo para cargar la nueva lectura del JSon y que no se acumulen con los de una consulta anterior
    		//recorro el JSONArray de bookings
            for(int i=0; i < ((JSONArray) jsonObject.get("bookings")).length(); i++){
            	//por cada iteración se crea un nuevo objeto Client
    	        objClient = new Client();//instancio objeto que se llenará y se colocara en cada posición del ArrayList
            	//lleno cada atributo del objeto (Cliente) instancido con las posiciones contenidas en el JSon
            	objClient.setBooking_id(((JSONArray) jsonObject.get("bookings")).getJSONObject(i).getString("booking_id"));
            	objClient.setAddress(((JSONArray) jsonObject.get("bookings")).getJSONObject(i).getString("address"));
            	objClient.setNeighborhood(((JSONArray) jsonObject.get("bookings")).getJSONObject(i).getString("neighborhood"));
            	objClient.setLatitude(((JSONArray) jsonObject.get("bookings")).getJSONObject(i).getDouble("lat"));
            	objClient.setLongitude(((JSONArray) jsonObject.get("bookings")).getJSONObject(i).getDouble("lon"));
            	//anexo cada objeto al ArrayList de Clientes
            	this.clients.add(objClient);
    		}
    	    return this.clients;//retorno el listado de Clientes
        } 
        catch (ClientProtocolException e) {
        	Log.d("PROTOCOL_ERROR","Error: "+e);//para hacer seguimiento de la excepción
        	return null;
        } 
        catch (IOException e) {
        	Log.d("IO_ERROR","Error: "+e);//para hacer seguimiento de la excepción
        	return null;
        } 
        catch (JSONException e) {
        	Log.d("JSON_ERROR","Error: "+e);//para hacer seguimiento de la excepción
        	return null;
		}
	}
	public Centroid centroid(){
		//reinicio los valores del centroide para cuando se vuelva a calcular
		this.centroid.setLatitude(0);
		this.centroid.setLongitude(0);
		//recorro el arraylist obtenido de la lectura del JSon para ubicar hacer la integral de las coordenadas de los clientes
        for(int i=0; i < this.clients.size(); i++){
        	this.centroid.setLatitude(this.centroid.getLatitude()+this.clients.get(i).getLatitude());
        	this.centroid.setLongitude(this.centroid.getLongitude()+this.clients.get(i).getLongitude());
        }
        //al final anexamos la ubicación del usuario respecto a los clientes
        this.centroid.setLatitude(this.centroid.getLatitude()+this.position.getLatitude());
        this.centroid.setLongitude(this.centroid.getLongitude()+this.position.getLongitude());
        //divido entre la cantidad de clientes + 1 (la posición del usuario)
        this.centroid.setLatitude(this.centroid.getLatitude()/(this.clients.size()+1));
        this.centroid.setLongitude(this.centroid.getLongitude()/(this.clients.size()+1));
        //retorno el centroide		
        return this.centroid;
	}
}
