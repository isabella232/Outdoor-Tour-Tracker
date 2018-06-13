package de.esri.outdoortourtracker.tracking;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Class to parse or write a gpx file.
 */
public class GpxParser {

    public Gpx parse(InputStream inputStream) {
        try {
            DocumentBuilder documentbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentbuilder.parse(inputStream);
            Node rootNode = document.getFirstChild();
            if(rootNode != null && rootNode.getNodeName().equals(GpxConstants.GPX_NODE)) {
                Gpx gpx = new Gpx();
                NamedNodeMap attributes = rootNode.getAttributes();
                Node versionAttr = attributes.getNamedItem(GpxConstants.VERSION_ATTR);
                gpx.setVersion(versionAttr.getNodeValue());
                Node creatorAttr = attributes.getNamedItem(GpxConstants.CREATOR_ATTR);
                gpx.setCreator(creatorAttr.getNodeValue());
                NodeList nodelist = rootNode.getChildNodes();
                for(int i = 0; i < nodelist.getLength(); i++) {
                    Node node = nodelist.item(i);
                    if(node.getNodeName().equals(GpxConstants.WPT_NODE)) {
                        Waypoint waypoint = parseWaypoint(node);
                        gpx.addWaypoint(waypoint);
                        continue;
                    }
                    if(node.getNodeName().equals(GpxConstants.RTE_NODE)) {
                        Route route = parseRoute(node);
                        gpx.addRoute(route);
                        continue;
                    }
                    if(node.getNodeName().equals(GpxConstants.TRK_NODE)) {
                        Track track = parseTrack(node);
                        gpx.addTrack(track);
                        continue;
                    }
                }
                return gpx;
            }else {
                // not gpx format
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Waypoint parseWaypoint(Node node) {
        Waypoint waypoint = new Waypoint();
        NamedNodeMap attributes = node.getAttributes();
        Node latAttr = attributes.getNamedItem(GpxConstants.LAT_ATTR);
        if(latAttr != null) {
            try {
                double latitude = Double.parseDouble(latAttr.getNodeValue());
                waypoint.setLatitude(latitude);
            }catch(NumberFormatException e) {}
        }
        Node lonAttr = attributes.getNamedItem(GpxConstants.LON_ATTR);
        if(lonAttr != null) {
            try {
                double longitude = Double.parseDouble(lonAttr.getNodeValue());
                waypoint.setLongitude(longitude);
            }catch(NumberFormatException e) {}
        }
        NodeList nodelist = node.getChildNodes();
        for(int i = 0; i < nodelist.getLength(); i++) {
            Node subNode = nodelist.item(i);
            if(subNode.getNodeName().equals(GpxConstants.ELE_NODE)) {
                waypoint.setElevation(getNodeValueAsDouble(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.TIME_NODE)) {
                waypoint.setTime(getNodeValueAsDate(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.NAME_NODE)) {
                waypoint.setName(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.CMT_NODE)) {
                waypoint.setComment(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.DESC_NODE)) {
                waypoint.setDescription(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.SRC_NODE)) {
                waypoint.setSrc(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.SYM_NODE)) {
                waypoint.setSym(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.TYPE_NODE)) {
                waypoint.setType(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.FIX_NODE)) {
                waypoint.setFixType(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.SAT_NODE)) {
                waypoint.setSat(getNodeValueAsInteger(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.HDOP_NODE)) {
                waypoint.setHdop(getNodeValueAsDouble(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.VDOP_NODE)) {
                waypoint.setVdop(getNodeValueAsDouble(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.PDOP_NODE)) {
                waypoint.setPdop(getNodeValueAsDouble(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.AGEOFGPSDATA_NODE)) {
                waypoint.setAgeOfGpsData(getNodeValueAsDouble(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.DGPSID_NODE)) {
                waypoint.setDgpsId(getNodeValueAsInteger(subNode));
                continue;
            }
        }
        return waypoint;
    }

    private Route parseRoute(Node node) {
        Route route = new Route();
        NodeList nodelist = node.getChildNodes();
        for(int i = 0; i < nodelist.getLength(); i++) {
            Node subNode = nodelist.item(i);
            if(subNode.getNodeName().equals(GpxConstants.NAME_NODE)) {
                route.setName(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.CMT_NODE)) {
                route.setComment(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.DESC_NODE)) {
                route.setDescription(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.SRC_NODE)) {
                route.setSrc(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.NUMBER_NODE)) {
                route.setNumber(getNodeValueAsInteger(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.TYPE_NODE)) {
                route.setType(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.RTEPT_NODE)) {
                Waypoint routePoint = parseWaypoint(subNode);
                route.addRoutePoint(routePoint);
            }
        }
        return route;
    }

    private Track parseTrack(Node node) {
        Track track = new Track();
        NodeList nodelist = node.getChildNodes();
        for(int i = 0; i < nodelist.getLength(); i++) {
            Node subNode = nodelist.item(i);
            if(subNode.getNodeName().equals(GpxConstants.NAME_NODE)) {
                track.setName(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.CMT_NODE)) {
                track.setComment(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.DESC_NODE)) {
                track.setDescription(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.SRC_NODE)) {
                track.setSrc(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.NUMBER_NODE)) {
                track.setNumber(getNodeValueAsInteger(subNode));
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.TYPE_NODE)) {
                track.setType(subNode.getFirstChild().getNodeValue());
                continue;
            }
            if(subNode.getNodeName().equals(GpxConstants.TRKSEG_NODE)) {
                TrackSegment trackSegment = parseTrackSegment(subNode);
                track.addTrackSegment(trackSegment);
            }
        }
        return track;
    }

    private TrackSegment parseTrackSegment(Node node) {
        TrackSegment trackSegment = new TrackSegment();
        NodeList nodelist = node.getChildNodes();
        for(int i = 0; i < nodelist.getLength(); i++) {
            Node subNode = nodelist.item(i);
            if(subNode.getNodeName().equals(GpxConstants.TRKPT_NODE)) {
                Waypoint trackPoint = parseWaypoint(subNode);
                trackSegment.addTrackPoint(trackPoint);
            }
        }
        return trackSegment;
    }

    private Double getNodeValueAsDouble(Node node){
        Double value = null;
        try{
            value = Double.valueOf(Double.parseDouble(node.getFirstChild().getNodeValue()));
        }catch(Exception e){
            e.printStackTrace();
        }
        return value;
    }

    private Integer getNodeValueAsInteger(Node node){
        Integer value = null;
        try{
            value = Integer.valueOf(Integer.parseInt(node.getFirstChild().getNodeValue()));
        }catch(Exception e){
            e.printStackTrace();
        }
        return value;
    }

    private Date getNodeValueAsDate(Node node){
        Date date = null;
        try{
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
            date = simpledateformat.parse(node.getFirstChild().getNodeValue());
        }catch(Exception e){
            e.printStackTrace();
        }
        return date;
    }

    public void writeGpx(Gpx gpx, OutputStream outputStream) {
        try {
            DocumentBuilder documentbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentbuilder.newDocument();
            Element element = document.createElement(GpxConstants.GPX_NODE);
            NamedNodeMap attributes = element.getAttributes();
            if(gpx.getVersion() != null) {
                Attr attr = document.createAttribute(GpxConstants.VERSION_ATTR);
                attr.setNodeValue(gpx.getVersion());
                attributes.setNamedItem(attr);
            }
            if(gpx.getCreator() != null) {
                Attr attr = document.createAttribute(GpxConstants.CREATOR_ATTR);
                attr.setNodeValue(gpx.getCreator());
                attributes.setNamedItem(attr);
            }
            if(gpx.getWaypoints().size() > 0) {
                for(Waypoint waypoint : gpx.getWaypoints()) {
                    addWaypointToGpx(waypoint, GpxConstants.WPT_NODE, element, document);
                }
            }
            if(gpx.getRoutes().size() > 0) {
                for(Route route : gpx.getRoutes()) {
                    addRouteToGpx(route, element, document);
                }
            }
            if(gpx.getTracks().size() > 0) {
                for(Track track : gpx.getTracks()) {
                    addTrackToGpx(track, element, document);
                }
            }
            document.appendChild(element);
            TransformerFactory transformerfactory = TransformerFactory.newInstance();
            Transformer transformer = transformerfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domsource = new DOMSource(document);
            StreamResult streamresult = new StreamResult(outputStream);
            transformer.transform(domsource, streamresult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWaypointToGpx(Waypoint waypoint, String elementName, Node node, Document document) {
        Element wpElement = document.createElement(elementName);
        NamedNodeMap attributes = wpElement.getAttributes();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.00000000", dfs);
        if(waypoint.getLatitude() != null) {
            Attr latAttr = document.createAttribute(GpxConstants.LAT_ATTR);
            latAttr.setNodeValue(df.format(waypoint.getLatitude()));
            attributes.setNamedItem(latAttr);
        }
        if(waypoint.getLongitude() != null) {
            Attr lonAttr = document.createAttribute(GpxConstants.LON_ATTR);
            lonAttr.setNodeValue(df.format(waypoint.getLongitude()));
            attributes.setNamedItem(lonAttr);
        }
        if(waypoint.getElevation() != null) {
            Element elevationElement = document.createElement(GpxConstants.ELE_NODE);
            elevationElement.appendChild(document.createTextNode(waypoint.getElevation().toString()));
            wpElement.appendChild(elevationElement);
        }
        if(waypoint.getTime() != null) {
            Element timeElement = document.createElement(GpxConstants.TIME_NODE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'");
            timeElement.appendChild(document.createTextNode(dateFormat.format(waypoint.getTime())));
            wpElement.appendChild(timeElement);
        }
        if (waypoint.getMagneticVariation() != null){
            Element mvElement = document.createElement(GpxConstants.MAGVAR_NODE);
            mvElement.appendChild(document.createTextNode(waypoint.getMagneticVariation().toString()));
            wpElement.appendChild(mvElement);
        }
        if (waypoint.getGeoidHeight() != null){
            Element geoidHeightElement = document.createElement(GpxConstants.GEOIDHEIGHT_NODE);
            geoidHeightElement.appendChild(document.createTextNode(waypoint.getGeoidHeight().toString()));
            wpElement.appendChild(geoidHeightElement);
        }
        if (waypoint.getName() != null){
            Element nameElement = document.createElement(GpxConstants.NAME_NODE);
            nameElement.appendChild(document.createTextNode(waypoint.getName()));
            wpElement.appendChild(nameElement);
        }
        if (waypoint.getComment() != null){
            Element commentElement = document.createElement( GpxConstants.CMT_NODE);
            commentElement.appendChild(document.createTextNode(waypoint.getComment()));
            wpElement.appendChild(commentElement);
        }
        if (waypoint.getDescription() != null){
            Element descElement = document.createElement(GpxConstants.DESC_NODE);
            descElement.appendChild(document.createTextNode(waypoint.getDescription()));
            wpElement.appendChild(descElement);
        }
        if (waypoint.getSym() != null){
            Element symElement = document.createElement(GpxConstants.SYM_NODE);
            symElement.appendChild(document.createTextNode(waypoint.getSym()));
            wpElement.appendChild(symElement);
        }
        if (waypoint.getType() != null){
            Element typeElement = document.createElement(GpxConstants.TYPE_NODE);
            typeElement.appendChild(document.createTextNode(waypoint.getType()));
            wpElement.appendChild(typeElement);
        }
        if (waypoint.getFixType() != null){
            Element fixTypeElement = document.createElement(GpxConstants.NAME_NODE);
            fixTypeElement.appendChild(document.createTextNode(waypoint.getFixType()));
            wpElement.appendChild(fixTypeElement);
        }
        if (waypoint.getSat() != null){
            Element satElement = document.createElement(GpxConstants.SAT_NODE);
            satElement.appendChild(document.createTextNode(waypoint.getSat().toString()));
            wpElement.appendChild(satElement);
        }
        if (waypoint.getHdop() != null){
            Element hdopElement = document.createElement(GpxConstants.HDOP_NODE);
            hdopElement.appendChild(document.createTextNode(waypoint.getHdop().toString()));
            wpElement.appendChild(hdopElement);
        }
        if (waypoint.getVdop() != null){
            Element vdopElement = document.createElement(GpxConstants.VDOP_NODE);
            vdopElement.appendChild(document.createTextNode(waypoint.getVdop().toString()));
            wpElement.appendChild(vdopElement);
        }
        if (waypoint.getPdop() != null){
            Element pdopElement = document.createElement(GpxConstants.PDOP_NODE);
            pdopElement.appendChild(document.createTextNode(waypoint.getPdop().toString()));
            wpElement.appendChild(pdopElement);
        }
        if (waypoint.getAgeOfGpsData() != null){
            Element ageElement = document.createElement(GpxConstants.AGEOFGPSDATA_NODE);
            ageElement.appendChild(document.createTextNode(waypoint.getAgeOfGpsData().toString()));
            wpElement.appendChild(ageElement);
        }
        if (waypoint.getDgpsId() != null){
            Element dgpsElement = document.createElement(GpxConstants.DGPSID_NODE);
            dgpsElement.appendChild(document.createTextNode(waypoint.getDgpsId().toString()));
            wpElement.appendChild(dgpsElement);
        }
        node.appendChild(wpElement);
    }

    private void addRouteToGpx(Route route, Node node, Document document) {
        Element routeElement = document.createElement(GpxConstants.TRK_NODE);
        if(route.getName() != null) {
            Element nameElement = document.createElement(GpxConstants.NAME_NODE);
            nameElement.appendChild(document.createTextNode(route.getName()));
            routeElement.appendChild(nameElement);
        }
        if(route.getComment() != null) {
            Element commentElement = document.createElement(GpxConstants.CMT_NODE);
            commentElement.appendChild(document.createTextNode(route.getComment()));
            routeElement.appendChild(commentElement);
        }
        if(route.getDescription() != null) {
            Element descElement = document.createElement(GpxConstants.DESC_NODE);
            descElement.appendChild(document.createTextNode(route.getDescription()));
            routeElement.appendChild(descElement);
        }
        if(route.getSrc() != null) {
            Element sourceElement = document.createElement(GpxConstants.SRC_NODE);
            sourceElement.appendChild(document.createTextNode(route.getSrc()));
            routeElement.appendChild(sourceElement);
        }
        if(route.getNumber() != null) {
            Element numberElement = document.createElement(GpxConstants.NUMBER_NODE);
            numberElement.appendChild(document.createTextNode(route.getNumber().toString()));
            routeElement.appendChild(numberElement);
        }
        if(route.getType() != null) {
            Element typeElement = document.createElement(GpxConstants.TYPE_NODE);
            typeElement.appendChild(document.createTextNode(route.getType()));
            routeElement.appendChild(typeElement);
        }
        for(Waypoint routePoint : route.getRoutePoints()) {
            addWaypointToGpx(routePoint, GpxConstants.RTEPT_NODE, routeElement, document);
        }
        node.appendChild(routeElement);
    }

    private void addTrackToGpx(Track track, Node node, Document document) {
        Element trackElement = document.createElement(GpxConstants.TRK_NODE);
        if(track.getName() != null) {
            Element nameElement = document.createElement(GpxConstants.NAME_NODE);
            nameElement.appendChild(document.createTextNode(track.getName()));
            trackElement.appendChild(nameElement);
        }
        if(track.getComment() != null) {
            Element commentElement = document.createElement(GpxConstants.CMT_NODE);
            commentElement.appendChild(document.createTextNode(track.getComment()));
            trackElement.appendChild(commentElement);
        }
        if(track.getDescription() != null) {
            Element descElement = document.createElement(GpxConstants.DESC_NODE);
            descElement.appendChild(document.createTextNode(track.getDescription()));
            trackElement.appendChild(descElement);
        }
        if(track.getSrc() != null) {
            Element sourceElement = document.createElement(GpxConstants.SRC_NODE);
            sourceElement.appendChild(document.createTextNode(track.getSrc()));
            trackElement.appendChild(sourceElement);
        }
        if(track.getNumber() != null) {
            Element numberElement = document.createElement(GpxConstants.NUMBER_NODE);
            numberElement.appendChild(document.createTextNode(track.getNumber().toString()));
            trackElement.appendChild(numberElement);
        }
        if(track.getType() != null) {
            Element typeElement = document.createElement(GpxConstants.TYPE_NODE);
            typeElement.appendChild(document.createTextNode(track.getType()));
            trackElement.appendChild(typeElement);
        }
        for(TrackSegment trackSegment : track.getTrackSegments()) {
            addTrackSegmentToGpx(trackSegment, trackElement, document);
        }
        node.appendChild(trackElement);
    }

    private void addTrackSegmentToGpx(TrackSegment trackSegment, Node node, Document document) {
        Element trackSegmentElement = document.createElement(GpxConstants.TRKSEG_NODE);
        for(Waypoint trackpoint : trackSegment.getTrackPoints()) {
            addWaypointToGpx(trackpoint, GpxConstants.TRKPT_NODE, trackSegmentElement, document);
        }
        node.appendChild(trackSegmentElement);
    }
}
