/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thevoxelbox.voxelsniper.brush.perform;

import com.thevoxelbox.voxelsniper.bukkit.VoxelMessage;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

/**
 * @author Voxel
 */
public class pComboComboNoPhysics extends vPerformer {

    private BlockData voxelSubstance;
    private BlockData targetSubstance;

    public pComboComboNoPhysics() {
        name = "Combo-Combo, No Physics";
    }

    @Override
    public void init(com.thevoxelbox.voxelsniper.snipe.SnipeData v) {
        w = v.getWorld();
        voxelSubstance = v.getVoxelSubstance();
        targetSubstance = v.getReplaceSubstance();
    }

    @Override
    public void info(VoxelMessage vm) {
        vm.performerName(name);
        vm.voxel();
        vm.replace();
        vm.data();
        vm.replaceData();
    }

    @Override
    public void perform(Block b) {
        if (b.getBlockData().matches(targetSubstance)) {
            h.put(b);
            b.setBlockData(voxelSubstance, false);
        }
    }

    @Override
    public boolean isUsingReplaceMaterial() {
        return true;
    }
}
