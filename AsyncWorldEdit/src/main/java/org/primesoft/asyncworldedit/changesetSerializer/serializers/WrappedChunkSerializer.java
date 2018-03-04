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
package org.primesoft.asyncworldedit.changesetSerializer.serializers;

import com.sk89q.worldedit.Vector2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.IWrappedChunk;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.directChunk.DcUtils;

/**
 *
 * @author SBPrime
 */
public class WrappedChunkSerializer {

    /**
     * Serialize the wrapped chunk
     *
     * @param stream
     * @param wChunk
     * @throws IOException
     */
    public static void serialize(DataOutput stream, IWrappedChunk wChunk) throws IOException {
        IPlayerEntry player = wChunk.getPlayer();
        IWorld world = wChunk.getWorld();

        UUID playerUUID = player.getUUID();
        UUID worldUUID = world.getUUID();

        int x = wChunk.getX();
        int z = wChunk.getZ();

        stream.writeInt(x);
        stream.writeInt(z);

        stream.writeLong(playerUUID.getLeastSignificantBits());
        stream.writeLong(playerUUID.getMostSignificantBits());

        stream.writeLong(worldUUID.getLeastSignificantBits());
        stream.writeLong(worldUUID.getMostSignificantBits());
    }

    /**
     * Serialize the wrapped chunk
     *
     * @param stream
     * @param awe
     * @return
     * @throws IOException
     */
    public static IWrappedChunk deserialize(DataInput stream,
            IAsyncWorldEdit awe) throws IOException {

        ITaskDispatcher taskDispatcher = awe.getTaskDispatcher();
        IDirectChunkAPI dcApi = awe.getDirectChunkAPI();
        IPlayerManager playerManager = awe.getPlayerManager();
        
        int x = stream.readInt();
        int z = stream.readInt();

        long playerL = stream.readLong();
        long playerM = stream.readLong();

        long worldL = stream.readLong();
        long worldM = stream.readLong();

        UUID playerUUID = new UUID(playerM, playerL);
        UUID worldUUID = new UUID(worldM, worldL);

        IPlayerEntry pEntry = playerManager.getPlayer(playerUUID);
        IWorld world = awe.getWorld(worldUUID);

        return DcUtils.wrapChunk(taskDispatcher, dcApi, awe.getWorldEditIntegrator().getWorld(world), world, pEntry, new Vector2D(x, z));
    }
}
