package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.minion.MoveType;
import cat.nyaa.rpgitems.minion.minion.SpinMode;
import cat.nyaa.rpgitems.minion.minion.TargetMode;
import cat.nyaa.rpgitems.minion.power.BasePluginPower;
import org.bukkit.entity.EntityType;
import think.rpgitems.power.Deserializer;
import think.rpgitems.power.Property;
import think.rpgitems.power.Serializer;
import think.rpgitems.utils.cast.RangedDoubleValue;
import think.rpgitems.utils.cast.RangedValueSerializer;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseMinionPower extends BasePluginPower {

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
    double targetRange = 24d;
    @Property
    int attackInteval = 20;
    @Property
    double ranAtkIntFac = 0.1;
    @Property
    int ttl = 2400;
    @Property
    int slotCost = 1;
    @Property
    double damage = 1;
    @Property
    List<String> nbtTags = new ArrayList<>();
    @Property
    boolean autoAttackTarget = true;
    @Property
    double spawnRange = 64;

    @Property
    SpinMode spinMode = SpinMode.OFF;

    @Property
    @Serializer(RangedValueSerializer.class)
    @Deserializer(RangedValueSerializer.class)
    RangedDoubleValue spinSpeed = RangedDoubleValue.of("0");

    public SpinMode getSpinMode() {
        return spinMode;
    }

    public RangedDoubleValue getSpinSpeed() {
        return spinSpeed;
    }

    public double getSpawnRange() {
        return spawnRange;
    }

    public boolean isAutoAttackTarget() {
        return autoAttackTarget;
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

    public double getDamage() {
        return damage;
    }

    public double getRanAtkIntFac() {
        return ranAtkIntFac;
    }

    public List<String> getNbtTags() {
        return nbtTags;
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
