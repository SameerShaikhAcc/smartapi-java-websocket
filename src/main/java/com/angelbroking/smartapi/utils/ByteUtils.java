package com.angelbroking.smartapi.utils;

import com.angelbroking.smartapi.smartstream.models.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import lombok.Data;

@Data
public class ByteUtils {

	// Private constructor to prevent instantiation from outside the class
	private ByteUtils() {
		// This constructor is intentionally left blank
	}
	
	private static final int CHAR_ARRAY_SIZE = 25;

	public static LTP mapByteBufferToLTP(ByteBuffer buffer) {
		return new LTP(buffer);
	}

	public static Quote mapByteBufferToQuote(ByteBuffer buffer) {
		return new Quote(buffer);
	}


	public static SnapQuote mapByteBufferToSnapQuote(ByteBuffer buffer) {
		return new SnapQuote(buffer);
	}


	public static TokenID getTokenID(ByteBuffer byteBuffer) {
		byte[] token = new byte[CHAR_ARRAY_SIZE];

		for(int i=0; i<CHAR_ARRAY_SIZE; i++) {
			token[i] = byteBuffer.get(2+i);
		}
		return new TokenID(ExchangeType.findByValue(byteBuffer.get(1)), new String(token, StandardCharsets.UTF_8));
	}
}
