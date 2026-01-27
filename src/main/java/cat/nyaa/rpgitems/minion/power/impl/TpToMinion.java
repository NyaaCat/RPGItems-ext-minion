package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.power.BasePluginPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import think.rpgitems.event.BeamEndEvent;
import think.rpgitems.event.BeamHitBlockEvent;
import think.rpgitems.power.*;

import java.util.*;
import java.util.stream.Collectors;

import static think.rpgitems.power.Utils.checkCooldown;

@Meta(defaultTrigger = "RIGHT_CLICK", implClass = TpToMinion.Impl.class)
public class TpToMinion extends BasePluginPower {
    @Property
    double rangeMin = 0;
    @Property
    double rangeMax = 64;
    @Property
    SelectionMode selection = SelectionMode.NEAREST;
    @Property
    String particle = "";
    @Property
    int particleCount = 30;
    @Property
    double particleOffsetX = 0.5;
    @Property
    double particleOffsetY = 1.0;
    @Property
    double particleOffsetZ = 0.5;
    @Property
    double particleSpeed = 0.1;
    @Property
    String soundSuccess = "";
    @Property
    float soundSuccessVolume = 1.0f;
    @Property
    float soundSuccessPitch = 1.0f;
    @Property
    String soundFail = "";
    @Property
    float soundFailVolume = 1.0f;
    @Property
    float soundFailPitch = 1.0f;
    @Property
    boolean ignoreWall = false;
    @Property
    int cooldown = 0;
    @Property
    int cost = 0;

    public double getRangeMin() {
        return rangeMin;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    public SelectionMode getSelection() {
        return selection;
    }

    public String getParticle() {
        return particle;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public double getParticleOffsetX() {
        return particleOffsetX;
    }

    public double getParticleOffsetY() {
        return particleOffsetY;
    }

    public double getParticleOffsetZ() {
        return particleOffsetZ;
    }

    public double getParticleSpeed() {
        return particleSpeed;
    }

    public String getSoundSuccess() {
        return soundSuccess;
    }

    public float getSoundSuccessVolume() {
        return soundSuccessVolume;
    }

    public float getSoundSuccessPitch() {
        return soundSuccessPitch;
    }

    public String getSoundFail() {
        return soundFail;
    }

    public float getSoundFailVolume() {
        return soundFailVolume;
    }

    public float getSoundFailPitch() {
        return soundFailPitch;
    }

    public boolean isIgnoreWall() {
        return ignoreWall;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String displayText() {
        return "teleport to minion (selection: " + selection.name().toLowerCase() + ", range: " + rangeMin + "-" + rangeMax + ")";
    }

    @Override
    public String getName() {
        return "tptominion";
    }

    public class Impl implements Pimpls {

        @Override
        public PowerResult<Void> fire(Player player, ItemStack stack) {
            if (!checkCooldown(getPower(), player, getCooldown(), true, true)) return PowerResult.cd();
            if (!getItem().consumeDurability(stack, getCost())) return PowerResult.cost();

            List<IMinion> minions = MinionManager.getInstance().getMinions(player);
            if (minions.isEmpty()) {
                playFailSound(player);
                return PowerResult.fail();
            }

            Location playerLoc = player.getLocation();
            Vector playerDir = playerLoc.getDirection().normalize();

            // Filter minions by range and line of sight
            List<IMinion> validMinions = minions.stream()
                    .filter(m -> m.getEntity() != null && !m.getEntity().isDead())
                    .filter(m -> {
                        double distance = m.getEntity().getLocation().distance(playerLoc);
                        return distance >= rangeMin && distance <= rangeMax;
                    })
                    .filter(m -> ignoreWall || hasLineOfSight(player, m.getEntity()))
                    .collect(Collectors.toList());

            if (validMinions.isEmpty()) {
                playFailSound(player);
                return PowerResult.fail();
            }

            // Select target based on selection mode
            IMinion selectedMinion = selectMinion(validMinions, playerLoc, playerDir);
            if (selectedMinion == null || selectedMinion.getEntity() == null) {
                playFailSound(player);
                return PowerResult.fail();
            }

            // Teleport player to minion location
            Location targetLoc = selectedMinion.getEntity().getLocation();
            // Preserve player's yaw and pitch
            targetLoc.setYaw(player.getLocation().getYaw());
            targetLoc.setPitch(player.getLocation().getPitch());

            // Spawn particle at origin
            spawnParticle(playerLoc);

            player.teleport(targetLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);

            // Spawn particle at destination
            spawnParticle(targetLoc);

            playSuccessSound(player);
            return PowerResult.ok();
        }

        private IMinion selectMinion(List<IMinion> minions, Location playerLoc, Vector playerDir) {
            switch (selection) {
                case RANDOM:
                    Collections.shuffle(minions);
                    return minions.get(0);

                case NEAREST:
                    return minions.stream()
                            .min(Comparator.comparingDouble(m -> m.getEntity().getLocation().distanceSquared(playerLoc)))
                            .orElse(null);

                case FARTHEST:
                    return minions.stream()
                            .max(Comparator.comparingDouble(m -> m.getEntity().getLocation().distanceSquared(playerLoc)))
                            .orElse(null);

                case AIMING:
                    return minions.stream()
                            .max(Comparator.comparingDouble(m -> {
                                Vector toMinion = m.getEntity().getLocation().toVector()
                                        .subtract(playerLoc.toVector()).normalize();
                                return playerDir.dot(toMinion);
                            }))
                            .orElse(null);

                default:
                    return minions.get(0);
            }
        }

        private boolean hasLineOfSight(Player player, Entity target) {
            Location eyeLoc = player.getEyeLocation();
            Location targetLoc = target instanceof LivingEntity
                    ? ((LivingEntity) target).getEyeLocation()
                    : target.getLocation();

            if (!eyeLoc.getWorld().equals(targetLoc.getWorld())) {
                return false;
            }

            double distance = eyeLoc.distance(targetLoc);
            if (distance < 1.0) {
                return true;
            }

            Vector direction = targetLoc.toVector().subtract(eyeLoc.toVector()).normalize();
            return eyeLoc.getWorld().rayTraceBlocks(eyeLoc, direction, distance) == null;
        }

        private void spawnParticle(Location loc) {
            if (particle == null || particle.isEmpty()) {
                return;
            }
            try {
                Particle p = Particle.valueOf(particle.toUpperCase());
                loc.getWorld().spawnParticle(p, loc, particleCount, particleOffsetX, particleOffsetY, particleOffsetZ, particleSpeed);
            } catch (IllegalArgumentException ignored) {
            }
        }

        private void playSuccessSound(Player player) {
            if (soundSuccess == null || soundSuccess.isEmpty()) {
                return;
            }
            player.playSound(player.getLocation(), soundSuccess, soundSuccessVolume, soundSuccessPitch);
        }

        private void playFailSound(Player player) {
            if (soundFail == null || soundFail.isEmpty()) {
                return;
            }
            player.playSound(player.getLocation(), soundFail, soundFailVolume, soundFailPitch);
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
            return TpToMinion.this;
        }
    }
}
