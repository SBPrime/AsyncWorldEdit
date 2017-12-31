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
package org.primesoft.asyncworldedit.directChunk.relighter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkData;
import org.primesoft.asyncworldedit.api.inner.IBlockRelighter;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.inner.IChunkWatch;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.configuration.ConfigDirectChunkApi;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.directChunk.DcUtils;
import org.primesoft.asyncworldedit.platform.api.IPlatform;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.platform.api.ITask;

/**
 *
 * @author SBPrime
 */
public class BlockReligher implements IBlockRelighter {

    private interface IRelighterMethods {

        public byte getLightLevel(IDirectChunkData chunk, int x, int y, int z, char id);

        public int getOpacityLevel(char tBlockType);

        public byte getCurrentLight(IDirectChunkData chunk, int x, int y, int z);

        public void setCurrentLight(IDirectChunkData chunk, int x, int y, int z, byte level);

        public boolean isFullY();
    }

    /**
     * Is the relight disposed
     */
    private boolean m_isDisposed = false;

    /**
     * The DirectChunk API
     */
    private final IDirectChunkAPI m_dcApi;

    /**
     * The wait mutex
     */
    private final Object m_waitMutex = new Object();

    /**
     * The task
     */
    private ITask m_task;

    /**
     * List of all queued entries
     */
    private final HashMap<UUID, QueueEntry> m_worlds = new LinkedHashMap<UUID, QueueEntry>();

    private final LinkedList<UUID> m_worldsStack = new LinkedList<UUID>();

    /**
     * The data mutex
     */
    private final Object m_dataMutex = new Object();

    /**
     * The chunk watcher
     */
    private IChunkWatch m_chunkWatcher;

    /**
     * The task dispatcher
     */
    private final ITaskDispatcher m_taskDispatcher;

    /**
     * The emission relighter
     */
    private final IRelighterMethods m_relighterEmission;

    /**
     * The sky light relighter
     */
    private final IRelighterMethods m_relighterSky;

    public BlockReligher(IDirectChunkAPI dcApi, ITaskDispatcher taskDispatcher) {
        m_dcApi = dcApi;
        m_taskDispatcher = taskDispatcher;

        m_relighterEmission = new IRelighterMethods() {
            @Override
            public byte getLightLevel(IDirectChunkData chunk, int x, int y, int z, char id) {
                return m_dcApi.getLightEmissionLevel(id);
            }

            @Override
            public int getOpacityLevel(char tBlockType) {
                return Math.max(1, m_dcApi.getOpacityLevel(tBlockType));
            }

            @Override
            public byte getCurrentLight(IDirectChunkData chunk, int x, int y, int z) {
                return chunk.getEmissionLight(x, y, z);
            }

            @Override
            public void setCurrentLight(IDirectChunkData chunk, int x, int y, int z, byte level) {
                chunk.setEmissionLight(x, y, z, level);
            }

            @Override
            public boolean isFullY() {
                return false;
            }
        };

        m_relighterSky = new IRelighterMethods() {
            @Override
            public byte getLightLevel(IDirectChunkData chunk, int x, int y, int z, char id) {
                if (chunk == null) {
                    return 0;
                }

                int opacity = 0;
                for (int py = y; py < 256 && opacity < 15; py++) {
                    id = chunk.getRawBlockData(x, py, z);
                    opacity = Math.max(opacity, m_dcApi.getOpacityLevelSkyLight(id));
                }

                return (byte) Math.max(0, 15 - opacity);
            }

            @Override
            public int getOpacityLevel(char tBlockType) {
                return Math.max(1, m_dcApi.getOpacityLevelSkyLight(tBlockType));
            }

            @Override
            public byte getCurrentLight(IDirectChunkData chunk, int x, int y, int z) {
                return chunk.getSkyLight(x, y, z);
            }

            @Override
            public void setCurrentLight(IDirectChunkData chunk, int x, int y, int z, byte level) {
                chunk.setSkyLight(x, y, z, level);
            }

            @Override
            public boolean isFullY() {
                return true;
            }
        };
    }

    /**
     * Converts X, Z position to encoded chunk
     *
     * @param x X coordinate (not chunk!)
     * @param z Z coordinate (not chunk!)
     * @return
     */
    public static long encodeChunk(int x, int z) {
        long cx = ((x >> 4) & 0xfffffff);
        long cz = ((z >> 4) & 0xfffffff);

        return cx | (cz << 28);
    }

