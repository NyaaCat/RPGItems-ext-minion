package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.power.BasePluginPower;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.event.BeamEndEvent;
import think.rpgitems.event.BeamHitBlockEvent;
import think.rpgitems.power.*;

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "RIGHT_CLICK", implClass = MinionSlotMax.Impl.class)
public class MinionSlotMax extends BasePluginPower {
    @Property
    int cooldown = 0;
    @Property
    int cost = 0;
    @Property
    public int max = 1;

    public int getMax() {
        return max;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public String displayText() {
        return "increase minion slots";
    }

    @Override
    public String getName() {
        return "minionslotmax";
    }

    public class Impl implements Pimpls {

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
            return fire(player, stack).with(event.getForce());
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
        public PowerResult<Void> fire(Player player, ItemStack stack, Location location) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> offhandClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            PlayerData playerData = Database.getInstance().getPlayerData(player);
            playerData.slotMax = playerData.slotMax + getMax();
            Database.getInstance().setPlayerData(playerData);
            return PowerResult.ok();
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
            return MinionSlotMax.this;
        }
    }
}
