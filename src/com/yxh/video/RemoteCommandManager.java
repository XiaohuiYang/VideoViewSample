package com.yxh.video;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.jmdns.ServiceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.yxh.network.NsdHelper2;

public class RemoteCommandManager {
	
	public static final int COMMAND_PLAY = 0;
	public static final int COMMAND_PAUSE = 1;
	protected static final String TAG = RemoteCommandManager.class.getName();
	private NsdHelper2 nsdHelper;
	private ServerSocket serverSocket;
	private Thread serverThread;
	
	public RemoteCommandManager(NsdHelper2 mNsdHelper) {
		this.nsdHelper = mNsdHelper;
	}

	public void broadcastCammand(final int commandType, final String source) {
		List<ServiceInfo> buddies = nsdHelper.getServiceInfos();
		for (final ServiceInfo endPoint : buddies) {
			Thread t = new Thread () {
				public void run() {
					try {
						Socket s = new Socket(endPoint.getInet4Addresses()[0], endPoint.getPort());
						sendMessage(s, commandType, source);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}

			};
			t.start();
		}
	}
	
	public void sendMessage(Socket s, final int commandType, final String source) {
		try {
			PrintWriter pw = new PrintWriter(s.getOutputStream());
			JSONObject output = new JSONObject();
			output.put("type", commandType);
			output.put("source", source);
			pw.println(output.toString());
			Log.d(TAG, output.toString() + " Socket : " + s.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void broadcastCammand(int commandPlay) {
		broadcastCammand(commandPlay, generateSource());
	}

	private String generateSource() {
		return android.os.Build.MANUFACTURER + android.os.Build.PRODUCT ;
	}

	public void listen(final BlockingQueue<JSONObject> blockingQueue) throws IOException {
		serverSocket = new ServerSocket(0);
		nsdHelper.registerService(serverSocket.getLocalPort());
		serverThread = new Thread () {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Socket socket = serverSocket.accept();
						handleSocket(socket, blockingQueue);
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			private void handleSocket(Socket socket, BlockingQueue<JSONObject> blockingQueue) {
				try {
					String msg = getMessageFromSocket(socket);
					JSONObject json  = new JSONObject(msg);
					Log.d(TAG, msg);
					blockingQueue.put(json);
				} catch (JSONException e) {
					Log.e(RemoteCommandManager.class.getName(), "Recieved wrong formatted Message.");
				} catch (IOException e) {
					Log.e(RemoteCommandManager.class.getName(), "Can not get remote msg from socket.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			private String getMessageFromSocket(Socket socket) throws IOException {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String msg = br.readLine();
				br.close();
				return msg;
			}
		};
		serverThread.start();	
	}
	
	public void tearDown() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
			if (serverThread != null) {
				serverThread.interrupt();
			}
			nsdHelper.reset();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
