package org.zehetner.homeautomation.utils;

public class TestUtils {
	public static boolean closeEnough(final double a, final double b) {
		return (Math.abs(a - b) < 0.001);
	}

}
