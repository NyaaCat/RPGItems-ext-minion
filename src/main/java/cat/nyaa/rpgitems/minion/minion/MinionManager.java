package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.events.MinionMaxEvent;
import cat.nyaa.rpgitems.minion.utils.BaseTicker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import think.rpgitems.power.Utils;

import javax.xml.crypto.Data;
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
    }

    public void init(){
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
        if (iMinion.isRemoved()){
            return;
        }
        OfflinePlayer oPlayer = iMinion.getOwner();
        if (oPlayer == null || !oPlayer.isOnline()) {
            removeMinion(iMinion);
            return;
        }
        if (minionTick%20 == 0){
            boolean ticked = false;
            Entity entity = iMinion.getEntity();
            if (entity == null){
                iMinion.tick(minionTick);
                ticked = true;
            }
            Set<String> scoreboardTags = entity.getScoreboardTags();
            if (!scoreboardTags.contains(Utils.INVALID_TARGET)){
                entity.addScoreboardTag(Utils.INVALID_TARGET);
            }
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).setAI(false);
            }
            if (ticked){
                return;
            }
        }
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

    public void removeMinion(UUID uuid) {
        IMinion iMinion = entityMinionMap.remove(uuid);
        if (iMinion!=null){
            iMinion.remove();
            OfflinePlayer opt = iMinion.getOwner();
            Player player;
            if (opt != null && (player = opt.getPlayer()) != null){
                playerMinionMap.computeIfAbsent(player.getUniqueId(), uuid1 -> createMinionList()).remove(iMinion);
            }
        }
    }

    public void removeMinion(IMinion iMinion) {
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
            setBatchInterval(0);
        }

        @Override
        public void accept(IMinion iMinion) {
            doMinionTick(iMinion, minionTick++);
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
            doMaxCheck(player);
        }

        private void doMaxCheck(Player player) {
            PlayerData playerData = Database.getInstance().getPlayerData(player.getUniqueId());
            int baseMax = playerData.slotMax;
            MinionMaxEvent event = new MinionMaxEvent(player, baseMax);
            Bukkit.getPluginManager().callEvent(event);
            int max = event.getMax();
            MinionManager.getInstance().setMinionMax(player, max);
            checkMax(player);
        }

        @Override
        protected Collection<? extends Player> getNextBatch() {
            return Bukkit.getOnlinePlayers();
        }
    }

    public void setMinionMax(Player player, int max) {
        minionMaxMap.put(player.getUniqueId(), max);
    }

    public int getMinionMax(Player player){
        return minionMaxMap.get(player.getUniqueId());
    }
}
