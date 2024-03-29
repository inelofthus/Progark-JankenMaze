package com.tdt4240.jankenmaze.gameecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.tdt4240.jankenmaze.gameMessages.HealthMessage;
import com.tdt4240.jankenmaze.gameMessages.PositionMessage;
import com.tdt4240.jankenmaze.gameecs.components.HUDItemInfo;
import com.tdt4240.jankenmaze.gameecs.components.Health;
import com.tdt4240.jankenmaze.gameecs.components.LocalPlayer;
import com.tdt4240.jankenmaze.gameecs.components.PlayerInfo;
import com.tdt4240.jankenmaze.gameecs.components.PlayerNetworkData;
import com.tdt4240.jankenmaze.gameecs.components.Position;
import com.tdt4240.jankenmaze.gameecs.components.Remote;
import com.tdt4240.jankenmaze.gameecs.components.Renderable;
import com.tdt4240.jankenmaze.gameecs.components.SpriteComponent;
import com.tdt4240.jankenmaze.gamesettings.GameSettings;
import com.tdt4240.jankenmaze.gamesettings.PlayerType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jonas on 18/03/2018.
 * Adds HUD-display to game
 */

public class HUDSystem extends EntitySystem {
    GameSettings gameSettings;
    ImmutableArray<Entity> hudEntities;
    ImmutableArray<Entity> localPlayerEntities;
    ImmutableArray<Entity> remoteEntities;
    ComponentMapper<Health> healthComponentMapper;
    ComponentMapper<HUDItemInfo> hudItemInfoComponentMapper;
    ComponentMapper<SpriteComponent> spriteComponentMapper;
    ComponentMapper<Position> positionComponentMapper;
    ComponentMapper<PlayerInfo> playerInfoMapper;
    ComponentMapper<PlayerNetworkData> netDataComponentMapper;
    private int numOfHealthSprites;
    private int maxHealthSpriteX;
    private boolean typeSpritesNotMade;

    private int typeSpriteWidth;
    private int typepriteHeight;

    HashMap<String, ArrayList<Entity>> remoteHP;

    int playerHealth;
    ArrayList<Entity> playerHearts;

    public HUDSystem() {
        healthComponentMapper = ComponentMapper.getFor(Health.class);
        hudItemInfoComponentMapper = ComponentMapper.getFor(HUDItemInfo.class);
        spriteComponentMapper = ComponentMapper.getFor(SpriteComponent.class);
        positionComponentMapper = ComponentMapper.getFor(Position.class);
        playerInfoMapper = ComponentMapper.getFor(PlayerInfo.class);
        // netDataComponentMapper = ComponentMapper.getFor(PlayerNetworkData.class);

        gameSettings = GameSettings.getInstance();
        typepriteHeight = 160;
        typeSpriteWidth = 160;

        playerHearts = new ArrayList<Entity>();
        numOfHealthSprites = 0;
        typeSpritesNotMade = true;
        maxHealthSpriteX = gameSettings.viewPortWidth - typeSpriteWidth;

        //remoteHP = new HashMap<String, ArrayList<Entity>>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        hudEntities = engine.getEntitiesFor(Family.all(
                HUDItemInfo.class, Renderable.class, Position.class).get());
        localPlayerEntities = engine.getEntitiesFor(Family.one(LocalPlayer.class).get());
        remoteEntities = engine.getEntitiesFor(Family.all(Remote.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        hudEntities = engine.getEntitiesFor(Family.all(
                HUDItemInfo.class, Renderable.class, Position.class).get());
        localPlayerEntities = engine.getEntitiesFor(Family.one(LocalPlayer.class).get());
        remoteEntities = engine.getEntitiesFor(Family.all(Remote.class).get());
    }

    /**
     * Updates HUD. Checks that the correct amount of health is displayed, and only changes sprites if not.
     * @param deltaTime
     */
    @Override
    public void update(float deltaTime) {
        //Health-display
        if (localPlayerEntities.size() == 0) {
            System.out.println("HUDSys:95: LocalplayerEntities is empty!");
            return;
        }
        Entity localPlayer = localPlayerEntities.first();
        int localPlayerHealth = healthComponentMapper.get(localPlayer).health;
        if(localPlayerHealth != numOfHealthSprites){
            addOrRemovePlayerHP(localPlayerHealth);
        }

        //Type display. Wrapped in if-clause to only run once (uses created sprites so shouldn't be in constructor)
        if (typeSpritesNotMade) {
            makeTypeDisplay(localPlayer);
            typeSpritesNotMade = false;
        }
    }

    private void makeTypeDisplay(Entity localPlayer){
        Engine engine = getEngine();

        //Make big localplayer-sprite
        Texture playerTexture = spriteComponentMapper.get(localPlayer).sprite.getTexture();
        float bigPlayerSpriteX = (gameSettings.viewPortWidth - typeSpriteWidth);
        float bigPlayerSpriteY = (gameSettings.viewPortHeight - typepriteHeight);

        engine.addEntity(new Entity()
                .add(new SpriteComponent(playerTexture, 160, 160))
                .add(new Position(bigPlayerSpriteX, bigPlayerSpriteY))
                .add(new Renderable())
                .add(new HUDItemInfo("playerType")));
    }


    private void addOrRemovePlayerHP(int localPlayerHealth) {
        //Either adds or removes healthsprites depending on player health.
        //Uses arrays keeping track of entities to check status quicker (no searching for entities)
        Texture healthTexture = new Texture("greenSquare.png");
        //  Adding healthsprites:
        int margin = 8;
        while (numOfHealthSprites < localPlayerHealth) {
            Entity heartEntity = new Entity()
                    .add(new HUDItemInfo("playerHealth"))
                    .add(new SpriteComponent(healthTexture))
                    .add(new Position(maxHealthSpriteX,
                            gameSettings.viewPortHeight - (typepriteHeight + healthTexture.getHeight()) - margin))
                    .add(new Renderable());

            getEngine().addEntity(heartEntity);
            playerHearts.add(heartEntity);
            maxHealthSpriteX += healthTexture.getWidth() + margin;
            numOfHealthSprites++;
        }

        //Removing healthsprites:
        while (numOfHealthSprites > localPlayerHealth) {
            if (playerHearts.size() >= 1) {
                Entity lastHeart = playerHearts.get(playerHearts.size() - 1);
                getEngine().removeEntity(lastHeart);
                playerHearts.remove(lastHeart);
                maxHealthSpriteX -= healthTexture.getWidth() + margin;

            }
            numOfHealthSprites--;
        }

    }

    private void makeOpponentHud() {
        Entity localPlayer = localPlayerEntities.first();
        int remoteHpSize = 12;
        int margin = 4;
        //Check HP for remotes, then maybe add/remove sprites
        HashMap<String, Position> positionMap = PositionMessage.getInstance().getRemotePlayerPositions();
        HealthMessage healthMessage = HealthMessage.getInstance();
        // String[] playerIds = (String[]) healthMessage.getRemotePlayerHealth().keySet().toArray();
    }
}