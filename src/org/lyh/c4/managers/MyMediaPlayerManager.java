package org.lyh.c4.managers;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

public class MyMediaPlayerManager {
	
	private MediaPlayer mediaPlayer;
	Context context;
	
//	boolean isPrepare = false;
	
	public MyMediaPlayerManager(Context context) {
		this.mediaPlayer = new MediaPlayer();
		this.context = context;
	}

	public void setMusic(int resID) throws IOException{
		mediaPlayer.reset();
		AssetFileDescriptor file = context.getResources().openRawResourceFd(resID);
		this.mediaPlayer.setDataSource(file.getFileDescriptor(),file.getStartOffset(), file.getLength());
		this.mediaPlayer.prepare();
		this.mediaPlayer.setOnPreparedListener(new PrepareListener());
	}
	public void setRepetition(){
		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer player) {
						mediaPlayer.seekTo(0);
						mediaPlayer.start();
					}
				});
	}
	public void setUnRepetition(){
		mediaPlayer.setOnCompletionListener(null);
	}
	public void play(){
			mediaPlayer.start();
	}
	
	public void stop(){
		if(mediaPlayer.isPlaying()){
			mediaPlayer.stop();
		}
	}

	
	
	public class PrepareListener implements OnPreparedListener {
		
		@Override
		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			play();
		}
		
	}
	
	public void release(){
		this.mediaPlayer.release();
		this.mediaPlayer = null;
	}
}
