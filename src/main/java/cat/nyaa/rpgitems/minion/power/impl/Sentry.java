package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.events.MinionSpawnEvent;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.minion.impl.MinionSentry;
import cat.nyaa.rpgitems.minion.minion.impl.enums.SentryTypes;
import org.bukkit.Bukkit;
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

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "RIGHT_CLICK", implClass = Sentry.Impl.class)
public class Sentry extends BaseMinionPower {
    @Property
    SentryTypes sentryType = SentryTypes.REGULAR;

    public SentryTypes getSentryType() {
        return sentryType;
    }

    @Override
    public String displayText() {
        return "summon a Sentry to fight for you";
    }

    @Override
    public String getName() {
        return "sentry";
    }

    public class Impl implements PowerRightClick, PowerLeftClick, PowerPlain, PowerSneak, PowerLocation, PowerBeamHit, PowerHit, PowerHitTaken, PowerOffhandClick, PowerBowShoot, PowerTick, PowerProjectileHit, PowerProjectileLaunch, PowerHurt, PowerSprint{

        private Location getTargetLocation(LivingEntity player){
            CastUtils.CastLocation castLocation = CastUtils.rayTrace(player, player.getEyeLocation(), player.getEyeLocation().getDirection(), getSpawnRange());
            return castLocation.getTargetLocation();
        }

        @Override
        public PowerResult<Void> leftClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            return fire(player, stack, getTargetLocation(player));
        }

        @Override
        public PowerResult<Void> rightClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public Power getPower() {
            return Sentry.this;
        }

        @Override
        public PowerResult<Void> hitBlock(Player player, ItemStack stack, Location location, BeamHitBlockEvent event) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            return fire(player, stack, event.getLocation());
        }

        @Override
        public PowerResult<Void> beamEnd(Player player, ItemStack stack, Location location, BeamEndEvent event) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            return fire(player, stack, event.getLocation());
        }

        @Override
        public PowerResult<Float> bowShoot(Player player, ItemStack stack, EntityShootBowEvent event) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            return fire(player, stack).with(event.getForce());
        }

        @Override
        public PowerResult<Double> hit(Player player, ItemStack stack, LivingEntity entity, double damage, EntityDamageByEntityEvent event) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            return fire(player, stack, entity.getLocation()).with(damage);
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
        public PowerResult<Void> fire(Player player, ItemStack stack, Location location) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            MinionSentry minionSentry = new MinionSentry(player, Sentry.this, stack);
            MinionSpawnEvent minionSpawnEvent = new MinionSpawnEvent(minionSentry, location);
            Bukkit.getPluginManager().callEvent(minionSpawnEvent);
            if (minionSpawnEvent.isCanceled()){
                return PowerResult.fail();
            }
            minionSentry.respawn(location);
            MinionManager.getInstance().registerMinion(player, minionSentry);
            return PowerResult.ok();
        }

        @Override
        public PowerResult<Void> offhandClick(Player player, ItemStack stack, PlayerInteractEvent event) {
            return fire(player, stack);
        }

        @Override
        public PowerResult<Void> projectileHit(Player player, ItemStack stack, ProjectileHitEvent event) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();
            Entity hitEntity = event.getHitEntity();
            Block hitBlock = event.getHitBlock();
            BlockFace hitBlockFace = event.getHitBlockFace();
            Location loc = null;
            if (hitEntity != null){
                loc = hitEntity.getLocation();
            }else if (hitBlock != null){
                loc = hitBlock.getLocation().add(0.5,0.5,0.5).add(hitBlockFace.getDirection().multiply(0.5));
            }
            return fire(player, stack, loc);
        }

        @Override
        public PowerResult<Void> projectileLaunch(Player player, ItemStack stack, ProjectileLaunchEvent event) {
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
    }
}
