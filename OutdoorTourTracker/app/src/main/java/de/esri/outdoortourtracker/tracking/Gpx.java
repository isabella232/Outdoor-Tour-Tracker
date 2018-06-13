package de.esri.outdoortourtracker.tracking;

import java.util.ArrayList;
import java.util.List;

public class Gpx {
    // attributes
    private String version = "1.1";
    private String creator = "";
    // sub elements
    private List<Waypoint> waypoints = new ArrayList<Waypoint>();
    private List<Route> routes = new ArrayList<Route>();
    private List<Track> tracks = new ArrayList<Track>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }
}
