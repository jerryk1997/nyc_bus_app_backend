package com.jerry.busappbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jerry.busappbackend.exception.InvalidGeoJsonException;
import com.jerry.busappbackend.model.BusTrip;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * Service class for building GeoJSON representations of bus trip data.
 * <p>
 * This class provides functionality to convert lists of {@link BusTrip} objects into a standardized GeoJSON format.
 * It includes support for validating the generated GeoJSON against a predefined schema to ensure it meets the
 * GeoJSON specifications.
 */
@Service
public class GeoJsonBuilder {
    SimpleFeatureType featureType;
    ObjectMapper mapper;
    SimpleFeatureBuilder featureBuilder;
    GeometryFactory geometryFactory;
    JsonSchema geoJsonSchema;

    public GeoJsonBuilder() {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();

        typeBuilder.setName("FeatureCollection");

        typeBuilder.add("geometry", Geometry.class);
        typeBuilder.add("properties", String[][].class);

        featureType = typeBuilder.buildFeatureType();

        this.featureBuilder = new SimpleFeatureBuilder(featureType);
        this.geometryFactory = new GeometryFactory();
        
        this.mapper = new ObjectMapper();

        // Creating schema for validation
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V7, builder -> 
            builder.schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://geojson.org/schema/", "classpath:schema/"))
        );
            
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        this.geoJsonSchema = jsonSchemaFactory.getSchema(SchemaLocation.of("https://geojson.org/schema/FeatureCollection.json"), config);
    }

    /**
     * Builds a GeoJSON string from a list of {@link BusTrip} instances.
     *
     * @param trips List of {@link BusTrip} instances to be converted into GeoJSON format.
     * @return A string representing the GeoJSON data of the bus trips.
     * @throws InvalidGeoJsonException If the generated GeoJSON does not conform to the predefined schema.
     * @throws JsonProcessingException If there is an error in processing JSON data during GeoJSON generation.
     */
    public String build(List<BusTrip> trips) throws InvalidGeoJsonException, JsonProcessingException {
        List<SimpleFeature> features = trips.stream()
            .map(this::buildFeature)
            .toList();
                
        ArrayNode featuresArray = mapper.createArrayNode();
        features.stream().forEach(feature -> {

            // Create geometry node
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            ObjectNode geometryNode = this.createGeometryNode(geometry.getGeometryType(), geometry.getCoordinates());

            // Create properties node
            String[][] properties = (String[][]) feature.getAttribute("properties");
            ObjectNode propertiesNode = this.createPropertiesNode(properties);
            
            // Combine into feature node
            ObjectNode featureNode = mapper.createObjectNode();
            featureNode.put("type", "Feature");
            featureNode.set("geometry", geometryNode);
            featureNode.set("properties", propertiesNode);

            // Add to features node
            featuresArray.add(featureNode);
        });

        // Create feature collection node 
        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.set("features", featuresArray);

        // Convert to string and validate
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String geoJsonString =  writer.writeValueAsString(featureCollection);
        validateGeoJson(geoJsonString);

        return geoJsonString;
    }

    /**
     * Helper method to build a {@link SimpleFeature} from a {@link BusTrip}.
     * This method creates geometrical data points or lines based on the coordinates provided in the trip data.
     *
     * @param trip The {@link BusTrip} instance from which to construct the feature.
     * @return A {@link SimpleFeature} representing the geographical attributes of the trip.
     */
    private SimpleFeature buildFeature(BusTrip trip) {
        if (trip.getCoords().length > 1) {
            // Add line string to builder
            Coordinate[] coords = Arrays.stream(trip.getCoords())
                .map(coord -> new Coordinate(coord[0], coord[1]))
                .toArray(Coordinate[]::new);
            
            LineString lineString = geometryFactory.createLineString(coords);
            
            featureBuilder.add(lineString);
        } else {
            // Add point to builder
            double[] coord = trip.getCoords()[0];

            Point point = geometryFactory.createPoint(new Coordinate(coord[0], coord[1]));
            
            featureBuilder.add(point);
        }

        // Add properties to builder
        featureBuilder.add(trip.getAllPointInfo());

        // Build feature and return
        return featureBuilder.buildFeature(null);
    }

    /**
     * Creates a geometry node for the GeoJSON structure. This node describes the type of geometry (e.g., Point, LineString)
     * and includes coordinates formatted appropriately.
     *
     * @param geometryType The type of geometry to be created (e.g., "Point", "LineString").
     * @param coords The coordinates that make up the geometry.
     * @return An {@link ObjectNode} representing the geometry part of a GeoJSON feature.
     */
    private ObjectNode createGeometryNode(String geometryType, Coordinate[] coords) {
        ObjectNode geometryNode = mapper.createObjectNode();
        
        // Set geometry type 
        geometryNode.put("type", geometryType);

        // Set coordinates
        ArrayNode coordinatesNode = mapper.createArrayNode();
        
        if (geometryType.equals("Point")) {
            coordinatesNode.add(coords[0].x);
            coordinatesNode.add(coords[0].y); 
        } else {
            Arrays.stream(coords).forEach(coord -> {
                ArrayNode coordinateNode = mapper.createArrayNode();
                coordinateNode.add(coord.x);
                coordinateNode.add(coord.y);
                coordinatesNode.add(coordinateNode);
            });
        }
        geometryNode.set("coordinates", coordinatesNode);

        return geometryNode;
    }

    /**
     * Constructs a properties node for the GeoJSON structure. This node includes various properties associated
     * with the bus trip. Refer to {@link BusTrip} class definition for detailed list of properties.
     *
     * @param properties An array of string pairs representing the property names and values to be included in the GeoJSON.
     * @return An {@link ObjectNode} representing the properties part of a GeoJSON feature.
     */
    private ObjectNode createPropertiesNode(String[][] properties) {
        ObjectNode propertiesNode = mapper.createObjectNode();
        Arrays.stream(properties).forEach(property -> { 
            propertiesNode.put(property[0], property[1]);
        });

        return propertiesNode;
    }

    /**
     * Validates the generated GeoJSON string against the predefined schema to ensure it adheres to the GeoJSON specifications.
     * Throws an exception if the validation fails.
     *
     * @param geoJson The GeoJSON string to be validated.
     * @throws InvalidGeoJsonException If the GeoJSON does not meet the schema requirements.
     * @throws JsonProcessingException If there is an error in parsing the GeoJSON string.
     */
    private void validateGeoJson(String geoJson) throws InvalidGeoJsonException, JsonProcessingException {
        JsonNode geoJsonNode = JsonMapper.builder().build().readTree(geoJson);
        Set<ValidationMessage> assertions = this.geoJsonSchema.validate(geoJsonNode);
        if (assertions.size() != 0) {
            throw new InvalidGeoJsonException(assertions, geoJsonNode);
        }
    }
}

