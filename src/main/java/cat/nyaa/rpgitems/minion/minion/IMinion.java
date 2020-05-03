package cat.nyaa.rpgitems.minion.minion;

public interface IMinion extends Targetable, Movable, EntityHolder {

    void ambientAction();

    MinionStatus getStatus();
}
