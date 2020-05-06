package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
    private EntityType type;
    private List<String> tags;
    private String nbt;

    public EntityInfo(EntityType type, List<String> tags, String nbt){
        this.type = type;
        this.tags = tags;
        this.nbt = nbt;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getNbt() {
        return nbt;
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
    }
}
