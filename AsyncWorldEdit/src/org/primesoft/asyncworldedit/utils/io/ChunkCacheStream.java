/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.utils.io;

import org.primesoft.asyncworldedit.api.inner.IChunkCacheStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import org.primesoft.asyncworldedit.utils.LruLinkedHashMap;

/**
 *
 * @author SBPrime
 */
public class ChunkCacheStream implements IChunkCacheStream {

    /**
     * Constant used to convert
     */
    private final int BYTE_CONVERT = 256;

    /**
     * The underlying stream
     */
    private final RandomAccessFile m_stream;

    /**
     * The current file position
     */
    private long m_streamPos;

    /**
     * The current file pointer
     */
    private long m_filePointer;

    /**
     * The chunk cache
     */
    private final HashMap<Long, byte[]> m_chunkCache;

    /**
     * The chunk size
     */
    private final int m_chunkSize;

    /**
     * The file length
     */
    private long m_length = -1;

    public ChunkCacheStream(RandomAccessFile stream, int chunkSize) {
        this(stream, 100, chunkSize);
    }

    public ChunkCacheStream(RandomAccessFile stream) {
        this(stream, 100, 0x10000);
    }

    public ChunkCacheStream(RandomAccessFile stream, int maxChunks,
            int chunkSize) {
        m_stream = stream;
        m_chunkSize = chunkSize;
        m_streamPos = 0;

        m_chunkCache = new LruLinkedHashMap(maxChunks);
    }

    /**
     * Returns the current offset in this file.
     *
     * @return the offset from the beginning of the file, in bytes, at which the
     * next read or write occurs.
     * @exception IOException if an I/O error occurs.
     */
    public long getFilePointer() throws IOException {
        return m_filePointer;
    }

    /**
     * Returns the length of this file.
     *
     * @return the length of this file, measured in bytes.
     * @exception IOException if an I/O error occurs.
     */
    public long length() throws IOException {
        if (m_length == -1) {
            m_length = m_stream.length();
        }
        return m_length;
    }

    /**
     * Sets the current position of this stream to the given value.
     *
     * @param pos The point relative to origin from which to begin seeking.
     * @param origin Specifies the beginning, the end, or the current position
     * as a reference point for offset, using a value of type SeekOrigin.
     * @throws IOException
     * @return The new file position
     */
    public long seek(long pos, SeekOrigin origin) throws IOException {
        long newOffset;
        long length = length();

        switch (origin) {
            case Begin:
                newOffset = pos;
                break;
            case Current:
                newOffset = m_filePointer + pos;
                break;
            case End:
                newOffset = length - pos;
                break;
            default:
                return -1;

        }

        if (newOffset < 0) {
            throw new IOException("Negative seek offset");
        }
        if (newOffset > length) {
            throw new IOException("Seek offset larget then file size");
        }

        m_filePointer = newOffset;
        return newOffset;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p>
     * A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @exception IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        final long length = length();
        final long position = m_filePointer;

        if (position >= length) {
            return -1;
        }

        m_filePointer++;

        final int idx = (int) (position % m_chunkSize);
        final byte[] data = getChunk(position / m_chunkSize, position - idx);

        if (idx >= data.length) {
            throw new IOException();
        }

        int i = data[idx];
        if (i < 0) {
            i += BYTE_CONVERT;
        }

        return i;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        long pos = m_filePointer;
        int offset = (int) (pos % m_chunkSize);

        while (len > 0) {
            final byte[] data = getChunk(pos / m_chunkSize, pos - offset);

            int toCopy = len > (m_chunkSize - offset) ? (m_chunkSize - offset) : len;
            if (toCopy > data.length) {
                throw new IOException();
            }

            System.arraycopy(data, offset, b, n, toCopy);
            n += toCopy;
            len -= toCopy;
            pos += toCopy;
            offset = 0;
        }

        m_filePointer = pos;
    }

    @Override
    public int skipBytes(int n) throws IOException {
        long pos = m_filePointer;
        seek(n, SeekOrigin.Current);

        return (int) (m_filePointer - pos);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return (readByte() != 0);
    }

    @Override
    public byte readByte() throws IOException {
        final long length = length();
        final long position = m_filePointer;

        if (position >= length) {
            throw new EOFException();
        }

        m_filePointer++;

        final int idx = (int) (position % m_chunkSize);
        final byte[] data = getChunk(position / m_chunkSize, position - idx);
                
        if (idx >= data.length) {
            throw new IOException();
        }

        return data[idx];
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int i = readByte();

        if (i < 0) {
            i += BYTE_CONVERT;
        }

        return i;
    }

    @Override
    public short readShort() throws IOException {
        final int b1 = readUnsignedByte();
        final int b2 = readUnsignedByte();

        return (short) ((b1 << 8) | b2);
    }

    @Override
    public int readUnsignedShort() throws IOException {
        final int b1 = readUnsignedByte();
        final int b2 = readUnsignedByte();

        return (short) ((b1 << 8) | b2);
    }

    @Override
    public char readChar() throws IOException {
        final int b1 = readUnsignedByte();
        final int b2 = readUnsignedByte();

        return (char) ((b1 << 8) | b2);
    }

    @Override
    public int readInt() throws IOException {
        int b1 = readUnsignedByte();
        int b2 = readUnsignedByte();
        int b3 = readUnsignedByte();
        int b4 = readUnsignedByte();

        return ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);
    }

    @Override
    public long readLong() throws IOException {
        return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFl);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int ch = -1;
        boolean eol = false;

        while (!eol) {
            ch = read();
            switch (ch) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    if ((read()) != '\n') {
                        seek(-1, SeekOrigin.Current);
                    }
                    break;
                default:
                    input.append((char) ch);
                    break;
            }
        }

        if ((ch == -1) && (input.length() == 0)) {
            return null;
        }
        return input.toString();
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    /**
     * Get a chunk for a offset
     *
     * @param offset
     * @return
     */
    private byte[] getChunk(Long chunkId, long offset) throws IOException {        
        byte[] result = m_chunkCache.get(chunkId);
        if (result != null) {
            return result;
        }

        long length = length();
        long dataAvailable = length - offset;

        if (dataAvailable <= 0) {
            throw new IOException();
        }

        if (m_streamPos != offset) {
            m_stream.seek(offset);
            m_streamPos = offset;
        }

        int toRead = dataAvailable > m_chunkSize ? m_chunkSize : (int) dataAvailable;
        byte[] data = new byte[toRead];
        m_stream.readFully(data);
        m_streamPos += toRead;

        m_chunkCache.put(chunkId, data);

        return data;

    }
}
