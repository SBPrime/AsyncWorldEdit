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
package org.primesoft.asyncworldedit.worldedit.extent.clipboard.io;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.primesoft.asyncworldedit.utils.io.NbtHelper;

/**
 * This class was based on:
 * com.sk89q.worldedit.extent.clipboard.io.SchematicWriter
 * Write schematic file that contains biomes
 * 
 * @author SBPrime
 */
public class BiomeSchematicWriter implements ClipboardWriter {

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;

    /**
     * The stream
     */
    private final NBTOutputStream m_outputStream;

    public BiomeSchematicWriter(NBTOutputStream outputStream) {
        m_outputStream = outputStream;
    }

    @Override
    public void write(Clipboard clipboard, WorldData data) throws IOException {
        if (clipboard == null) {
            throw new IllegalArgumentException("Clipboard is null", new NullPointerException());
        }

        final Region region = clipboard.getRegion();
        if (region == null) {
            throw new IllegalArgumentException("No region found in clipboard");
        }

        final int w = region.getWidth();
        final int h = region.getHeight();
        final int l = region.getLength();

        if (w > MAX_SIZE) {
            throw new IllegalArgumentException("Width of region too large for a .schematic");
        }
        if (h > MAX_SIZE) {
            throw new IllegalArgumentException("Height of region too large for a .schematic");
        }
        if (l > MAX_SIZE) {
            throw new IllegalArgumentException("Length of region too large for a .schematic");
        }

        write(clipboard, w, h, l);
    }

    @Override
    public void close() throws IOException {
        m_outputStream.close();
    }

    /**
     * Perform the schematic write
     *
     * @param clipboard
     * @param w
     * @param h
     * @param l
     */
    private void write(Clipboard clipboard, int w, int h, int l) throws IOException {
        HashMap<String, Tag> root = new HashMap<String, Tag>();

        Vector min = clipboard.getMinimumPoint();

        writeMetadata(root, clipboard, min, w, h, l);
        writeBlocks(root, clipboard, min, w, h, l);
        writeBiomes(root, clipboard, min, w, l);
        writeEntities(root, clipboard);

        m_outputStream.writeNamedTag(FormatSchematic.TAG_ROOT, new CompoundTag(root));
    }

    /**
     * Write root metadata
     *
     * @param schematic
     * @param clipboard
     * @param min
     * @param w
     * @param h
     * @param l
     */
    private void writeMetadata(HashMap<String, Tag> root,
            Clipboard clipboard,
            Vector min, int w, int h, int l) {

        Vector offset = min.subtract(clipboard.getOrigin());

        root.put(FormatSchematic.TAG_WIDTH, new ShortTag((short) w));
        root.put(FormatSchematic.TAG_LENGTH, new ShortTag((short) l));
        root.put(FormatSchematic.TAG_HEIGHT, new ShortTag((short) h));

        root.put(FormatSchematic.TAG_MATERIALS, new StringTag(FormatSchematic.MATERIAL));
        root.put(FormatSchematic.TAG_ORIGIN_X, new IntTag(min.getBlockX()));
        root.put(FormatSchematic.TAG_ORIGIN_Y, new IntTag(min.getBlockY()));
        root.put(FormatSchematic.TAG_ORIGIN_Z, new IntTag(min.getBlockZ()));
        root.put(FormatSchematic.TAG_OFFSET_X, new IntTag(offset.getBlockX()));
        root.put(FormatSchematic.TAG_OFFSET_Y, new IntTag(offset.getBlockY()));
        root.put(FormatSchematic.TAG_OFFSET_Z, new IntTag(offset.getBlockZ()));
    }

