package cat.nyaa.rpgitems.minion.utils;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * automatically poll out elements and distribute them to ticks evenly and apply functions.
 * @param <T> Type of elements that need to tick.
 */
public abstract class BaseTicker<T> implements Runnable, Consumer<T> {
    private static final int tickSpeed = 20;
    Queue<T> queue = new LinkedList<>();
    private int batchInterval = 20;

    public int getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(int batchInterval) {
        this.batchInterval = batchInterval;
    }

    int c = 0;
    int size = 0;

    public BaseTicker() {
        super();
        fill();
    }

    @Override
    public void run() {
        int batchInterval = getBatchInterval();
        if (batchInterval >= 0 && c++ >= batchInterval){
            fill();
            c = 0;
        }
        int tasksInThisTick = (int) (Math.ceil((double) size / (double) batchInterval) + 1);
        while (!queue.isEmpty() && tasksInThisTick-- > 0){
            T poll = queue.poll();
            this.accept(poll);
        }
    }

    private void fill(){
        queue.addAll(getNextBatch());
        size = queue.size();
    }

    private boolean running = false;
    BukkitTask bukkitTask;

    public void start(){
        bukkitTask = Bukkit.getScheduler().runTaskTimer(MinionExtensionPlugin.plugin, this, 0, 1);
        running = true;
    }

    public void stop(){
        running = false;
        if (bukkitTask == null) {
            return;
        }
        bukkitTask.cancel();
        bukkitTask = null;
    }

    public boolean isRunning(){
        return running;
    }

    protected abstract Collection<? extends T> getNextBatch();
}