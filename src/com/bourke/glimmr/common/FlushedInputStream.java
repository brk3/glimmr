package com.bourke.glimmr;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/*
 * An InputStream that skips the exact number of bytes provided, unless it
 * reaches EOF.
 */
public class FlushedInputStream extends FilterInputStream {

	public FlushedInputStream(InputStream inputStream) {
		super(inputStream);
	}

	@Override
	public long skip(long n) throws IOException {
		long totalBytesSkipped = 0L;
		while (totalBytesSkipped < n) {
			long bytesSkipped = in.skip(n - totalBytesSkipped);
			if (bytesSkipped == 0L) {
				int b = read();
				if (b < 0) {
					break; // we reached EOF
				} else {
					bytesSkipped = 1; // we read one byte
				}
			}
			totalBytesSkipped += bytesSkipped;
		}
		return totalBytesSkipped;
	}
}
