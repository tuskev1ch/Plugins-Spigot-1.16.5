package ru.tuskev1ch;

import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class Pipi extends JavaPlugin implements Listener {
   public void onEnable() {
      this.getServer().getPluginManager().registerEvents(this, this);
   }

   public void onDisable() {
      Iterator var1 = this.getServer().getWorlds().iterator();

      while(var1.hasNext()) {
         World world = (World)var1.next();
         Iterator var3 = world.getEntities().iterator();

         while(var3.hasNext()) {
            Entity entity = (Entity)var3.next();
            if (entity instanceof Item && entity.hasMetadata("pipi_block")) {
               entity.remove();
            }
         }
      }

   }

   @EventHandler
   public void onItemPickup(EntityPickupItemEvent event) {
      if (event.getItem().hasMetadata("pipi_block")) {
         event.setCancelled(true);
      }

   }

   @EventHandler
   public void onHopperPickup(InventoryPickupItemEvent event) {
      if (event.getItem().hasMetadata("pipi_block")) {
         event.setCancelled(true);
      }

   }

   private void spawnGoldBlocks(final Player target) {
      Iterator var2 = target.getWorld().getPlayers().iterator();

      while(var2.hasNext()) {
         Player player = (Player)var2.next();
         if (player != target && player.getLocation().distance(target.getLocation()) <= 5.0D) {
            player.sendMessage("Вас обоссали :(");
         }
      }

      (new BukkitRunnable() {
         int count = 0;

         public void run() {
            if (this.count >= 40) {
               this.cancel();
            } else {
               Location loc = target.getLocation();
               Vector direction = target.getLocation().getDirection().normalize();
               Location dropLoc = loc.clone();
               dropLoc.setY(loc.getY() + 0.5D);
               dropLoc.subtract(direction.clone().multiply(0.5D));
               final Item item = target.getWorld().dropItem(dropLoc, new ItemStack(Material.GOLD_BLOCK));
               item.setMetadata("pipi_block", new FixedMetadataValue(Pipi.this, true));
               item.setPickupDelay(Integer.MAX_VALUE);
               item.setInvulnerable(true);
               Vector velocity = direction.clone();
               velocity.setY(-0.5D);
               velocity.multiply(0.7D);
               item.setVelocity(velocity);
               (new BukkitRunnable() {
                  public void run() {
                     item.remove();
                  }
               }).runTaskLater(Pipi.this, 20L);
               ++this.count;
            }
         }
      }).runTaskTimer(this, 0L, 2L);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("pipi")) {
         if (!sender.hasPermission("pipi.use")) {
            sender.sendMessage("У вас нет прав на использование этой команды!");
            return true;
         } else {
            Player target;
            if (args.length > 0) {
               if (!sender.hasPermission("pipi.others")) {
                  sender.sendMessage("У вас нет прав использовать эту команду на других игроках!");
                  return true;
               }

               target = this.getServer().getPlayer(args[0]);
               if (target == null) {
                  sender.sendMessage("Игрок " + args[0] + " не найден!");
                  return true;
               }
            } else {
               if (!(sender instanceof Player)) {
                  sender.sendMessage("Укажите имя игрока: /pipi <игрок>");
                  return true;
               }

               target = (Player)sender;
            }

            this.spawnGoldBlocks(target);
            return true;
         }
      } else {
         return false;
      }
   }
}