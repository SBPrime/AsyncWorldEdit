/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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
package org.primesoft.asyncworldedit.api.inner;

import java.io.DataInput;
import java.io.IOException;
import org.primesoft.asyncworldedit.utils.io.SeekOrigin;

/**
 *
 * @author SBPrime
 */
public interface IChunkCacheStream extends DataInput {

    /**
     * Returns the current offset in this file.
     *
     * @return the offset from the beginning of the file, in bytes, at which the
     * next read or write occurs.
     * @exception IOException if an I/O error occurs.
     */
    long getFilePointer() throws IOException;

    /**
     * Returns the length of this file.
     *
     * @return the length of this file, measured in bytes.
     * @exception IOException if an I/O error occurs.
     */
    long length() throws IOException;

    /**
     * Sets the current position of this stream to the given value.
     *
     * @param pos The point relative to origin from which to begin seeking.
     * @param origin Specifies the beginning, the end, or the current position
     * as a reference point for offset, using a value of type SeekOrigin.
     * @throws IOException
     * @return The new file position
     */
    long seek(long pos, SeekOrigin origin) throws IOException;
    
}
