package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.utils.BaseTicker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MinionManager {
    private static MinionManager instance;

    public static MinionManager getInstance(){
        if (instance == null){
            synchronized (MinionManager.class){
                if (instance == null){
                    instance = new MinionManager();
                }
            }
        }
        return instance;
    }

    private Map<UUID, IMinion> entityMinionMap = new HashMap<>();
    private Map<UUID, List<IMinion>> playerMinionMap = new HashMap<>();
    private Map<UUID, Integer> minionMaxMap = new HashMap<>();

    public IMinion toIMinion(Entity entity){
        return entityMinionMap.get(entity.getUniqueId());
    }

    public List<IMinion> getMinions(Player player){
        return playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid -> createMinionList());
    }

    private List<IMinion> createMinionList() {
        return new ArrayList<>();
    }

    public int getMinionSlotMax(Player player){
        return minionMaxMap.computeIfAbsent(player.getUniqueId(), uuid -> 1);
    }

    public int getSlotUsed(Player player){
        return playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid -> createMinionList()).stream()
                .mapToInt(IMinion::getSlotCost)
                .sum();
    }

    class MinionTicker extends BaseTicker<IMinion> {
        @Override
        public void accept(IMinion iMinion) {

        }
    }

    class PlayerTicker extends BaseTicker<Player>{
        @Override
        public void accept(Player player) {

        }
    }
}
