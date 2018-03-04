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
package org.primesoft.asyncworldedit.changesetSerializer;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import org.primesoft.asyncworldedit.api.changesetSerializer.IUndoEntry;
import org.primesoft.asyncworldedit.api.inner.IChunkCacheStream;
import org.primesoft.asyncworldedit.utils.io.UnsafeDataInput;

/**
 *
 * @author SBPrime
 */
public class UndoEntry implements IUndoEntry {

    private final static int LONG_SIZE = 8;
    private final static int INT_SIZE = 4;
    private final static Charset UTF8 = Charset.forName("UTF8");

    private final String m_type;
    private final byte[] m_data;
    private long m_id = -1;

    @Override
    public String getType() {
        return m_type;
    }

    @Override
    public byte[] getData() {
        return m_data;
    }

    UndoEntry(String type, byte[] data, long id) {
        m_type = type == null ? "" : type;
        m_data = data == null ? new byte[0] : data;
        m_id = id;
    }

    /**
     * Save the undo data to stream
     *
     * @param stream
     * @throws IOException
     */
    static int save(RandomAccessFile stream, IUndoEntry entry) throws IOException {
        String type = entry.getType();
        byte[] data =  entry.getData();        
        
        byte[] typeBytes = type.getBytes(UTF8);
        int typeLen = typeBytes.length;
        int dataLen = data.length;
        long id = entry.getId();

        int totalSize
                = LONG_SIZE
                + INT_SIZE + typeLen + //Type len and type string                
                INT_SIZE + dataLen; //Data len and data                

        stream.writeLong(id);
        stream.writeInt(typeLen);
        stream.write(typeBytes);
        stream.writeInt(dataLen);
        stream.write(data);

        return totalSize;
    }

    /**
     * Save the undo data to stream
     *
     * @param stream
     * @throws IOException
     */
    public static int save(DataOutput stream, IUndoEntry entry) throws IOException {
        String type = entry.getType();
        byte[] data =  entry.getData();

        byte[] typeBytes = type.getBytes(UTF8);
        int typeLen = typeBytes.length;
        int dataLen = data.length;
        long id = entry.getId();

        int totalSize
                = LONG_SIZE
                + INT_SIZE + typeLen + //Type len and type string                
                INT_SIZE + dataLen; //Data len and data

        stream.writeLong(id);
        stream.writeInt(typeLen);
        stream.write(typeBytes);
        stream.writeInt(dataLen);
        stream.write(data);

        return totalSize;
    }

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    static IUndoEntry load(RandomAccessFile stream) throws IOException {
        long pos = stream.getFilePointer();
        long length = stream.length();

        if (length - pos < LONG_SIZE) {
            return null;
        }
        long id = stream.readLong();//The ID
        pos += LONG_SIZE;

        if (length - pos < INT_SIZE) {
            return null;
        }
        int typeLen = stream.readInt();
        pos += INT_SIZE;

        if (length - pos < typeLen) {
            return null;
        }
        byte[] typeBytes = new byte[typeLen];
        stream.read(typeBytes);
        pos += typeLen;

        if (length - pos < INT_SIZE) {
            return null;
        }
        int dataLen = stream.readInt();
        pos += INT_SIZE;

        if (length - pos < dataLen) {
            return null;
        }
        byte[] data = new byte[dataLen];
        stream.read(data);

        return new UndoEntry(new String(typeBytes, UTF8), data, id);
    }

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    static UndoEntry load(IChunkCacheStream stream) throws IOException {
        long pos = stream.getFilePointer();
        long length = stream.length();

        if (length - pos < LONG_SIZE) {
            return null;
        }
        long id = stream.readLong();//The ID
        pos += LONG_SIZE;

        if (length - pos < INT_SIZE) {
            return null;
        }
        int typeLen = stream.readInt();
        pos += INT_SIZE;

        if (length - pos < typeLen) {
            return null;
        }
        byte[] typeBytes = new byte[typeLen];
        stream.readFully(typeBytes);
        pos += typeLen;

        if (length - pos < INT_SIZE) {
            return null;
        }
        int dataLen = stream.readInt();
        pos += INT_SIZE;

        if (length - pos < dataLen) {
            return null;
        }
        byte[] data = new byte[dataLen];
        stream.readFully(data);

        return new UndoEntry(new String(typeBytes, UTF8), data, id);
    }

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    public static IUndoEntry load(UnsafeDataInput stream) throws IOException {

        long available = stream.available();

        if (available < LONG_SIZE) {
            return null;
        }
        long id = stream.readLong();
        available -= LONG_SIZE;

        if (available < INT_SIZE) {
            return null;
        }
        int typeLen = stream.readInt();
        available -= INT_SIZE;

        if (available < typeLen) {
            return null;
        }
        byte[] typeBytes = new byte[typeLen];
        stream.readFully(typeBytes);
        available -= typeLen;

        if (available < INT_SIZE) {
            return null;
        }
        int dataLen = stream.readInt();
        available -= INT_SIZE;

        if (available < dataLen) {
            return null;
        }
        byte[] data = new byte[dataLen];
        stream.readFully(data);

        return new UndoEntry(new String(typeBytes, UTF8), data, id);
    }

    /**
     * Load the undo data from stream
     *
     * @param stream
     * @return
     * @throws IOException
     */
    static IUndoEntry load(DataInputStream stream) throws IOException {

        long available = stream.available();

        if (available < LONG_SIZE) {
            return null;
        }
        long id = stream.readLong();
        available -= LONG_SIZE;

        if (available < INT_SIZE) {
            return null;
        }
        int typeLen = stream.readInt();
        available -= INT_SIZE;

        if (available < typeLen) {
            return null;
        }
        byte[] typeBytes = new byte[typeLen];
        stream.readFully(typeBytes);
        available -= typeLen;

        if (available < INT_SIZE) {
            return null;
        }
        int dataLen = stream.readInt();
        available -= INT_SIZE;

        if (available < dataLen) {
            return null;
        }
        byte[] data = new byte[dataLen];
        stream.readFully(data);

        return new UndoEntry(new String(typeBytes, UTF8), data, id);
    }

    @Override
    public long getId() {
        return m_id;
    }

    void setId(long id) {
        m_id = id;
    }
}