    /**
     * Write the blocks
     *
     * @param root
     * @param clipboard
     * @param min
     * @param w
     * @param h
     * @param l
     */
    private void writeBiomes(HashMap<String, Tag> root,
            Clipboard clipboard,
            Vector min, int w, int l) {
        int[] biomeData = new int[w * l];

        final Region region = clipboard.getRegion();

        final HashSet<Integer> stored = new LinkedHashSet<Integer>();

        for (Vector point : region) {
            Vector relative = point.subtract(min);
            int x = relative.getBlockX();
            int z = relative.getBlockZ();
            int index = z * w + x;

            if (stored.contains(index)) {
                continue;
            }

            BaseBiome biome = clipboard.getBiome(point.toVector2D());
            biomeData[index] = (biome == null) ? 0 : biome.getId();
            
            stored.add(index);
        }

        root.put(FormatSchematic.TAG_BIOMES, new IntArrayTag(biomeData));
    }

    /**
     * Write the blocks
     *
     * @param root
     * @param clipboard
     * @param min
     * @param w
     * @param h
     * @param l
     */
    private void writeBlocks(HashMap<String, Tag> root,
            Clipboard clipboard,
            Vector min, int w, int h, int l) {
        byte[] blockIds = new byte[w * h * l];
        byte[] blockIdsAdd = null;
        byte[] blockData = new byte[w * h * l];
        List<Tag> tileEntities = new ArrayList<Tag>();

        final Region region = clipboard.getRegion();

        for (Vector point : region) {
            Vector relative = point.subtract(min);
            int x = relative.getBlockX();
            int y = relative.getBlockY();
            int z = relative.getBlockZ();

            int index = y * w * l + z * w + x;
            BaseBlock block = clipboard.getBlock(point);

            // Save 4096 IDs in an AddBlocks section
            if (block.getType() > 255) {
                if (blockIdsAdd == null) { // Lazily create section
                    blockIdsAdd = new byte[(blockIds.length >> 1) + 1];
                }

                blockIdsAdd[index >> 1] = (byte) (((index & 1) == 0)
                        ? blockIdsAdd[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                        : blockIdsAdd[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
            }

            blockIds[index] = (byte) block.getType();
            blockData[index] = (byte) block.getData();

            // Store TileEntity data
            CompoundTag rawTag = block.getNbtData();
            if (rawTag != null) {
                Map<String, Tag> values = new HashMap<String, Tag>();
                for (Map.Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                    values.put(entry.getKey(), entry.getValue());
                }

                values.put("id", new StringTag(block.getNbtId()));
                values.put("x", new IntTag(x));
                values.put("y", new IntTag(y));
                values.put("z", new IntTag(z));

                CompoundTag tileEntityTag = new CompoundTag(values);
                tileEntities.add(tileEntityTag);
            }
        }

        root.put(FormatSchematic.TAG_BLOCKS_ID, new ByteArrayTag(blockIds));
        root.put(FormatSchematic.TAG_BLOCKS_DATA, new ByteArrayTag(blockData));
        root.put(FormatSchematic.TAG_TILEENTITIES, new ListTag(CompoundTag.class, tileEntities));

        if (blockIdsAdd != null) {
            root.put(FormatSchematic.TAG_BLOCKS_IDEX, new ByteArrayTag(blockIdsAdd));
        }
    }

    /**
     * Write the entities
     *
     * @param root
     * @param clipboard
     */
    private void writeEntities(HashMap<String, Tag> root,
            Clipboard clipboard) {
        List<Tag> entities = new ArrayList<Tag>();
        for (Entity entity : clipboard.getEntities()) {
            BaseEntity state = entity.getState();

            if (state == null) {
                continue;
            }

            Map<String, Tag> nbt = new HashMap<String, Tag>();

            // Put NBT provided data
            CompoundTag rawTag = state.getNbtData();
            if (rawTag != null) {
                nbt.putAll(rawTag.getValue());
            }

            // Store WE specific data (overrides the NBT data)
            nbt.put(FormatSchematic.TAG_ID, new StringTag(state.getTypeId()));
            nbt.put(FormatSchematic.TAG_POSITION, NbtHelper.createVector(entity.getLocation().toVector()));
            nbt.put(FormatSchematic.TAG_ROTATION, NbtHelper.createRotation(entity.getLocation()));

            CompoundTag entityTag = new CompoundTag(nbt);
            entities.add(entityTag);
        }

        root.put(FormatSchematic.TAG_ENTITIES, new ListTag(CompoundTag.class, entities));
    }
}
