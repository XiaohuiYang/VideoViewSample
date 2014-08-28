/* Copyright (c) 2013, Intel Corporation
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright notice, 
*   this list of conditions and the following disclaimer.
* - Redistributions in binary form must reproduce the above copyright notice, 
*   this list of conditions and the following disclaimer in the documentation 
*   and/or other materials provided with the distribution.
* - Neither the name of Intel Corporation nor the names of its contributors 
*   may be used to endorse or promote products derived from this software 
*   without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
*
*/

package com.example.videoviewsample;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.widget.MediaController;

import com.yxh.network.NsdHelper2;
import com.yxh.video.MVideoView;
import com.yxh.video.RemoteMediaController;

public class VideoViewSample extends Activity
{
	private MVideoView mVideoView;

	private MediaController mController;
	
	MediaMetadataRetriever mMetadataRetriever;

	private RemoteMediaController rc;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mVideoView = (MVideoView) findViewById(R.id.myplaysurface);
		
		mMetadataRetriever = new MediaMetadataRetriever();

		Intent intent = new Intent();

		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(Intent.createChooser(intent, "Video File to Play"), 0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	public void startPlayback(String videoPath)
	{
		mMetadataRetriever.setDataSource(videoPath);
		
		Uri uri = Uri.parse(videoPath);
		mVideoView.setVideoURI(uri);
		
		NsdHelper2 mNsdHelper = new NsdHelper2(this);
        mNsdHelper.discoverServices();
		
		mController = new MediaController(this, false);
		rc = new RemoteMediaController(mNsdHelper);
		
		mVideoView.setMediaController(mController);
		mVideoView.setmPlayerListener(rc);
		rc.run();
		mVideoView.requestFocus();
		mVideoView.remoteStart();
		mVideoView.remotePause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.video_view_sample, menu);
	
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 0)
		{
			if (resultCode == RESULT_OK)
			{
				Uri sourceUri = data.getData();
				String source = getPath(sourceUri);

				startPlayback(source);
			}
		}
	}

	public String getPath(Uri uri)
	{
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);

		if (cursor == null)
		{
			return uri.getPath();
		} else
		{
			cursor.moveToFirst();

			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

			return cursor.getString(idx);
		}
	}
	
    @Override
    protected void onDestroy() {
    	if (rc != null) {
    		rc.tareDown();
    	}
        super.onDestroy();
    }
}
