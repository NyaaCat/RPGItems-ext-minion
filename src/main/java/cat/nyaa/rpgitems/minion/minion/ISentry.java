package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.power.impl.Sentry;
import think.rpgitems.item.RPGItem;

public interface ISentry extends IMinion {
    Sentry getSentryPower();
}
