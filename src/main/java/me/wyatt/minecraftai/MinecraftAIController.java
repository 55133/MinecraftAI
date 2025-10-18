package me.wyatt.minecraftai;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class MinecraftAIController {

//    private final Player player;
//    private final JavaPlugin plugin;
//    private final Location spawnLoc;
//    private ArmorStand npc;
//
//    public MinecraftAIController(Player player, Location spawnLoc, JavaPlugin plugin) {
//        this.player = player;
//        this.spawnLoc = spawnLoc;
//        this.plugin = plugin;
//    }
//
//    public void spawn() {
//        npc = (ArmorStand) player.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
//
//        npc.customName(Component.text("MinecraftAI").color(net.kyori.adventure.text.format.NamedTextColor.RED));
//        npc.setCustomNameVisible(true);
//        npc.setArms(true);
//        npc.setBasePlate(false);
//        npc.setGravity(true);
//        npc.setInvisible(false);
//
//        // Simple follow loop
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (npc == null || !player.isOnline()) {
//                    cancel();
//                    return;
//                }
//
//                Location playerLoc = player.getLocation();
//                double distance = npc.getLocation().distance(playerLoc);
//
//                if (distance > 3) {
//                    npc.teleport(playerLoc.clone().subtract(playerLoc.getDirection().multiply(-1)));
//                }
//            }
//        }.runTaskTimer(plugin, 0L, 20L);
//    }
//
//    public void say(String message) {
//        Bukkit.getServer().sendMessage(
//                Component.text("[MinecraftAI] ").color(NamedTextColor.RED)
//                        .append(Component.text(message).color(NamedTextColor.WHITE))
//        );
//    }
//
//    public void remove() {
//        if (npc != null) npc.remove();
//    }
}