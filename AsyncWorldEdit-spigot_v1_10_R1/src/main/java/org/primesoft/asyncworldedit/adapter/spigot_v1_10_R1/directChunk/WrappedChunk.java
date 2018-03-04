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
package org.primesoft.asyncworldedit.adapter.spigot_v1_10_R1.directChunk;

import com.sk89q.worldedit.BlockVector2D;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.Chunk;
import net.minecraft.server.v1_10_R1.ChunkSection;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityHuman;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EntityTracker;
import net.minecraft.server.v1_10_R1.EntityTrackerEntry;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.TileEntity;
import net.minecraft.server.v1_10_R1.World;
import net.minecraft.server.v1_10_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.directChunk.IChunkData;
import org.primesoft.asyncworldedit.api.directChunk.IDirectChunkData;
import org.primesoft.asyncworldedit.api.directChunk.ISerializedEntity;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.platform.bukkit.directChunk.BaseBukkitWrappedChunk;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.PositionHelper;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class WrappedChunk extends BaseBukkitWrappedChunk<CraftChunk> {

    private static final Field s_fMaxHeight;

    private static final Field s_fGaps;

    /**
     * This field does not need to be copied use removeEntities and
     * removeEntities
     */
    private static final Field s_fEntitiesLoaded;

    private static final Field s_fRecalculateGaps;
    private static final Field s_fEntityCount;
    private static final Field s_fUnknown1;
    private static final Field s_fUnknown2;
    private static final Field s_fUnknown3;

    private static final boolean s_initialized;

    public static boolean isInitialized() {
        return s_initialized;
    }

    static {
        Class<?> cChunk = Chunk.class;

        s_fMaxHeight = Reflection.findTypedField(cChunk, int[].class, "h", "Unable to find the MaxHeight \"h\" field");
        s_fGaps = Reflection.findTypedField(cChunk, boolean[].class, "i", "Unable to find the Gaps \"i\" field");
        s_fEntitiesLoaded = Reflection.findTypedField(cChunk, boolean.class, "j", "Unable to find the EntitiesLoaded \"j\" field");
        s_fEntityCount = Reflection.findField(cChunk, "entityCount", "Unable to find the entityCount field");
        s_fRecalculateGaps = Reflection.findTypedField(cChunk, boolean.class, "m", "Unable to find the recalculate gaps \"m\" field");

        s_fUnknown1 = Reflection.findTypedField(cChunk, boolean.class, "s", "Unable to find the unknown \"s\" field");
        s_fUnknown2 = Reflection.findTypedField(cChunk, boolean.class, "t", "Unable to find the unknown \"t\" field");

        s_fUnknown3 = Reflection.findTypedField(cChunk, int.class, "x", "Unable to find the unknown \"x\" field");

        s_initialized = s_fMaxHeight != null
                && s_fGaps != null && s_fEntitiesLoaded != null
                && s_fRecalculateGaps != null && s_fEntityCount != null
                && s_fUnknown1 != null && s_fUnknown2 != null && s_fUnknown3 != null;
    }

    WrappedChunk(IBlocksHubIntegration blocksHub, org.bukkit.World world, int cx, int cz,
            IPlayerEntry player) {
        super(blocksHub, world, cx, cz, player, CraftChunk.class);
    }

    @Override
    public IChunkData getData() {
        final CraftChunk bukkitChunk = getChunk();

        if (bukkitChunk == null || !isValid(bukkitChunk)) {
            return null;
        }

        Chunk chunk = bukkitChunk.getHandle();
        ChunkSection[] dChunkSection = chunk.getSections();
        byte[] dBiomeData = chunk.getBiomeIndex();
        int[] dMaxHeight = Reflection.get(chunk, int[].class, s_fMaxHeight, "Unable to get data");
        boolean[] dGaps = Reflection.get(chunk, boolean[].class, s_fGaps, "Unable to get data");
        int[] dHeightMap = chunk.r();

        List<TileEntity> dTileEntities = new LinkedList<TileEntity>(chunk.getTileEntities().values());
        List<Entity>[] dEntitySlices = chunk.getEntitySlices();
        boolean dDone = chunk.isDone();
        boolean dLit = chunk.v();

        boolean dQ = Reflection.get(chunk, Boolean.class, s_fUnknown1, "Unable to get data");
        boolean dR = Reflection.get(chunk, Boolean.class, s_fUnknown2, "Unable to get data");
        long dU = chunk.w();
        int dV = Reflection.get(chunk, Integer.class, s_fUnknown3, "Unable to get data");

        ChunkData result = new ChunkData();

        result.setChunkCoords(new BlockVector2D(chunk.locX, chunk.locZ));

        result.setChunkSections(dChunkSection);
        result.setTileEntity(Nbt.serialise(m_cx, m_cz, dTileEntities));
        result.setEntity(Nbt.serialise(m_cx, m_cz, dEntitySlices));
        result.setMaxHeight(dMaxHeight);
        result.setHeightMap(dHeightMap);
        result.setBiomeData(dBiomeData);
        result.setGaps(dGaps);
        result.setDone(dDone);
        result.setLit(dLit);

        result.setUnknownQ(dQ);
        result.setUnknownR(dR);
        result.setUnknownU(dU);
        result.setUnknownV(dV);

        return result;
    }

    
    @Override
    public IDirectChunkData getDirectDataManipulator() {
        final CraftChunk bukkitChunk = getChunk();

        if (bukkitChunk == null || !isValid(bukkitChunk)) {
            return null;
        }

        return new DirectChunkData(this, bukkitChunk.getHandle());
    }

    
    
    /**
     * Set the actual chunk data
     *
     * @param data
     * @param removeAllEntities
     * @param entitiesToRemove
     * @param removeAllTileEntities
     * @param setSections
     * @param setBiome
     * @param setAdditional
     * @param setUnknown
     * @param entitiesToAdd
     * @param setTileEntities
     * @param addedEntities
     * @return
     */
    @Override
    protected boolean setData(IChunkData data,
            boolean removeAllEntities, UUID[] entitiesToRemove,
            boolean removeAllTileEntities,
            boolean setSections, boolean setBiome,
            boolean setAdditional, boolean setUnknown,
            ISerializedEntity[] entitiesToAdd, boolean setTileEntities,
            InOutParam<UUID[]> addedEntities) {
        final CraftChunk bukkitChunk = getChunk();
        if (bukkitChunk == null || !isValid(bukkitChunk)) {
            return false;
        }

        Chunk chunk = bukkitChunk.getHandle();
        ChunkData cData = (ChunkData) data;

        unloadEntities(chunk);
        if (removeAllEntities) {
            removeAllEntities(chunk);
        } else if (entitiesToRemove != null) {
            removeEntities(chunk, entitiesToRemove);
        }
        if (removeAllTileEntities) {
            removeAllTileEntities(chunk);
        }

        final int locX = chunk.locX;
        final int locZ = chunk.locZ;
        final World world = chunk.getWorld();

        //Set chunk data
        if (setSections) {
            chunk.a(cData.getChunkSections());
        }
        if (setBiome) {
            chunk.a(cData.getBiomeData());
        }
        if (setAdditional) {
            chunk.a(cData.getHeightMap());
            //cData.getGaps()
            chunk.d(cData.isDone());
            chunk.e(cData.isLit());
        }
        if (setUnknown) {
            chunk.c(cData.getUnknownU());
            Reflection.set(chunk, s_fUnknown1, cData.getUnknownQ(), "Unable to set data");
            Reflection.set(chunk, s_fUnknown2, cData.getUnknownR(), "Unable to set data");
            Reflection.set(chunk, s_fUnknown3, cData.getUnknownV(), "Unable to set data");
        }
        if (setTileEntities) {
            addTileEntities(world, locX, locZ, data, chunk);
        }
        if (entitiesToAdd != null) {
            UUID[] tAddedEntities = addEntities(locX, locZ, chunk, entitiesToAdd);

            if (addedEntities != null) {
                addedEntities.setValue(tAddedEntities);
            }

        }
        
        addEntities(chunk);

        return true;
    }

    private void removeAllEntities(Chunk chunk) {
        //Clear all entities
        Object o = Reflection.get(chunk, s_fEntityCount, "Unable to get entity count");
        TObjectIntHashMap<Class> entityCount = o instanceof TObjectIntHashMap
                ? (TObjectIntHashMap<Class>) o : null;
        if (entityCount != null) {
            entityCount.clear();
        }
        for (List<Entity> entry : chunk.getEntitySlices()) {
            entry.clear();
        }
    }
    
    
    private void unloadEntities(Chunk chunk) {
        //Are the entities loaded
        if (chunk.p()) {
            chunk.removeEntities();
        }
    }

    private void addEntities(Chunk chunk) {
        //Are the entities loaded
        if (chunk.p()) {
            return;
        }
        
        Reflection.set(chunk, s_fEntitiesLoaded, true, "Unable to set entities loaded flag");
        chunk.world.b(chunk.tileEntities.values());
        for (List<Entity> slice : chunk.getEntitySlices()) {
            List<Entity> toAdd = new ArrayList();
            for (Entity entity : slice) {
                if (entity instanceof EntityPlayer) {
                    continue;
                }
                
                toAdd.add(entity);
            }
        
            
            chunk.world.a(toAdd);
        }
    }

    private void addTileEntities(final World world, int locX, int locZ, IChunkData cData, final Chunk chunk) {
        //Insert new tile entities
        for (TileEntity te : Nbt.deserialise(world, locX, locZ, cData.getTileEntity())) {
            chunk.a(te);
            te.update();
        }
    }

    private void removeAllTileEntities(final Chunk chunk) {
        //Clear all tile enties
        Map<BlockPosition, TileEntity> dTileEntities = chunk.getTileEntities();
        for (TileEntity te : new LinkedList<TileEntity>(dTileEntities.values())) {
            te.y();
        }
        dTileEntities.clear();
    }

    private void removeEntities(final Chunk chunk, UUID[] dataEntitiesRemoveUUID) {
        //Remove entities
        List<Entity>[] dEntitySlices = chunk.getEntitySlices();
        List<Entity> toRemoveEntity = new ArrayList<Entity>();
        HashSet<UUID> toRemove = new HashSet<UUID>();
        for (UUID u : dataEntitiesRemoveUUID) {
            if (!toRemove.contains(u)) {
                toRemove.add(u);
            }
        }
        for (List<Entity> sSlice : dEntitySlices) {
            for (Entity e : sSlice) {
                if (toRemove.contains(e.getUniqueID())) {
                    toRemoveEntity.add(e);
                }
            }
        }
        for (Entity e : toRemoveEntity) {
            e.world.removeEntity(e);
            e.die();
        }
    }

    /**
     * Add entities to chunk
     *
     * @param locX
     * @param locZ
     * @param chunk
     * @param entities
     * @return
     */
    private UUID[] addEntities(int locX, int locZ, Chunk chunk, ISerializedEntity[] entities) {
        final List<UUID> addedEntitiesUUID = new ArrayList<UUID>();
        final List<Entity> addedEntities = new ArrayList<Entity>();
        final World ws = chunk.world;
        
        //Insert new entities
        for (ISerializedEntity sEntity : entities) {
            Stack<SerializedEntity> stack = new Stack<SerializedEntity>();

            do {
                if (!(sEntity instanceof SerializedEntity)) {
                    log(String.format("Unsupported entity type: %1$s", sEntity != null ? sEntity.getClass().getCanonicalName() : "null"));
                    break;
                }

                stack.push((SerializedEntity) sEntity);
                sEntity = sEntity.getVehicle();
            } while (sEntity != null);

            Entity vehicle = null;
            Entity entity = null;
            while (!stack.isEmpty()) {
                vehicle = entity;

                SerializedEntity ssEntity = stack.pop();
                entity = ssEntity.getEntity(locX, locZ, chunk.world);
                if (entity == null) {
                    log("Unable to deserialise entity data.");
                    continue;
                }

                if (vehicle != null) {
                    entity.startRiding(vehicle);
                    //entity.vehicle = vehicle;
                    //vehicle.passenger = entity;
                }
                
                chunk.a(entity);
                
                addedEntitiesUUID.add(entity.getUniqueID());
                addedEntities.add(entity);
            }
        }

        chunk.world.a(addedEntities);
        
        return addedEntitiesUUID.toArray(new UUID[0]);
    }

    @Override
    protected boolean isValid(org.bukkit.Chunk chunk) {
        if (!s_initialized) {
            return false;
        }

        return super.isValid(chunk);
    }

    @Override
    public void initLighting() {
        final CraftChunk bukkitChunk = getChunk();

        Chunk chunk = bukkitChunk != null ? bukkitChunk.getHandle() : null;
        if (chunk != null) {
            chunk.initLighting();
        }
    }

    @Override
    public void updateLight(int x, int y, int z) {
        if (x < 0 || x > 15 || y < 0 || y > 255 || z < 0 || z > 15) {
            return;
        }

        final CraftChunk bukkitChunk = getChunk();
        final CraftWorld bukkitWorld = bukkitChunk != null ? ((CraftWorld) bukkitChunk.getWorld()) : null;
        final World world = bukkitWorld != null ? bukkitWorld.getHandle() : null;

        if (world != null) {
            BlockPosition pos = new BlockPosition(
                    PositionHelper.chunkToPosition(m_cx) + x,
                    y,
                    PositionHelper.chunkToPosition(m_cz) + z
            );
            world.w(pos);
        }
    }

    @Override
    public void flush() {
        final CraftChunk bukkitChunk = getChunk();

        Chunk chunk = bukkitChunk.getHandle();
        chunk.mustSave = true;

        double view = PositionHelper.chunkToPosition(Bukkit.getServer().getViewDistance());
        double x = PositionHelper.chunkToPosition(bukkitChunk.getX());
        double z = PositionHelper.chunkToPosition(bukkitChunk.getZ());

        WorldServer world = (WorldServer) (bukkitChunk.getHandle().world);
        EntityTracker tracker = world.getTracker();

        List<EntityTrackerEntry> trackerEntries = new ArrayList<EntityTrackerEntry>();
        final List<Packet> updatePackages = new ArrayList<Packet>();

        for (List<Entity> entitySlice : chunk.getEntitySlices()) {
            for (Entity entity : entitySlice) {
                int id = entity.getId();

                EntityTrackerEntry trackerEntry = tracker.trackedEntities.get(id);
                if (trackerEntry == null) {
                    tracker.track(entity);
                    trackerEntry = tracker.trackedEntities.get(id);
                }

                if (trackerEntry != null) {
                    trackerEntries.add(trackerEntry);
                }
            }
        }

        updatePackages.add(new PacketPlayOutMapChunk(chunk, 0xff00));
        updatePackages.add(new PacketPlayOutMapChunk(chunk, 0x00ff));
        for (TileEntity te : new LinkedList<TileEntity>(chunk.getTileEntities().values())) {
            Packet p = te.getUpdatePacket();
            if (p != null) {
                updatePackages.add(p);
            }
        }

        for (EntityHuman eh : (List<EntityHuman>) world.players) {
            if (!(eh instanceof EntityPlayer)) {
                continue;
            }

            double diffx = Math.abs(eh.locX - x);
            double diffz = Math.abs(eh.locZ - z);

            EntityPlayer ep = (EntityPlayer) eh;
            if (diffx <= view && diffz <= view) {
                PlayerConnection pc = ep.playerConnection;

                for (Packet p : updatePackages) {
                    pc.sendPacket(p);
                }
            }

            for (EntityTrackerEntry trackerEntry : trackerEntries) {
                trackerEntry.clear(ep);
            }
            tracker.a(ep, chunk);
        }
    }

    @Override
    public void setDirty() {
        final CraftChunk bukkitChunk = getChunk();

        Chunk chunk = bukkitChunk.getHandle();
        chunk.mustSave = true;
    }
    
    

    @Override
    public void sendChunkUpdate() {
        final CraftChunk bukkitChunk = getChunk();

        Chunk chunk = bukkitChunk.getHandle();

        double view = PositionHelper.chunkToPosition(Bukkit.getServer().getViewDistance());
        double x = PositionHelper.chunkToPosition(bukkitChunk.getX());
        double z = PositionHelper.chunkToPosition(bukkitChunk.getZ());

        WorldServer world = (WorldServer) (bukkitChunk.getHandle().world);
        final List<Packet> updatePackages = new ArrayList<Packet>();

        updatePackages.add(new PacketPlayOutMapChunk(chunk, 0xff00));
        updatePackages.add(new PacketPlayOutMapChunk(chunk, 0x00ff));

        for (EntityHuman eh : (List<EntityHuman>) world.players) {
            if (!(eh instanceof EntityPlayer)) {
                continue;
            }

            double diffx = Math.abs(eh.locX - x);
            double diffz = Math.abs(eh.locZ - z);

            EntityPlayer ep = (EntityPlayer) eh;
            if (diffx <= view && diffz <= view) {
                PlayerConnection pc = ep.playerConnection;

                for (Packet p : updatePackages) {
                    pc.sendPacket(p);
                }
            }
        }
    }        
}
