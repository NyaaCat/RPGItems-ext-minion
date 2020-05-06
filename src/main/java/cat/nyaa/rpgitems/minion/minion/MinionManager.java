package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.utils.BaseTicker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
    MinionTicker minionTicker;
    PlayerTicker playerTicker;

    public MinionManager() {
        minionTicker = new MinionTicker();
        playerTicker = new PlayerTicker();
        minionTicker.start();
        playerTicker.start();
    }

    private Map<UUID, IMinion> entityMinionMap = new HashMap<>();
    private Map<UUID, List<IMinion>> playerMinionMap = new HashMap<>();
    private Map<UUID, Integer> minionMaxMap = new HashMap<>();

    public IMinion toIMinion(Entity entity){
        return entityMinionMap.get(entity.getUniqueId());
    }

    private Collection<? extends IMinion> getMinions() {
        return entityMinionMap.values();
    }

    public List<IMinion> getMinions(Player player){
        return Collections.unmodifiableList(playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid -> createMinionList()));
    }

    private List<IMinion> createMinionList() {
        return new LinkedList<>();
    }

    public int getMinionSlotMax(Player player){
        return minionMaxMap.computeIfAbsent(player.getUniqueId(), uuid -> 1);
    }

    public int getSlotUsed(Player player){
        return playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid -> createMinionList()).stream()
                .mapToInt(IMinion::getSlotCost)
                .sum();
    }

    public void registerMinion(Player player, IMinion minion){
        UUID entityId = minion.getEntity().getUniqueId();
        UUID uniqueId = player.getUniqueId();
        entityMinionMap.put(entityId, minion);
        List<IMinion> iMinions = playerMinionMap.computeIfAbsent(uniqueId, uuid -> createMinionList());
        iMinions.add(minion);
        checkMax(player);
    }

    private void checkMax(Player player) {
        int minionSlotMax = getMinionSlotMax(player);
        int slotUsed = getSlotUsed(player);
        while (slotUsed > minionSlotMax){
            List<IMinion> minions = getMinions(player);
            IMinion iMinion = minions.get(0);
            int slotCost = iMinion.getSlotCost();
            removeMinion(iMinion);
            slotUsed -= slotCost;
        }
    }

    private void doMinionTick(IMinion iMinion, int minionTick) {
        OfflinePlayer oPlayer = iMinion.getOwner();
        if (oPlayer == null || !oPlayer.isOnline()) {
            removeMinion(iMinion);
            return;
        }
        Player player = oPlayer.getPlayer();
        //todo
        iMinion.tick(minionTick);
    }

    private void doSanityCheck(Player player) {
        List<UUID> toRemove = new ArrayList<>();
        getMinions(player).forEach(iMinion -> {
            if (!entityMinionMap.containsKey(iMinion.getEntity().getUniqueId())) {
                toRemove.add(iMinion.getEntity().getUniqueId());
            }
        });
        if (toRemove.isEmpty()) {
            return;
        }
        toRemove.forEach(this::removeMinion);
    }

    private void removeMinion(UUID uuid) {
        IMinion iMinion = entityMinionMap.remove(uuid);
        if (iMinion!=null){
            iMinion.despawn();
            OfflinePlayer opt = iMinion.getOwner();
            Player player;
            if (opt != null && (player = opt.getPlayer()) != null){
                playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid1 -> createMinionList()).remove(iMinion);
            }
        }
    }

    private void removeMinion(IMinion iMinion) {
        removeMinion(iMinion.getEntity().getUniqueId());
    }

    public void clear(){
        ArrayList<UUID> uuids = new ArrayList<>(entityMinionMap.keySet());
        uuids.forEach(this::removeMinion);
    }

    public void replaceEntity(UUID oldUUID, UUID uniqueId, IMinion minion) {
        if (oldUUID != null){
            entityMinionMap.remove(oldUUID);
        }
        entityMinionMap.put(uniqueId, minion);
    }

    class MinionTicker extends BaseTicker<IMinion> {
        int minionTick = 0;

        public MinionTicker(){
            setBatchInterval(1);
        }

        @Override
        public void accept(IMinion iMinion) {
            doMinionTick(iMinion, minionTick);
        }

        @Override
        protected Collection<? extends IMinion> getNextBatch() {
            return getMinions();
        }
    }

    class PlayerTicker extends BaseTicker<Player>{
        @Override
        public void accept(Player player) {
            doSanityCheck(player);
        }
        @Override
        protected Collection<? extends Player> getNextBatch() {
            return Bukkit.getOnlinePlayers();
        }
    }
}
