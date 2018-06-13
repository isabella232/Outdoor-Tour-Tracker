package de.esri.outdoortourtracker;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DrawStatus;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedEvent;
import com.esri.arcgisruntime.mapping.view.DrawStatusChangedListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.esri.outdoortourtracker.tracking.GpsPoint;
import de.esri.outdoortourtracker.tracking.ServiceCallback;
import de.esri.outdoortourtracker.tracking.TrackUtil;
import de.esri.outdoortourtracker.tracking.TrackingService;

public class MainActivity extends AppCompatActivity implements ServiceCallback{
    private static final String TAG = "MainActivity";
    private static final int GPS_PERMISSION = 0;
    private static final int FILE_PERMISSION = 1;
    private TabHost tabHost;
    private boolean serviceBound;
    private TrackingService trackingService;
    private Button startButton;
    private Button stopButton;
    private Button saveButton;
    private Button discardButton;
    private MapView mapView;
    private ArcGISMap map;
    private GraphicsOverlay trackOverlay;
    private Graphic trackGraphic;
    private LocationDisplay locationDisplay;
    private boolean showingGpsPosition;
    private ListView tracksList;
    private TrackAdapter trackAdapter;
    private List<HashMap<String, String>> tracks;
    private Handler timerHandler = new Handler();
    private long startTime;
    private TextView timeText;
    private TextView distanceText;
    private TextView speedText;
    private TextView paceText;
    private TextView avgSpeedText;
    private TextView maxSpeedText;
    private TextView altitudeText;
    private TextView altitudeDifferenceText;
    private String[] tourTypes = { Const.TYPE_HIKE, Const.TYPE_MOUNTAINEERING, Const.TYPE_BIKE, Const.TYPE_RUN };

    // Zeit (hh:mm:ss)
    // Strecke (km)
    // Geschw. (km/h)    Tempo (min/km)
    // DurchSchn. geschw.  Max. geschw.
    // Höhe (m) Höhenmeter (m)
    // min. Höhe  max. Höhe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8073461844,none,4N5X0H4AH4GF8YAJM165");

        tabHost = (TabHost) findViewById(R.id.tab_host);
        tabHost.setup();
        // record tab (Aufnahme): Start/Stop Strecke, Zeit, Geschwindigkeit, Durchschnittsgeschwindigkeit, Max. Geschw., Höhe, Gesamthöhenmeter,
        TabHost.TabSpec recordSpec = tabHost.newTabSpec("record");
        View recordTab = LayoutInflater.from(this).inflate(R.layout.tab, null);
        TextView recordTabTitle = (TextView) recordTab.findViewById(R.id.tab_title);
        recordTabTitle.setText(R.string.record_tab);
        ImageView recordTabImage = (ImageView) recordTab.findViewById(R.id.tab_image);
        recordTabImage.setImageDrawable(getResources().getDrawable(R.drawable.record_tab_selector));
        recordSpec.setIndicator(recordTab);
        recordSpec.setContent(R.id.record_tab);
        tabHost.addTab(recordSpec);

        // map tab (Karte)
        TabHost.TabSpec mapSpec = tabHost.newTabSpec("map");
        View mapTab = LayoutInflater.from(this).inflate(R.layout.tab, null);
        TextView mapTabTitle = (TextView) mapTab.findViewById(R.id.tab_title);
        mapTabTitle.setText(R.string.map_tab);
        ImageView mapTabImage = (ImageView) mapTab.findViewById(R.id.tab_image);
        mapTabImage.setImageDrawable(getResources().getDrawable(R.drawable.map_tab_selector));
        mapSpec.setIndicator(mapTab);
        mapSpec.setContent(R.id.map_tab);
        tabHost.addTab(mapSpec);

        // tracks tab (Strecken) Liste der Strecken, Detailansicht
        TabHost.TabSpec tracksSpec = tabHost.newTabSpec("tracks");
        View tracksTab = LayoutInflater.from(this).inflate(R.layout.tab, null);
        TextView tracksTabTitle = (TextView) tracksTab.findViewById(R.id.tab_title);
        tracksTabTitle.setText(R.string.tracks_tab);
        ImageView tracksTabImage = (ImageView) tracksTab.findViewById(R.id.tab_image);
        tracksTabImage.setImageDrawable(getResources().getDrawable(R.drawable.tracks_tab_selector));
        tracksSpec.setIndicator(tracksTab);
        tracksSpec.setContent(R.id.tracks_tab);
        tabHost.addTab(tracksSpec);

