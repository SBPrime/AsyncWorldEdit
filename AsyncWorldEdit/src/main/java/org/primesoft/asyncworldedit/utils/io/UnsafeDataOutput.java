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

import java.io.DataOutput;
import java.io.IOException;
import org.primesoft.asyncworldedit.utils.UnsafeUtils;

/**
 *
 * @author SBPrime
 */
public class UnsafeDataOutput extends UnsafeUtils implements DataOutput {
    private static final int DEFAULT = 32;
    
    private byte[] m_data;

    private int m_filePointer;      

    public UnsafeDataOutput() {
        m_data = new byte[DEFAULT];
        m_filePointer = 0;
    }
    
    public UnsafeDataOutput(int initialSize) {
        m_data = new byte[initialSize];
        m_filePointer = 0;
    }
    
    @Override
    public void write(int b) throws IOException {
        grow(SIZE_BYTE);        
        s_unsafe.putByte(m_data, OFFSET_BYTE_ARRAY + m_filePointer, (byte)b);        
        m_filePointer += SIZE_BYTE;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        grow(len);
        
        s_unsafe.copyMemory(b, OFFSET_BYTE_ARRAY + off, m_data, OFFSET_BYTE_ARRAY + m_filePointer, len);
        m_filePointer += len;
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 0xff : 0x00);
    }

    @Override
    public void writeByte(int v) throws IOException {
        grow(SIZE_BYTE);
        s_unsafe.putByte(m_data, OFFSET_BYTE_ARRAY + m_filePointer, (byte)v);
        m_filePointer += SIZE_BYTE;
    }

    @Override
    public void writeShort(int v) throws IOException {
        grow(SIZE_SHORT);
        s_unsafe.putShort(m_data, OFFSET_BYTE_ARRAY + m_filePointer, (short)v);
        m_filePointer += SIZE_SHORT;
    }

    @Override
    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        grow(SIZE_INT);
        s_unsafe.putInt(m_data, OFFSET_BYTE_ARRAY + m_filePointer, v);
        m_filePointer += SIZE_INT;
    }

    @Override
    public void writeLong(long v) throws IOException {
        grow(SIZE_LONG);
        s_unsafe.putLong(m_data, OFFSET_BYTE_ARRAY + m_filePointer, v);
        m_filePointer += SIZE_LONG;
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(String s) throws IOException {        
        int len = s.length();
        byte[] buff = new byte[len];
        
        for (int i = 0 ; i < len ; i++) {
            buff[i] = (byte)s.charAt(i);
        }
        
        write(buff);
    }

    @Override
    public void writeChars(String s) throws IOException {
        char[] chars = s.toCharArray();
        
        int len = chars.length * SIZE_SHORT;
        grow(len);
        
        s_unsafe.copyMemory(chars, OFFSET_CHAR_ARRAY, m_data, OFFSET_BYTE_ARRAY + m_filePointer, len);
        
        m_filePointer += len;
    }

    @Override
    public void writeUTF(String s) throws IOException {
        write(s.getBytes("UTF-8"));
    }

    
    
    private void grow(int additionalData) {
        int length = m_data.length;
        if (m_filePointer + additionalData < length) {
            return;
        }

        int newSize = length + Math.max(length >> 2, additionalData);
        byte[] newBuff = new byte[newSize];
        
        s_unsafe.copyMemory(m_data, OFFSET_BYTE_ARRAY, 
                newBuff, OFFSET_BYTE_ARRAY, m_filePointer);
        
        m_data = newBuff;
    }
    
    
    public byte[] toByteArray() {
        byte[] result = new byte[m_filePointer];
        
        s_unsafe.copyMemory(m_data, OFFSET_BYTE_ARRAY, 
                result, OFFSET_BYTE_ARRAY, m_filePointer);
        
        return result;
    }
}
