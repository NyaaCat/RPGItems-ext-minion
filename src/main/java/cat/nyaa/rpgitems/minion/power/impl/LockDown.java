package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
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
import think.rpgitems.utils.cast.CastUtils;

import java.util.List;

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "LEFT_CLICK", implClass = LockDown.Impl.class)
public class LockDown extends BasePower {
    @Property
    int cooldown = 0;
    @Property
    int cost = 0;
    @Property
    int targetRange = 64;

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
        return "lock down a target for your minions";
    }

    public int getTargetRange() {
        return targetRange;
    }

    @Override
    public String getName() {
        return "lockdown";
    }

    public class Impl implements PowerRightClick, PowerLeftClick, PowerPlain, PowerSneak, PowerLocation, PowerBeamHit, PowerHit, PowerHitTaken, PowerOffhandClick, PowerBowShoot, PowerTick, PowerProjectileHit, PowerProjectileLaunch, PowerHurt, PowerSprint {

        @Override
        public PowerResult<Void> hitBlock(Player player, ItemStack stack, Location location, BeamHitBlockEvent event) {
            return fire(player, stack, location);
        }

        @Override
        public PowerResult<Void> beamEnd(Player player, ItemStack stack, Location location, BeamEndEvent event) {
            return fire(player, stack, location);
        }

        @Override
        public PowerResult<Float> bowShoot(Player player, ItemStack stack, EntityShootBowEvent event) {
            return fire(player, stack).with(0f);
        }

        @Override
        public PowerResult<Double> hit(Player player, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            return fire(player, stack, entity).with(damage);
        }

        @Override
        public PowerResult<Double> takeHit(Player target, ItemStack stack, double damage, EntityDamageEvent event) {
            if (event instanceof EntityDamageByEntityEvent){
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager != null){
                    return fire(target, stack, damager).with(damage);
                }
                return fire(target, stack).with(damage);
            }
            return fire(target, stack).with(damage);
        }

        @Override
        public PowerResult<Void> hurt(Player target, ItemStack stack, EntityDamageEvent event) {
            if (event instanceof EntityDamageByEntityEvent){
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager != null){
                    return fire(target, stack, damager);
                }
                return fire(target, stack);
            }
            return fire(target, stack);
        }

        @Override
        public PowerResult<Void> leftClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack, Location location) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            List<IMinion> minions = MinionManager.getInstance().getMinions(player);
            if (!minions.isEmpty()) {
                minions.forEach(minion -> {
                    minion.setTargetLocation(location);
                });
            }
            return PowerResult.ok();
        }

        public PowerResult<Void> fire(Player player, ItemStack stack, Entity entity) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            List<IMinion> minions = MinionManager.getInstance().getMinions(player);
            if (!minions.isEmpty()) {
                minions.forEach(minion -> {
                    minion.setTarget(entity);
                });
            }
            return PowerResult.ok();
        }


        @Override
        public PowerResult<Void> offhandClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            CastUtils.CastLocation castLocation = CastUtils.rayTrace(player, player.getEyeLocation(), player.getEyeLocation().getDirection(), getTargetRange());
            Entity hitEntity = castLocation.getHitEntity();
            Location targetLocation = castLocation.getTargetLocation();
            if (hitEntity!=null){
                return fire(player, stack, hitEntity);
            }else if (targetLocation != null){
                return fire(player, stack, targetLocation);
            }
            return PowerResult.ok();
        }

        @Override
        public PowerResult<Void> projectileHit(Player player, ItemStack stack, ProjectileHitEvent event) {
            Entity hitEntity = event.getHitEntity();
            if (hitEntity != null){
                return fire(player, stack, hitEntity);
            }
            Block hitBlock = event.getHitBlock();
            if (hitBlock != null){
                BlockFace hitBlockFace = event.getHitBlockFace();
                Location loc = null;
                loc = hitBlock.getLocation().add(0.5,0.5,0.5).add(hitBlockFace.getDirection().multiply(0.5));
                return fire(player, stack, loc);
            }
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
            return LockDown.this;
        }
    }
}

