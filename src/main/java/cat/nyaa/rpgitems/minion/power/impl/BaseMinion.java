package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionStatus;
import cat.nyaa.rpgitems.minion.minion.MoveType;
import cat.nyaa.rpgitems.minion.minion.TargetMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import think.rpgitems.power.Property;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMinion extends BasePower implements IMinion {
    protected Entity entity;

    @Override
    public Entity getEntity() {
        return entity;
    }

    protected Entity target;
    protected Location targetLocation;

    @Property
    EntityType entityType = EntityType.ZOMBIE;
    @Property
    String nbt = "";
    @Property
    String display = "minion";
    @Property
    TargetMode targetMode = TargetMode.MOBS;
    @Property
    MoveType moveType = MoveType.TELEPORT;
    @Property
    double movementSpeed = 5d;
    @Property
    double targetRange = 48d;
    @Property
    int attackInteval = 20;
    @Property
    int ttl = 2400;
    @Property
    int slotCost = 1;
    @Property
    List<String> tags = new ArrayList<>();

    {
        tags.add("rpgitems-minion");
    }

    @Override
    public int getSlotCost() {
        return slotCost;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getNbt() {
        return nbt;
    }

    public String getDisplay() {
        return display;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public int getAttackInteval() {
        return attackInteval;
    }

    public int getTtl() {
        return ttl;
    }

    public List<String> getTags() {
        return tags;
    }

    public double getTargetRange() {
        return targetRange;
    }

    @Override
    public TargetMode getTargetMode() {
        return targetMode;
    }

    public void iTest(){}

    @Override
    public void attack(Location location) {

    }

    @Override
    public void attack(Entity entity) {

    }

    @Override
    public void ambientAction() {

    }

    @Override
    public MinionStatus getStatus() {
        return null;
    }

    @Override
    public Entity getTarget() {
        return null;
    }

    @Override
    public void setTarget(Entity target) {

    }

    @Override
    public void respawn() {

    }

    @Override
    public void despawn() {

    }
}