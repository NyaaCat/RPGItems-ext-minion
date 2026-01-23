package cat.nyaa.rpgitems.minion.minion;

/**
 * Priority levels for minion targeting.
 * Higher ordinal = higher priority.
 */
public enum TargetPriority {
    /**
     * Auto-targeting by the minion itself.
     * Lowest priority, can be overridden by any other source.
     */
    AUTO,

    /**
     * Target acquired from player combat (player attacking or being attacked).
     * Medium priority, overrides AUTO but not MANUAL.
     */
    PLAYER_COMBAT,

    /**
     * Manually designated target by the player.
     * Highest priority, cannot be overridden by auto-targeting or combat.
     */
    MANUAL;

    /**
     * Check if this priority can override another priority.
     * @param other The priority to compare against
     * @return true if this priority is higher than or equal to the other
     */
    public boolean canOverride(TargetPriority other) {
        if (other == null) {
            return true;
        }
        return this.ordinal() >= other.ordinal();
    }
}
