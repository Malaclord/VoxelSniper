package com.github.kevindagame.voxelsniperforge;

import com.github.kevindagame.VoxelProfileManager;
import com.github.kevindagame.snipe.Sniper;
import com.github.kevindagame.voxelsniper.block.BlockFace;
import com.github.kevindagame.voxelsniper.block.IBlock;
import com.github.kevindagame.voxelsniper.entity.player.IPlayer;
import com.github.kevindagame.voxelsniper.material.VoxelMaterial;
import com.github.kevindagame.voxelsniperforge.permissions.ForgePermissionManager;
import com.github.kevindagame.voxelsniperforge.location.ForgeLocation;
import com.github.kevindagame.voxelsniperforge.world.ForgeWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author KevinDaGame
 */
public class VoxelSniperListener {

    private static final String SNIPER_PERMISSION = "voxelsniper.sniper";
    private final VoxelSniperForge main;
    private final List<UUID> cooldown = new ArrayList<>();

    /**
     * @param main The main
     */
    public VoxelSniperListener(final VoxelSniperForge main) {
        this.main = main;
    }

    @SubscribeEvent
    public final void onPermissionGatherEvent(final PermissionGatherEvent.Nodes event) {
        ForgePermissionManager.register(event);
    }

    /**
     * @param event PlayerInteractEvent
     */
    @SubscribeEvent
    public final void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getSide() == LogicalSide.CLIENT) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        IPlayer player = main.getPlayer(event.getEntity().getUUID());
        assert player != null;
        if (!player.hasPermission(SNIPER_PERMISSION)) return;
        if (cooldown.contains(player.getUniqueId())) return;
        try {
            Sniper sniper = VoxelProfileManager.getInstance().getSniperForPlayer(player);
            var action = getAction(event);
            if (sniper.isEnabled() && action != null && sniper.snipe(
                    action,
                    getItemStack(event),
                    getClickedBlock(event),
                    getBlockFace(event))) {
                event.setCanceled(true);
                cooldown.add(player.getUniqueId());
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cooldown.remove(player.getUniqueId());
                }).start();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private BlockFace getBlockFace(PlayerInteractEvent event) {
        var face = event.getFace();
        if (face == null) return null;
        return BlockFace.valueOf(face.name());
    }

    private @Nullable
    IBlock getClickedBlock(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.LeftClickBlock || event instanceof PlayerInteractEvent.RightClickBlock) {
            var world = new ForgeWorld(((ServerLevel) event.getLevel()));
            var pos = ForgeLocation.fromForgeBlockPos(world, event.getPos());
            return world.getBlock(pos);

        }
        return null;
    }

    private @Nullable
    VoxelMaterial getItemStack(PlayerInteractEvent event) {
        var itemKey = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (itemKey == null) return null;
        return new VoxelMaterial(itemKey.getNamespace(), itemKey.getPath());
    }

    private Sniper.Action getAction(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            return Sniper.Action.LEFT_CLICK_BLOCK;
        }
        if (event instanceof PlayerInteractEvent.RightClickBlock) {
            return Sniper.Action.RIGHT_CLICK_BLOCK;
        }
        if (event instanceof PlayerInteractEvent.LeftClickEmpty) {
            return Sniper.Action.LEFT_CLICK_AIR;
        }
        if (event instanceof PlayerInteractEvent.RightClickEmpty) {
            return Sniper.Action.RIGHT_CLICK_AIR;
        }
        throw new IllegalArgumentException("Unknown action type");

    }

    /**
     * @param event PlayerJoinEvent
     */
    @SubscribeEvent
    public final void onPlayerJoin(final PlayerEvent.PlayerLoggedInEvent event) {
        IPlayer player = main.getPlayer(event.getEntity().getUUID());
        assert player != null;
        Sniper sniper = VoxelProfileManager.getInstance().getSniperForPlayer(player);

        if (player.hasPermission(SNIPER_PERMISSION) && main.getVoxelSniperConfiguration().isMessageOnLoginEnabled()) {
            sniper.displayInfo();
        }
    }

    @SubscribeEvent
    public final void onPlayerLeave(final PlayerEvent.PlayerLoggedOutEvent event) {
        cooldown.remove(event.getEntity().getUUID());
    }
}
