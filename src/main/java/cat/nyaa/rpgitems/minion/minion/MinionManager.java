package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.events.MinionMaxEvent;
import cat.nyaa.rpgitems.minion.minion.impl.BaseMinion;
import cat.nyaa.rpgitems.minion.utils.BaseTicker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import think.rpgitems.power.Utils;

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

    public Optional<SharedTarget> getSharedTarget(Player player, IMinion requester, Entity seeker, double range) {
        if (player == null || requester == null || seeker == null || seeker.isDead()) {
            return Optional.empty();
        }

        Map<UUID, SharedTargetCandidate> candidates = new HashMap<>();
        for (IMinion minion : getMinions(player)) {
            Entity target = minion.getTarget();
            if (!isShareableTarget(requester, seeker, target, range)) {
                continue;
            }

            TargetPriority priority = minion.getTargetPriority();
            if (priority == null) {
                priority = TargetPriority.AUTO;
            }
            double distanceSquared = distanceSquared(seeker, target);
            SharedTargetCandidate candidate = candidates.computeIfAbsent(target.getUniqueId(), uuid -> new SharedTargetCandidate(target));
            candidate.add(priority, distanceSquared);
        }

        return candidates.values().stream()
                .max(Comparator
                        .comparingInt(SharedTargetCandidate::priorityWeight)
                        .thenComparingInt(SharedTargetCandidate::getCount)
                        .thenComparingDouble(candidate -> -candidate.getNearestDistanceSquared()))
                .map(SharedTargetCandidate::toSharedTarget);
    }

    private boolean isShareableTarget(IMinion requester, Entity seeker, Entity target, double range) {
        return target != null
                && !target.isDead()
                && requester.isValidTarget(target)
                && isReachable(seeker, target, range);
    }

    private boolean isReachable(Entity seeker, Entity target, double range) {
        try {
            Location seekerLocation = BaseMinion.getSelfLocation(seeker);
            Location targetLocation = BaseMinion.getSelfLocation(target);
            if (seekerLocation.getWorld() == null || !seekerLocation.getWorld().equals(targetLocation.getWorld())) {
                return false;
            }
            double maxDistance = Math.max(0, range);
            return seekerLocation.distanceSquared(targetLocation) <= maxDistance * maxDistance;
        } catch (Exception e) {
            return false;
        }
    }

    private double distanceSquared(Entity seeker, Entity target) {
        try {
            return BaseMinion.getSelfLocation(seeker).distanceSquared(BaseMinion.getSelfLocation(target));
        } catch (Exception e) {
            return Double.MAX_VALUE;
        }
    }

    public static class SharedTarget {
        private final Entity entity;
        private final TargetPriority priority;

        private SharedTarget(Entity entity, TargetPriority priority) {
            this.entity = entity;
            this.priority = priority;
        }

        public Entity getEntity() {
            return entity;
        }

        public TargetPriority getPriority() {
            return priority;
        }
    }

    private static class SharedTargetCandidate {
        private final Entity entity;
        private TargetPriority priority = TargetPriority.AUTO;
        private int count = 0;
        private double nearestDistanceSquared = Double.MAX_VALUE;

        private SharedTargetCandidate(Entity entity) {
            this.entity = entity;
        }

        private void add(TargetPriority priority, double distanceSquared) {
            if (priority.ordinal() > this.priority.ordinal()) {
                this.priority = priority;
            }
            count++;
            nearestDistanceSquared = Math.min(nearestDistanceSquared, distanceSquared);
        }

        private int priorityWeight() {
            return priority.ordinal();
        }

        private int getCount() {
            return count;
        }

        private double getNearestDistanceSquared() {
            return nearestDistanceSquared;
        }

        private SharedTarget toSharedTarget() {
            return new SharedTarget(entity, priority);
        }
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
        Entity entity = iMinion.getEntity();
        if (entity == null){
            // Entity is null, just tick to trigger respawn
            iMinion.tick(minionTick);
            return;
        }
        Set<String> scoreboardTags = entity.getScoreboardTags();
        if (!scoreboardTags.contains(Utils.INVALID_TARGET)){
            entity.addScoreboardTag(Utils.INVALID_TARGET);
        }
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setAI(false);
        }
        iMinion.tick(minionTick);
    }

    private void doSanityCheck(Player player) {
        List<IMinion> toRemove = new ArrayList<>();
        getMinions(player).forEach(iMinion -> {
            Entity entity = iMinion.getEntity();
            if (entity == null || !entityMinionMap.containsKey(entity.getUniqueId())) {
                toRemove.add(iMinion);
            }
        });
        if (toRemove.isEmpty()) {
            return;
        }
        toRemove.forEach(this::removeMinion);
    }

    public void removeMinion(UUID uuid) {
        IMinion iMinion = entityMinionMap.remove(uuid);
        if (iMinion != null){
            iMinion.remove();
            OfflinePlayer owner = iMinion.getOwner();
            if (owner != null){
                // Use owner's UUID directly instead of relying on getPlayer()
                // This ensures cleanup works even when owner is offline
                List<IMinion> playerMinions = playerMinionMap.get(owner.getUniqueId());
                if (playerMinions != null) {
                    playerMinions.remove(iMinion);
                }
            }
        } else {
            // Minion not in map, but try to remove entity by UUID anyway
            // This handles cases where entity persists but minion tracking was lost
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
    }

    public void removeMinion(IMinion iMinion) {
        if (iMinion == null) {
            return;
        }
        Entity entity = iMinion.getEntity();
        if (entity != null) {
            UUID entityUuid = entity.getUniqueId();
            // Remove from map
            entityMinionMap.remove(entityUuid);
        }
        // Also try to find and remove from entityMinionMap by value (in case UUID doesn't match)
        entityMinionMap.values().remove(iMinion);

        // Always call remove() to ensure entity is despawned
        iMinion.remove();

        // Clean up player minion list
        OfflinePlayer owner = iMinion.getOwner();
        if (owner != null) {
            List<IMinion> playerMinions = playerMinionMap.get(owner.getUniqueId());
            if (playerMinions != null) {
                playerMinions.remove(iMinion);
            }
        }
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
            int batchInterval = 20;
            if (MinionExtensionPlugin.plugin != null && MinionExtensionPlugin.plugin.config() != null) {
                batchInterval = MinionExtensionPlugin.plugin.config().minionTickInterval;
            }
            setBatchInterval(Math.max(1, batchInterval));
        }

        @Override
        public void accept(IMinion iMinion) {
            doMinionTick(iMinion, minionTick);
        }

        @Override
        public void run() {
            super.run();
            minionTick++;
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
