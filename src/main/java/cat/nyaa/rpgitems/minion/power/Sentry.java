package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.minion.impl.ISentry;
import think.rpgitems.item.RPGItem;

public class Sentry extends BaseMinion implements ISentry {

    @Override
    public Sentry getSentryPower() {
        return this;
    }

    @Override
    public RPGItem getRPGItem() {
        return getItem();
    }

    @Override
    public String displayText() {
        return "summon a Sentry to fight for you";
    }

    @Override
    public String getName() {
        return "sentry";
    }
}
