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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import com.sk89q.worldedit.world.storage.NBTConversions;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primesoft.asyncworldedit.utils.io.NbtHelper;

/**
 * This class was based on:
 * com.sk89q.worldedit.extent.clipboard.io.SchematicReader Read schematic file
 * that contains biomes
 *
 * @author SBPrime
 */
public class BiomeSchematicReader implements ClipboardReader {

    private final NBTInputStream m_inputStream;

    public BiomeSchematicReader(NBTInputStream inputStream) {
        m_inputStream = inputStream;
    }

    @Override
    public Clipboard read(WorldData data) throws IOException {
        final NamedTag rootTag = m_inputStream.readNamedTag();
        if (!rootTag.getName().equals(FormatSchematic.TAG_ROOT)) {
            throw new IOException(String.format("Tag '%1$s' does not exist or is not first", FormatSchematic.TAG_ROOT));
        }

        final CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey(FormatSchematic.TAG_BLOCKS_ID)) {
            throw new IOException(String.format("Schematic file is missing a '%1$s' tag", FormatSchematic.TAG_BLOCKS_ID));
        }

        // Check type of Schematic
        String materials = NbtHelper.getString(schematic, FormatSchematic.TAG_MATERIALS);
        if (!materials.equals(FormatSchematic.MATERIAL)) {
            throw new IOException(String.format("Expected schematic format: %1$s, got: %2$s.", FormatSchematic.MATERIAL, materials));
        }

        // Get information
        short width = NbtHelper.getShort(schematic, FormatSchematic.TAG_WIDTH);
        short height = NbtHelper.getShort(schematic, FormatSchematic.TAG_HEIGHT);
        short length = NbtHelper.getShort(schematic, FormatSchematic.TAG_LENGTH);

        int originX = NbtHelper.getInt(schematic, FormatSchematic.TAG_ORIGIN_X, 0);
        int originY = NbtHelper.getInt(schematic, FormatSchematic.TAG_ORIGIN_Y, 0);
        int originZ = NbtHelper.getInt(schematic, FormatSchematic.TAG_ORIGIN_Z, 0);

        int offsetX = NbtHelper.getInt(schematic, FormatSchematic.TAG_OFFSET_X, 0);
        int offsetY = NbtHelper.getInt(schematic, FormatSchematic.TAG_OFFSET_Y, 0);
        int offsetZ = NbtHelper.getInt(schematic, FormatSchematic.TAG_OFFSET_Z, 0);

        final Vector offset = new Vector(offsetX, offsetY, offsetZ);
        final Vector min = new Vector(originX, originY, originZ);
        final Vector origin = min.subtract(offset);
        final Region region = new CuboidRegion(min, min.add(width, height, length).subtract(Vector.ONE));

        Clipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        loadBlocks(schematic, clipboard, min, width, height, length);
        loadBiomes(schematic, clipboard, min, width, length);
        loadEntities(schematic, clipboard);

