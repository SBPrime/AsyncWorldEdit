/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.directChunk;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.world.World;
import java.util.Arrays;
import org.primesoft.asyncworldedit.api.IChunk;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.configuration.ConfigDirectChunkApi;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.MutexProvider;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 * A few helper functions for DirectChunk API
 *
 * @author SBPrime
 */
public class DcUtils {

    public static final int CHUNK_SIZE = 16 * 16 * 256;
    
    
    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param mutex
     * @param weWorld
     * @param world
     * @param player
     * @param cx
     * @param cz
     * @return
     */
    private static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            Object mutex, final IWorld world, final IPlayerEntry player,
            final int cx, final int cz) {
        IWrappedChunk wrappedChunk = taskDispatcher.performSafe(mutex,
                new IFunc<IWrappedChunk>() {
                    @Override
                    public IWrappedChunk execute() {
                        IChunk chunk = world.getChunkAt(cx, cz);

                        if (player== null) {
                            return chunkApi.wrapChunk(chunk);
                        } else {
                            return chunkApi.wrapChunk(chunk, player);
                        }
                    }
                },
                world, PositionHelper.chunkToPosition(cx, 0, cz)
        );

        return wrappedChunk;
    }
    
    
    
    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param weWorld
     * @param world
     * @param player
     * @param cx
     * @param cz
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            World weWorld, final IWorld world, final IPlayerEntry player,
            final int cx, final int cz) {
        return wrapChunk(taskDispatcher, chunkApi, MutexProvider.getMutex(weWorld), world, player, cx, cz);
    }
    

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param weWorld
     * @param world
     * @param player
     * @param cPos
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            World weWorld, final IWorld world, final IPlayerEntry player,
            Vector2D cPos) {
        return wrapChunk(taskDispatcher, chunkApi, weWorld, world, player, cPos.getBlockX(), cPos.getBlockZ());

    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param weWorld
     * @param world
     * @param cPos
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            World weWorld, final IWorld world, Vector2D cPos) {
        return wrapChunk(taskDispatcher, chunkApi, weWorld, world, null, cPos.getBlockX(), cPos.getBlockZ());
    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param weWorld
     * @param world
     * @param cx
     * @param cz
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            World weWorld, final IWorld world, int cx, int cz) {
        return wrapChunk(taskDispatcher, chunkApi, weWorld, world, null, cx, cz);
    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param world
     * @param player
     * @param cPos
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            final IWorld world, final IPlayerEntry player,
            Vector2D cPos) {
        return wrapChunk(taskDispatcher, chunkApi, world, player, cPos.getBlockX(), cPos.getBlockZ());

    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param world
     * @param player
     * @param cx
     * @param cz
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            final IWorld world, final IPlayerEntry player,
            final int cx, final int cz) {
        return wrapChunk(taskDispatcher, chunkApi, MutexProvider.getMutex(world), world, player, cx, cz);
    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param world
     * @param cPos
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            final IWorld world, Vector2D cPos) {
        return wrapChunk(taskDispatcher, chunkApi, world, null, cPos.getBlockX(), cPos.getBlockZ());
    }

    /**
     * Wrap the provided chunk using the task dispatcher
     *
     * @param taskDispatcher
     * @param chunkApi
     * @param world
     * @param cx
     * @param cz
     * @return
     */
    public static IWrappedChunk wrapChunk(ITaskDispatcher taskDispatcher, final IDirectChunkAPI chunkApi,
            final IWorld world, int cx, int cz) {
        return wrapChunk(taskDispatcher, chunkApi, world, null, cx, cz);
    }
    
    
    
    
    /**
     * Initializes the light array
     * @return 
     */
    public static byte[] newSectionEmittedLight() {
        byte[] result = new byte[2048];
        
        ConfigDirectChunkApi dc = ConfigProvider.directChunk();
        int light = (dc == null ? 0 : dc.getSectionLight()) & 0xf;
        
        Arrays.fill(result, (byte)(light | (light << 4)));
        
        return result;
    }
}
