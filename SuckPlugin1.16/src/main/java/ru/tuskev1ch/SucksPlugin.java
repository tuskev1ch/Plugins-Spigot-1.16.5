package ru.tuskev1ch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class SucksPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, UUID> suckRequests = new HashMap();
    private final Map<UUID, UUID> activeSucks = new HashMap();
    private final Map<UUID, Location> originalLocations = new HashMap();
    private final Map<UUID, BukkitTask> soundTasks = new HashMap();
    private final Map<UUID, Boolean> movingForward = new HashMap();
    private final Map<UUID, Location> centerPositions = new HashMap();
    private final Map<UUID, UUID> suckerToTarget = new HashMap();

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player)sender;
        if (command.getName().equalsIgnoreCase("suck")) {
            if (args.length >= 1 && args.length <= 2) {
                final Player target;
                if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        player.sendMessage(createGradientMessage("Некому сосать, увы!"));
                        return true;
                    } else if (this.suckRequests.containsKey(player.getUniqueId()) && ((UUID)this.suckRequests.get(player.getUniqueId())).equals(target.getUniqueId())) {
                        if (player.getLocation().distance(target.getLocation()) > 15.0D) {
                            player.sendMessage(createGradientMessage("Вы слишком далеко от игрока!"));
                            return true;
                        } else {
                            this.suckRequests.remove(player.getUniqueId());
                            this.activeSucks.put(player.getUniqueId(), target.getUniqueId());
                            this.suckerToTarget.put(player.getUniqueId(), target.getUniqueId());
                            this.originalLocations.put(player.getUniqueId(), player.getLocation());
                            Vector direction = target.getLocation().getDirection().normalize();
                            Location targetLoc = target.getLocation().clone();
                            targetLoc.add(direction.multiply(0.5D));
                            targetLoc.setY(target.getLocation().getY() - 1.0D);
                            Vector lookDirection = target.getLocation().toVector().subtract(targetLoc.toVector()).normalize();
                            targetLoc.setDirection(lookDirection);
                            player.teleport(targetLoc);
                            this.centerPositions.put(player.getUniqueId(), targetLoc.clone());
                            this.movingForward.put(player.getUniqueId(), true);
                            Location requesterLoc = target.getLocation().clone();
                            requesterLoc.setPitch(-60.0F);
                            target.teleport(requesterLoc);
                            player.setSneaking(true);

                            Location playerLoc = player.getLocation().clone();

                            playerLoc.setPitch(80.0F);

                            player.teleport(playerLoc);
                            player.sendMessage(createGradientMessage("Начинаем отсасывать!"));
                            target.sendMessage(createGradientMessage("Игрок начал отсасывать!"));
                            (new BukkitRunnable() {
                                private double time = 0.0D;

                                public void run() {
                                    if (SucksPlugin.this.activeSucks.containsKey(player.getUniqueId())) {
                                        this.time += 0.5D;
                                        Location loc = player.getLocation().clone();
                                        loc.setPitch(60.0F + (float)(Math.sin(this.time) * 5.0D));
                                        loc.setYaw((float)((double)loc.getYaw() + Math.cos(this.time) * 2.0D));
                                        Vector lookDir = target.getLocation().toVector().subtract(loc.toVector()).normalize();
                                        if (Math.sin(this.time) > 0.0D) {
                                            loc.add(lookDir.multiply(0.05D));
                                        } else {
                                            loc.subtract(lookDir.multiply(0.05D));
                                        }

                                        player.teleport(loc);
                                        player.setSneaking(true);
                                    } else {
                                        this.cancel();
                                    }

                                }
                            }).runTaskTimer(this, 0L, 1L);
                            BukkitTask soundTask = (new BukkitRunnable() {
                                public void run() {
                                    if (SucksPlugin.this.activeSucks.containsKey(player.getUniqueId())) {
                                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 1.0F);
                                        player.setSneaking(true);
                                    } else {
                                        this.cancel();
                                    }

                                }
                            }).runTaskTimer(this, 0L, 20L);
                            this.soundTasks.put(player.getUniqueId(), soundTask);
                            (new BukkitRunnable() {
                                public void run() {
                                    if (SucksPlugin.this.activeSucks.containsKey(player.getUniqueId())) {
                                        Location particleLoc = player.getEyeLocation().clone();
                                        player.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 30, 0.3D, 0.3D, 0.3D, 0.0D);
                                        player.getWorld().spawnParticle(Particle.SNOW_SHOVEL, particleLoc, 25, 0.2D, 0.2D, 0.2D, 0.0D);
                                        player.getWorld().spawnParticle(Particle.WATER_SPLASH, particleLoc, 40, 0.3D, 0.3D, 0.3D, 0.0D);
                                        player.getWorld().spawnParticle(Particle.WATER_DROP, particleLoc, 20, 0.2D, 0.2D, 0.2D, 0.0D);
                                        Location spreadLoc = particleLoc.clone().add(0.0D, 0.2D, 0.0D);

                                        for(int i = 0; i < 5; ++i) {
                                            player.getWorld().spawnParticle(Particle.CLOUD, spreadLoc, 10, 0.4D, 0.1D, 0.4D, 0.05D);
                                            spreadLoc.add(0.0D, 0.1D, 0.0D);
                                        }
                                    }

                                }
                            }).runTaskLater(this, 160L);
                            (new BukkitRunnable() {
                                public void run() {
                                    if (SucksPlugin.this.activeSucks.containsKey(player.getUniqueId())) {
                                        SucksPlugin.this.finishAction(player, target);
                                    }

                                }
                            }).runTaskLater(this, 200L);
                            return true;
                        }
                    } else {
                        player.sendMessage(createGradientMessage("У вас нет активных запросов от этого игрока!"));
                        return true;
                    }
                } else {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(createGradientMessage("Игрок не найден!"));
                        return true;
                    } else if (target.equals(player)) {
                        player.sendMessage(createGradientMessage("Вы не можете отсосать самому себе!"));
                        return true;
                    } else if (player.getLocation().distance(target.getLocation()) > 15.0D) {
                        player.sendMessage(createGradientMessage("Вы слишком далеко от игрока!"));
                        return true;
                    } else {
                        this.suckRequests.put(target.getUniqueId(), player.getUniqueId());
                        player.sendMessage(createGradientMessage("Запрос на отсос отправлен игроку " + target.getName()));
                        target.sendMessage(createGradientMessage("Игрок " + player.getName() + " хочет чтобы вы отсосали у него. Напишите /suck accept " + player.getName()));
                        (new BukkitRunnable() {
                            public void run() {
                                if (SucksPlugin.this.suckRequests.containsKey(target.getUniqueId())) {
                                    SucksPlugin.this.suckRequests.remove(target.getUniqueId());
                                    player.sendMessage(createGradientMessage("Запрос на отсос к " + target.getName() + " истек!"));
                                    target.sendMessage(createGradientMessage("Запрос на отсос от " + player.getName() + " истек!"));
                                }

                            }
                        }).runTaskLater(this, 600L);
                        return true;
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (this.suckerToTarget.containsValue(player.getUniqueId())) {
            event.setCancelled(true);
            Location loc = event.getFrom().clone();
            loc.setPitch(-60.0F);
            event.setTo(loc);
        } else {
            if (this.activeSucks.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (this.activeSucks.containsKey(player.getUniqueId()) || this.suckerToTarget.containsValue(player.getUniqueId())) {
            event.setCancelled(true);
        }

    }

    private void finishAction(Player player, Player requester) {
        this.activeSucks.remove(player.getUniqueId());
        this.movingForward.remove(player.getUniqueId());
        this.centerPositions.remove(player.getUniqueId());
        this.suckerToTarget.remove(player.getUniqueId());
        BukkitTask soundTask = (BukkitTask)this.soundTasks.remove(player.getUniqueId());
        if (soundTask != null) {
            soundTask.cancel();
        }

        Location endLoc = player.getLocation().clone();
        endLoc.setY(endLoc.getY() + 1.0D);
        player.teleport(endLoc);
        player.setSneaking(false);
        Location requesterLoc = requester.getLocation().clone();
        requesterLoc.setPitch(0.0F);
        requester.teleport(requesterLoc);
        requester.setSneaking(false);
        player.sendMessage(createGradientMessage("Успешно отсосано!"));
        requester.sendMessage(createGradientMessage("Вам успешно отсосали!"));
        this.originalLocations.remove(player.getUniqueId());
    }

    private String createGradientMessage(String whiteText) {
        StringBuilder gradientMessage = new StringBuilder();
        String[] colors = {
                "#FFB74D",
                "#FF9800",
                "#FF6F00",
                "#FF5722",
                "#FF6F00",
                "#FF9800",
                 "#FFB74D"
         };
        int length = "RageCube".length();

        gradientMessage.append(ChatColor.WHITE).append("[");

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int colorIndex = (int) (ratio * (colors.length - 1));
            String color = colors[colorIndex];
            gradientMessage.append(ChatColor.of(color)).append("RageCube".charAt(i));
        }

        gradientMessage.append(ChatColor.WHITE).append("] >> ").append(whiteText);

        return gradientMessage.toString();
    }

    public void onDisable() {
        Iterator var1 = this.soundTasks.values().iterator();

        while(var1.hasNext()) {
            BukkitTask task = (BukkitTask)var1.next();
            task.cancel();
        }

        this.soundTasks.clear();
        this.movingForward.clear();
        this.centerPositions.clear();
        var1 = this.originalLocations.entrySet().iterator();

        while(var1.hasNext()) {
            Entry<UUID, Location> entry = (Entry)var1.next();
            Player player = Bukkit.getPlayer((UUID)entry.getKey());
            if (player != null && player.isOnline()) {
                player.teleport((Location)entry.getValue());
            }
        }

        this.suckRequests.clear();
        this.activeSucks.clear();
        this.originalLocations.clear();
        this.suckerToTarget.clear();
    }
}