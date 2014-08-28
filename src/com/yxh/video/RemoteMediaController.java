package com.yxh.video;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

import com.yxh.network.NsdHelper2;

public class RemoteMediaController implements MVideoView.PlayerListener {
	
	private RemoteCommandManager commanderManager;
	private MVideoView mPlayer;
	private BlockingQueue<JSONObject> commandQueue =   new ArrayBlockingQueue<JSONObject>(100);
	private Thread serverThread;

	public RemoteMediaController(NsdHelper2 mNsdHelper) {
		commanderManager = new RemoteCommandManager(mNsdHelper);
	}

	public void doPlay(){
		commanderManager.broadcastCammand(RemoteCommandManager.COMMAND_PLAY);
	}
	
	public void doPause() {
		commanderManager.broadcastCammand(RemoteCommandManager.COMMAND_PAUSE);
	}
	
	
	public void run() {
		try {
			commanderManager.listen(getCommandQueue());
			serverThread = new Thread() {
				public void run() {
					while (true) {
						try {
							JSONObject task = getCommandQueue().take();
	
							if (task.getInt("type") == RemoteCommandManager.COMMAND_PLAY && !mPlayer.isPlaying()) {
								mPlayer.remoteStart();
							}
							else if (task.getInt("type") ==  RemoteCommandManager.COMMAND_PAUSE && mPlayer.isPlaying()) {
								mPlayer.remotePause();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				}
			};
			serverThread.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public BlockingQueue<JSONObject> getCommandQueue() {
		return commandQueue;
	}
	
	public void tareDown() {
		if (serverThread != null) {
			serverThread.interrupt();
		}
		commanderManager.tearDown();
	}

	@Override
	public void setPlayer(MVideoView player) {
		this.mPlayer = player;
	}

	
}
