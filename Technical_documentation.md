
# NYC Bus App Backend - Technical Documentation

## Overview
The NYC Bus App Backend is a Spring Boot application that serves as a RESTful API, providing necessary bus trip data to the frontend map application. Users are required to provide a CSV file from this [Kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics) during application start up, which the application will ingest, clean, and process into memory for data serving. This approach eliminates the need for a traditional database, thereby reducing overhead and simplifying management. The dataset includes information on bus locations and metadata such as published line name, origin name, destination name, direction reference, expected arrival time, arrival proximity, vehicle reference, and coordinates.

## Data
The dataset that we use is a CSV file from this [Kaggle dataset](https://www.kaggle.com/datasets/stoney71/new-york-city-transport-statistics), and contains a collection of buses' streamed GPS location and other live data. For our application, we are also only using the first 100, 000 rows of data. 
Here are the data from each row that we will be using: 
-  **Published line name** - This bus services this bus line
-  **Origin name**
-  **Destination name**
-  **Direction reference** - The direction of travel with respect to the line this bus is servicing
-  **Expected arrival time** - Expected time of arrival at the next bus stop
-  **Arrival proximity** - How many stops away is the bus from the next stop
- **Distance from stop**
-  **Vehicle Reference** - The license plate of the bus
-  **Coordinates** of the vehicle's location
During the initial analysis of the dataset, it was discovered that there are a few problematic values that need to be handled for OpenCSV to work properly and parse the data into the required Entity objects for further processing. 
### Problematic values
- Values containing commas
	1. `" ( non-public,for GEO)"`
	2. `" (non-public,for GEO)"`
	- These are substrings that exist within the dataset containing commas, which will result in an extra column being detected when the file is parsed as a CSV file. We remove the commas in all occurrences of these substrings within the dataset, before using OpenCSV to parse it.
- Null values
	- Null values in this dataset are represented with the string `"NA"`
	- These are replaced with an empty string, which OpenCSV automatically handles as null values.
### Aggregation 
Every row will be aggregated into corresponding bus trips.

**Bus Trip**
- A journey made by a bus in service of a published line.
- Consists of:
	1. Published line name 
	2. Vehicle reference 
	3. Direction of travel (a -> b or b -> a) 
	4. Origin name 
	5. Destination name
	6. Coordinates of the bus near each stop
	7. Start time
	8. End time
	9. Miscellaneous information about bus' current location
		- Proximity to next stop
		- Expected arrival time
 
## Implementation Details
### Application Initialisation
Upon initialization, the application follows a multi-step process to become ready to serve data. The data is cleaned and ingested, before being aggregated into their respective bus trips.

![app_initialise_seq_diagram](https://github.com/jerryk1997/nyc_bus_app_backend/assets/54168384/61660b9b-ab81-4965-ba74-36e3c8e691c0)

**Data Cleaning and Ingestion**
The application cleans the dataset by removing [problematic values](#problematic-values) and saving a cleaned CSV file. 

**Note**: The above is skipped if the application detects a cleaned CSV file in the same directory. If the specified raw data file is `mta_1706.csv`,  the application looks for `mta_1706_clean.csv`.

After which OpenCSV is used to parse each row into the `BusRecordEntity` object.

**Aggregating records into bus trips**

**1. Grouping Records**
Records undergo a preliminary round of grouping based using a composite key comprising of: 
1. Published line name 
2. Vehicle reference 
3. Direction of travel (a -> b or b -> a) 
4. Origin name 
5. Destination name
> If the above set of values differ, the record is guaranteed to belong to a different trip. The reverse however is not true, as the bus can service the same line multiple times throughout the day, resulting in the composite key being the same. 

Within each grouping, the records are then sorted by their `Expected Arrival Time`, from earliest to latest.

**2. Separating Trips** 
The records within each group will be further separated into their respective trips by using the `Expected arrival time` of each record. This is done using the `TRIP_WINDOW`.

`TRIP_WINDOW` - The largest interval between `Expected Arrival Times` where the application will consider two records to be from the same trip. If the interval is larger than this, the record will belong to the next trip.

> **Determining the TRIP_WINDOW** 
> 
> Since we are not using any data regarding the bus trips itself (i.e. The actual stops of the trips), we have to infer them using the data that we currently have. The records contain the names of the next stop that the bus will reach, but since we do not know what stops are in each trip, we cannot use this information to separate each record into their individual trips.
>   
> We instead will use the interval between the Expected Arrival Times of consecutive records in each grouping to group these records into trips. These intervals can be categorised into two types:
> - **Type 1:** Intervals between consecutive records from the same trip
> - **Type 2:** Intervals between trips. (i.e. Last record of the previous trip and first record of the next trip)
>  
>  As a basis for our analysis, we also make the following assumption:
> - **Assumption:** Most of the intervals between records in each group, should be type 1 intervals. In other words, when we analyse the intervals within each group, the total percentage of type 1 intervals should be much higher than the percentage of type 2 intervals.
>
> We can now begin data analysis to determine the appropriate trip window to use.
>
> **Data analysis**
> Our goal here is to find the value of the interval that captures a large percentage of all the intervals within each group. Such an interval will then be determined as the `TRIP_WINDOW`. We arbitrarily choose 10 minutes as the first interval to look at, and progressively increase this by 10 and observe the changes in percentage of intervals captured. 
> 
>  Below you will see the results.
> | Intervals <= ___ mins | % | % Increase |
> |--------|------------|---------------------|
> | 10     | 72.92%     | NA                    	|
> | 20     | 88.62%     | 15.70%             	|
> | 30     | 90.32%     | 1.70%               	|
> | 40     | 91.15%     | 0.83%               	|
> | 50     | 92.14%     | 0.99%               	|
> | 60     | 93.56%     | 1.42%               	|
> | 70     | 95.00%     | 1.44%               	|
> | 80     | 96.29%     | 1.29%               	|
> | 90     | 97.25%     | 0.96%               	|
> | 100    | 97.96%     | 0.71%               |
> | 110    | 98.43%     | 0.47%               |
> | 120    | 98.77%     | 0.34%               |
> 
> We can see from the table that 10 minutes was indeed a good starting point, capturing a vast majority of the intervals already.
>  
>Working with our assumption that type 1 intervals form the majority of the intervals, we will determine that the `TRIP_WINDOW` should be the largest value, where the percentage increase, is still relatively large. From the table we can that the value that satisfies this is 20, so that will be our `TRIP_WINDOW`.
> 
> The remaining percentage increases do show a bit of variation, with some relatively larger than the rest (above 1% increase), and we make the assumption here that these are all type 2 intervals being captured, as the percentage increase is much smaller than the initial increase.
>
> **TRIP_WINDOW: 20 mins**

Records in each group will then be iterated through, and each interval between consecutive records where intervals fall within the `TRIP_WINDOW` (i.e. intervals 20 minutes or quicker) will be grouped as the same trip and stored in a `BusTrip` object. 

**Storing Data**

During the previous step, as the records are being grouped into individual trips, they will be stored in a [BusTrip](#aggregation) object. 

Since each bus trip can be referenced either keys:
- Published line name, or
- Vehicle reference

We store a centralised list of trips, and then map the keys to a list of indexes of their respective trips within this list. (Each key should be mapped to one more more trips)  

### Serving Data

The API provides four types of data endpoints, all in JSON format:
   - **List of vehicle references**: Returns an array of strings.
   - **List of published line names**: Returns an array of strings.

   - **All trips for a published line name**: Returns GeoJSON data.
   - **All trips for a vehicle reference**: Returns GeoJSON data.

Serving of the list of vehicle references and published line names is trivial, so we will instead dive a little deeper into creating the GeoJSON data.

When the client calls either of the GeoJSON endpoints, the GeoJSON data is created on the fly. All the bus trip data is fetched and passed to the `GeoJsonBuilder` service to construct the GeoJSON object, which is then validated before being served to the client.

![api_call](https://github.com/jerryk1997/nyc_bus_app_backend/assets/54168384/8c46905c-55de-4399-9e39-e2b7845e4726)

## GeoJSON 
Our GeoJSON object follows the [RFC 7946](https://datatracker.ietf.org/doc/html/rfc7946#section-3.2) standard. For our purposes, we only return GeoJSON objects that are **Feature Collections**, and each **Feature** is either a **LineString** or **Point**. 

Each object also contains custom properties:
- Published line name 
-  Vehicle reference 
- Direction of travel (a -> b or b -> a) 
- Origin name 
- Destination name
- Start time
- End time
- Number of points
- And for each point in the LineString or Point:
	- Coordinates
	- Arrival proximity text
	- Distance from the next stop
	- Expected arrival time to the next stop

## Performance Analysis

As mentioned earlier, we do not use a traditional database to store the data that is being served. While this simplifies the implementation initially by eliminating the need to create and manage a separate database, it can potentially lead to performance issues.

### Potential issues
**Application start up time**
- At the very minimum, on each start up,  the application is required to read the cleaned raw data and transform it into the correct form to be served to the client. Additionally, on the very first start up, the raw data has to be cleaned before being processed. This adds additional overhead for the application start up and can potentially lead to long start up times, which may not be acceptable depending on how the application is being used.

**Memory usage**
- With a sufficiently large amount of data being served, we may also see the application exceeding it's available memory.

To monitor these issues, there are two utility classes, `Timer` and `MemoryTracker`. 

### Timer
The `Timer` class allows us to set tracking points within the application to monitor the elapsed time for any application functionality. The functionality's implementation is relatively straightforward. 
1. Call the start timer function, which returns a timer ID
2. The class stores the current time, and maps it to the ID
3. Call the stop timer function which takes in the timer ID
4. The class compares the current and stored time to output the elapsed time.
- Additionally, you can also monitor the total elapsed time from the creation of the Timer bean.

Using this, we can effectively log the run time of different application functionalities and help us identify bottlenecks where they exist. For our purposes, we use it to track elapsed time taken for the spring boot application to be fully initialised to determine if it is viable to not use a database. On average, even from the first start up, with the additional overhead of cleaning the data, the application only required less than 5 seconds to start up, which is acceptable to us. This however might change if the amount of data served were to be increased. 
> Recall: We only use the first 100, 000 rows of data.

### Memory Tracker

Similarly the `MemoryTracker` class lets us monitor and report memory usage within our application at various points during its execution.

It leverages Java's `Runtime` class to capture and report memory usage. The class also provides similar functionality to the `Timer` class, allowing for snapshots of current application memory usage, and also allows for point to point code execution tracking. 


It is important to note that due to the Java JVM's own memory management, the reported memory usage may not always accurately reflect the application's memory usage, especially when tracking from point to point in the code execution. This is because:

-   **Garbage Collection**: The JVM periodically performs garbage collection, which can free up memory and affect the memory usage reported.
-   **Memory Allocation**: The JVM manages memory allocation dynamically, which can cause fluctuations in reported memory usage.

Despite these limitations, we use this class to monitor memory usage, particularly after all raw data is processed and stored in application memory. This helps us ensure that the memory usage is within acceptable limits before serving the data.

## API Endpoints

The NYC Bus App Backend includes four API endpoints:
1. **/getVehRef**: Returns a list of vehicle references as an array of strings in JSON.
2. **/getPubLineName**: Returns a list of all published line names as an array of strings in JSON.
3. **/getBusTripByVehRef/{vehicleRef}**: Returns a GeoJSON string representing all bus trips for the specified vehicle reference.
4. **/getBusTripByPubLineName/{publishedLineName}**: Returns a GeoJSON string representing all bus trips for the specified published line name.

## Additional Information

Provided below is the full class diagram.

![class_diagram](https://github.com/jerryk1997/nyc_bus_app_backend/assets/54168384/a11723f2-9eea-48c8-95b6-73e166222565)


