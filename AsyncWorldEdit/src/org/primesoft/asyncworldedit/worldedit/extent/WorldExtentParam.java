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
package org.primesoft.asyncworldedit.worldedit.extent;

import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.UUID;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;

/**
 *
 * @author SBPrime
 * @param <T>
 */
public class WorldExtentParam<T> {
    static BaseBlock extract;

    /**
     * Extract parameters
     * @param <T>
     * @param data
     * @return
     */
    public static <T> WorldExtentParam<T> extract(T data) {
        int jobId = -1;
        boolean isAsync = false;
        UUID player = ConfigProvider.DEFAULT_USER;

        if (data instanceof IAsyncWrapper) {
            IAsyncWrapper wrapper = (IAsyncWrapper) data;
            jobId = wrapper.getJobId();
            data = (T)wrapper.getParent();
            isAsync = wrapper.isAsync();
            player = wrapper.getPlayer();
        }

        return new WorldExtentParam(data, isAsync, jobId, player);
    }

    private final boolean m_isAsync;
    private final T m_data;
    private final int m_jobId;
    private final UUID m_player;

    private WorldExtentParam(T data, boolean isAsync, int jobId, UUID player) {
        m_data = data;
        m_isAsync = isAsync;
        m_jobId = jobId;
        m_player = player;
    }

    public boolean isAsync() {
        return m_isAsync;
    }

    public T getData() {
        return m_data;
    }

    public int getJobId() {
        return m_jobId;
    }

    public UUID getPlayer() {
        return m_player;
    }
}