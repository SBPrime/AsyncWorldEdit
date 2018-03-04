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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author SBPrime
 */
public class VarInt {

    private final static int MASK_VALUE = 0x7f;
    private final static int MASK_MORE = 0x80;
    private final static int SHIFT = 7;

    /**
     * Write variable length long to data output
     *
     * @param out
     * @param value
     * @throws IOException
     */
    public static void writeLong(DataOutput out, long value) throws IOException {
        do {
            byte v = (byte) (value & MASK_VALUE);

            value >>= SHIFT;

            if (value != 0) {
                v |= MASK_MORE;
            }

            out.writeByte(v);
        } while (value != 0);
    }

    /**
     * Write variable length int to data output
     *
     * @param out
     * @param value
     * @throws IOException
     */
    public static void writeInt(DataOutput out, int value) throws IOException {
        do {
            byte v = (byte) (value & MASK_VALUE);

            value >>= SHIFT;

            if (value != 0) {
                v |= MASK_MORE;
            }

            out.writeByte(v);
        } while (value != 0);
    }

    /**
     * Write variable length short to data output
     *
     * @param out
     * @param value
     * @throws IOException
     */
    public static void writeShord(DataOutput out, short value) throws IOException {
        do {
            byte v = (byte) (value & MASK_VALUE);

            value >>= SHIFT;

            if (value != 0) {
                v |= MASK_MORE;
            }

            out.writeByte(v);
        } while (value != 0);
    }
    
    
    /**
     * Read variable length long from data input
     * @param in
     * @return
     * @throws IOException 
     */
    public static long readLong(DataInput in) throws IOException {        
        int hasMore = MASK_MORE;
        long result = 0;
        
        int shl = 0;
        while (hasMore > 0) {
            byte v = in.readByte();
            
            result |= (long)(v & MASK_VALUE) << shl;
            shl += SHIFT;
            
            hasMore = v & MASK_MORE;
        }
        
        return result;
    }
    
    
    /**
     * Read variable length int from data input
     * @param in
     * @return
     * @throws IOException 
     */
    public static int readInt(DataInput in) throws IOException {        
        int hasMore = MASK_MORE;
        int result = 0;
        
        int shl = 0;
        while (hasMore > 0) {
            byte v = in.readByte();
            
            result |= (v & MASK_VALUE) << shl;
            shl += SHIFT;
            
            hasMore = v & MASK_MORE;
        }
        
        return result;
    }
    
        /**
     * Read variable length short from data input
     * @param in
     * @return
     * @throws IOException 
     */
    public static short readShort(DataInput in) throws IOException {        
        int hasMore = MASK_MORE;
        short result = 0;
        
        int shl = 0;
        while (hasMore > 0) {
            byte v = in.readByte();
            
            result |= (v & MASK_VALUE) << shl;
            shl += SHIFT;
            
            hasMore = v & MASK_MORE;
        }
        
        return result;
    }
}
