package de.esri.outdoortourtracker.tracking;

import java.util.List;

public interface ServiceCallback {
    void showTrack(List<GpsPoint> track);
}
