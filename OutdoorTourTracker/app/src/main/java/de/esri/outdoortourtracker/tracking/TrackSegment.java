package de.esri.outdoortourtracker.tracking;

import java.util.ArrayList;
import java.util.List;

public class TrackSegment {
    private List<Waypoint> trackPoints = new ArrayList<Waypoint>();

    public List<Waypoint> getTrackPoints() {
        return trackPoints;
    }

    public void addTrackPoint(Waypoint trackPoint) {
        trackPoints.add(trackPoint);
    }

    public void addTrackPoints(List<Waypoint> trackPoints) {
        trackPoints.addAll(trackPoints);
    }
}