    /**
     * Converts X, Y, Z position to in chunk position
     *
     * @param x X coordinate (not chunk!)
     * @param y Y coordinate (not chunk!)
     * @param z Z coordinate (not chunk!)
     * @return
     */
    public static short encodePosition(int x, int y, int z) {
        return (short) ((x & 0xf) | ((z & 0xf) << 4) | ((y & 0xff) << 8));
    }

    @Override
    public void initialize(IPlatform platform) {
        IScheduler scheduler = platform.getScheduler();

        m_chunkWatcher = platform.getChunkWatcher();

        m_task = scheduler.runTaskAsynchronously(new Runnable() {

            @Override
            public void run() {
                relightLoop();
            }
        });
    }

    @Override
    public void stop() {
        m_isDisposed = true;

        synchronized (m_waitMutex) {
            m_waitMutex.notifyAll();
        }

        synchronized (m_dataMutex) {
            m_worlds.clear();
            m_worldsStack.clear();
        }

        m_task.cancel();
    }

    @Override
    public void queueBlock(IWorld world, int x, int y, int z) {
        ConfigDirectChunkApi dcConfig = ConfigProvider.directChunk();
        if (dcConfig == null || !dcConfig.isAutoRelightEnabled()) {
            return;
        }

        forceQueueBlock(world, x, y, z);
    }

    @Override
    public void forceQueueBlock(IWorld world, int x, int y, int z) {
        if (world == null) {
            return;
        }

        synchronized (m_dataMutex) {
            if (m_isDisposed) {
                return;
            }

            UUID uuid = world.getUUID();

            QueueEntry queue = m_worlds.get(uuid);
            if (queue == null) {
                queue = new QueueEntry(world);
                m_worlds.put(uuid, queue);
            }

            if (queue.queueBlock(x, y, z)) {
                if (!m_worldsStack.contains(uuid)) {
                    m_worldsStack.push(uuid);
                }
            }
        }

        synchronized (m_waitMutex) {
            m_waitMutex.notifyAll();
        }
    }

    @Override
    public Object getDataMutex() {
        return m_dataMutex;
    }

