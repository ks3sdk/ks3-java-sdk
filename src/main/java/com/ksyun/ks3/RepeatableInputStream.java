package com.ksyun.ks3;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author lijunwei[lijunwei@kingsoft.com]  
 * 
 * @date 2014年10月22日 下午5:40:34
 * 
 * @description
 **/
public class RepeatableInputStream extends InputStream {

	private InputStream is;
	private int bufferSize;
	private int bufferOffset;
	private long bytesReadPastMark;
	private byte[] buffer;
	private boolean hasWarnedBufferOverflow;

	public RepeatableInputStream(InputStream inputStream, int bufferSize) {
		if (inputStream == null) {
			throw new IllegalArgumentException("InputStream cannot be null");
		}

		this.is = inputStream;
		this.bufferSize = bufferSize;
		this.buffer = new byte[this.bufferSize];
	}

	public void reset() throws IOException {
		if (bytesReadPastMark <= bufferSize) {
			bufferOffset = 0;
		} else {
			throw new IOException(
					"Input stream cannot be reset as "
							+ this.bytesReadPastMark
							+ " bytes have been written, exceeding the available buffer size of "
							+ this.bufferSize);
		}
	}

	/**
	 * @see InputStream#markSupported()
	 */
	public boolean markSupported() {
		return true;
	}

	/**
	 * This method can only be used while less data has been read from the input
	 * stream than fits into the buffer. The readLimit parameter is ignored
	 * entirely.
	 */
	public synchronized void mark(int readlimit) {
		if (bytesReadPastMark <= bufferSize && buffer != null) {
			/*
			 * Clear buffer of already-read data to make more space. It's safe
			 * to cast bytesReadPastMark to an int because it is known to be
			 * less than bufferSize, which is an int.
			 */
			byte[] newBuffer = new byte[this.bufferSize];
			System.arraycopy(buffer, bufferOffset, newBuffer, 0,
					(int) (bytesReadPastMark - bufferOffset));
			this.buffer = newBuffer;
			this.bytesReadPastMark -= bufferOffset;
			this.bufferOffset = 0;
		} else {
			// If mark is called after the buffer was already exceeded, create a
			// new buffer.
			this.bufferOffset = 0;
			this.bytesReadPastMark = 0;
			this.buffer = new byte[this.bufferSize];
		}
	}

	/**
	 * @see InputStream#available()
	 */
	public int available() throws IOException {
		return is.available();
	}

	/**
	 * @see InputStream#close()
	 */
	public void close() throws IOException {
		is.close();
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	public int read(byte[] out, int outOffset, int outLength)
			throws IOException {
		// Check whether we already have buffered data.
		if (bufferOffset < bytesReadPastMark && buffer != null) {
			// Data is being repeated, so read from buffer instead of wrapped
			// input stream.
			int bytesFromBuffer = outLength;
			if (bufferOffset + bytesFromBuffer > bytesReadPastMark) {
				bytesFromBuffer = (int) bytesReadPastMark - bufferOffset;
			}

			// Write to output.
			System.arraycopy(buffer, bufferOffset, out, outOffset,
					bytesFromBuffer);
			bufferOffset += bytesFromBuffer;
			return bytesFromBuffer;
		}

		// Read data from input stream.
		int count = is.read(out, outOffset, outLength);

		if (count <= 0) {
			return count;
		}

		// Fill the buffer with data, as long as we won't exceed its capacity.
		if (bytesReadPastMark + count <= bufferSize) {
			System.arraycopy(out, outOffset, buffer, (int) bytesReadPastMark,
					count);
			bufferOffset += count;
		} else {
			// We have exceeded the buffer capacity, after which point it is of
			// no use. Free the memory.
			if (!hasWarnedBufferOverflow) {
				hasWarnedBufferOverflow = true;
			}

			buffer = null;
		}

		bytesReadPastMark += count;

		return count;
	}

	/**
	 * @see InputStream#read()
	 */
	public int read() throws IOException {
		byte[] tmp = new byte[1];
		int count = read(tmp);
		if (count != -1) {
			int unsignedByte = (int) tmp[0] & 0xFF;
			return unsignedByte;
		} else {
			return count;
		}
	}
}