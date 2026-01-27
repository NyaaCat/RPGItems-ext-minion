package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.minion.impl.BaseMinion;
import cat.nyaa.rpgitems.minion.minion.impl.ForceAttackService;
import cat.nyaa.rpgitems.minion.power.BasePluginPower;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.event.BeamEndEvent;
import think.rpgitems.event.BeamHitBlockEvent;
import think.rpgitems.power.*;

import java.util.List;

import static think.rpgitems.power.Utils.checkCooldown;

/**
 * Force all minions to attack immediately.
 * Can optionally modify attack frequency for a duration.
 */
@Meta(defaultTrigger = "LEFT_CLICK", implClass = ForceAttack.Impl.class)
public class ForceAttack extends BasePluginPower {
    @Property
    int duration = 1;  // Duration in ticks, 1 = single attack
    @Property
    double intervalMultiplier = 1.0;  // >1 slows down, <1 speeds up
    @Property
    int cooldown = 0;
    @Property
    int cost = 0;

    public int getDuration() {
        return duration;
    }

    public double getIntervalMultiplier() {
        return intervalMultiplier;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String displayText() {
        if (duration == 1) {
            return "force minions to attack once";
        }
        return "force minions to attack (duration: " + duration + " ticks, speed: " + (1.0 / intervalMultiplier) + "x)";
    }

    @Override
    public String getName() {
        return "forceattack";
    }

    public class Impl implements Pimpls {

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();

            List<IMinion> minions = MinionManager.getInstance().getMinions(player);
            if (minions.isEmpty()) {
                return PowerResult.fail();
            }

            boolean anyAttacked = false;
            for (IMinion minion : minions) {
                // Skip minions in ceasefire state
                if (!minion.isAutoAttack()) {
                    continue;
                }

                if (!(minion instanceof BaseMinion)) {
                    continue;
                }

                BaseMinion baseMinion = (BaseMinion) minion;

                if (duration == 1) {
                    // Single attack
                    baseMinion.forceAttack();
                    anyAttacked = true;
                } else {
                    // Ensure duration keeps forcing attacks (batched service)
                    ForceAttackService.getInstance().apply(baseMinion, duration, intervalMultiplier);
                    // Force immediate first attack
                    baseMinion.forceAttack();
                    anyAttacked = true;
                }
            }

            return anyAttacked ? PowerResult.ok() : PowerResult.fail();
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack, Location location) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> hitBlock(Player player, ItemStack stack, Location location, BeamHitBlockEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> beamEnd(Player player, ItemStack stack, Location location, BeamEndEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Float> bowShoot(Player player, ItemStack stack, EntityShootBowEvent event) {
            return fire(player, stack).with(0f);
        }

        @Override
        public PowerResult<Double> hit(Player player, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            return fire(player, stack).with(damage);
        }

        @Override
        public PowerResult<Double> takeHit(Player target, ItemStack stack, double damage, EntityDamageEvent event) {
            return fire(target, stack).with(damage);
        }

        @Override
        public PowerResult<Void> hurt(Player target, ItemStack stack, EntityDamageEvent event) {
            return fire(target, stack);
        }

        @Override
        public PowerResult<Void> leftClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> offhandClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> projectileHit(Player player, ItemStack stack, ProjectileHitEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> projectileLaunch(Player player, ItemStack stack, ProjectileLaunchEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> rightClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> sneak(Player player, ItemStack stack, PlayerToggleSneakEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> sprint(Player player, ItemStack stack, PlayerToggleSprintEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> tick(Player player, ItemStack stack) {
            return fire(player, stack);
        }

        @Override
        public Power getPower() {
            return ForceAttack.this;
        }
    }
}
