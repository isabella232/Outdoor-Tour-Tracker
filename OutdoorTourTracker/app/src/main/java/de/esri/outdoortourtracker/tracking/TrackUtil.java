package de.esri.outdoortourtracker.tracking;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.esri.outdoortourtracker.Const;

public class TrackUtil {
    private static final String TAG = "TrackUtil";

    public TrackUtil(){
    }

    public static void saveGpxFile(String gpxName, List<GpsPoint> gpsPoints, String tourType, double distance, long duration){
        Log.d(TAG, "Save Gpx File");

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDir = new File(Environment.getExternalStorageDirectory(), "OutdoorTourTracker");
            if(!appDir.exists()){
                boolean created = appDir.mkdir();
                Log.d(TAG, "App dir created: " +created);
            }

            // write the gpx file
            String fileName = gpxName + ".gpx";
            File gpxFile = new File(appDir, fileName);
            Log.d(TAG, "GPX file: " + gpxFile.getAbsolutePath());
            try{
                Gpx gpx = new Gpx();
                gpx.setVersion("1.1");
                gpx.setCreator("Outdoor Tour Tracker");
                Track track = new Track();
                track.setName(gpxName);
                TrackSegment trackSegment = new TrackSegment();
                for(GpsPoint gpsPoint : gpsPoints){
                    Waypoint waypoint = new Waypoint();
                    waypoint.setLatitude(gpsPoint.getLatitude());
                    waypoint.setLongitude(gpsPoint.getLongitude());
                    waypoint.setElevation(gpsPoint.getElevation());
                    waypoint.setTime(new Date(gpsPoint.getTime()));
                    trackSegment.addTrackPoint(waypoint);
                }
                track.addTrackSegment(trackSegment);
                gpx.addTrack(track);

                // write to gpx file
                GpxParser parser = new GpxParser();
                FileOutputStream out = new FileOutputStream(gpxFile);
                parser.writeGpx(gpx, out);
                out.close();
            } catch(Exception ex){
                Log.e(TAG, "Error: " + ex.getMessage());
            }

            // write to track list file
            JSONArray tracks = null;
            File tracksFile = new File(appDir, "Tracks.json");
            if(tracksFile.exists()){
                int length = (int) tracksFile.length();
                byte[] bytes = new byte[length];
                FileInputStream in = null;
                try {
                    in = new FileInputStream(tracksFile);
                    in.read(bytes);
                    String tracksJson = new String(bytes);
                    tracks = new JSONArray(tracksJson);
                }catch(Exception ex){
                    Log.e(TAG, "Error: " + ex.getMessage());
                } finally {
                    try {
                        in.close();
                    }catch(IOException ex){
                    }
                }
            }else{
                try{
                    tracks = new JSONArray();
                }catch(Exception ex){
                    Log.e(TAG, "Error: " + ex.getMessage());
                }
            }
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(tracksFile);
                JSONObject track = new JSONObject();
                track.put(Const.NAME, gpxName);
                track.put(Const.FILE, fileName);
                track.put(Const.TYPE, tourType);
                track.put(Const.DISTANCE, distance);
                track.put(Const.DURATION, duration);
                tracks.put(track);
                String tracksList = tracks.toString();
                out.write(tracksList.getBytes());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }finally{
                try {
                    if(out != null){
                        out.close();
                    }
                }catch(IOException ex){
                }
            }
        }
    }

    public static List<HashMap<String, String>> getTracks(){
        Log.i(TAG, "getTracks");
        List<HashMap<String, String>> trackList = new ArrayList<>();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File appDir = new File(Environment.getExternalStorageDirectory(), "OutdoorTourTracker");
            if (appDir.exists()) {
                File tracksFile = new File(appDir, "Tracks.json");
                if (tracksFile.exists()) {
                    Log.i(TAG, "Tracks File: " + tracksFile.getAbsolutePath());
                    int length = (int) tracksFile.length();
                    byte[] bytes = new byte[length];
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(tracksFile);
                        in.read(bytes);
                        String tracksJson = new String(bytes);
                        JSONArray tracks = new JSONArray(tracksJson);
                        for(int i = 0; i < tracks.length(); i++){
                            JSONObject track = tracks.getJSONObject(i);
                            String name = track.getString(Const.NAME);
                            Log.i(TAG, "Track: " + name);
                            String type = track.getString(Const.TYPE);
                            double distance = track.getDouble(Const.DISTANCE);
                            String distanceStr = String.format("%.3f km",(distance / 1000));
                            long duration = track.getLong(Const.DURATION);
                            String durationStr = String.format("%02d:%02d:%02d h",
                                    TimeUnit.MILLISECONDS.toHours(duration),
                                    TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                            HashMap<String, String> trackItem = new HashMap<String, String>();
                            trackItem.put(Const.TYPE, type);
                            trackItem.put(Const.NAME, name);
                            trackItem.put(Const.DISTANCE, distanceStr);
                            trackItem.put(Const.DURATION, durationStr);
                            trackList.add(trackItem);
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error: " + ex.getMessage());
                    } finally {
                        try {
                            if(in != null){
                                in.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                }
            }
        }
        return trackList;
    }

    public static void deleteGpxFile(int position){
        Log.d(TAG, "Delete Gpx File");

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDir = new File(Environment.getExternalStorageDirectory(), "OutdoorTourTracker");
            // read tracks list
            JSONArray tracks = null;
            File tracksFile = new File(appDir, "Tracks.json");
            if(tracksFile.exists()){
                int length = (int) tracksFile.length();
                byte[] bytes = new byte[length];
                FileInputStream in = null;
                try {
                    in = new FileInputStream(tracksFile);
                    in.read(bytes);
                    String tracksJson = new String(bytes);
                    tracks = new JSONArray(tracksJson);
                }catch(Exception ex){
                    Log.e(TAG, "Error: " + ex.getMessage());
                } finally {
                    try {
                        in.close();
                    }catch(IOException ex){
                    }
                }
            }
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(tracksFile);
                JSONObject track = tracks.getJSONObject(position);
                String gpxFileName = track.getString(Const.FILE);
                File gpxFile = new File(appDir, gpxFileName);
                gpxFile.delete();
                JSONArray newTtracks = new JSONArray();
                for(int i = 0; i < tracks.length(); i++){
                    if(i != position){
                        newTtracks.put(tracks.getJSONObject(i));
                    }
                }
                String tracksList = newTtracks.toString();
                out.write(tracksList.getBytes());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }finally{
                try {
                    out.close();
                }catch(IOException ex){
                }
            }
        }
    }

    public static List<GpsPoint> getTrack(int position){
        Log.d(TAG, "Get track: " + position);
        List<GpsPoint> gpsPoints = new ArrayList<>();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDir = new File(Environment.getExternalStorageDirectory(), "OutdoorTourTracker");
            // read tracks list
            JSONArray trackArray = null;
            File tracksFile = new File(appDir, "Tracks.json");
            if(tracksFile.exists()){
                int length = (int) tracksFile.length();
                byte[] bytes = new byte[length];
                FileInputStream in = null;
                try {
                    in = new FileInputStream(tracksFile);
                    in.read(bytes);
                    String tracksJson = new String(bytes);
                    trackArray = new JSONArray(tracksJson);
                }catch(Exception ex){
                    Log.e(TAG, "Error: " + ex.getMessage());
                } finally {
                    try {
                        in.close();
                    }catch(IOException ex){
                    }
                }
            }
            InputStream in = null;
            try{
                JSONObject trackJson = trackArray.getJSONObject(position);
                String gpxFileName = trackJson.getString(Const.FILE);
                Log.d(TAG, "GPX File: " + gpxFileName);
                File gpxFile = new File(appDir, gpxFileName);
                if(gpxFile.exists()){
                    in = new BufferedInputStream(new FileInputStream(gpxFile));
                    GpxParser parser = new GpxParser();
                    Gpx gpx = parser.parse(in);
                    List<Track> tracks = gpx.getTracks();
                    for (Track track: tracks) {
                        List<TrackSegment> trackSegments = track.getTrackSegments();
                        for (TrackSegment trackSegment: trackSegments) {
                            List<Waypoint> waypoints = trackSegment.getTrackPoints();
                            for (Waypoint waypoint: waypoints) {
                                GpsPoint gpsPoint = new GpsPoint(waypoint.getLatitude(), waypoint.getLongitude(), waypoint.getElevation(), waypoint.getTime().getTime());
                                gpsPoints.add(gpsPoint);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }finally{
                try {
                    if(in != null){
                        in.close();
                    }
                }catch(IOException ex){
                }
            }
        }
        return gpsPoints;
    }
}
