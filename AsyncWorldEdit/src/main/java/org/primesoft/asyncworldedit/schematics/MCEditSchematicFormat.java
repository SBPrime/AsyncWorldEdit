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
package org.primesoft.asyncworldedit.schematics;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.AweEditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.registry.WorldData;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.primesoft.asyncworldedit.api.worldedit.IAweEditSession;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.OrientationTransform;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 *
 * @author SBPrime
 */
public class MCEditSchematicFormat implements ISchematicReader {

    private final static String TAG_MATERIALS = "Materials";
    private final static String TAG_WIDTH = "Width";
    private final static String TAG_HEIGHT = "Height";
    private final static String TAG_LENGTH = "Length";
    private final static String TAG_ORIGIN_X = "WEOriginX";
    private final static String TAG_ORIGIN_Y = "WEOriginY";
    private final static String TAG_ORIGIN_Z = "WEOriginZ";
    private final static String TAG_BLOCKS = "Blocks";
    private final static String TAG_BLOCKS_A = "AddBlocks";
    private final static String TAG_TILE_ENTITIES = "TileEntities";
    private final static String TAG_ENTITIES = "Entities";
    private final static String TAG_DATA = "Data";

    private static <T extends Tag> boolean tryGetTag(Map<String, Tag> items, String key, Class<T> cls, InOutParam<T> out) {
        if (!items.containsKey(key)) {
            return false;
        }

        Tag tag = items.get(key);
        if (!cls.isInstance(tag)) {
            return false;
        }

        out.setValue(cls.cast(tag));
        return true;
    }

    @Override
    public SchematicData readData(InputStream is) throws IOException {
        NBTInputStream inputStream = new NBTInputStream(new GZIPInputStream(is));

        // Schematic tag
        NamedTag rootTag = inputStream.readNamedTag();
        if (!rootTag.getName().equals("Schematic")) {
            throw new IOException("Invalid file format. Expected 'Schematic' tag.");
        }
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        Map<String, Tag> schematic = schematicTag.getValue();

        InOutParam<ByteArrayTag> blocks = InOutParam.Out();
        InOutParam<ByteArrayTag> blocksAdd = InOutParam.Out();
        InOutParam<ByteArrayTag> data = InOutParam.Out();
        InOutParam<ListTag> tileEntities = InOutParam.Out();
        InOutParam<ListTag> entities = InOutParam.Out();

        InOutParam<StringTag> materialsValue = InOutParam.Out();
        InOutParam<ShortTag> wValue = InOutParam.Out();
        InOutParam<ShortTag> hValue = InOutParam.Out();
        InOutParam<ShortTag> lValue = InOutParam.Out();

        InOutParam<IntTag> xOrigin = InOutParam.Out();
        InOutParam<IntTag> yOrigin = InOutParam.Out();
        InOutParam<IntTag> zOrigin = InOutParam.Out();

        tryGetTag(schematic, TAG_BLOCKS, ByteArrayTag.class, blocks);
        tryGetTag(schematic, TAG_BLOCKS_A, ByteArrayTag.class, blocksAdd);
        tryGetTag(schematic, TAG_DATA, ByteArrayTag.class, data);
        tryGetTag(schematic, TAG_TILE_ENTITIES, ListTag.class, tileEntities);
        tryGetTag(schematic, TAG_ENTITIES, ListTag.class, entities);

        tryGetTag(schematic, TAG_MATERIALS, StringTag.class, materialsValue);

        tryGetTag(schematic, TAG_ORIGIN_X, IntTag.class, xOrigin);
        tryGetTag(schematic, TAG_ORIGIN_Y, IntTag.class, yOrigin);
        tryGetTag(schematic, TAG_ORIGIN_Z, IntTag.class, zOrigin);

        tryGetTag(schematic, TAG_WIDTH, ShortTag.class, wValue);
        tryGetTag(schematic, TAG_HEIGHT, ShortTag.class, hValue);
        tryGetTag(schematic, TAG_LENGTH, ShortTag.class, lValue);

        BlockVector origin;
        if (!xOrigin.isSet() || !yOrigin.isSet() || !zOrigin.isSet()) {
            origin = null;
        } else {
            origin = new BlockVector(xOrigin.getValue().getValue(),
                    yOrigin.getValue().getValue(), zOrigin.getValue().getValue());
        }

        SchematicData result = new SchematicData(materialsValue.isSet() ? materialsValue.getValue().getValue() : "<unknown>",
                wValue.isSet() ? wValue.getValue().getValue() : null,
                hValue.isSet() ? hValue.getValue().getValue() : null,
                lValue.isSet() ? lValue.getValue().getValue() : null,
                origin);

        if (blocks.isSet()) {
            result.setBlocks(blocks.getValue().getValue());
        }
        if (blocksAdd.isSet()) {
            result.setBlocksEx(blocksAdd.getValue().getValue());
        }
        if (data.isSet()) {
            result.setData(data.getValue().getValue());
        }
        if (tileEntities.isSet()) {
            result.setTileEntities(tileEntities.getValue().getValue());
        }
        if (entities.isSet() && origin != null) {
            result.setEntities(entities.getValue().getValue());
        }

        return result;
    }

