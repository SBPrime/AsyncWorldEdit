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
import com.sk89q.worldedit.Vector;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primesoft.asyncworldedit.api.directChunk.IBiomeEntry;
import org.primesoft.asyncworldedit.api.directChunk.IBlockEntry;
import org.primesoft.asyncworldedit.api.directChunk.IChangesetData;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkAPI;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.directChunk.entries.BiomeEntry;
import org.primesoft.asyncworldedit.directChunk.entries.BlockEntry;
import org.primesoft.asyncworldedit.directChunk.ReadonlyChangesetData;

/**
 *
 * @author SBPrime
 */
class ChangesetSerializer {

    static void serialize(DataOutput stream, IChangesetData changeset) throws IOException {
        ISerializedEntity[] addedEntity = null;
        ISerializedEntity[] removedEntity = null;
        IBlockEntry[] changedBlocks = null;
        IBiomeEntry[] changedBiomes = null;

        if (changeset != null) {
            addedEntity = changeset.getAddedEntities();
            removedEntity = changeset.getRemovedEntities();
            changedBlocks = changeset.getChangedBlocks();
            changedBiomes = changeset.getChangedBiomes();
        }

        if (addedEntity == null) {
            addedEntity = new ISerializedEntity[0];
        }
        if (removedEntity == null) {
            removedEntity = new ISerializedEntity[0];
        }
        if (changedBlocks == null) {
            changedBlocks = new IBlockEntry[0];
        }

        if (changedBiomes == null) {
            changedBiomes = new IBiomeEntry[0];
        }

        stream.writeInt(addedEntity.length);
        for (ISerializedEntity entity : addedEntity) {
            serializeEntity(stream, entity);
        }

        stream.writeInt(removedEntity.length);
        for (ISerializedEntity entity : removedEntity) {
            serializeEntity(stream, entity);
        }

        stream.writeInt(changedBlocks.length);
        for (IBlockEntry block : changedBlocks) {
            serializeBlock(stream, block);
        }

        stream.writeInt(changedBiomes.length);
        for (IBiomeEntry biome : changedBiomes) {
            serializeBiome(stream, biome);
        }
    }

    static IChangesetData deserialize(DataInput stream, IDirectChunkAPI directChunkAPI) throws IOException {
        int addedEntityCount = stream.readInt();
        List<ISerializedEntity> addedEntity = new ArrayList<ISerializedEntity>();
        for (int i = 0; i < addedEntityCount; i++) {
            ISerializedEntity entity = deserializeEntity(stream, directChunkAPI);
            if (entity != null) {
                addedEntity.add(entity);
            }
        }

        int removedEntityCount = stream.readInt();
        List<ISerializedEntity> removedEntity = new ArrayList<ISerializedEntity>();
        for (int i = 0; i < removedEntityCount; i++) {
            ISerializedEntity entity = deserializeEntity(stream, directChunkAPI);
            if (entity != null) {
                removedEntity.add(entity);
            }
        }

        int changedBlocksCount = stream.readInt();
        IBlockEntry[] changedBlocks = new IBlockEntry[changedBlocksCount];
        for (int i = 0; i < changedBlocksCount; i++) {
            changedBlocks[i] = deserializeBlock(stream);
        }

        int changedBiomesCount = stream.readInt();
        IBiomeEntry[] changedBiomes = new IBiomeEntry[changedBiomesCount];
        for (int i = 0; i < changedBiomesCount; i++) {
            changedBiomes[i] = deserializeBiome(stream);
        }

        return new ReadonlyChangesetData(
                addedEntity.toArray(new ISerializedEntity[0]), removedEntity.toArray(new ISerializedEntity[0]),
                changedBlocks, changedBiomes);
    }

