package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.*;
import cat.nyaa.rpgitems.minion.power.*;

public class BaseTrigger {
    public static MinionAmbient MINION_AMBIENT = new MinionAmbient("MINION_AMBIENT", MinionAmbientEvent.class, PowerMinionAmbient.class, Void.class, Void.class);
    public static MinionAttack MINION_ATTACK = new MinionAttack("MINION_ATTACK", MinionAttackEvent.class, PowerMinionAttack.class, Void.class, Void.class);
    public static MinionChangeTarget MINION_CHANGE_TARGET = new MinionChangeTarget("MINION_CHANGE_TARGET", MinionChangeTargetEvent.class, PowerMinionChangeTarget.class, Void.class, Void.class);
    public static MinionMove MINION_MOVE = new MinionMove("MINION_MOVE", MinionMoveEvent.class, PowerMinionMove.class, Void.class, Void.class);
    public static MinionSpawn MINION_SPAWN = new MinionSpawn("MINION_SPAWN", MinionSpawnEvent.class, PowerMinionSpawn.class, Void.class, Void.class);
}
