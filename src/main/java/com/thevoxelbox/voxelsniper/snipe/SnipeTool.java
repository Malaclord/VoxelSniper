package com.thevoxelbox.voxelsniper.snipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.thevoxelbox.voxelsniper.bukkit.VoxelMessage;
import com.thevoxelbox.voxelsniper.brush.IBrush;
import com.thevoxelbox.voxelsniper.brush.SnipeBrush;
import com.thevoxelbox.voxelsniper.voxelsniper.material.IMaterial;
import com.thevoxelbox.voxelsniper.voxelsniper.material.VoxelMaterial;
import org.bukkit.Material;

/**
 *
 * @author ervinnnc
 */
public class SnipeTool {

    Class<? extends IBrush> currentBrush;
    VoxelMessage messageHelper;
    ClassToInstanceMap<IBrush> brushes = MutableClassToInstanceMap.create();
    private Class<? extends IBrush> previousBrush;
    private final BiMap<SnipeAction, VoxelMaterial> actionTools = HashBiMap.create();
    SnipeData snipeData;

    protected SnipeTool(Sniper owner) {
        this(SnipeBrush.class, new SnipeData(owner));
    }

    protected SnipeTool(Class<? extends IBrush> currentBrush, SnipeData snipeData) {
        this.snipeData = snipeData;
        messageHelper = new VoxelMessage(snipeData);
        snipeData.setVoxelMessage(messageHelper);

        IBrush newBrushInstance = instanciateBrush(currentBrush);
        if (snipeData.owner().getPlayer().hasPermission(newBrushInstance.getPermissionNode())) {
            brushes.put(currentBrush, newBrushInstance);
            this.currentBrush = currentBrush;
        }
    }

    public IBrush getCurrentBrush() {
        if (currentBrush == null) {
            return null;
        }
        return brushes.getInstance(currentBrush);
    }

    public SnipeData getSnipeData() {
        return snipeData;
    }

    public boolean hasToolAssigned(VoxelMaterial material) {
        return actionTools.containsValue(material);
    }

    public VoxelMessage getMessageHelper() {
        return messageHelper;
    }

    public IBrush previousBrush() {
        if (previousBrush == null) {
            return null;
        }
        return setCurrentBrush(previousBrush);
    }

    public void unassignAction(VoxelMaterial itemInHand) {
        actionTools.inverse().remove(itemInHand);
    }

    public IBrush setCurrentBrush(Class<? extends IBrush> brush) {
        Preconditions.checkNotNull(brush, "Can't set brush to null.");
        IBrush brushInstance = brushes.get(brush);
        if (brushInstance == null) {
            brushInstance = instanciateBrush(brush);
            Preconditions.checkNotNull(brushInstance, "Could not instanciate brush class.");
            if (snipeData.owner().getPlayer().hasPermission(brushInstance.getPermissionNode())) {
                brushes.put(brush, brushInstance);
                previousBrush = currentBrush;
                currentBrush = brush;
                return brushInstance;
            }
        }
        if (snipeData.owner().getPlayer().hasPermission(brushInstance.getPermissionNode())) {
            previousBrush = currentBrush;
            currentBrush = brush;
            return brushInstance;
        }
        return null;
    }

    public SnipeAction getActionAssigned(VoxelMaterial itemInHand) {
        return actionTools.inverse().get(itemInHand);
    }

    public VoxelMaterial getToolAssigned(SnipeAction action) {
        return actionTools.get(action);
    }

    public BiMap<SnipeAction, VoxelMaterial> getActionTools() {
        return ImmutableBiMap.copyOf(actionTools);
    }

    IBrush instanciateBrush(Class<? extends IBrush> brush) {
        try {
            return brush.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    public void assignAction(SnipeAction action, VoxelMaterial itemInHand) {
        actionTools.forcePut(action, itemInHand);
    }

}
