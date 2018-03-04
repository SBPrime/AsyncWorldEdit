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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author SBPrime
 */
public class ListStream {

    private final static Charset UTF8 = Charset.forName("UTF-8");

    public static void write(List<Byte> buffer, int val) {
        buffer.add((byte) ((val & 0xff000000) >> 24));
        buffer.add((byte) ((val & 0x00ff0000) >> 16));
        buffer.add((byte) ((val & 0x0000ff00) >> 8));
        buffer.add((byte) (val & 0x000000ff));
    }

    public static void write(List<Byte> buffer, short val) {
        buffer.add((byte) ((val & 0x0000ff00) >> 8));
        buffer.add((byte) (val & 0x000000ff));
    }

    public static void write(List<Byte> buffer, byte val) {
        buffer.add((byte) (val & 0x000000ff));
    }

    public static void write(List<Byte> buffer, byte[] val) {
        if (val == null) {
            return;
        }

        for (byte b : val) {
            buffer.add(b);
        }
    }

    public static void write(List<Byte> buffer, String s) {
        if (s == null) {
            write(buffer, (int) 0);
            return;
        }

        byte[] data = s.getBytes(UTF8);
        write(buffer, data.length);
        write(buffer, data);
    }

    public static byte[] readBuff(Queue<Byte> buffer, int size) {
        byte[] result = new byte[size];

        for (int i = 0; i < size; i++) {
            if (buffer.isEmpty()) {
                result[i] = 0;
            } else {
                result[i] = buffer.poll();
            }
        }

        return result;
    }

    public static int readInt32(Queue<Byte> buffer) {
        byte[] data = readBuff(buffer, 4);

        return ((int) (data[0]) << 24)
                | ((int) (data[1]) << 16)
                | ((int) (data[2]) << 8)
                | ((int) data[3]);
    }

    public static short readInt16(Queue<Byte> buffer) {
        byte[] data = readBuff(buffer, 2);

        return (short) (((short) (data[0]) << 8)
                | ((short) data[1]));
    }

    public static byte readInt8(Queue<Byte> buffer) {
        if (buffer.isEmpty()) {
            return 0;
        } else {
            return buffer.poll();
        }
    }
    
    public static String readString(Queue<Byte> buffer) {
        int size = readInt32(buffer);
        byte[] data = readBuff(buffer, size);
        
        return new String(data, UTF8);
    }
}
