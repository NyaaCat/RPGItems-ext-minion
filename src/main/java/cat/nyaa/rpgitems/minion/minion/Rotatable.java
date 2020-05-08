package cat.nyaa.rpgitems.minion.minion;

public interface Rotatable {
    void setSpeed(double speed);
    double getSpeed();

    void reset();
    void commitRotating();
    void cancelRotating();

    boolean isRotating();
}
