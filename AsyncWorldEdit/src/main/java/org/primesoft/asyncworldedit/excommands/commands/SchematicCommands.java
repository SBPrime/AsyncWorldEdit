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
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;
import org.primesoft.asyncworldedit.injector.classfactory.IJob;
import org.primesoft.asyncworldedit.injector.classfactory.IJobProcessor;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;
import org.primesoft.asyncworldedit.injector.injected.command.FlattenedClipboardTransformFactory;
import org.primesoft.asyncworldedit.injector.injected.command.IFlattenedClipboardTransform;
import org.primesoft.asyncworldedit.injector.injected.function.operation.IForwardExtentCopy;

/**
 * The AsyncWorldEdit schematic injected commands
 *
 * @author SBPrime
 */
public class SchematicCommands {
/*
    private static final Logger log = Logger.getLogger(com.sk89q.worldedit.command.SchematicCommands.class.getCanonicalName());

    /**
     * Instance of WorldEdit
     */
/*    private final WorldEdit m_worldEdit;

    /**
     * The job processor
     */
/*    private final IJobProcessor m_jobProcessor;

    /**
     * The AsyncWorldEdit
     */
/*    private final IAsyncWorldEdit m_asyncWorldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     * @param awe the AsyncWorldEdit
     */
/*    public SchematicCommands(WorldEdit worldEdit, IAsyncWorldEdit awe) {
        if (worldEdit == null) {
            throw new NullPointerException("worldEdit");
        }

        m_asyncWorldEdit = awe;
        m_jobProcessor = InjectorCore.getInstance().getClassFactory().getJobProcessor();
        m_worldEdit = worldEdit;
    }

    @Command(
            aliases = {"load"},
            usage = "[<format>] <filename>",
            desc = "Load a schematic into your clipboard",
            min = 1, max = 2
    )
    @CommandPermissions({"worldedit.clipboard.load", "worldedit.schematic.load"})
    public void load(Player player, LocalSession session, @Optional("sponge") String formatName, String filename) throws FilenameException {
        LocalConfiguration config = m_worldEdit.getConfiguration();

        final File dir = m_worldEdit.getWorkingDirectoryFile(config.saveDir);
        final File f = m_worldEdit.getSafeOpenFile(player, dir, filename, "schematic", "schematic");

        if (!f.exists()) {
            player.printError("Schematic " + filename + " does not exist!");
            return;
        }

        ClipboardFormat tmpFormat = ClipboardFormats.findByFile(f);
        if (tmpFormat == null) {
            tmpFormat = ClipboardFormats.findByAlias(formatName);
        }
        final ClipboardFormat format = tmpFormat;
        if (format == null) {
            player.printError("Unknown schematic format: " + formatName);
            return;
        }

        m_jobProcessor.executeJob(player, new IJob() {
            @Override
            public String getName() {
                return "loadSchematic";
            }

            @Override
            public void execute() {
                try (Closer closer = Closer.create()) {
                    FileInputStream fis = closer.register(new FileInputStream(f));
                    BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                    ClipboardReader reader = closer.register(format.getReader(bis));

                    Clipboard clipboard = reader.read();
                    session.setClipboard(new ClipboardHolder(clipboard));

                    log.info(player.getName() + " loaded " + f.getCanonicalPath());
                    player.print(filename + " loaded. Paste it with //paste");
                } catch (IOException e) {
                    player.printError("Schematic could not read or it does not exist: " + e.getMessage());
                    log.log(Level.WARNING, "Failed to load a saved clipboard", e);
                }
            }
        });
    }

    @Command(
            aliases = { "save" },
            usage = "[<format>] <filename>",
            desc = "Save a schematic into your clipboard",
            min = 1, max = 2
    )
    @CommandPermissions({ "worldedit.clipboard.save", "worldedit.schematic.save" })
    public void save(Player player, LocalSession session, @Optional("sponge") String formatName, String filename) throws CommandException, WorldEditException {
        LocalConfiguration config = m_worldEdit.getConfiguration();

        File dir = m_worldEdit.getWorkingDirectoryFile(config.saveDir);
        final File f = m_worldEdit.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

        ClipboardFormat tmpFormat = ClipboardFormats.findByFile(f);
        if (tmpFormat == null) {
            tmpFormat = ClipboardFormats.findByAlias(formatName);
        }
        final ClipboardFormat format = tmpFormat;
        if (format == null) {
            player.printError("Unknown schematic format: " + formatName);
            return;
        }

        final ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();
        Transform transform = holder.getTransform();
        final Clipboard target;

        // If we have a transform, bake it into the copy
        if (!transform.isIdentity()) {
            IFlattenedClipboardTransform result = FlattenedClipboardTransformFactory.transform(clipboard, transform);
            target = new BlockArrayClipboard(result.getTransformedRegion());
            target.setOrigin(clipboard.getOrigin());

            Operation copyOperation = result.copyTo(target);

            if (copyOperation instanceof IForwardExtentCopy) {
                ((IForwardExtentCopy) copyOperation).setBiomeCopy(true);
            }

            Operations.completeLegacy(copyOperation);
        } else {
            target = clipboard;
        }

        m_jobProcessor.executeJob(player, new IJob() {
            @Override
            public String getName() {
                return "saveSchematic";
            }

            @Override
            public void execute() {
                Closer closer = Closer.create();
                try {
                    // Create parent directories
                    File parent = f.getParentFile();
                    if (parent != null && !parent.exists()) {
                        if (!parent.mkdirs()) {
                            log.info("Could not create folder for schematics!");
                            return;
                        }
                    }

                    FileOutputStream fos = closer.register(new FileOutputStream(f));
                    BufferedOutputStream bos = closer.register(new BufferedOutputStream(fos));
                    ClipboardWriter writer = closer.register(format.getWriter(bos));
                    writer.write(target);
                    log.info(player.getName() + " saved " + f.getCanonicalPath());
                    player.print(filename + " saved.");
                } catch (IOException e) {
                    player.printError("Schematic could not written: " + e.getMessage());
                    log.log(Level.WARNING, "Failed to write a saved clipboard", e);
                } finally {
                    try {
                        closer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }*/
}
