/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.async;

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.injector.validators.OperationValidator;
import org.primesoft.asyncworldedit.injector.validators.StackValidator;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.regions.Region;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.injector.classfactory.IOperationProcessor;
import org.primesoft.asyncworldedit.injector.scanner.ClassScanner;
import org.primesoft.asyncworldedit.injector.scanner.ClassScannerResult;
import org.primesoft.asyncworldedit.injector.utils.ExceptionOperationAction;
import org.primesoft.asyncworldedit.injector.utils.OperationAction;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.utils.Pair;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;
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
    private final IScheduler m_schedule;

    /**
     * The AWE core
     */
    private final IAsyncWorldEditCore m_aweCore;

    /**
     * Async block placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * The class scanner
     */
    private final ClassScanner m_classScanner;

    public AsyncOperationProcessor(IAsyncWorldEditCore aweCore) {
        m_aweCore = aweCore;
        m_schedule = aweCore.getPlatform().getScheduler();
        m_blockPlacer = m_aweCore.getBlockPlacer();
        m_classScanner = m_aweCore.getPlatform().getClasScanner();
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
        final IPlayerEntry playerEntry = asyncSession.getPlayer();
        
        if (!asyncSession.checkAsync(name)) {
            try {
                action.Execute(op);
            } catch (Exception ex) {
                ErrorHandler.handleError(playerEntry, name, null, ex);
            }
            return;
        }

        final WaitFor wait = asyncSession.getWait();        
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule, new AsyncTask(cancelableSession, playerEntry,
                name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        try {
                            wait.checkAndWait(null);
                            action.Execute(op);

                            return session.getChangeSet().size();
                        } catch (Exception ex) {
                            return ErrorHandler.handleError(playerEntry, name, session, ex);
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
        final IPlayerEntry playerEntry = asyncSession.getPlayer();

        if (!asyncSession.checkAsync(name)) {
            try {
                action.Execute(op);
            } catch (Exception ex) {
                ErrorHandler.handleError(playerEntry, name, null, ex);
            }
            return;
        }

        final WaitFor wait = asyncSession.getWait();        
        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final CancelabeEditSession cancelableSession = new CancelabeEditSession(asyncSession, asyncSession.getMask(), jobId);
        final JobEntry job = new JobEntry(playerEntry, cancelableSession, jobId, name);

        injectEditSession(sessions, cancelableSession);

        m_blockPlacer.addJob(playerEntry, job);

        SchedulerUtils.runTaskAsynchronously(m_schedule,
                new AsyncTask(cancelableSession, playerEntry,
                        name, m_blockPlacer, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        try {
                            wait.checkAndWait(null);
                            action.Execute(op);

                            return cancelableSession.getChangeSet().size();
                        } catch (Exception ex) {
                            return ErrorHandler.handleError(playerEntry, name, session, ex);
                        }
                    }
                }
        );
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
        return session
                != null;
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

        for (Pair<Region, List<ClassScannerResult>> rEntry
                : regions.values()) {
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