        // record tab
        startButton = (Button)findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);
                stopButton.setVisibility(View.VISIBLE);
                startRecording();
            }
        });

        stopButton = (Button)findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
                discardButton.setVisibility(View.VISIBLE);
                stopRecording();
            }
        });

        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setVisibility(View.GONE);
                discardButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                saveTrack();
            }
        });

        discardButton = (Button)findViewById(R.id.discard_button);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setVisibility(View.GONE);
                discardButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                discardTrack();
            }
        });
        timeText = (TextView)findViewById(R.id.time);
        distanceText = (TextView)findViewById(R.id.distance);
        speedText = (TextView)findViewById(R.id.speed);
        paceText = (TextView)findViewById(R.id.pace);
        avgSpeedText = (TextView)findViewById(R.id.avg_spped);
        maxSpeedText = (TextView)findViewById(R.id.max_spped);
        altitudeText = (TextView)findViewById(R.id.altitude);
        altitudeDifferenceText = (TextView)findViewById(R.id.altitude_difference);

        // map tab
        mapView = (MapView)findViewById(R.id.map);
        map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 50.4, 10.8, 5);
        mapView.setMap(map);
        trackOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(trackOverlay);
        locationDisplay = mapView.getLocationDisplay();
        locationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.OFF);

        final ImageButton gpsButton = (ImageButton)findViewById(R.id.gps_button);
        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showingGpsPosition) {
                    locationDisplay.stop();
                    gpsButton.setImageResource(R.drawable.ic_location_on);
                    showingGpsPosition = false;
                } else {
                    locationDisplay.startAsync();
                    mapView.setViewpointCenterAsync(locationDisplay.getLocation().getPosition(), 20000);
                    gpsButton.setImageResource(R.drawable.ic_location_off);
                    showingGpsPosition = true;
                }
            }
        });

        // tracks tab
        tracksList = (ListView)findViewById(R.id.tracks_list);
        // read the tracks
        tracks = TrackUtil.getTracks();
        trackAdapter = new TrackAdapter(this, tracks);
        tracksList.setAdapter(trackAdapter);
        tracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Object item = parent.getItemAtPosition(position);
