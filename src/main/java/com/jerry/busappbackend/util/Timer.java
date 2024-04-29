package com.jerry.busappbackend.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class Timer {
    private long appStartTimeMillis;
    private long timerMillis;
    private AtomicInteger rNum;
    private ConcurrentHashMap<Integer, Long> timerDurations;

    public Timer() {
        appStartTimeMillis = System.currentTimeMillis();
        rNum = new AtomicInteger(0);
        timerDurations = new ConcurrentHashMap<>();
    }

    public int startTimer() {
        int rNumInt = rNum.getAndIncrement();
        timerDurations.put(rNumInt, System.currentTimeMillis());
        return rNumInt;
    }

    public String getPrettyTime(int timerID) {
        return getDurationString(timerDurations.remove(timerID));
    }

    public long getTimeInMs(int timerID) {
        long startTimeMillis = timerDurations.remove(timerID);
        return System.currentTimeMillis() - startTimeMillis;
    }


    private String getDurationString(long startTimeMillis) {
        long minutesDivisor = 60 * 1000;
        long secondsDivisor = 1000;
        
        long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        
        long elapsedTimeMinutes = elapsedTimeMillis / minutesDivisor;
        elapsedTimeMillis %= minutesDivisor;

        long elapsedTimeSeconds = elapsedTimeMillis / secondsDivisor;
        elapsedTimeMillis %= secondsDivisor;

        StringBuilder resultBuilder = new StringBuilder();
        if (elapsedTimeMinutes != 0) {
            resultBuilder.append(elapsedTimeMinutes + "M ");
        } 

        if (elapsedTimeSeconds != 0) {
            resultBuilder.append(elapsedTimeSeconds + "S ");
        }

        if (elapsedTimeMillis != 0) {
            resultBuilder.append(elapsedTimeMillis + "ms");
        }

        return resultBuilder.toString();
    }

    public synchronized String getElapsedTime() {
        return "Elapsed time: " + getDurationString(appStartTimeMillis);
    }

    public synchronized String getTimerDurationString() {
        return getDurationString(timerMillis);
    }
}
