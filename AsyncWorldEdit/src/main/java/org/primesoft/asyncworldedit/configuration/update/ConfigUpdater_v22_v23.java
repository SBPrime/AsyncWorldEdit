/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.configuration.update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
class ConfigUpdater_v22_v23 extends BaseConfigurationUpdater {

    public ConfigUpdater_v22_v23() {
    }

    @Override
    public int updateConfig(IConfiguration config) {
        log("Updating configuration v22 --> v23");

        IConfigurationSection mainSection = config.getConfigurationSection("awe");
        if (mainSection == null) {
            return -1;
        }
        
        updateClassScanner(mainSection);        
        updatePhysicsFreeze(mainSection);
        setIfNone(getOrCreate(mainSection, "rendering"), "bps-avg-data-points", 5);
        
        mainSection.set("version", 23);

        return 23;
    }

    private void updateClassScanner(IConfigurationSection mainSection) {
        IConfigurationSection classScanner = getOrCreate(mainSection, "classScanner");
        IConfigurationSection blackList = getOrCreate(classScanner, "blackList");
        
        blackList.set("net\\.minecraft\\..*", new ArrayList<>());
        blackList.set("org\\.bukkit\\..*", new ArrayList<>());
        blackList.set("org\\.spigotmc\\..*", new ArrayList<>());
        blackList.set("io\\.netty\\..*", new ArrayList<>());
        blackList.set("com\\.sk89q\\.worldedit\\.function\\.mask\\.BlockMask", Stream.of("blocks").collect(Collectors.toList()));
        blackList.set("com\\.sk89q\\.worldedit\\.extent\\.reorder\\.ChunkBatchingExtent", Stream.of("batches").collect(Collectors.toList()));
    }

    private void updatePhysicsFreeze(IConfigurationSection mainSection) {
            
        boolean physicsFreez = getAndRemoveBoolean(mainSection, "physicsFreez", true);
    
        IConfigurationSection physicsFreeze = getOrCreate(mainSection, "physicsFreeze");
        
        setIfNone(physicsFreeze, "enabled", physicsFreez);
        List<String> entries = physicsFreeze.getStringList("blackList");
        if (entries == null) {
            entries = new ArrayList<>();
        }
        
        for (String s : new String[]{"minecraft:acacia_stairs",
            "minecraft:andesite_stairs",
            "minecraft:birch_stairs",
            "minecraft:brick_stairs",
            "minecraft:cobblestone_stairs",
            "minecraft:dark_oak_stairs",
            "minecraft:dark_prismarine_stairs",
            "minecraft:diorite_stairs",
            "minecraft:end_stone_brick_stairs",
            "minecraft:granite_stairs",
            "minecraft:jungle_stairs",
            "minecraft:mossy_stone_brick_stairs",
            "minecraft:nether_brick_stairs",
            "minecraft:oak_stairs",
            "minecraft:polished_andesite_stairs",
            "minecraft:polished_diorite_stairs",
            "minecraft:polished_granite_stairs",
            "minecraft:prismarine_brick_stairs",
            "minecraft:prismarine_stairs",
            "minecraft:purpur_stairs",
            "minecraft:quartz_stairs",
            "minecraft:red_nether_brick_stairs",
            "minecraft:red_sandstone_stairs",
            "minecraft:sandstone_stairs",
            "minecraft:smooth_quartz_stairs",
            "minecraft:smooth_red_sandstone_stairs",
            "minecraft:smooth_sandstone_stairs",
            "minecraft:spruce_stairs",
            "minecraft:stone_brick_stairs",
            "minecraft:stone_stairs"}) {
            addToList(entries, s);
        }                
        
        physicsFreeze.set("blackList", entries);
    }    
}
