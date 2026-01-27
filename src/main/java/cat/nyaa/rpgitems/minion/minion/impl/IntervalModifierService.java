package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IntervalModifierService implements Runnable {
    private static final IntervalModifierService INSTANCE = new IntervalModifierService();

    private final Map<BaseMinion, IntervalEntry> active = new HashMap<>();
    private BukkitTask task;

    public static IntervalModifierService getInstance() {
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

    public synchronized void apply(BaseMinion minion, double multiplier, int durationTicks) {
        if (minion == null || durationTicks <= 0 || multiplier <= 0) {
            return;
        }
        start();
        int currentInterval = minion.attackInterval;
        IntervalEntry entry = active.get(minion);
        if (entry == null) {
            entry = new IntervalEntry(minion, minion.attackInterval);
            active.put(minion, entry);
        }
        entry.remainingTicks = durationTicks;
        entry.multiplier = multiplier;

        int newInterval = (int) Math.round(entry.originalInterval * multiplier);
        if (newInterval < 1) {
            newInterval = 1;
        }
        if (currentInterval > 0 && minion.attackCooldown > 0) {
            double ratio = (double) newInterval / (double) currentInterval;
            int adjustedCooldown = (int) Math.round(minion.attackCooldown * ratio);
            minion.attackCooldown = Math.max(0, adjustedCooldown);
        }
        minion.attackInterval = newInterval;
    }

    @Override
    public synchronized void run() {
        if (active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BaseMinion, IntervalEntry>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            IntervalEntry entry = iterator.next().getValue();
            BaseMinion minion = entry.minion;
            if (minion == null || minion.isRemoved()) {
                iterator.remove();
                continue;
            }
            entry.remainingTicks--;
            if (entry.remainingTicks <= 0) {
                minion.attackInterval = entry.originalInterval;
                iterator.remove();
            }
        }
    }

    private static class IntervalEntry {
        private final BaseMinion minion;
        private final int originalInterval;
        private int remainingTicks;
        private double multiplier;

        private IntervalEntry(BaseMinion minion, int originalInterval) {
            this.minion = minion;
            this.originalInterval = originalInterval;
        }
    }
}
