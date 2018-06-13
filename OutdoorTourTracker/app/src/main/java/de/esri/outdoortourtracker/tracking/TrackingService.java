package de.esri.outdoortourtracker.tracking;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.esri.outdoortourtracker.R;

/**
 * Android service that listens for GPS location updates to create a track.
 */
public class TrackingService extends Service {
    private static final String TAG = "TrackingService";
    private IBinder binder = new LocalBinder();
    private ServiceCallback serviceCallback;
    private LocationManager locationManager;
    private TrackLocationListener locationListener;
    private Location lastLocation;
    private List<GpsPoint> gpsPoints = new ArrayList<GpsPoint>();
    private float distance; // distance in meters
    private long startTime;
    private long time;
    private float speed; // speed in m/s
    private float maxSpeed; // max speed in m/s
    private float avgSpeed; // average speed in m/s
    private double altitude; // altitude in meters
    private double lastAltitude; // last altitude in meters
    private double altitudeDifference;
    private List<Double> lastAltitudes = new ArrayList<>();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new TrackLocationListener();
        int trackInterval = getResources().getInteger(R.integer.track_intervall);
        int trackDistance = getResources().getInteger(R.integer.track_distance);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, trackInterval, trackDistance, locationListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    public class LocalBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }

    public void setCallbacks(ServiceCallback callback) {
        serviceCallback = callback;
    }

    public void stopTracking(){
        if(locationManager != null){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    public List<GpsPoint> getTrack(){
        return gpsPoints;
    }

    public double getDistance(){
        return distance;
    }

    public long getDuration(){
        long duration = time - startTime;
        return duration;
    }

    public double getSpeed(){
        return speed;
    }

    public double getMaxSpeed(){
        return maxSpeed;
    }

    public double getAvgSpeed(){
        return avgSpeed;
    }

    public double getAltitude(){
        return altitude;
    }

    public double getAltitudeDifference(){
        return altitudeDifference;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopTracking();
    }

    private void addAltitude(double altitude){
        if(lastAltitudes.size() > 6){
            lastAltitudes.remove(0);
        }
        lastAltitudes.add(altitude);
    }

    private double calculateAltitude(){
        double altitude = Double.MIN_VALUE;
        if(lastAltitudes.size() >= 2){
            double sum = 0.0;
            int num = lastAltitudes.size();
            for(int i = 0; i < num; i++){
                sum += lastAltitudes.get(i);
            }
            altitude = sum / num;
        }
        return altitude;
    }

    private class TrackLocationListener implements android.location.LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
            double lat = location.getLatitude();
            //Log.d(TAG, "Lat: " + lat);
            double lon = location.getLongitude();
            //Log.d(TAG, "Long: " + lon);
            double ele = location.getAltitude();
            //Log.d(TAG, "Altitude: " + ele);
            if(startTime == 0){
                startTime = location.getTime();
            }
            time = location.getTime();
            //Log.d(TAG, "Time: " + time);
            speed = location.getSpeed();
            //Log.d(TAG, "Speed: " + speed);
            if(speed > maxSpeed){
                maxSpeed = speed;
            }
            if(lastLocation != null){
                distance += location.distanceTo(lastLocation);
                avgSpeed = distance / (time - startTime) * 1000;
            }
            if(lastAltitude != 0.0){
                addAltitude(ele);
            }
            lastAltitude = altitude;
            //Log.d(TAG, "Last altitude: " + lastAltitude);
            altitude = calculateAltitude();
            if(altitude > Double.MIN_VALUE && lastAltitude > Double.MIN_VALUE && altitude > lastAltitude){
                altitudeDifference += (altitude - lastAltitude);
            }
            GpsPoint gpsPoint = new GpsPoint(lat, lon, ele, time);
            gpsPoints.add(gpsPoint);
            lastLocation = location;
            serviceCallback.showTrack(gpsPoints);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }
    }
}