    @Override
    public int place(WorldData worldData, BlockVector to, OrientationTransform transform, boolean ignoreAirBlocks,
            SchematicData data, IAweEditSession editSesstion) throws MaxChangedBlocksException {
        if (data == null || !data.isValid()) {
            return 0;
        }

        if (editSesstion == null) {
            throw new NullPointerException("editSession");
        }

        final short width = data.getW();
        final short height = data.getH();
        final short length = data.getL();

        to = to.add(transform.calc(1 - width, 0, 1)).toBlockVector();
        int cnt = 0;

        cnt += placeBlocks(worldData, data, height, length, width, ignoreAirBlocks, to, transform, editSesstion);
        cnt += placeEntities(data, to, transform, editSesstion);

        return cnt;
    }

    private int placeEntities(SchematicData data,
            BlockVector to, OrientationTransform transform, IAweEditSession editSesstion) {
        int cnt = 0;
        BlockVector origin = data.getOrigin();
        if (origin == null) {
            origin = new BlockVector(0, 0, 0);
        }

        double ox = origin.getBlockX() + 0.5;
        double oy = origin.getBlockY() + 0.5;
        double oz = origin.getBlockZ() + 0.5;

        for (Tag tag : data.getEntities()) {
            if (tag instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag) tag;
                String id = ct.getString("id");

                if (id.isEmpty()) {
                    continue;
                }

                ListTag positionTag = ct.getListTag("Pos");
                ListTag rot = ct.getListTag("Rotation");
                double x = positionTag.asDouble(0) - ox;
                double y = positionTag.asDouble(1) - oy;
                double z = positionTag.asDouble(2) - oz;
                float yaw = (float) transform.transformRotation(rot.asDouble(0));
                float pitch = (float) rot.asDouble(1);

                Vector translated = transform.calc(x, y, z);
                Vector position = to.add(translated).add(0.5, 0.5, 0.5);
                Location location = new Location(editSesstion.getWorld(), position, yaw, pitch);

                if (!id.isEmpty()) {
                    editSesstion.createEntity(location, new BaseEntity(id, fixNBT(ct, position, yaw)));
                }

                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Place blocks on the map
     *
     * @param worldData
     * @param data
     * @param height
     * @param length
     * @param width
     * @param ignoreAirBlocks
     * @param to
     * @param transform
     * @param editSesstion
     * @return
     * @throws MaxChangedBlocksException
     */
    private int placeBlocks(WorldData worldData, SchematicData data,
            final short height, final short length, final short width,
            boolean ignoreAirBlocks,
            BlockVector to, OrientationTransform transform, IAweEditSession editSesstion) throws MaxChangedBlocksException {

        final byte[] blockData = data.getData();
        final short[] blocks = calculateIds(data);
        final HashMap<Integer, HashMap<Integer, HashMap<Integer, CompoundTag>>> tileEntties
                = mapTileEntites(data);

        int cnt = 0;
        int index = 0;
        for (int y = 0; y < height; y++) {
            final HashMap<Integer, HashMap<Integer, CompoundTag>> zMap = tileEntties.get(y);

            for (int z = 0; z < length; z++) {
                final HashMap<Integer, CompoundTag> xMap = zMap != null ? zMap.get(z) : null;

                for (int x = 0; x < width; x++) {
                    short id = blocks[index];
                    short d = blockData[index];

                    if (id != 0 || !ignoreAirBlocks) {
                        BaseBlock block = new BaseBlock(id, d);
                        BlockVector pos = to.add(transform.calc(x, y, z)).toBlockVector();

                        if (xMap != null && xMap.containsKey(x)) {
                            CompoundTag ct = xMap.get(x);
                            Map<String, Tag> map = ct.getValue();
                            HashMap<String, Tag> result = new HashMap<String, Tag>(map);
                            result.put("x", new IntTag(pos.getBlockX()));
                            result.put("y", new IntTag(pos.getBlockY()));
                            result.put("z", new IntTag(pos.getBlockZ()));

                            //We can not reuse the previous NBT value!
                            block.setNbtData(new CompoundTag(result));
                        }
                        block = transform.transform(block, worldData);
                        editSesstion.setBlock(pos, block);
                        cnt++;
                    }
                    index++;
                }
            }
        }
        return cnt;
    }

    private short[] calculateIds(SchematicData data) {
        final byte[] blockId = data.getBlocks();
        final byte[] blockIdEx = data.getBlocksAdd();
        final short[] blocks = new short[blockId.length];
        //Combine the ID and IDEx data.
        for (int index = 0; index < blockId.length; index++) {
            final int exIndex = index >> 1;
            int id = (short) (blockId[index] & 0xFF);

            if (exIndex < blockIdEx.length) {
                if ((index & 1) == 0) {
                    id = id | (short) ((blockIdEx[exIndex] & 0x0F) << 8);
                } else {
                    id = id | (short) ((blockIdEx[exIndex] & 0xF0) << 4);
                }
            }

            blocks[index] = (short) id;
        }

        return blocks;
    }

    private HashMap<Integer, HashMap<Integer, HashMap<Integer, CompoundTag>>> mapTileEntites(SchematicData data) {
        HashMap<Integer, HashMap<Integer, HashMap<Integer, CompoundTag>>> yMap
                = new HashMap<Integer, HashMap<Integer, HashMap<Integer, CompoundTag>>>();
        for (Tag tag : data.getTileEntities()) {
            if (!(tag instanceof CompoundTag)) {
                continue;
            }

            CompoundTag ct = (CompoundTag) tag;

            int y = ct.getInt("y");
            int z = ct.getInt("z");
            int x = ct.getInt("x");

            HashMap<Integer, HashMap<Integer, CompoundTag>> zMap;
            if (yMap.containsKey(y)) {
                zMap = yMap.get(y);
            } else {
                zMap = new HashMap<Integer, HashMap<Integer, CompoundTag>>();
                yMap.put(y, zMap);
            }

            HashMap<Integer, CompoundTag> xMap;
            if (zMap.containsKey(z)) {
                xMap = zMap.get(z);
            } else {
                xMap = new HashMap<Integer, CompoundTag>();
                zMap.put(z, xMap);
            }

            xMap.put(x, ct);
        }

        return yMap;
    }

    /**
     * Correct the NBT data
     *
     * @param ct
     */
    private static CompoundTag fixNBT(CompoundTag nbt, Vector pos, float yaw) {
        double px = pos.getX();
        double py = pos.getY();
        double pz = pos.getZ();

        Map<String, Tag> values = new HashMap<String, Tag>(nbt.getValue());

        if (values.containsKey("Pos")) {
            final DoubleTag[] position = new DoubleTag[]{
                new DoubleTag(px),
                new DoubleTag(py),
                new DoubleTag(pz)
            };

            values.put("Pos", new ListTag(DoubleTag.class, Arrays.asList(position)));
        }

        //Fix tile
        if (values.containsKey("xTile")) {
            values.put("xTile", new ShortTag((short) PositionHelper.positionToBlockPosition(px)));
        }
        if (values.containsKey("yTile")) {
            values.put("yTile", new ShortTag((short) PositionHelper.positionToBlockPosition(py)));
        }
        if (values.containsKey("zTile")) {
            values.put("zTile", new ShortTag((short) PositionHelper.positionToBlockPosition(pz)));
        }
        
        if (values.containsKey("TileX")) {
            values.put("TileX", new ShortTag((short) PositionHelper.positionToBlockPosition(px)));
        }
        if (values.containsKey("TileY")) {
            values.put("TileY", new ShortTag((short) PositionHelper.positionToBlockPosition(py)));
        }
        if (values.containsKey("TileZ")) {
            values.put("TileZ", new ShortTag((short) PositionHelper.positionToBlockPosition(pz)));
        }

        //Set the new YAW & PITCH
        if (values.containsKey("Rotation")) {
            ListTag rotation = (ListTag) values.get("Rotation");
            float pitch = rotation.getFloat(1);

            values.put("Rotation", new ListTag(FloatTag.class, Arrays.asList(new FloatTag[]{
                new FloatTag(yaw), new FloatTag(pitch)
            })));
        }

        if (values.containsKey("Facing")) {
            byte oldFacing = nbt.getByte("Facing");
            byte newFacing = (byte) (yaw / 90);

            if (oldFacing != newFacing) {
                values.put("Facing", new ByteTag(newFacing));
            }
        }

        boolean hasDirection = values.containsKey("Direction");
        boolean hasDir = values.containsKey("Dir");
        if (hasDir || hasDirection) {
            byte mcDirection = (byte) MCDirections.toHanging(findDirection(yaw));

            if (hasDirection) {
                values.put("Direction", new ByteTag(mcDirection));
            }
            if (hasDir) {
                values.put("Dir", new ByteTag(MCDirections.toLegacyHanging(mcDirection)));
            }
        }

        return new CompoundTag(values);
    }

    /**
     * Get the Direction for yaw
     *
     * @param yaw
     * @return
     */
    private static Direction findDirection(float yaw) {
        switch ((int) yaw) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.WEST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
            default:
                return Direction.SOUTH;
        }
    }
}
