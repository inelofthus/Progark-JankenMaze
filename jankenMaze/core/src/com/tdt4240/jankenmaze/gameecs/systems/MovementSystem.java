package com.tdt4240.jankenmaze.gameecs.systems;

import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.math.Rectangle;
import com.tdt4240.jankenmaze.gameMessages.PositionMessage;
import com.tdt4240.jankenmaze.gameecs.components.*;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.tdt4240.jankenmaze.gameecs.components.Position;
import com.tdt4240.jankenmaze.gameecs.components.Velocity;
import com.tdt4240.jankenmaze.gameecs.events.EventQueue;
import com.tdt4240.jankenmaze.gameecs.events.GameEvent;
import com.tdt4240.jankenmaze.gamesettings.GameSettings;

/**
 * Created by jonas on 07/03/2018.
 *
 * Super-quick How-to-System:
 * 1: Implement the appropiate logic (i.e. move the hudEntities who're supposed to be moved by device input or draw drawable stuff)
 * 2: Make an Array of relevant entites in addedToEnginge
 * -- Important: Here the array of hudEntities is null until addedToEngine is called.
 * -- Note that addedToEngine will be called automatically by the engine.
 * -- Note also that it's in addedToEngine the entity-selection takes place. (Family.all(...))
 * 3: In update(float dt), apply logic to the hudEntities (like movement).
 * 4: The ComponentMappers makes it easier to select the right entity-components (and gives good performance)
 * -- See line 30 and 42 for example.
 *
 * This system is updating the player positions.
 */

public class MovementSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private ImmutableArray<Entity> localPlayers;
    private ImmutableArray<Entity> remotePlayers;
    private ImmutableArray<Entity> walls;

    private ComponentMapper<PlayerNetworkData> playerDataCompMapper = ComponentMapper.getFor(PlayerNetworkData.class);
    private ComponentMapper<Position> positionMapper = ComponentMapper.getFor(Position.class);
    private ComponentMapper<Velocity> velocityMapper = ComponentMapper.getFor(Velocity.class);
    private ComponentMapper<BoundsBox> boundsBoxMapper =ComponentMapper.getFor(BoundsBox.class);
    private ComponentMapper<com.tdt4240.jankenmaze.gameecs.components.BoundsBox> bb= ComponentMapper.getFor(com.tdt4240.jankenmaze.gameecs.components.BoundsBox.class);

    private Signal<GameEvent> gameEventSignal;
    private EventQueue eventQueue;

    public MovementSystem (Signal<GameEvent> gameEventSignal){
        //creates an gameEventSignal
        this.gameEventSignal = gameEventSignal;
        eventQueue = new EventQueue();
        gameEventSignal.add(eventQueue);}

    public void addedToEngine(Engine engine){
        //get entities with velocity and position
        entities = engine.getEntitiesFor(Family.all(Position.class, Velocity.class).get());
        //get localPLayer
        localPlayers = engine.getEntitiesFor(Family.all(LocalPlayer.class).get());
        //get remotePLayers
        remotePlayers = engine.getEntitiesFor(Family.all(Remote.class).get());
        //get walls
        walls = engine.getEntitiesFor(Family.all(com.tdt4240.jankenmaze.gameecs.components.BoundsBox.class).exclude(com.tdt4240.jankenmaze.gameecs.components.PlayerInfo.class, com.tdt4240.jankenmaze.gameecs.components.PowerUpInfo.class).get());

    }

    public void update(float dt){
        //checks if there are some entities with both a positionComponent and a velocityComponent
        if(entities != null){
            //for each entity
            for (int i = 0; i < entities.size(); i++){
                Entity entity = entities.get(i);
                //get the position&velocity&boundingbox
                Position position = positionMapper.get(entity);
                Velocity velocity = velocityMapper.get(entity);
                BoundsBox bounds = boundsBoxMapper.get(entity);
                if (entity == localPlayers.get(0)) {
                    if(velocity.futureX != 0 || velocity.futureY != 0) {
                        //This is where future velocity is tested
                        bounds.boundsBox.setX(position.x+velocity.futureX*dt*4);
                        bounds.boundsBox.setY(position.y+velocity.futureY*dt*4);
                        boolean collision = false;
                        for(int k=0; k < walls.size(); k++) {
                            Rectangle wallBox = bb.get(walls.get(k)).boundsBox;
                            //If future velocity causes a collision
                            if (bounds.boundsBox.overlaps(wallBox)) {
                                collision = true;
                            }
                        }
                        if(collision){
                                bounds.boundsBox.setX(position.x += velocity.currentX * dt);
                                bounds.boundsBox.setY(position.y += velocity.currentY * dt);
                        }
                        else {
                            velocity.currentX = velocity.futureX;
                            velocity.currentY = velocity.futureY;
                            velocity.futureY = 0;
                            velocity.futureX = 0;
                            bounds.boundsBox.setX(position.x+velocity.currentX*dt);
                            bounds.boundsBox.setY(position.y+velocity.currentY*dt);
                        }
                    }
                    else {
                        bounds.boundsBox.setX(position.x += velocity.currentX * dt);
                        bounds.boundsBox.setY(position.y += velocity.currentY * dt);
                    }
                }
                else {
                    position.x += velocity.currentX * dt;
                    position.y += velocity.currentY * dt;
                    bounds.boundsBox.setX(position.x);
                    bounds.boundsBox.setY(position.y);
                }
            }
        }

        //Update the positions of the remote players
        for (Entity remotePlayer: remotePlayers){
            PlayerNetworkData netData = playerDataCompMapper.get(remotePlayer);
            Position newPos = PositionMessage.getInstance().getRemotePlayerPositions().get(netData.participantId);
            Position posComp = positionMapper.get(remotePlayer);

            posComp.x = newPos.x;
            posComp.y = newPos.y;
        }




    }
}
