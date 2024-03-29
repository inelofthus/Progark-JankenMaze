package com.tdt4240.jankenmaze;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tdt4240.jankenmaze.PlayServices.PlayServices;
import com.tdt4240.jankenmaze.gamesettings.GameSettings;
import com.tdt4240.jankenmaze.states.GameStateManager;
import com.tdt4240.jankenmaze.states.MultiPlayState;
import com.tdt4240.jankenmaze.states.OfflineMenuState;
import com.tdt4240.jankenmaze.states.OnlineMenuState;
import com.tdt4240.jankenmaze.states.PlayState;

public class JankenMaze extends ApplicationAdapter implements PlayServices.GameListener {
	SpriteBatch batch;
	GameStateManager gsm;
	PlayServices playServices;
	PlayState multiPlayState;


	//Constructor for the android app
	public JankenMaze(PlayServices playServices) {
		this.playServices = playServices;
	}

	//constructor for the Desktop app
	public JankenMaze() {

	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		gsm = GameStateManager.getGsm();
		gsm.setPlayServices(playServices);
		playServices.setGameListener(this);
		this.multiPlayState = new MultiPlayState(batch);
		if (playServices.isSignedIn()){
			gsm.push(new OnlineMenuState());
		}else {
			gsm.push(new OfflineMenuState());
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		gsm.update(Gdx.graphics.getDeltaTime());
		gsm.render(batch);
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	@Override
	public void onMultiplayerGameStarting() {
		System.out.println("JankenMaze: onMultiplayerGameStarting");
		gsm.set(multiPlayState);
	}

	@Override
	public void onDisconnectedFromRoom() {
		GameSettings.getInstance().reset();
		//multiPlayState.reset();
		this.multiPlayState = new MultiPlayState(batch);
		gsm.push(new OnlineMenuState("you got disconnected from the room"));
	}

	@Override
	public void resetGameVariables() {
		GameSettings.getInstance().reset();
		System.out.println("JankenMaze: resetGameVariables");
		//this.multiPlayState = new MultiPlayState(batch);
	}

	@Override
	public String toString() {
		return "JankenMazeClass";
	}
}
