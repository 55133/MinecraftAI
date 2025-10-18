

package me.wyatt.minecraftai;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonObject;

public class Main extends JavaPlugin implements Listener {

    private NPC npc;
    private NPCRegistry registry;
    //private ChatGPTHelper ChatGPTHelper;
    private boolean shouldFollowPlayer;
    private EnvironmentData env;

    @Override
    public void onEnable() {
        getLogger().info("MinecraftAI enabled!");
        registry = CitizensAPI.getNPCRegistry();
        getServer().getPluginManager().registerEvents(this, this);

        // Spawn immediately if a player is already online
        Player first = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (first != null && npc == null) {
            spawnNPC(first);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (npc == null) {
            spawnNPC(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() <= 1 && npc != null) {
            npc.destroy();
            npc = null;
        }
    }

    private void spawnNPC(Player player) {
        Location spawnLoc = player.getLocation().add(2, 0, 0);
        npc = registry.createNPC(EntityType.PLAYER, "Asuka");
        shouldFollowPlayer = true;
        npc.setProtected(true);
        npc.spawn(spawnLoc);

        env = new EnvironmentData(npc, player);

        //startDummyAI(npc, player, 40L); // 20 ticks = 1 second per action
        startReactiveAI(npc, player, 5L);
        //startCombatAI(npc, player, 10L);
        // Set skin using Citizens commands
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc select " + npc.getId());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "npc skin iHxku -slim");

        // Make NPC follow player
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc == null || !player.isOnline()) {
                    cancel();
                    return;
                }
                getLogger().info(env.getSummary());
                Bukkit.getLogger().info(env.getSummary());
                // Update NPC navigator
                if (shouldFollowPlayer) {
                    npc.getNavigator().setTarget(player.getLocation());
                }
                // Teleport if too far
                if (npc.getEntity().getLocation().distance(player.getLocation()) > 15) {
                    npc.getEntity().teleport(player.getLocation().add(1, 0, 0));
                }

                // Ask ChatGPT
                String prompt = "You are Asuka in Minecraft. Player is at " + player.getLocation() +
                        ", NPC is at " + npc.getEntity().getLocation() +
                        ". Give a simple command like say, move, jump, or place_block.";

//                Bukkit.getScheduler().runTaskAsynchronously(Main.this, () -> {
//                    ChatGPTHelper.askChatGPTAsync(prompt, action -> {
//                        if (action != null) {
//                            getLogger().info("AI command: " + action);
//                            // Run AI action on main thread
//                            Bukkit.getScheduler().runTask(Main.this, () -> {
//                                performAIAction(npc, player, action);
//                            });
//                        }
//                    });
//                });

            }
        }.runTaskTimer(this, 0L, 10L);
    }

    private void makeNPCJump(NPC npc) {
        if (npc == null || !npc.isSpawned()) return;
        Entity entity = npc.getEntity();

        // Make sure it's affected by gravity
        if (entity instanceof LivingEntity living) {
            living.setGravity(true);
            living.setVelocity(new Vector(0, 0.6, 0)); // gives a nice hop
        } else {
            // fallback — teleport up slightly if no physics
            Location loc = entity.getLocation();
            loc.add(0, 1, 0);
            npc.teleport(loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    private void performAIAction(NPC npc, Player player, String action) {
        if (action == null) return;

        action = action.toLowerCase();
        switch (action) {
            case "say hi":
                Bukkit.getServer().sendMessage(Component.text("Asuka: Hello, " + player.getName() + "!"));
                break;
            case "say hello":
                npc.getEntity().sendMessage("Hello, " + player.getName() + "!");
                break;
            case "jump":
                npc.getEntity().setVelocity(npc.getEntity().getVelocity().add(new Vector(0, 10, 0)));
                break;
            case "move forward":
                npc.getNavigator().setTarget(npc.getEntity().getLocation().add(npc.getEntity().getLocation().getDirection().multiply(3)));
                break;
            case "place block":
                Location loc = npc.getEntity().getLocation().add(1, 0, 0);

                if (npc.getEntity().getWorld().getBlockAt(loc).getType() == Material.AIR) {
                    npc.getEntity().getWorld().getBlockAt(loc).setType(Material.STONE);
                }
                break;
            default:
                getLogger().info("Unknown AI action: " + action);
        }
    }

    boolean hasSaid = false;

    private void startDummyAI(NPC npc, Player player, long intervalTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npc == null || !player.isOnline()) {
                    cancel();
                    return;
                }

                // Update NPC navigation to follow player
                npc.getNavigator().setTarget(player.getLocation());

                // Teleport if too far
                if (npc.getEntity().getLocation().distance(player.getLocation()) > 10) {
                    npc.getEntity().teleport(player.getLocation().add(1, 0, 0));
                }

                // Pick a random dummy AI action
                String action = getDummyAIAction(player, npc);
                getLogger().info("Dummy AI action: " + action);

                // Perform the action on the main thread
                performAIAction(npc, player, action);
            }
        }.runTaskTimer(this, 0L, intervalTicks);
    }

    // Helper method for generating a random dummy command
    private String getDummyAIAction(Player player, NPC npc) {
        String[] actions = {"say hi", "jump", "move forward", "place block"};
        int index = (int) (Math.random() * actions.length);
        return actions[index];
    }

    private enum AIState { IDLE, FOLLOWING, COMBAT, EXPLORING }

    private AIState currentState = AIState.FOLLOWING;

    private void startReactiveAI(NPC npc, Player player, long intervalTicks) {
        new BukkitRunnable() {
            Monster currentTarget = null;
            int idleTicks = 0;

            @Override
            public void run() {
                if (npc == null || !npc.isSpawned() || !player.isOnline()) {
                    cancel();
                    return;
                }

                LivingEntity asuka = (LivingEntity) npc.getEntity();
                Location npcLoc = asuka.getLocation();

                // --- ENVIRONMENT AWARENESS ---
                Material blockBelow = npcLoc.clone().add(0, -1, 0).getBlock().getType();
                if (blockBelow == Material.LAVA) {
                    Bukkit.broadcast(Component.text("Shinji! I'm literally burning here!"));
                    asuka.setVelocity(new Vector(0, 1, 0.3));
                    currentState = AIState.EXPLORING;
                } else if (blockBelow == Material.WATER) {
                    Bukkit.broadcast(Component.text("Ugh! My shoes are soaked, Shinji!"));
                }

                if (asuka.getFireTicks() > 0 && Math.random() < 0.05) {
                    Bukkit.broadcast(Component.text("It’s hot! I’m not built for this!"));
                }

                if (npc.getEntity().getWorld().hasStorm() && Math.random() < 0.03) {
                    Bukkit.broadcast(Component.text("It’s raining again... I hate this weather."));
                }

                if (npcLoc.getBlock().getLightLevel() < 4 && Math.random() < 0.03) {
                    Bukkit.broadcast(Component.text("It’s too dark, Shinji. I can’t see anything."));
                }

                // --- MOB DETECTION / COMBAT ---
                if (currentTarget == null || currentTarget.isDead() || !currentTarget.isValid()) {
                    // find new target
                    for (Entity e : asuka.getNearbyEntities(10, 6, 10)) {
                        if (e instanceof Monster mob) {
                            currentTarget = mob;
                            currentState = AIState.COMBAT;
                            Bukkit.broadcast(Component.text("Finally, some action!"));
                            break;
                        }
                    }
                }

                // actively pursue target
                if (currentTarget != null && currentState == AIState.COMBAT) {
                    if (currentTarget.isDead() || !currentTarget.isValid()) {
                        currentTarget = null;
                        currentState = AIState.FOLLOWING;
                        Bukkit.broadcast(Component.text("Tch. That was too easy."));
                    } else {
                        // chase and attack
                        npc.getNavigator().setTarget(currentTarget.getLocation());
                        double dist = npcLoc.distance(currentTarget.getLocation());
                        if (dist < 2.5) {
                            currentTarget.damage(5.0, asuka);
                            if (asuka instanceof Player p) p.swingMainHand();
                            if (Math.random() < 0.15)
                                Bukkit.broadcast(Component.text("You’re no match for me!"));
                        }
                    }
                }

                // --- REACT TO PLAYER HEALTH ---
                if (player.getHealth() < 8 && Math.random() < 0.2) {
                    Bukkit.broadcast(Component.text("Shinji, you’re bleeding out! Do I have to do everything?"));
                    if (player.getHealth() < player.getMaxHealth()) {
                        player.setHealth(Math.min(player.getHealth() + 1, player.getMaxHealth()));
                    }
                }

                // --- IDLE BEHAVIOR ---
                if (currentState == AIState.FOLLOWING && currentTarget == null) {
                    npc.getNavigator().setTarget(player.getLocation());

                    if (npcLoc.distance(player.getLocation()) > 15) {
                        npc.getEntity().teleport(player.getLocation().add(1, 0, 0));
                        Bukkit.broadcast(Component.text("Hey! Don’t just run off, Shinji!"));
                    }

                    if (Math.random() < 0.02) {
                        makeNPCJump(npc);
                    }

                    // Random idle actions
                    if (Math.random() < 0.01) {
                        String[] idle = {
                                "What are we even doing here?",
                                "If you die, I’m not cleaning up after you.",
                                "Ugh, I’m bored.",
                                "Are we lost? Because it feels like it.",
                                "Shinji, can you *not* stand so close?"
                        };
                        Bukkit.broadcast(Component.text(idle[(int) (Math.random() * idle.length)]));
                    }
                }

                // --- OCCASIONAL EXPLORATION ---
                if (Math.random() < 0.01 && currentState == AIState.FOLLOWING) {
                    Location explore = player.getLocation().clone().add(Math.random() * 10 - 5, 0, Math.random() * 10 - 5);
                    npc.getNavigator().setTarget(explore);
                    currentState = AIState.EXPLORING;
                    Bukkit.broadcast(Component.text("I’m checking this area out."));
                }

                // return to following after exploring for a bit
                if (currentState == AIState.EXPLORING) {
                    idleTicks++;
                    if (idleTicks > 60) { // after ~3 seconds
                        idleTicks = 0;
                        currentState = AIState.FOLLOWING;
                    }
                }
            }
        }.runTaskTimer(this, 0L, intervalTicks);
    }








    @Override
    public void onDisable() {
        if (npc != null) {
            npc.destroy();
        }
    }




}


