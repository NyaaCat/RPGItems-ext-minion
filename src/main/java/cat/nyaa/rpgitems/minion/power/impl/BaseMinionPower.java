package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.minion.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import think.rpgitems.power.Property;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMinionPower extends BasePower {

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

    public TargetMode getTargetMode() {
        return targetMode;
    }

    public int getSlotCost() {
        return slotCost;
    }
}
