package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ForceAttackService implements Runnable {
    private static final ForceAttackService INSTANCE = new ForceAttackService();

    private final Map<BaseMinion, ForceEntry> active = new HashMap<>();
    private BukkitTask task;

    public static ForceAttackService getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (task != null) {
            return;
        }
        task = Bukkit.getScheduler().runTaskTimer(MinionExtensionPlugin.plugin, this, 1, 1);
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        active.clear();
    }

    public synchronized void apply(BaseMinion minion, int durationTicks, double intervalMultiplier) {
        if (minion == null || durationTicks <= 0 || intervalMultiplier <= 0) {
            return;
        }
        start();
        ForceEntry entry = active.get(minion);
        int baseInterval = minion.getAttackInterval();
        if (baseInterval < 1) {
            baseInterval = 1;
        }
        if (entry == null) {
            entry = new ForceEntry(baseInterval);
            active.put(minion, entry);
        }
        entry.remainingTicks = Math.max(entry.remainingTicks, durationTicks);
        entry.multiplier = intervalMultiplier;
        entry.forcedInterval = Math.max(1, (int) Math.round(entry.baseInterval * intervalMultiplier));
        entry.nextAttackIn = entry.forcedInterval;

        minion.attackInterval = entry.forcedInterval;
        if (minion.attackCooldown > 0) {
            minion.attackCooldown = Math.min(minion.attackCooldown, entry.forcedInterval);
        }
    }

    @Override
    public synchronized void run() {
        if (active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BaseMinion, ForceEntry>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BaseMinion, ForceEntry> entry = iterator.next();
            BaseMinion minion = entry.getKey();
            ForceEntry data = entry.getValue();
            if (minion == null || minion.isRemoved()) {
                iterator.remove();
                continue;
            }

            data.remainingTicks--;
            if (data.remainingTicks <= 0) {
                minion.attackInterval = data.baseInterval;
                iterator.remove();
                continue;
            }

            int forcedInterval = Math.max(1, (int) Math.round(data.baseInterval * data.multiplier));
            if (data.forcedInterval != forcedInterval) {
                data.forcedInterval = forcedInterval;
                if (data.nextAttackIn > forcedInterval) {
                    data.nextAttackIn = forcedInterval;
                }
            }
            if (minion.attackInterval != forcedInterval) {
                minion.attackInterval = forcedInterval;
            }

            if (minion.isAutoAttack() && (minion.getTarget() != null || minion.getTargetLocation() != null)) {
                data.nextAttackIn--;
                if (data.nextAttackIn <= 0) {
                    data.nextAttackIn = forcedInterval;
                    minion.forceAttack();
                }
                minion.attackCooldown = Math.max(0, data.nextAttackIn);
            } else {
                data.nextAttackIn = forcedInterval;
            }
        }
    }

    private static class ForceEntry {
        private final int baseInterval;
        private double multiplier;
        private int forcedInterval;
        private int remainingTicks;
        private int nextAttackIn;

        private ForceEntry(int baseInterval) {
            this.baseInterval = baseInterval;
        }
    }
}
