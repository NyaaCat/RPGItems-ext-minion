package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionStatus;
import cat.nyaa.rpgitems.minion.minion.MoveType;
import cat.nyaa.rpgitems.minion.minion.TargetMode;
import cat.nyaa.rpgitems.minion.minion.impl.enums.SentryTypes;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import think.rpgitems.power.Property;

public abstract class BaseMinion extends BasePower implements IMinion {
    protected Entity entity;

    @Override
    public Entity getEntity() {
        return entity;
    }

    protected Entity target;
    protected Location targetLocation;

    @Property
    SentryTypes type = SentryTypes.REGULAR;
    @Property
    MoveType moveType = MoveType.TELEPORT;
    @Property
    double movementSpeed = 5d;
    @Property
    double targetRange = 48d;
    @Property
    TargetMode targetMode = TargetMode.MOBS;

    public SentryTypes getType() {
        return type;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public double getMovementSpeed() {
        return movementSpeed;
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
    public void moveTo(Location location) {
        teleportTo(location);
    }

    @Override
    public void teleportTo(Location location) {
        entity.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public void rotate(double angle) {
        Location location = entity.getLocation();
        float initialYaw = location.getYaw();
        float initialPitch = location.getPitch();

    }

    @Override
    public void rotate(double angle, double speed) {

    }

    @Override
    public void rotateAngle(double angle) {

    }

    @Override
    public void rotateAngle(double angle, double speed) {

    }
    @Override
    public void pitch(double angle) {

    }

    @Override
    public void pitch(double angle, double speed) {

    }

    @Override
    public void pitchAngle(double angle) {

    }

    @Override
    public void pitchAngle(double angle, double speed) {

    }

    @Override
    public void commitRotating() {

    }

    @Override
    public void cancelRotating() {

    }


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
}