    /**
     * The main relight loop
     */
    private void relightLoop() {
        boolean dataProcessed = false;

        while (!m_isDisposed) {
            if (!dataProcessed) {
                synchronized (m_waitMutex) {
                    try {
                        m_waitMutex.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            } else {
                dataProcessed = false;
            }

            IWorld world = null;
            Long encodedChunk = null;
            HashSet<Short> blocksToProcess = null;

            synchronized (m_dataMutex) {
                if (m_isDisposed) {
                    //Relighter is disposed
                    continue;
                }

                while (!m_worldsStack.isEmpty() && blocksToProcess == null) {
                    UUID worldUUID = m_worldsStack.pop();
                    QueueEntry entry = m_worlds.get(worldUUID);

                    Queue<Long> hashQueue = entry.getQueue();
                    HashMap<Long, HashSet<Short>> blockQueue = entry.getBlockQueue();

                    if (hashQueue.isEmpty()) {
                        //No data? strange this should not happen...
                        //Continue to next world
                        continue;
                    }

                    encodedChunk = hashQueue.poll();
                    blocksToProcess = blockQueue.get(encodedChunk);
                    blockQueue.remove(encodedChunk);
                    world = entry.getWorld();

                    if (!hashQueue.isEmpty()) {
                        m_worldsStack.push(worldUUID);
                    }
                }
            }

            if (blocksToProcess != null) {
                relight(world, encodedChunk, blocksToProcess);
                dataProcessed = true;
            }
        }
    }

    /**
     * Relight blocks
     *
     * @param encodedChunk
     * @param blocks
     */
    private void relight(IWorld world, long encodedChunk, HashSet<Short> blocks) {
        //HACK: We need to do SHL then SHR to handle the negative chunks propwerly
        int cx = ((int) ((encodedChunk & 0xfffffff) << 4)) >> 4;
        int cz = ((int) (((encodedChunk >> 28) & 0xfffffff) << 4)) >> 4;
        String worldName = world.getName();

        int[] ccx = new int[3];
        int[] ccz = new int[3];

        for (int i = -1; i < 2; i++) {
            ccx[i + 1] = cx + i;
            ccz[i + 1] = cz + i;
        }

        final IWrappedChunk[] chunks = new IWrappedChunk[9];
        final IDirectChunkData[] chunkData = new IDirectChunkData[9];

        try {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    m_chunkWatcher.add(ccx[i], ccz[j], worldName);
                }
            }

            for (int i = 0; i < 9; i++) {
                IWrappedChunk wChunk = DcUtils.wrapChunk(m_taskDispatcher, m_dcApi, world,
                        ccx[i % 3], ccz[i / 3]);
                chunks[i] = wChunk;
                chunkData[i] = wChunk.getDirectDataManipulator();

                wChunk.initLighting();
            }

            //relight(chunks, chunkData, blocks, m_relighterEmission);
            relight(chunks, chunkData, blocks, m_relighterSky);
        } finally {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    m_chunkWatcher.remove(ccx[i], ccz[j], worldName);
                }
            }
        }
    }

    private final static int[] DD_X = new int[]{-1, 1, 0, 0, 0, 0};
    private final static int[] DD_Y = new int[]{0, 0, -1, 1, 0, 0};
    private final static int[] DD_Z = new int[]{0, 0, 0, 0, -1, 1};

    /**
     * Recalculate light starting from blocks
     *
     * @param chunks
     */
    private static void relight(IWrappedChunk[] chunks, IDirectChunkData[] chunkData, HashSet<Short> blocks,
            IRelighterMethods relighter) {
        //Move position to center chunk
        final Set<Integer> diamond = new HashSet<Integer>();
        final HashMap<Byte, Queue<Integer>> blockQueue = new LinkedHashMap<Byte, Queue<Integer>>();
        final Set<Integer> blockQueueFlat = new HashSet<Integer>();

        calculateDiamond(blocks, diamond, chunkData, relighter.isFullY());
        queueDiamond(diamond, chunkData, blockQueue, blockQueueFlat, relighter);

        while (!blockQueue.isEmpty()) {
            int data = dequeueBlock(blockQueue, blockQueueFlat);
            if (data == -1) {
                continue;
            }

            int x = data & 0x3f;
            int y = (data >> 12) & 0xff;
            int z = (data >> 6) & 0x3f;

            if (!diamond.contains(data)) {
                continue;
            }

            int idx = (x / 16) + (z / 16) * 3;
            IDirectChunkData chunk = chunkData[idx];

            int lightCurrent = relighter.getCurrentLight(chunk, x & 0xf, y, z & 0xf);
            int lightExpected = getExpectedLight(x, y, z, chunkData, relighter);

            if (lightCurrent == lightExpected) {
                continue;
            }

            relighter.setCurrentLight(chunk, x & 0xf, y, z & 0xf, (byte) lightExpected);

            char tBlockType = (char) chunk.getRawBlockData(x & 0xf, y, z & 0xf);
            int tOpacity = relighter.getOpacityLevel(tBlockType);

            lightExpected -= tOpacity;
            if (lightExpected < 1) {
                continue;
            }

            for (int i = 0; i < DD_X.length; i++) {
                int newX = x + DD_X[i];
                int newY = y + DD_Y[i];
                int newZ = z + DD_Z[i];

                data = encodeRelightPosition(newX, newY, newZ);
                if (!diamond.contains(data) || newY < 0 || newY > 255) {
                    continue;
                }

                idx = (newX / 16) + (newZ / 16) * 3;
                chunk = chunkData[idx];
                lightCurrent = relighter.getCurrentLight(chunk, newX & 0xf, newY, newZ & 0xf);

                if (lightCurrent >= lightExpected) {
                    continue;
                }

                if (blockQueueFlat.contains(data)) {
                    continue;
                }

                queueBlock((byte) (lightExpected & 0xf), data, blockQueue, blockQueueFlat);
            }
        }

        for (IWrappedChunk chunk : chunks) {
            chunk.setDirty();
            chunk.sendChunkUpdate();
        }
    }

    private static void queueDiamond(final Set<Integer> diamond, IDirectChunkData[] chunkData,
            final HashMap<Byte, Queue<Integer>> blockQueue, Set<Integer> blockQueueFlat,
            IRelighterMethods relighter) {
        for (Integer data : diamond) {
            int x = data & 0x3f;
            int y = (data >> 12) & 0xff;
            int z = (data >> 6) & 0x3f;

            int idx = (x / 16) + (z / 16) * 3;
            IDirectChunkData chunk = chunkData[idx];
            relighter.setCurrentLight(chunk, x & 0xf, y, z & 0xf, (byte) 0);
            char id = chunk.getRawBlockData(x & 0xf, y, z & 0xf);
            byte lightLevel = relighter.getLightLevel(chunk, x & 0xf, y, z & 0xf, id);

            queueBlock(lightLevel, data, blockQueue, blockQueueFlat);
        }
    }

    /**
     * Queue all blocks inside a diamond shape
     *
     * @param blockQueue
     * @param chunkData
     */
    private static void calculateDiamond(Set<Short> blocks, final Set<Integer> diamond, IDirectChunkData[] chunkData,
            boolean fullY) {
        for (Short data : blocks) {
            int px = 16 + (data & 0xf);
            int py = (data >> 8) & 0xff;
            int pz = 16 + ((data >> 4) & 0xf);

            for (int tx = -15; tx <= 15; tx++) {
                int dx = tx < 0 ? -tx : tx;
                int x = px + tx;
                for (int tz = -15 + dx; tz <= 15 - dx; tz++) {
                    int z = pz + tz;
                    int dz = tz < 0 ? -tz : tz;

                    int yMin, yMax;
                    if (fullY) {
                        yMin = 0;
                        yMax = 255;
                    } else {
                        yMin = py + -15 + dx + dz;
                        yMax = py + 15 - dx - dz;
                    }

                    for (int y = yMin; y <= yMax; y++) {
                        if (!isValidPos(x, y, z)) {
                            return;
                        }

                        Integer encoded = encodeRelightPosition(x, y, z);
                        if (!diamond.contains(encoded)) {
                            diamond.add(encoded);
                        }
                    }
                }
            }
        }
    }

    /**
     * Encode relight block position and expected light value
     *
     * @param x
     * @param y
     * @param z
     * @param light
     * @return
     */
    private static int encodeRelightPosition(int x, int y, int z) {
        return (x & 0x3f) | ((z & 0x3f) << 6) | ((y & 0xff) << 12);
    }

    /**
     * Is the position valid
     *
     * @param px
     * @param py
     * @param pz
     * @return
     */
    private static boolean isValidPos(int px, int py, int pz) {
        return px >= 0 && px < 48 && pz >= 0 && pz < 48 && py >= 0 && py < 256;
    }

    /**
     * Get the expected light level for block
     *
     * @param x
     * @param y
     * @param z
     * @param chunkData
     * @return
     */
    private static int getExpectedLight(int x, int y, int z, IDirectChunkData[] chunkData,
            IRelighterMethods relighter) {
        if (!isValidPos(x, y, z)) {
            return -1;
        }

        IDirectChunkData chunk = chunkData[(x / 16) + (z / 16) * 3];
        char tBlockType = (char) chunk.getRawBlockData(x & 0xf, y, z & 0xf);

        int tLight = relighter.getLightLevel(chunk, x & 0xf, y, z & 0xf, tBlockType);
        int tOpacity = relighter.getOpacityLevel(tBlockType);

        if (tOpacity >= 15) {
            return 0;
        }
        if (tLight >= 14) {
            return tLight;
        }

        for (int i = 0; i < DD_X.length && tLight < 15; i++) {
            int newX = x + DD_X[i];
            int newY = y + DD_Y[i];
            int newZ = z + DD_Z[i];

            if (!isValidPos(newX, newY, newZ) || newY < 0 || newY > 255) {
                continue;
            }

            chunk = chunkData[(newX / 16) + (newZ / 16) * 3];
            tBlockType = (char) chunk.getRawBlockData(newX & 0xf, newY, newZ & 0xf);
            tLight = Math.max(tLight, relighter.getCurrentLight(chunk, newX & 0xf, newY, newZ & 0xf) - tOpacity);
        }

        return tLight;
    }

    /**
     * Queue block to block queue
     *
     * @param emission
     * @param data
     * @param blockQueue
     */
    private static void queueBlock(Byte emission, Integer data,
            HashMap<Byte, Queue<Integer>> blockQueue, Set<Integer> blockQueueFlat) {
        Queue<Integer> entry = blockQueue.get(emission);
        if (entry == null) {
            entry = new LinkedList<Integer>();
            blockQueue.put(emission, entry);
        }

        entry.add(data);
        blockQueueFlat.add(data);
    }

    private static int dequeueBlock(HashMap<Byte, Queue<Integer>> blockQueue,
            Set<Integer> blockQueueFlat) {
        if (blockQueue.isEmpty()) {
            return -1;
        }

        for (Byte emission = 15; emission >= 0; emission--) {
            Queue<Integer> queue = blockQueue.get(emission);
            if (queue == null) {
                continue;
            }
            if (queue.isEmpty()) {
                blockQueue.remove(emission);
                continue;
            }

            Integer result = queue.poll();
            blockQueueFlat.remove(result);
            if (queue.isEmpty()) {
                blockQueue.remove(emission);
            }

            return result;
        }

        return -1;
    }
}
