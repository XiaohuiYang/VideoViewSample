package com.yxh.video;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class MVideoView extends VideoView {
	
	private PlayerListener mPlayerListener;

	public MVideoView(Context context) {
		super(context);
	}

    public MVideoView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }

    public MVideoView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }
    
    public PlayerListener getmPlayerListener() {
		return mPlayerListener;
	}

	public void setmPlayerListener(PlayerListener mPlayerListener) {
		this.mPlayerListener = mPlayerListener;
		mPlayerListener.setPlayer(this);
	}
	
	@Override
	public void start() {
		synchronized(this) {
			super.start();
			if (mPlayerListener != null) {
				mPlayerListener.doPlay();
			}
		}
	}
	
	@Override
	public void pause() {
		synchronized(this) {
			super.pause();
			if (mPlayerListener != null) {
				mPlayerListener.doPause();
			}
		}
	}
	
	public void remoteStart() {
		synchronized(this) {
			super.start();
		}
	}
	
	public void remotePause() {
		synchronized (this) {
			super.pause();
		}
	}

	public interface PlayerListener {
    	void doPlay();
    	void doPause();
    	void setPlayer(MVideoView player);
    }
}
