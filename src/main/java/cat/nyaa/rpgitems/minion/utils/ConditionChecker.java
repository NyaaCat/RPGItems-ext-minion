package cat.nyaa.rpgitems.minion.utils;

import cat.nyaa.rpgitems.minion.power.marker.ConditionedMarker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Condition;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.PropertyHolder;

import java.util.*;
import java.util.stream.Collectors;

public class ConditionChecker {
    public static <T> PowerResult<T> checkConditions(Player player, ItemStack i, ConditionedMarker<?> marker, List<Condition<?>> conds, Map<PropertyHolder, PowerResult<?>> context) {
        Set<String> markerConditions = marker.getConditions();
        List<Condition<?>> conditions = conds.stream().filter(p -> markerConditions.contains(p.id())).collect(Collectors.toList());
        List<Condition<?>> failed = conditions.stream().filter(p -> p.isStatic() ? !context.get(p).isOK() : !p.check(player, i, context).isOK()).collect(Collectors.toList());
        if (failed.isEmpty()) return null;
        return failed.stream().anyMatch(Condition::isCritical) ? PowerResult.abort() : PowerResult.condition();
    }

    public static Map<Condition<?>, PowerResult<?>> checkStaticCondition(Player player, ItemStack i, List<Condition<?>> conds, List<? extends ConditionedMarker<?>> markers) {
        Set<String> ids = markers.stream().flatMap(p -> p.getConditions().stream()).collect(Collectors.toSet());
        List<Condition<?>> statics = conds.stream().filter(Condition::isStatic).filter(p -> ids.contains(p.id())).collect(Collectors.toList());
        Map<Condition<?>, PowerResult<?>> result = new LinkedHashMap<>();
        for (Condition<?> c : statics) {
            result.put(c, c.check(player, i, Collections.unmodifiableMap(result)));
        }
        return result;
    }
}
