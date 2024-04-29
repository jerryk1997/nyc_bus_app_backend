package com.jerry.busappbackend.util;

import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class MemoryTracker {
    private static ConcurrentHashMap<Integer, Long> memoryTrackers = new ConcurrentHashMap<>();
    private static Runtime runtime = Runtime.getRuntime();
    private static AtomicInteger rNum = new AtomicInteger(0);

    private static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#.####"));

    public static int startTracking() {
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        int rNumInt = rNum.getAndIncrement();
        memoryTrackers.put(rNumInt, usedMemory);
        return rNumInt;
    }
    
    public static String getMaxMemory() {
        double maxMemory = toGB(runtime.maxMemory());
        return "Maximum memory available: " + DECIMAL_FORMAT.get().format(maxMemory) + "GB";
    }

    public static String getMemoryUsage() {
        double totalMemory = toGB(runtime.totalMemory());
        double freeMemory = toGB(runtime.freeMemory());
        double usedMemory = totalMemory - freeMemory;
        double usedMemoryPercentage = usedMemory / totalMemory * 100;

        return "Memory Usage:\n" + 
            "\t" + DECIMAL_FORMAT.get().format(usedMemory) + "GB / " + DECIMAL_FORMAT.get().format(totalMemory) + "GB\n" +
            "\t" + DECIMAL_FORMAT.get().format(usedMemoryPercentage) + "% Used\n" + 
            "\t" + DECIMAL_FORMAT.get().format(freeMemory) + "GB available";
    }

    public static String getAllMemory() {
        String maxMemory = getMaxMemory();
        String memoryUsage = getMemoryUsage();

        return maxMemory + "\n" + memoryUsage;
    }

    public static String stopTracking(int trackerID) {
        long prevUsedMemory = memoryTrackers.remove(trackerID);
        long currUsedMemory = runtime.totalMemory() - runtime.freeMemory();

        return "Memory used: " + DECIMAL_FORMAT.get().format(toGB(currUsedMemory - prevUsedMemory)) + "GB";
    }

    private static double toGB(long bytes) {
        return bytes / (1024.0 * 1024 * 1024);
    }
}
