/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.FilenameException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.MaxRadiusException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.SessionCheck;
import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public class WorldEditProxy extends WorldEdit {

    /**
     * The edit session factory
     */
    private AsyncEditSessionFactory m_factory;

    /**
     * The world edit plugin
     */
    private WorldEditPlugin m_plugin;

    /**
     * World edit parent
     */
    private WorldEdit m_parent;

    public WorldEditProxy(WorldEdit we) {
        super(we.getServer(), we.getConfiguration());
    }

    public void initialize(WorldEditPlugin plugin, WorldEdit we, AsyncEditSessionFactory factory) {
        m_factory = factory;
        m_plugin = plugin;
        m_parent = we;
    }

    @Override
    public void checkMaxBrushRadius(double radius) throws MaxBrushRadiusException {
        m_parent.checkMaxBrushRadius(radius); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void checkMaxRadius(double radius) throws MaxRadiusException {
        m_parent.checkMaxRadius(radius); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearSessions() {
        m_parent.clearSessions(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] commandDetection(String[] split) {
        return m_parent.commandDetection(split); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flushBlockBag(LocalPlayer player, EditSession editSession) {
        m_parent.flushBlockBag(player, editSession); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flushExpiredSessions(SessionCheck checker) {
        m_parent.flushExpiredSessions(checker); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void forgetPlayer(LocalPlayer player) {
        m_parent.forgetPlayer(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseBlock getBlock(LocalPlayer player, String id) throws UnknownItemException, DisallowedItemException {
        return m_parent.getBlock(player, id); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed) throws UnknownItemException, DisallowedItemException {
        return m_parent.getBlock(player, arg, allAllowed); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed, boolean allowNoData) throws UnknownItemException, DisallowedItemException {
        return m_parent.getBlock(player, arg, allAllowed, allowNoData); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Integer> getBlockIDs(LocalPlayer player, String list, boolean allBlocksAllowed) throws UnknownItemException, DisallowedItemException {
        return m_parent.getBlockIDs(player, list, allBlocksAllowed); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mask getBlockMask(LocalPlayer player, LocalSession session, String maskString) throws WorldEditException {
        return m_parent.getBlockMask(player, session, maskString); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pattern getBlockPattern(LocalPlayer player, String patternString) throws UnknownItemException, DisallowedItemException {
        return m_parent.getBlockPattern(player, patternString); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list) throws DisallowedItemException, UnknownItemException {
        return m_parent.getBlocks(player, list); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed) throws DisallowedItemException, UnknownItemException {
        return m_parent.getBlocks(player, list, allAllowed); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed, boolean allowNoData) throws DisallowedItemException, UnknownItemException {
        return m_parent.getBlocks(player, list, allAllowed, allowNoData); //To change body of generated methods, choose Tools | Templates.
    }

    public Logger getCommandLogger() {
        return commandLogger;
    }

    @Override
    public Map<String, String> getCommands() {
        return m_parent.getCommands(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CommandsManager<LocalPlayer> getCommandsManager() {
        return m_parent.getCommandsManager(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return m_parent.getConfiguration(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector getDiagonalDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
        return m_parent.getDiagonalDirection(player, dirStr); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Vector getDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
        return m_parent.getDirection(player, dirStr); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CuboidClipboard.FlipDirection getFlipDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
        return m_parent.getFlipDirection(player, dirStr); //To change body of generated methods, choose Tools | Templates.
    }

    public static Logger getLogger() {
        return logger;
    }

    @Override
    public int getMaximumPolygonalPoints(LocalPlayer player) {
        return m_parent.getMaximumPolygonalPoints(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaximumPolyhedronPoints(LocalPlayer player) {
        return m_parent.getMaximumPolyhedronPoints(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getSafeOpenFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
        return m_parent.getSafeOpenFile(player, dir, filename, defaultExt, extensions); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getSafeSaveFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
        return m_parent.getSafeSaveFile(player, dir, filename, defaultExt, extensions); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServerInterface getServer() {
        return m_parent.getServer(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalSession getSession(LocalPlayer player) {
        return m_parent.getSession(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocalSession getSession(String player) {
        return m_parent.getSession(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public File getWorkingDirectoryFile(String path) {
        return m_parent.getWorkingDirectoryFile(path); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleArmSwing(LocalPlayer player) {
        return m_parent.handleArmSwing(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleBlockLeftClick(LocalPlayer player, WorldVector clicked) {
        return m_parent.handleBlockLeftClick(player, clicked); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleBlockRightClick(LocalPlayer player, WorldVector clicked) {
        return m_parent.handleBlockRightClick(player, clicked); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleDisconnect(LocalPlayer player) {
        m_parent.handleDisconnect(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleRightClick(LocalPlayer player) {
        return m_parent.handleRightClick(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasSession(LocalPlayer player) {
        return m_parent.hasSession(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void markExpire(LocalPlayer player) {
        m_parent.markExpire(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSession(LocalPlayer player) {
        m_parent.removeSession(player); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void runScript(LocalPlayer player, File f, String[] args) throws WorldEditException {
        m_parent.runScript(player, f, args); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EditSessionFactory getEditSessionFactory() {
        return m_factory;
    }

    @Override
    public void setEditSessionFactory(EditSessionFactory factory) {
        PluginMain.log("AWE WorldEdit wrapper active, edit session set to " + factory.getClass().getName() + " canceled.");
    }

    @Override
    public boolean handleCommand(LocalPlayer player, String[] split) {

        if (player instanceof BukkitPlayer) {
            player = new BukkitPlayerWrapper(m_plugin, getServer(), (BukkitPlayer) player);
        }

        return m_parent.handleCommand(player, split);
    }
}
