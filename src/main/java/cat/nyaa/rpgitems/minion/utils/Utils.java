package cat.nyaa.rpgitems.minion.utils;

import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static List<String> filtered(Arguments arguments, List<String> completeStr) {
        String next = arguments.at(arguments.length() - 1);
        return completeStr.stream().filter(s -> s.startsWith(next)).collect(Collectors.toList());
    }


    /**
     * get angle between two vectors, in radians.
     * if v2 is on the right half of v1, return value will be positive
     * @param v1 vector 1
     * @param v2 vector 2
     * @return angle between two vectors, in radians.
     */
    public static double angle(Vector v1, Vector v2){
        double angle;
        angle = v1.angle(v2);
        Vector crossProduct = v1.getCrossProduct(v2);
        double y = crossProduct.getY();
        if (y > 0){
            angle = -angle;
        }else if (y == 0){
            double length = crossProduct.length();
            if (length != 0 && Double.isFinite(length)){
                Vector va1 = v1.clone().normalize();
                Vector va2 = v2.clone().normalize();
                double atan1 = getAtan(va1);
                double atan2 = getAtan(va2);
                if (atan1 < atan2){
                    angle = -angle;
                }
            }
        }
        return angle;
    }

    private static double getAtan(Vector vector){
        double x = vector.getX();
        double z = vector.getZ();
        double y = vector.getY();
        double powX = Math.pow(x, 2);
        double powZ = Math.pow(z, 2);
        double atan = 90;
        if (powX + powZ != 0){
            atan = Math.atan2(y, Math.sqrt(powX + powZ));
        }
        return atan;
    }
}
