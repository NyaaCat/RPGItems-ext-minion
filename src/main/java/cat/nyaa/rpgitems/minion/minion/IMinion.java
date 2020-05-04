package cat.nyaa.rpgitems.minion.minion;

public interface IMinion extends Targetable, EntityHolder {

    void ambientAction();
    MinionStatus getStatus();

    int getSlotCost();
}
