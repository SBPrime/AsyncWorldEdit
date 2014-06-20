/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.blockPlacer.entries;

import com.sk89q.worldedit.Vector;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.utils.FuncEx;
import org.primesoft.asyncworldedit.worldedit.extent.WorldExtent;

/**
 *
 * @author Prime
 * @param <T> Func result type
 * @param <TException> Exception type
 */
public class WorldExtentFuncEntryEx<T, TException extends Exception>
        extends WorldExtentBlockEntry {

    private final FuncEx<T, TException> m_function;

    public WorldExtentFuncEntryEx(WorldExtent worldExtent,
            int jobId, Vector location, FuncEx<T, TException> function) {
        super(worldExtent, jobId, location);

        m_function = function;
    }

    @Override
    public boolean Process(BlockPlacer bp) {
        T funcResult;

        try {
            funcResult = m_function.Execute();
        } catch (Exception ex) {
            return false;
        }
        finally
        {
            if (m_worldName != null) {
                bp.getPhysicsWatcher().removeLocation(m_worldName, m_location);
            }
        }

        if (funcResult instanceof Boolean) {
            return (Boolean) funcResult;
        } else {
            return true;
        }
    }
}