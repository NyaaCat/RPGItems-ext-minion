package cat.nyaa.rpgitems.minion.utils;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }
}
