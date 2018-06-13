package de.esri.outdoortourtracker.tracking;

import java.util.Date;

public class Waypoint {
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private Date time;
    private Double magneticVariation;
    private Double	geoidHeight;
    private String	name;
    private String	comment;
    private String	description;
    private String	src;
    private String	sym;
    private String	type;
    private String fixType;
    private Integer sat;
    private Double hdop;
    private Double vdop;
    private Double pdop;
    private Double ageOfGpsData;
    private Integer dgpsId;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Double getMagneticVariation() {
        return magneticVariation;
    }

    public void setMagneticVariation(Double magneticVariation) {
        this.magneticVariation = magneticVariation;
    }

    public Double getGeoidHeight() {
        return geoidHeight;
    }

    public void setGeoidHeight(Double geoidHeight) {
        this.geoidHeight = geoidHeight;
    }

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

    public String getSym() {
        return sym;
    }

    public void setSym(String sym) {
        this.sym = sym;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFixType() {
        return fixType;
    }

    public void setFixType(String fixType) {
        this.fixType = fixType;
    }

    public Integer getSat() {
        return sat;
    }

    public void setSat(Integer sat) {
        this.sat = sat;
    }

    public Double getHdop() {
        return hdop;
    }

    public void setHdop(Double hdop) {
        this.hdop = hdop;
    }

    public Double getVdop() {
        return vdop;
    }

    public void setVdop(Double vdop) {
        this.vdop = vdop;
    }

    public Double getPdop() {
        return pdop;
    }

    public void setPdop(Double pdop) {
        this.pdop = pdop;
    }

    public Double getAgeOfGpsData() {
        return ageOfGpsData;
    }

    public void setAgeOfGpsData(Double ageOfGpsData) {
        this.ageOfGpsData = ageOfGpsData;
    }

    public Integer getDgpsId() {
        return dgpsId;
    }

    public void setDgpsId(Integer dgpsId) {
        this.dgpsId = dgpsId;
    }
}
