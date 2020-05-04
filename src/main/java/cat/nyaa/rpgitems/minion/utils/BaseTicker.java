package cat.nyaa.rpgitems.minion.utils;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public abstract class BaseTicker<T> extends BukkitRunnable implements Consumer<T> {

    @Override
    public void run() {

    }
}