    private static void serializeEntity(DataOutput stream, ISerializedEntity entity) throws IOException {
        if (entity == null) {
            stream.writeBoolean(false);
            return;
        }

        stream.writeBoolean(true);

        UUID uuid = entity.getUuid();
        if (uuid == null) {
            uuid = new UUID(0, 0);
        }

        stream.writeLong(uuid.getLeastSignificantBits());
        stream.writeLong(uuid.getMostSignificantBits());

        stream.writeFloat(entity.getYaw());
        stream.writeFloat(entity.getPitch());

        Vector vector = entity.getPosition();
        stream.writeDouble(vector.getX());
        stream.writeDouble(vector.getY());
        stream.writeDouble(vector.getZ());

        byte[] nbt = entity.getNBT();
        if (nbt == null) {
            nbt = new byte[0];
        }
        stream.writeInt(nbt.length);
        stream.write(nbt);

        ISerializedEntity vehicle = entity.getVehicle();

        if (vehicle != null) {
            stream.writeBoolean(true);
            serializeEntity(stream, entity);
        } else {
            stream.writeBoolean(false);
        }
    }

    private static ISerializedEntity deserializeEntity(DataInput stream,
            IDirectChunkAPI directChunkAPI) throws IOException {
        if (!stream.readBoolean()) {
            return null;
        }

        long uuidL = stream.readLong();
        long uuidM = stream.readLong();
        UUID uuid = new UUID(uuidM, uuidL);

        float yaw = stream.readFloat();
        float pitch = stream.readFloat();

        double x = stream.readDouble();
        double y = stream.readDouble();
        double z = stream.readDouble();
        Vector vector = new Vector(x, y, z);

        byte[] nbt = new byte[stream.readInt()];
        stream.readFully(nbt);

        boolean hasVehicle = stream.readBoolean();

        ISerializedEntity result = directChunkAPI.createEntity(uuid, vector, yaw, pitch, nbt);
        ISerializedEntity vehicle = hasVehicle ? deserializeEntity(stream, directChunkAPI) : null;

        result.setVehicle(vehicle);

        return result;
    }

    private static void serializeBlock(DataOutput stream, IBlockEntry block) throws IOException {
        if (block == null) {
            block = new BlockEntry((char) 0, 0, 0, 0, null, (byte)0);
        }
        
        stream.writeInt(block.getX());
        stream.writeInt(block.getY());
        stream.writeInt(block.getZ());

        stream.writeBoolean(block.hasBlock());
        stream.writeBoolean(block.hasLight());
        stream.writeBoolean(block.hasSkyLight());
                
        stream.writeByte(block.getEmission());
        stream.writeByte(block.getSky());
        stream.writeChar(block.getId());
                
        CompoundTag tag = block.getNbt();
        if (tag != null) {
            stream.writeBoolean(true);
            NbtTagSerializer.serialize(stream, block.getNbt());
        } else {
            stream.writeBoolean(false);
        }
    }

    private static IBlockEntry deserializeBlock(DataInput stream) throws IOException {        
        int x = stream.readInt();
        int y = stream.readInt();
        int z = stream.readInt();
        
        boolean hasBlock = stream.readBoolean();
        boolean hasLight = stream.readBoolean();
        boolean hasSkyLight = stream.readBoolean();
                
        byte emissionLevel = stream.readByte();
        byte skyLevel = stream.readByte();
        char id = stream.readChar();

        Tag nbt = stream.readBoolean() ? NbtTagSerializer.deserialize(stream) : null;

        if (hasBlock && (hasLight || hasSkyLight)) {
            return new BlockEntry(id, x, y, z, nbt instanceof CompoundTag
                    ? (CompoundTag) nbt : null, emissionLevel, skyLevel);
        }

        if (hasBlock) {
            return new BlockEntry(id, x, y, z, nbt instanceof CompoundTag
                    ? (CompoundTag) nbt : null);
        }
        
        return new BlockEntry(x, y, z, emissionLevel, skyLevel);
    }
    
    private static void serializeBiome(DataOutput stream, IBiomeEntry biome) throws IOException {
        if (biome == null) {
            biome = new BiomeEntry((byte) 0, 0, 0);
        }

        stream.writeByte(biome.getId());
        stream.writeInt(biome.getX());
        stream.writeInt(biome.getZ());
    }

    private static IBiomeEntry deserializeBiome(DataInput stream) throws IOException {
        byte id = stream.readByte();
        int x = stream.readInt();
        int z = stream.readInt();

        return new BiomeEntry(id, x, z);
    }
}
