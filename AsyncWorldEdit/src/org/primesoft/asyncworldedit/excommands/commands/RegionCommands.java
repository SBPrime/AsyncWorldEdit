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
package org.primesoft.asyncworldedit.excommands.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.ORIENTATION_REGION;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.buffer.ForgetfulExtentBuffer;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.command.binding.Range;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;
import java.lang.reflect.Constructor;
import java.util.Collection;
import org.primesoft.asyncworldedit.LoggerProvider;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.injector.classfactory.IEditSessionJob;
import org.primesoft.asyncworldedit.injector.classfactory.IJobProcessor;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.function.RegionMaskingFilterEx;
import org.primesoft.asyncworldedit.worldedit.function.block.KeepDataBlockReplace;
import org.primesoft.asyncworldedit.worldedit.function.mask.SkipDataBlockMask;

/**
 *
 * @author SBPrime
 */
public class RegionCommands {
    private final  static Constructor s_ctorHeightMapFilter;
    
    static {
        Class<?> cls = HeightMapFilter.class;
        Constructor[] ctors = cls.getDeclaredConstructors();
        if (ctors == null) {
            ctors = new Constructor[0];
        }
        
        final Collection<Class<?>> classes = Reflection.scanHierarchy(GaussianKernel.class);        
        Constructor foundCtor = null;
        for (Constructor ctor : ctors) {
            if (ctor.getParameterCount() != 1) {
                continue;
            }
            
            Class[] params = ctor.getParameterTypes();
            if (params == null || params.length != 1) {
                //Should not happen, but better safe then sorry.
                continue;
            }

            for (Class<?> c : classes) {
                if (params[0] == c) {
                    foundCtor = ctor;
                    break;
                }
            }
            
            if (foundCtor != null) {
                break;
            }
        }
        
        s_ctorHeightMapFilter = foundCtor;        
        if (s_ctorHeightMapFilter == null) {
            LoggerProvider.log("Error: Unable to find constructor for HeightMapFilter. Smooth operation disabled.");
        }
    }    

    /**
     * Instance of WorldEdit
     */
    private final WorldEdit m_worldEdit;

    /**
     * The job processor
     */
    private final IJobProcessor m_jobProcessor;

    /**
     * The AsyncWorldEdit
     */
    private final IAsyncWorldEdit m_asyncWorldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     * @param awe the AsyncWorldEdit
     */
    public RegionCommands(WorldEdit worldEdit, IAsyncWorldEdit awe) {
        if (worldEdit == null) {
            throw new NullPointerException("worldEdit");
        }

        m_asyncWorldEdit = awe;
        m_jobProcessor = InjectorCore.getInstance().getClassFactory().getJobProcessor();
        m_worldEdit = worldEdit;
    }

    @Command(
            aliases = {"/replacend", "/rend", "/repnd"},
            usage = "[from-block] <to-block>",
            desc = "Replace all blocks in the selection with another and keep the data",
            flags = "",
            min = 1,
            max = 2
    )
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public void replaceBlocks(Player player, EditSession editSession, @Selection Region region, @Optional Mask from, Pattern to) throws WorldEditException {
        if (from == null) {
            from = new ExistingBlockMask(editSession);
        }
        else if (from instanceof BlockMask) {
            from = new SkipDataBlockMask((BlockMask)from);
        }
        
        RegionFunction replace = new KeepDataBlockReplace(editSession, to);
        RegionFunction filter = new RegionMaskingFilterEx(from, replace);
        RegionVisitor visitor = new RegionVisitor(region, filter);
        
        Operations.completeLegacy(visitor);
        
        int affected = visitor.getAffected();
                
        player.print(affected + " block(s) have been replaced.");        
    }

