# NYC Bus App API

## Overview
This API is designed to serve up the bus trip data to the NYC Bus App frontend. The system parses the raw CSV files from this [kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics) (`mta_1706.csv` was used to developed this application). Detailed bus trip data is extracted, before making it accessible through RESTful APIs that will deliver the data in JSON / GeoJSON formats.

## Features
- **Data Parsing:** Automatically parse the raw CSV files.
- **Trip Construction:** Aggregates the parsed data into meaningful information about the bus trips.
- **GeoJSON Output:** Converts the extracted bus trip data into GeoJSON format.
- **RESTful API:** Exposes bus trip data through clean, easy-to-use API endpoints.

## Getting Started

These instructions will help you get a copy of the project up and running on your local machine for development and testing purposes.

### Running the JAR from Release

1. **Download the JAR file**: Obtain the latest release of the JAR file from the project's [releases page](https://github.com/jerryk1997/nyc_bus_app_backend/releases).

2. **Download the Dataset**: Download the dataset from the [Kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics).

3. **Run the JAR file**: Open a terminal, navigate to the directory containing the JAR file, and run the following command. Ensure `--data-path` points to the valid CSV file you downloaded.

    ```bash
    java -jar ./bus-app-backend-0.0.1-SNAPSHOT.jar --data-path="mta_1706.csv"
    ```

### Building from Source and Running

1. **Clone the Repository**: Clone the project repository from GitHub.

    ```bash
    git clone https://github.com/your-repo/nyc_bus_app_backend.git
    cd nyc_bus_app_backend
    ```

2. **Download the Dataset**: Download the dataset from the [Kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics). Copy it to the project's root directory.

3. **Build the Project**: Use Maven to build the project. Ensure you have Maven installed on your machine.

    ```bash
    mvn clean package
    ```

4. **Run the Application**: Navigate to the `target` directory and run the JAR file. Ensure `--data-path` points to the valid CSV file you downloaded.

    ```bash
    java -jar ./target/bus-app-backend-0.0.1-SNAPSHOT.jar --data-path="nyc_bus_app_backendmta_1706.csv"
    ```

By following these instructions, you can run the application either by using the pre-built JAR file from the releases page or by building the project from the source code. Ensure that the `--data-path` parameter points to a valid CSV file to start the application successfully.

> NOTE: After the first application start, a cleaned data file will be created in the same directory as the original raw data file. Subsequent runs will be faster, if this file is present.

## Built With
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [Maven](https://maven.apache.org/) - Dependency Management
- [OpenCSV](https://opencsv.sourceforge.net/) - CSV parsing
- [GeoTools](https://geotools.org/) - Feature processing
- [Jackson](https://github.com/FasterXML/jackson) - GeoJson building
