package com.angelbroking.smartapi.utils;

public class Utils {
	private Utils() {
		
	}

	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	public static boolean areCharArraysEqual(char[] a, char[] b) {
		if (a == null && b == null) {
			return true;
		}

		if (a != null && b != null) {
			if (a.length == b.length) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] != b[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean areByteArraysEqual(byte[] a, byte[] b) {
		if (a == null && b == null) {
			return true;
		}

		if (a != null && b != null) {
			if (a.length == b.length) {
				for (int i = 0; i < a.length; i++) {
					if (a[i] != b[i]) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public static <T> boolean validateInputNullCheck(T input) {
        return input == null;
    }

    public static <T> boolean validateInputNotNullCheck(T input) {
        return input != null;
    }

}
