package com.rapplogic.xbee.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper implements IIntInputStream {

	private InputStream in;

	public InputStreamWrapper(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		if(in.available() == 0) {
			long start = System.currentTimeMillis();
			while(in.available() == 0) {
				Thread.yield();
				if(System.currentTimeMillis() - start > 100) {
					throw new IOException("Timeout while reading from InputStream");
				}
			}
		}
		return in.read();
	}

	public int read(String s) throws IOException {
		return in.read();
	}
}
