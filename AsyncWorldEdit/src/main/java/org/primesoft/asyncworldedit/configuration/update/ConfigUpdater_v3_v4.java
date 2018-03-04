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
package org.primesoft.asyncworldedit.configuration.update;

import java.util.ArrayList;
import java.util.List;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.platform.api.IConfiguration;
import org.primesoft.asyncworldedit.platform.api.IConfigurationSection;

/**
 *
 * @author SBPrime
 */
class ConfigUpdater_v3_v4 extends BaseConfigurationUpdater {

    private final static String[] s_knownOperations = new String[]{
        "undo", "redo", "fillXZ", "removeAbove", "removeBelow", "removeNear",
        "setBlocks", "replaceBlocks", "makeCuboidFaces", "makeCuboidWalls",
        "overlayCuboidBlocks", "naturalizeCuboidBlocks", "stackCuboidRegion",
        "moveCuboidRegion", "drainArea", "fixLiquid", "makeCylinder",
        "makeSphere", "makePyramid", "thaw", "simulateSnow", "green",
        "makePumpkinPatches", "makeForest", "makeShape", "deformRegion",
        "hollowOutRegion", "paste", "copy", "cut", "regenerate", "center",
        "drawLine", "drawSpline", "makeBiomeShape", "forest", "flora", "setBiome",
        "loadSchematic", "saveSchematic", "craftScript", "makeFaces", "makeWalls",
        "overlayBlocks", "naturalizeBlocks", "stackRegion", "moveRegion"
    };

    @Override
    public int updateConfig(IConfiguration config) {
        log("Updating configuration v3 --> v4");

        IConfigurationSection mainSection = config.getConfigurationSection("awe");
        if (mainSection == null) {
            return -1;
        }

        mainSection.set("version", 4);
        List<String> enabledOperations = mainSection.getStringList("enabledOperations");
        List<String> disabledOperations = new ArrayList<String>();

        for (String s : s_knownOperations) {
            if (!enabledOperations.contains(s)) {
                disabledOperations.add(s);
            }
        }
        
        mainSection.set("enabledOperations", null);
        mainSection.set("disabledOperations", disabledOperations);
        
        return 4;
    }
}