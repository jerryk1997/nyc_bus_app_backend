package com.jerry.busappbackend;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.jerry.busappbackend.util.MemoryTracker;
import com.jerry.busappbackend.util.Timer;

@SpringBootApplication
public class BusAppBackendApplication implements CommandLineRunner {

	private static final Logger logger = LogManager.getLogger(BusAppBackendApplication.class);
	@Autowired
	private Timer timer;

	public BusAppBackendApplication() {
	}

	public static void main(String[] args) {	
		SpringApplication.run(BusAppBackendApplication.class, args);
	}

	@Override
	public void run(String... args) {
		int memoryID = MemoryTracker.startTracking();
		logger.info("Elapsed Time: " + timer.getElapsedTime());
		logger.info("\n" + MemoryTracker.getAllMemory());
		logger.info(MemoryTracker.stopTracking(memoryID));
	}
}
