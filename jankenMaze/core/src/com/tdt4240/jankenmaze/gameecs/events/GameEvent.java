package com.tdt4240.jankenmaze.gameecs.events;

/**
 * Created by Ine on 22.03.2018.
 * This enum is used to decide the kind of event had happened.
 */

public enum GameEvent {
    GAME_OVER,
    WALL_COLLISION,
    PLAYER_COLLISION,
    POWERUP_COLLISION,
    DECREASE_HEALTH,
    PLAYER_DEATH
}
