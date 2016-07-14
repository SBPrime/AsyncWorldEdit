/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
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
package org.primesoft.asyncworldedit.injector.async;

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.injector.validators.OperationValidator;
import org.primesoft.asyncworldedit.injector.validators.StackValidator;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.injector.classfactory.IOperationProcessor;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.injector.scanner.ClassScannerResult;
import org.primesoft.asyncworldedit.injector.utils.ExceptionOperationAction;
import org.primesoft.asyncworldedit.injector.utils.OperationAction;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.Pair;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.utils.WaitFor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author SBPrime
 */
public class AsyncOperationProcessor implements IOperationProcessor {

    /**
     * Bukkit schedule
     */
    private final BukkitScheduler m_schedule;

    /**
     * The parent plugin
     */
    private final AsyncWorldEditBukkit m_plugin;

    /**
     * Async block placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * The class scanner
     */
    private final ClassScanner m_classScanner;

    public AsyncOperationProcessor(AsyncWorldEditBukkit plugin, ClassScanner classScanner) {
        m_plugin = plugin;
        m_schedule = m_plugin.getServer().getScheduler();
        m_blockPlacer = m_plugin.getBlockPlacer();
        m_classScanner = classScanner;
    }

    @Override
    public <TException extends Exception> void process(final Operation op,
            final ExceptionOperationAction<TException> action) throws TException {
        InOutParam<String> operationName = InOutParam.Out();

        if (!StackValidator.isVaild(operationName) || !OperationValidator.isValid(op)) {
            action.Execute(op);
            return;
        }

        /**
         * What to do if scanner finds multiple different edit sessions?
         */
        List<ClassScannerResult> sessions = m_classScanner.scan(new Class<?>[]{AsyncEditSession.class, Region.class}, op);
        if (!validate(sessions)) {
            action.Execute(op);
            return;
        }

        final AsyncEditSession asyncSession = getFirst(AsyncEditSession.class, sessions);
        final String name = operationName.getValue();
        if (!asyncSession.checkAsync(name)) {
            action.Execute(op);
            return;
        }

        final WaitFor wait = asyncSession.getWait();
        final IPlayerEntry playerEntry = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerEntry,
                name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        try {
                            wait.checkAndWait(null);
                            action.Execute(op);

                            return cancelableSession.getChangeSet().size();
                        } catch (IllegalArgumentException ex) {
                            if (ex.getCause() instanceof SessionCanceled) {
                                m_player.say(MessageType.BLOCK_PLACER_CANCELED.format());
                            } else {
                                ExceptionHelper.printException(ex, String.format("Error while processing async operation %1$s", name));
                            }
                            return 0;
                        } catch (Exception ex) {
                            if (ex instanceof MaxChangedBlocksException) {
                                throw (MaxChangedBlocksException) ex;
                            }

                            ExceptionHelper.printException(ex, String.format("Error while processing async operation %1$s", name));
                            //Silently discard other errors :(
                            return 0;
                        }
                    }
                });
    }

    @Override
    public void process(final Operation op, final OperationAction action) {
        InOutParam<String> operationName = InOutParam.Out();

        if (!StackValidator.isVaild(operationName) || !OperationValidator.isValid(op)) {
            action.Execute(op);
            return;
        }

        /**
         * What to do if scanner finds multiple different edit sessions?
         */
        List<ClassScannerResult> sessions = m_classScanner.scan(new Class<?>[]{AsyncEditSession.class, Region.class}, op);
        if (!validate(sessions)) {
            action.Execute(op);
            return;
        }

        final AsyncEditSession asyncSession = getFirst(AsyncEditSession.class, sessions);
        final String name = operationName.getValue();

        if (!asyncSession.checkAsync(name)) {
            action.Execute(op);
            return;
        }

        final WaitFor wait = asyncSession.getWait();
        final IPlayerEntry playerEntry = asyncSession.getPlayer();
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);
        m_schedule.runTaskAsynchronously(m_plugin, new AsyncTask(cancelableSession, playerEntry,
                name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        wait.checkAndWait(null);
                        action.Execute(op);

                        return cancelableSession.getChangeSet().size();
                    }
                });
    }

    /**
     * Validate the edit sessions
     *
     * @param sessions
     * @return
     */
    private boolean validate(List<ClassScannerResult> sessions) {
        boolean debugOn = ConfigProvider.isDebugOn();

        AsyncEditSession session = null;

        if (debugOn) {
            log("****************************************************************");
            log("* Validating scann results");
            log("****************************************************************");
        }

        if (sessions.isEmpty()) {
            if (debugOn) {
                log("* No entries");
            }
            return false;
        }

        final Class<?> aweClass = AsyncEditSession.class;
        for (ClassScannerResult entry : sessions) {
            if (entry.getType() == aweClass) {
                AsyncEditSession s = (AsyncEditSession) entry.getValue();

                if (session == null) {
                    session = s;
                    if (debugOn) {
                        log("* Found EditSession");
                    }
                } else if (session != s) {
                    if (debugOn) {
                        log("* Found EditSessions do not match");
                    }
                    //We support only single edit session at this moment
                    return false;
                }
            }
        }

        if (debugOn) {
            if (session == null) {
                log("* No EditSession found");
            }
        }
        return session != null;
    }

    /**
     * Inject scanner results to operation
     *
     * @param entries
     * @param value
     */
    private void injectEditSession(List<ClassScannerResult> entries, Object value) {
        final Class<AsyncEditSession> aesClass = AsyncEditSession.class;
        final Class<Region> regionClass = Region.class;

        HashMap<Region, Pair<Region, List<ClassScannerResult>>> regions = new HashMap<Region, Pair<Region, List<ClassScannerResult>>>();

        boolean debugOn = ConfigProvider.isDebugOn();
        if (debugOn) {
            log("****************************************************************");
            log("* Injecting classes");
            log("****************************************************************");
        }

        for (ClassScannerResult entry : entries) {
            Class<?> type = entry.getType();
            Field field = entry.getField();
            Object parent = entry.getOwner();

            if (field == null || parent == null) {
                continue;
            }

            if (type == aesClass) {
                if (debugOn) {
                    log(String.format("* Injecting EditSession to %1$s %2$s", parent.getClass().getName(), field.getName()));
                }

                Reflection.set(parent, field, value, "edit session");
            } else if (regionClass.isAssignableFrom(type)) {
                if (debugOn) {
                    log("* Stored region entry ");
                }

                Region r = (Region) entry.getValue();
                List<ClassScannerResult> entriesList;
                if (!regions.containsKey(r)) {
                    entriesList = new ArrayList<ClassScannerResult>();
                    regions.put(r, new Pair<Region, List<ClassScannerResult>>(r.clone(), entriesList));
                } else {
                    entriesList = regions.get(r).getX2();
                }

                entriesList.add(entry);
            }
        }

        for (Pair<Region, List<ClassScannerResult>> rEntry : regions.values()) {
            Region region = rEntry.getX1();
            for (ClassScannerResult entry : rEntry.getX2()) {
                Class<?> type = entry.getType();
                Field field = entry.getField();
                Object parent = entry.getOwner();
                if (field == null || parent == null) {
                    continue;
                }
                if (debugOn) {
                    log(String.format("* Injecting Region to %1$s %2$s", parent.getClass().getName(), field.getName()));
                }

                Reflection.set(parent, field, region, "region");
            }
        }
    }

    private <T> T getFirst(Class<T> type, List<ClassScannerResult> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        for (ClassScannerResult entry : list) {
            if (entry.getType() == type) {
                return (T) entry.getValue();
            }
        }

        return null;
    }
}