//                HashMap<String, String> trackItem = (HashMap<String, String>) item;
//                String tourName = trackItem.get(Const.NAME);
//                Log.i(TAG, "Tour  Name: " + tourName);
                showTrackDialog(position);
            }
        });
    }

    private void showTrackDialog(final int position){
        final Dialog trackDialog = new Dialog(this);
        trackDialog.setContentView(R.layout.dialog_track);
        TextView showInMap = (TextView)trackDialog.findViewById(R.id.track_dialog_show_in_map);
        showInMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTrack(TrackUtil.getTrack(position));
                if(mapView.getDrawStatus() == DrawStatus.COMPLETED){
                    Envelope env = trackOverlay.getExtent();
                    if(!Double.isNaN(env.getWidth()) && !Double.isNaN(env.getHeight())){
                        mapView.setViewpointGeometryAsync(trackOverlay.getExtent(), 50.0);
                    }
                }else{
                    mapView.addDrawStatusChangedListener(new DrawStatusChangedListener() {
                        @Override
                        public void drawStatusChanged(DrawStatusChangedEvent drawStatusChangedEvent) {
                            if (drawStatusChangedEvent.getDrawStatus() == DrawStatus.COMPLETED) {
                                if(trackOverlay.getGraphics().size() > 0){
                                    Envelope env = trackOverlay.getExtent();
                                    if(!Double.isNaN(env.getWidth()) && !Double.isNaN(env.getHeight())){
                                        mapView.setViewpointGeometryAsync(trackOverlay.getExtent(), 50.0);
                                        mapView.removeDrawStatusChangedListener(this);
                                    }
                                }
                            }
                        }
                    });
                }
                tabHost.setCurrentTab(1);
                trackDialog.dismiss();
            }
        });
        TextView deleteTrack = (TextView)trackDialog.findViewById(R.id.track_dialog_delete_track);
        deleteTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog confirmDialog = new Dialog(MainActivity.this);
                confirmDialog.setContentView(R.layout.dialog_delete);
                Button okButton = (Button)confirmDialog.findViewById(R.id.button_delete_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tracks.remove(position);
                        trackAdapter.notifyDataSetChanged();
                        TrackUtil.deleteGpxFile(position);
                        confirmDialog.dismiss();
                    }
                });
                Button cancelButton = (Button)confirmDialog.findViewById(R.id.button_delete_cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                    }
                });
                confirmDialog.show();
                trackDialog.dismiss();
            }
        });
        trackDialog.show();
    }

    private void startRecording(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
        }else{
            startTracking();
        }
    }

    private void startTracking(){
        if(!serviceBound){
            Intent intent = new Intent(this, TrackingService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            serviceBound = true;
        }
    }

    private void stopRecording(){
        if(serviceBound){
            trackingService.stopTracking();
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    private void saveTrack(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILE_PERMISSION);
        }else{
            saveGpx();
        }
    }

    private void saveGpx(){
        // http://abhiandroid.com/ui/custom-spinner-examples.html

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_save);
        dialog.setTitle(getString(R.string.save_dialog_title));
        dialog.setCanceledOnTouchOutside(false);
        final EditText tourNameEditText = (EditText)dialog.findViewById(R.id.tour_name);
        final Spinner tourTypeSpinner = (Spinner)dialog.findViewById(R.id.tour_type);
        int[] tourTypeIcons = { R.drawable.ic_hike, R.drawable.ic_mountaineering, R.drawable.ic_bike, R.drawable.ic_run };
        String[] tourTypeNames = { getString(R.string.tour_type_hike), getString(R.string.tour_type_mountaineering), getString(R.string.tour_type_bike), getString(R.string.tour_type_run) };
        TourTypeAdapter adapter = new TourTypeAdapter(this, tourTypeIcons, tourTypeNames);
        tourTypeSpinner.setAdapter(adapter);

        Button okButton = (Button) dialog.findViewById(R.id.button_save_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tourName = tourNameEditText.getText().toString();
                if(!tourName.equals("")){
                    if(serviceBound){
                        List<GpsPoint> track = trackingService.getTrack();
                        double distance = trackingService.getDistance();
                        long duration = trackingService.getDuration();
                        int selectedPosition = tourTypeSpinner.getSelectedItemPosition();
                        String tourType = tourTypes[selectedPosition];
                        TrackUtil.saveGpxFile(tourName, track, tourType, distance, duration);
                        Log.d(TAG, "Saved file");
                        trackAdapter = null;
                        tracks = TrackUtil.getTracks();
                        trackAdapter = new TrackAdapter(MainActivity.this, tracks);
                        trackAdapter.notifyDataSetChanged();
                        tracksList.setAdapter(trackAdapter);
                    }
                    dialog.dismiss();
                    stopTracking();
                }
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.button_save_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void stopTracking(){
        if(serviceBound){
            trackingService.setCallbacks(null);
            unbindService(serviceConnection);
            serviceBound = false;
        }
        resetValues();
        clearTrack();
    }

    private void discardTrack(){
        stopTracking();
    }

    private void resetValues(){
        timeText.setText("00:00:00");
        distanceText.setText("0,000");
        speedText.setText("0,00");
        paceText.setText("0,00");
        avgSpeedText.setText("0,00");
        maxSpeedText.setText("0,00");
        altitudeText.setText("0,00");
        altitudeDifferenceText.setText("0,00");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GPS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking();
                }
                break;
            case FILE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveGpx();
                }
                break;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            TrackingService.LocalBinder localBinder = (TrackingService.LocalBinder)service;
            trackingService = localBinder.getService();
            trackingService.setCallbacks(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            trackingService = null;
        }
    };

    /**
     * Show the track on the map.
     */
    @Override
    public void showTrack(List<GpsPoint> track) {
        //Log.d(TAG, "showTrack");
        SpatialReference wgs84 = SpatialReferences.getWgs84();
        PointCollection pointCollection = new PointCollection(wgs84);
        for(GpsPoint gpsPoint : track){
            pointCollection.add(new Point(gpsPoint.getLongitude(), gpsPoint.getLatitude(), wgs84));
        }
        Polyline line = new Polyline(pointCollection);
        SimpleLineSymbol sls = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2.0f);
        clearTrack();
        trackGraphic = new Graphic(line, sls);
        trackOverlay.getGraphics().add(trackGraphic);
    }

    private void clearTrack(){
        if(trackGraphic != null){
            trackOverlay.getGraphics().remove(trackGraphic);
        }
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.d(TAG, "Timer");
            long time = System.currentTimeMillis() - startTime;
            String timeStr = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(time),
                    TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                    TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
            timeText.setText(timeStr);
            if(trackingService != null){
                double distance = trackingService.getDistance();
                String distanceStr = String.format("%.3f", (distance / 1000));
                distanceText.setText(distanceStr);
                //Log.d(TAG, "# Strecke: " + distance);
                double speed = trackingService.getSpeed();
                String speedStr = String.format("%.2f", (speed * 3.6));
                speedText.setText(speedStr);
                //Log.d(TAG, "# Speed: " + speedStr);
                double pace = 0.0;
                if(speed > 0.0){
                    pace = 60.0 / (speed * 3.6);
                }
                String paceStr = String.format("%.2f", pace);
                paceText.setText(paceStr);
                double avgSpeed = trackingService.getAvgSpeed();
                String avgSpeedStr = String.format("%.2f", avgSpeed * 3.6);
                avgSpeedText.setText(avgSpeedStr);
                double maxSpeed = trackingService.getMaxSpeed();
                String maxSpeedStr = String.format("%.2f", (maxSpeed * 3.6));
                maxSpeedText.setText(maxSpeedStr);
                double altitude = trackingService.getAltitude();
                String altitudeStr = String.format("%.2f", altitude);
                altitudeText.setText(altitudeStr);
                double altitudeDifference = trackingService.getAltitudeDifference();
                String altitudeDifferenceStr = String.format("%.2f", altitudeDifference);
                altitudeDifferenceText.setText(altitudeDifferenceStr);
            }

            timerHandler.postDelayed(this, 1000);
        }
    };
}
