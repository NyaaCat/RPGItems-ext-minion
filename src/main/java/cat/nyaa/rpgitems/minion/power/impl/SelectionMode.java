package cat.nyaa.rpgitems.minion.power.impl;

/**
 * Selection mode for choosing which minion to target.
 */
public enum SelectionMode {
    /**
     * Randomly select a valid minion.
     */
    RANDOM,
    /**
     * Select the nearest valid minion.
     */
    NEAREST,
    /**
     * Select the farthest valid minion.
     */
    FARTHEST,
    /**
     * Select the minion the player is aiming at (highest dot product with look direction).
     */
    AIMING
}
