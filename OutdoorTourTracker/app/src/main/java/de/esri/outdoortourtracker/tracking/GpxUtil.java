package de.esri.outdoortourtracker.tracking;

import android.os.Environment;
import android.util.Log;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollection;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GpxUtil {
    private static final String TAG = "GpxUtil";

    public GpxUtil(){
    }

    public static void saveGpxFile(String gpxFileName, String directory, List<GpsPoint> gpsPoints){
        Log.d(TAG, "Save Gpx File");

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDir = new File(Environment.getExternalStorageDirectory(), directory);
            if(!appDir.exists()){
                boolean created = appDir.mkdir();
                Log.d(TAG, "App dir created: " +created);
            }

            File gpxFile = new File(appDir, gpxFileName);
            Log.d(TAG, "GPX file: " + gpxFile.getAbsolutePath());
            try{
                Gpx gpx = new Gpx();
                gpx.setVersion("1.1");
                gpx.setCreator("On Tour Android App");
                Track track = new Track();
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
        }
    }

    /**
     * Read a GPX file in a FeatureCollection.
     * To show it in a map create a Layer: FeatureCollectionLayer tracksLayer = new FeatureCollectionLayer(featureCollection);
     * @param gpxFileName The name of the GPX file.
     * @param directory The directory of the Gpx file.
     * @param renderer The renderer for the tracks.
     * @return A FeatureCollection with the tracks.
     */
    public static FeatureCollection readGpxFile(String gpxFileName, String directory, Renderer renderer){
        Log.d(TAG, "Read Gpx File: " + gpxFileName);
        FeatureCollection featureCollection = new FeatureCollection();
        List<Field> gpxFields = new ArrayList<Field>();
        Field trackNameField = Field.createString("TrackName", "TrackName", 50);
        gpxFields.add(trackNameField);
        FeatureCollectionTable gpxTable = new FeatureCollectionTable(gpxFields, GeometryType.POLYLINE, SpatialReferences.getWgs84());
        if(renderer == null){
            renderer = new SimpleRenderer();
            SimpleLineSymbol sls = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xffff0000, 2.0f);
            ((SimpleRenderer)renderer).setSymbol(sls);
        }
        gpxTable.setRenderer(renderer);

        //List<GpsPoint> gpsPoints = new ArrayList<>();
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File appDir = new File(Environment.getExternalStorageDirectory(), directory);
            InputStream in = null;
            try{
                File gpxFile = new File(appDir, gpxFileName);
                if(gpxFile.exists()){
                    in = new BufferedInputStream(new FileInputStream(gpxFile));
                    GpxParser parser = new GpxParser();
                    Gpx gpx = parser.parse(in);
                    List<Track> tracks = gpx.getTracks();
                    for (Track track: tracks) {
                        List<TrackSegment> trackSegments = track.getTrackSegments();
                        for (TrackSegment trackSegment: trackSegments) {
                            PointCollection linePoints = new PointCollection(SpatialReferences.getWgs84());
                            List<Waypoint> waypoints = trackSegment.getTrackPoints();
                            for (Waypoint waypoint: waypoints) {
                                linePoints.add(waypoint.getLongitude(), waypoint.getLatitude());
                            }
                            Polyline polyline = new Polyline(linePoints);
                            Feature trackLine = gpxTable.createFeature();
                            trackLine.getAttributes().put("TrackName", track.getName());
                            trackLine.setGeometry(polyline);
                            gpxTable.addFeatureAsync(trackLine);
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
        featureCollection.getTables().add(gpxTable);
        return featureCollection;
    }
}
