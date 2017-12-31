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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.primesoft.asyncworldedit.api.changesetSerializer.IChangesetSerializer;
import org.primesoft.asyncworldedit.api.changesetSerializer.IMemoryStorage;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.io.UnsafeDataInput;
import org.primesoft.asyncworldedit.utils.io.UnsafeDataOutput;

/**
 *
 * @author SBPrime
 */
public class SerializerBlockChange implements IChangesetSerializer {
    private static final String CLASS_TYPE = BlockChange.class.getName();
    
    private static final int AIR = 0;

    @Override
    public boolean canSerialize(String type) {
        return (type != null) && type.equalsIgnoreCase(CLASS_TYPE);
    }

    @Override
    public byte[] serialize(Change change, IMemoryStorage storage) {
        BlockChange bChange = change instanceof BlockChange ? (BlockChange) change : null;
        if (bChange == null) {
            return null;
        }

        BlockVector position = bChange.getPosition();

        if (position == null) {
            return null;
        }

        BaseBlock current = bChange.getCurrent();
        BaseBlock previous = bChange.getPrevious();

        if (current == null) {
            current = new BaseBlock(AIR);
        }

        if (previous == null) {
            previous = new BaseBlock(AIR);
        }

        try {
            UnsafeDataOutput stream = new UnsafeDataOutput();

            stream.writeDouble(position.getX());
            stream.writeDouble(position.getY());
            stream.writeDouble(position.getZ());

            writeBlock(stream, previous);
            writeBlock(stream, current);

            return stream.toByteArray();
        } catch (IOException ex) {
            ExceptionHelper.printException(ex, "Unable to serialize BlockChange");
            return null;
        }
    }

    @Override
    public Change deserialize(byte[] data, IMemoryStorage storage) {
        if (data == null) {
            return null;
        }

        try {
            UnsafeDataInput stream = new UnsafeDataInput(data);
            
            double x = stream.readDouble();
            double y = stream.readDouble();
            double z = stream.readDouble();
            
            BaseBlock previous = readBlock(stream);
            BaseBlock current = readBlock(stream);
            
            return new BlockChange(new BlockVector(x, y, z), previous, current);
        } catch (IOException ioe) {
            ExceptionHelper.printException(ioe, "Unable to deserialize BlockChange");
            return null;
        }
    }

    /**
     * Serialize block data
     *
     * @param stream
     * @param block
     */
    private void writeBlock(DataOutput stream, BaseBlock block) throws IOException {
        stream.writeInt(block.getId());
        stream.writeInt(block.getData());

        NbtTagSerializer.serialize(stream, block.hasNbtData() ? block.getNbtData() : NbtTagSerializer.END_TAG);
    }

    private BaseBlock readBlock(DataInput stream) throws IOException {
        int id = stream.readInt();
        int data = stream.readInt();
        
        Tag nbtTag = NbtTagSerializer.deserialize(stream);
        if (nbtTag instanceof CompoundTag) {
            return new BaseBlock(id, data, (CompoundTag)nbtTag);
        }
        
        
        return new BaseBlock(id, data);
    }
}