    @Command(
        aliases = { "/smooth" },
        usage = "[iterations]",
        flags = "n",
        desc = "Smooth the elevation in the selection",
        help =
            "Smooths the elevation in the selection.\n" +
            "The -n flag makes it only consider naturally occuring blocks.",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.region.smooth")
    @Logging(REGION)
    public void smooth(final Player player, EditSession editSession, @Selection final Region region, @Optional("1") final int iterations, @Switch('n') final boolean affectNatural) throws WorldEditException {
        if (s_ctorHeightMapFilter == null) {   
            LoggerProvider.log("Error: smooth operation disabled.");
            return;
        }
        
        InjectorCore.getInstance().getClassFactory().getJobProcessor().executeJob(player, editSession, new IEditSessionJob() {
            @Override
            public String getName() {
                return "smooth";
            }

            @Override
            public void execute(EditSession es) {
                try {
                    HeightMap heightMap = new HeightMap(es, region, affectNatural);
                    GaussianKernel kernel = new GaussianKernel(5, 1.0);                    
                                        
                    HeightMapFilter filter = Reflection.create(HeightMapFilter.class, s_ctorHeightMapFilter, "Unable to create the HeightMapFilter.", kernel);

                    int affected = heightMap.applyFilter(filter, iterations);
                    player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");
                    
                    es.flushQueue();
                } catch (WorldEditException ex) {
                    player.printError("Error while executing CraftScript.");
                }
            }
        });
    }
    
    
    @Command(
        aliases = { "/stack" },
        usage = "[count] [direction]",
        flags = "sa",
        desc = "Repeat the contents of the selection",
        help =
            "Repeats the contents of the selection.\n" +
            "Flags:\n" +
            "  -s shifts the selection to the last stacked copy\n" +
            "  -m sets a source mask so that excluded blocks become air\n" +
            "  -a skips air blocks",
        min = 0,
        max = 2
    )        
    @CommandPermissions("worldedit.region.stack")
    @Logging(ORIENTATION_REGION)
    public void stack(Player player, EditSession editSession, LocalSession session,
                      @Selection Region region,
                      @Optional("1") @Range(min = 1) int count,
                      @Optional(Direction.AIM) @Direction Vector direction,
                      @Switch('s') boolean moveSelection,
                      @Switch('a') boolean ignoreAirBlocks,
                      @Switch('m') Mask mask) throws WorldEditException {        
        final Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        final Vector to = region.getMinimumPoint();
        
        final ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, editSession, to);
        copy.setRepetitions(count);
        copy.setTransform(new AffineTransform().translate(direction.multiply(size)));
        
        if (mask != null && ignoreAirBlocks) {
            copy.setSourceMask(new MaskUnion(mask, new ExistingBlockMask(editSession)));
        } else if (mask != null) {
            copy.setSourceMask(mask);
        } else if (ignoreAirBlocks) {
            copy.setSourceMask(new ExistingBlockMask(editSession));
        }
        Operations.completeLegacy(copy);
        
        
        int affected = copy.getAffected();

        if (moveSelection) {
            try {
                final Vector ss = region.getMaximumPoint().subtract(region.getMinimumPoint());

                final Vector shiftVector = direction.multiply(count * (Math.abs(direction.dot(ss)) + 1));
                region.shift(shiftVector);

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks changed. Undo with //undo");
    }
    
    @Command(
        aliases = { "/move" },
        usage = "[count] [direction] [leave-id]",
        flags = "sa",
        desc = "Move the contents of the selection",
        help =
            "Moves the contents of the selection.\n" +
            "Flags:\n" +
            "  -s flag shifts the selection to the target location.\n" +
            "  -m sets a source mask so that excluded blocks become air\n" +
            "  -a skips air blocks\n" +
            "Optionally fills the old location with <leave-id>.",
        min = 0,
        max = 3
    )
    @CommandPermissions("worldedit.region.move")
    @Logging(ORIENTATION_REGION)
    public void move(Player player, EditSession editSession, LocalSession session,
                     @Selection Region region,
                     @Optional("1") @Range(min = 1) int count,
                     @Optional(Direction.AIM) @Direction Vector direction,
                     @Optional("air") BaseBlock replace,
                     @Switch('s') boolean moveSelection,
                     @Switch('a') boolean ignoreAirBlocks,
                     @Switch('m') Mask mask) throws WorldEditException {
        Vector to = region.getMinimumPoint();

        // Remove the original blocks
        com.sk89q.worldedit.function.pattern.Pattern pattern = replace != null ?
                new BlockPattern(replace) :
                new BlockPattern(new BaseBlock(BlockID.AIR));
        BlockReplace remove = new BlockReplace(editSession, pattern);

        // Copy to a buffer so we don't destroy our original before we can copy all the blocks from it
        ForgetfulExtentBuffer buffer = new ForgetfulExtentBuffer(editSession, new RegionMask(region));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, buffer, to);
        copy.setTransform(new AffineTransform().translate(direction.multiply(count)));
        
        if (mask == null) {
            copy.setSourceFunction(remove); // Remove            
        } else {
            copy.setSourceFunction(new RegionMaskingFilter(mask, remove));
        }
        
        copy.setRemovingEntities(true);
        
        if (mask != null && ignoreAirBlocks) {
            copy.setSourceMask(new MaskUnion(mask, new ExistingBlockMask(editSession)));
        } else if (mask != null) {
            copy.setSourceMask(mask);
        } else if (ignoreAirBlocks) {
            copy.setSourceMask(new ExistingBlockMask(editSession));
        }
        
        // Then we need to copy the buffer to the world
        BlockReplace bReplace = new BlockReplace(editSession, buffer);
        RegionVisitor visitor = new RegionVisitor(buffer.asRegion(), bReplace);

        OperationQueue operation = new OperationQueue(copy, visitor);
        Operations.completeLegacy(operation);

        int affected = copy.getAffected();
        

        if (moveSelection) {
            try {
                region.shift(direction.multiply(count));

                session.getRegionSelector(player.getWorld()).learnChanges();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks moved.");
    }
}
