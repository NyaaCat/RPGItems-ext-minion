package cat.nyaa.rpgitems.minion.power.marker;

import think.rpgitems.power.Meta;
import think.rpgitems.power.Property;

@Meta(marker = true)
public class MinionMax extends ExtBaseMarker {
    @Property
    public int max = 1;

    public int getMax() {
        return max;
    }

    @Override
    public String displayText() {
        return "increase minion max";
    }

    @Override
    public String getName() {
        return "minionmax";
    }

    @Override
    public Integer getValue() {
        return max;
    }
}
