package com.jerry.busappbackend.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jerry.busappbackend.exception.InvalidGeoJsonException;
import com.jerry.busappbackend.model.BusTrip;
import com.jerry.busappbackend.service.BusTripDataProvider;
import com.jerry.busappbackend.service.GeoJsonBuilder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling all bus trip-related data requests.
 * <p>
 * This controller provides endpoints for fetching bus trip data,
 * such as lists of published line names, vehicle references, and detailed trip information
 * in GeoJSON format.
 */
@RestController
public class BusTripDataController {
    private static final Logger logger = LogManager.getLogger(BusTripDataController.class);

    @Autowired
    BusTripDataProvider dataProvider;
    
    @Autowired
    GeoJsonBuilder geoJsonBuilder;

    /**
     * Dummy endpoint to match with remote API that frontend originally uses.
     * 
     * @param request The HTTP request object.
     * @return Returns a JSON string indicating server ready.
     */
    @GetMapping(value = "/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getServerStatus(HttpServletRequest request) {
        ResponseEntity<String> response = ResponseEntity.ok().body("{ \"status\": \"ready\" }");
        return response;
    }
    

    /**
     * Retrieves all published line names available in the data provider.
     * 
     * @param request The HTTP request object.
     * @return A list of all published line names.
     */
    @GetMapping(value = "/getPubLineName", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAllPublishedLine(HttpServletRequest request) {
        List<String> responseData = dataProvider.getAllPublishedLineName();
        return responseData;
    }

    /**
     * Retrieves all vehicle references that are associated with any bus line.
     * 
     * @param request The HTTP request object.
     * @return A list of all vehicle references.
     */
    @GetMapping(value = "/getVehRef", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAllVehicleRef(HttpServletRequest request) {
        List<String> responseData = dataProvider.getAllVehicleRef();
        return responseData;
    }

    /**
     * Fetches bus trip data by vehicle reference and returns it in GeoJSON format.
     * 
     * @param vehicleRef The vehicle reference to query.
     * @param request The HTTP request object.
     * @return A {@link ResponseEntity} containing the GeoJSON data or an error message.
     */
    @GetMapping(value = "getBusTripByVehRef/{vehicleRef}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBusTripByVehRef(@PathVariable String vehicleRef, HttpServletRequest request) {
        List<BusTrip> trips = dataProvider.getTripByVehicleRef(vehicleRef);
        try {
            String geoJson = geoJsonBuilder.build(trips);
            return ResponseEntity.ok().body(geoJson);
        } catch (InvalidGeoJsonException e) {
            logger.error(
                "Invalid GeoJson was created for: " + vehicleRef + 
                "\n" + e
            );
            return ResponseEntity.internalServerError().body("There was an error");
        } catch (JsonProcessingException e) {
            logger.error(e);
            return ResponseEntity.internalServerError().body("There was an error");
        }
    }
    
    /**
     * Fetches bus trip data by vehicle reference and returns it in GeoJSON format.
     * 
     * @param vehicleRef The vehicle reference to query.
     * @param request The HTTP request object.
     * @return A {@link ResponseEntity} containing the GeoJSON data or an error message.
     */
    @GetMapping(value = "/getBusTripByPubLineName/{publishedLineName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBusTripByPubLineName(@PathVariable String publishedLineName, HttpServletRequest request) {
        List<BusTrip> trips = dataProvider.getTripByPublishedLineName(publishedLineName);
        try {
            String geoJson = geoJsonBuilder.build(trips);
            return ResponseEntity.ok().body(geoJson);
        } catch (InvalidGeoJsonException e) {
            logger.error(
                "Invalid GeoJson was created for: " + publishedLineName + 
                "\n" + e
            );
            return ResponseEntity.internalServerError().body("There was an error");
        } catch (JsonProcessingException e) {
            logger.error(e);
            return ResponseEntity.internalServerError().body("There was an error");
        }
    }
    
}
