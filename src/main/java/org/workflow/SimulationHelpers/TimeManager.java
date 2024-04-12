package org.workflow.SimulationHelpers;

import java.sql.Time;

public class TimeManager {

	private static long start;
	private static long end;

	private static long pausedAt;

	private static long pauseTimeAccumulated;

	public static void startTimeManager() {
		start = System.currentTimeMillis() / 1000; // seconds
		pauseTimeAccumulated = 0;
	}

	public static void pauseTimeManager() {
		pausedAt = System.currentTimeMillis() / 1000 - start;
	}

	public static void resumeTimeManager() {
		pauseTimeAccumulated +=
		System.currentTimeMillis() / 1000 - start - pausedAt;
	}

	public static void endTimeManager() {
		end = (int) System.currentTimeMillis() / 1000;
	}

	public static int getSimTime() {
		return (int) ((System.currentTimeMillis() / 1000) -
			start -
			pauseTimeAccumulated);
	}
}
