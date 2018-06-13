package de.esri.outdoortourtracker.tracking;

import java.util.ArrayList;
import java.util.List;

public class Track {
    private String name;
    private String comment;
    private String description;
    private String src;
    private Integer number;
    private String type;
    private List<TrackSegment> trackSegments = new ArrayList<TrackSegment>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TrackSegment> getTrackSegments() {
        return trackSegments;
    }

    public void addTrackSegment(TrackSegment trackSegment) {
        trackSegments.add(trackSegment);
    }
}