        return clipboard;
    }

    /**
     * Load entities and add them to clipboard
     * @param schematic
     * @param clipboard
     * @throws IOException 
     */
    private void loadEntities(Map<String, Tag> schematic, Clipboard clipboard) throws IOException {
        List<Tag> entityTags = NbtHelper.getList(schematic, FormatSchematic.TAG_ENTITIES, null);
        if (entityTags == null) {
            return;
        }

        for (Tag tag : entityTags) {
            if (tag instanceof CompoundTag) {
                CompoundTag compound = (CompoundTag) tag;
                String id = compound.getString(FormatSchematic.TAG_ID);
                Location location = NBTConversions.toLocation(clipboard,
                        compound.getListTag(FormatSchematic.TAG_POSITION),
                        compound.getListTag(FormatSchematic.TAG_ROTATION));

                if (!id.isEmpty()) {
                    BaseEntity state = new BaseEntity(id, compound);
                    clipboard.createEntity(location, state);
                }
            }
        }
    }

    /**
     * Load biomes from schematic (id exist)
     * @param schematic
     * @param clipboard
     * @param min
     * @param width
     * @param length
     * @throws IOException 
     */
    private void loadBiomes(final Map<String, Tag> schematic, final Clipboard clipboard,
            final Vector min,
            final int width, final int length) throws IOException {
        final int[] biomeData = NbtHelper.getIntArray(schematic, FormatSchematic.TAG_BIOMES, null);                
        if (biomeData == null) {
            return;
        }
        
        final Vector2D min2d = min.toVector2D();
        
        for (int x = 0; x < width; ++x) {
            for (int z = 0; z < length; ++z) {
                int index = z * width + x;
                BlockVector2D pt = new BlockVector2D(x, z);
                BaseBiome biome = new BaseBiome(biomeData[index]);

                clipboard.setBiome(min2d.add(pt), biome);                
            }
        }
    }

    /**
     * Load blocks and place blocks on clipboard
     * @param schematic
     * @param clipboard
     * @param min
     * @param width
     * @param height
     * @param length
     * @throws IOException 
     */
    private void loadBlocks(final Map<String, Tag> schematic, final Clipboard clipboard,
            final Vector min,
            final int width, final int height, final int length) throws IOException {
        final byte[] blockData = NbtHelper.getByteArray(schematic, FormatSchematic.TAG_BLOCKS_DATA);
        final short[] blocks = loadBlockIds(schematic);
        final Map<BlockVector, Map<String, Tag>> tileEntitiesMap = loadTileEntities(schematic);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BlockVector pt = new BlockVector(x, y, z);
                    BaseBlock block = new BaseBlock(blocks[index], blockData[index]);

                    if (tileEntitiesMap.containsKey(pt)) {
                        block.setNbtData(new CompoundTag(tileEntitiesMap.get(pt)));
                    }

                    try {
                        clipboard.setBlock(min.add(pt), block);
                    } catch (WorldEditException e) {
                        //We ignore the errors
                    }
                }
            }
        }
    }

    /**
     * Load block IDs
     * @param schematic
     * @return
     * @throws IOException 
     */
    private short[] loadBlockIds(Map<String, Tag> schematic) throws IOException {
        // Get blocks
        final byte[] blockId = NbtHelper.getByteArray(schematic, FormatSchematic.TAG_BLOCKS_ID);
        final byte[] blockIdEx = NbtHelper.getByteArray(schematic, FormatSchematic.TAG_BLOCKS_IDEX, new byte[0]);

        return combineIds(blockId, blockIdEx);
    }

    /**
     * Combine ID and extended ID
     *
     * @param blockId
     * @param blockIdEx
     * @return
     */
    private short[] combineIds(byte[] blockId, byte[] blockIdEx) {
        final short[] result = new short[blockId.length];

        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= blockIdEx.length) {
                result[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    result[index] = (short) (((blockIdEx[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    result[index] = (short) (((blockIdEx[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        return result;
    }

    /**
     * Load tile entities
     *
     * @param schematic
     * @return
     * @throws IOException
     */
    private Map<BlockVector, Map<String, Tag>> loadTileEntities(Map<String, Tag> schematic) throws IOException {
        final List<Tag> result = NbtHelper.getList(schematic, FormatSchematic.TAG_TILEENTITIES);
        final Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<BlockVector, Map<String, Tag>>();

        for (Tag tag : result) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }
            CompoundTag t = (CompoundTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, Tag> values = new HashMap<String, Tag>();

            for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                if (entry.getKey().equals(FormatSchematic.TAG_X)) {
                    if (entry.getValue() instanceof IntTag) {
                        x = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals(FormatSchematic.TAG_Y)) {
                    if (entry.getValue() instanceof IntTag) {
                        y = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals(FormatSchematic.TAG_Z)) {
                    if (entry.getValue() instanceof IntTag) {
                        z = ((IntTag) entry.getValue()).getValue();
                    }
                }

                values.put(entry.getKey(), entry.getValue());
            }

            BlockVector vec = new BlockVector(x, y, z);
            tileEntitiesMap.put(vec, values);
        }

        return tileEntitiesMap;
    }
}
