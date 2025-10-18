package minecraftai;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EnvironmentData {

    private final NPC npc;
    private final Player player;

    public EnvironmentData(NPC npc, Player player) {
        this.npc = npc;
        this.player = player;
    }

    // === NPC Info ===
    public Location getNPCLocation() {
        return npc.isSpawned() ? npc.getEntity().getLocation() : null;
    }

    public double getNPCHealth() {
        if (npc.getEntity() instanceof LivingEntity living) {
            return living.getHealth();
        }
        return 0;
    }

    public BlockFace getNPCFacingDirection() {
        return npc.getEntity().getFacing();
    }

    public float getNPCFallDistance() {
        return npc.getEntity().getFallDistance();
    }

    public boolean isNPCOnFire() {
        return npc.getEntity().getFireTicks() > 0;
    }

    public boolean isNPCInLava() {
        if (npc.getEntity().getLocation().clone().add(0, -1, 0).getBlock().getType().name().contains("LAVA"))
            return true;
        return false;
    }

    public boolean isNPCInWater() {
        String type = npc.getEntity().getLocation().clone().add(0, -1, 0).getBlock().getType().name();
        return type.contains("WATER");
    }

    // === Player Info ===
    public Location getPlayerLocation() {
        return player.getLocation();
    }

    public double getPlayerHealth() {
        return player.getHealth();
    }

    public BlockFace getPlayerFacingDirection() {
        return player.getFacing();
    }

    public boolean isPlayerSneaking() {
        return player.isSneaking();
    }

    public boolean isPlayerSprinting() {
        return player.isSprinting();
    }

    public boolean isPlayerInCombatRange(double range) {
        return getDistanceBetween() <= range;
    }

    // === Relationship Info ===
    public double getDistanceBetween() {
        if (!npc.isSpawned()) return Double.MAX_VALUE;
        return npc.getEntity().getLocation().distance(player.getLocation());
    }

    public boolean canSeeEachOther() {
        Entity npcEntity = npc.getEntity();
        if (!(npcEntity instanceof LivingEntity living)) return false;
        return living.hasLineOfSight(player);
    }

    // === Summary ===
    public String getSummary() {
        return """
                --- Environment Data ---
                NPC Health: %.1f
                Player Health: %.1f
                Distance Between: %.2f
                NPC Facing: %s
                Player Facing: %s
                NPC On Fire: %b
                NPC In Lava: %b
                NPC In Water: %b
                Player Sneaking: %b
                Player Sprinting: %b
                Line of Sight: %b
                """.formatted(
                getNPCHealth(),
                getPlayerHealth(),
                getDistanceBetween(),
                getNPCFacingDirection(),
                getPlayerFacingDirection(),
                isNPCOnFire(),
                isNPCInLava(),
                isNPCInWater(),
                isPlayerSneaking(),
                isPlayerSprinting(),
                canSeeEachOther()
        );
    }
}
