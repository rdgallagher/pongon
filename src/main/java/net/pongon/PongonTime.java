package net.pongon;

import net.minecraft.world.World;

/**
 * The shared notion of Pongon's day/night state. "Hot" = daytime = the design's
 * 300 °C+ threshold: the surface is lava, the star is up, and Lava Blobs spawn.
 * Both the day/night lava cycle and Lava Blob spawning/despawning key off this, so
 * the rule lives in one place.
 */
public final class PongonTime {
    private PongonTime() {}

    public static boolean isHot(World world) {
        return world.getTimeOfDay() % 24000L < 12000L;
    }
}
