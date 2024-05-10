# NYC Bus App API

## Overview
This API is designed to serve up the bus trip data to the NYC Bus App frontend. The system parses the raw CSV files from this [kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics) (`mta_1706.csv` was used to developed this application). Detailed bus trip data is extracted, before making it accessible through RESTful APIs that will deliver the data in JSON / GeoJSON formats.

## Features
- **Data Parsing:** Automatically parse the raw CSV files.
- **Trip Construction:** Aggregates the parsed data into meaningful information about the bus trips.
- **GeoJSON Output:** Converts the extracted bus trip data into GeoJSON format.
- **RESTful API:** Exposes bus trip data through clean, easy-to-use API endpoints.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

```bash
git clone https://github.com/jerryk1997/nyc_bus_app_backend.git
cd BusAppBackend

# Install dependencies
npm install

# Serve with hot reload at localhost:8080
npm start
```
## Built With
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [Maven](https://maven.apache.org/) - Dependency Management
- [OpenCSV](https://opencsv.sourceforge.net/) - CSV parsing
- [GeoTools](https://geotools.org/) - Feature processing
- [Jackson](https://github.com/FasterXML/jackson) - GeoJson building
