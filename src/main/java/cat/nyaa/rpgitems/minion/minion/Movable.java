package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;

public interface Movable {
    void moveTo(Location location);
    void teleportTo(Location location);
    void rotate(double angle);
    void rotate(double angle, double speed);
    void rotateAngle(double angle);
    void rotateAngle(double angle, double speed);
    void pitch(double angle);
    void pitch(double angle, double speed);
    void pitchAngle(double angle);
    void pitchAngle(double angle, double speed);

    void commitRotating();
    void cancelRotating();
}
