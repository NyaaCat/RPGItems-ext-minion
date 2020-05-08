package cat.nyaa.rpgitems.minion.database;

import cat.nyaa.nyaacore.orm.annotations.Column;
import cat.nyaa.nyaacore.orm.annotations.Table;
import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;

@Table("player_data")
public class PlayerData {
    @Column(primary = true, unique = true)
    public String uuid;
    @Column(name = "slot")
    public int slotMax = MinionExtensionPlugin.plugin.config().defaultSlotMax;
}
