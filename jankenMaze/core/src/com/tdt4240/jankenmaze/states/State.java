package com.tdt4240.jankenmaze.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by jonas on 25/01/2018.
 * Abstract class to add states (see state-pattern). Copypaste from earlier LibGDX-project.
 */

public abstract class State {
        protected OrthographicCamera cam;
        protected Vector3 mouse;
        protected GameStateManager gsm;
        Viewport viewport;

        protected State(){
            gsm = GameStateManager.getGsm();
            cam = new OrthographicCamera();
            mouse = new Vector3();

            cam.setToOrtho(false, 800, 480);
            viewport = new FitViewport(800, 480, cam);
            viewport.setScreenBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            viewport.apply();

        }

        protected abstract void handleInput();
        public abstract void update(float dt);
        public abstract void render(SpriteBatch sb);
        public abstract void dispose();
}
