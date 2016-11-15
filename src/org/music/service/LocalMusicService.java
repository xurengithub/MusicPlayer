package org.music.service;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;


public class LocalMusicService extends Service implements OnCompletionListener {
	private static final int PLAYING = 1;
	private static final int PAUSE = 2;
	private static final int STOP = 3;
	private static final int PROGRESS_CHANGE = 4;
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private MediaPlayer mp;
	private Handler handler;
	private Uri uri = null;
	private int id = 10000;
	private int currentTime;
	private int duration;

	@Override
	public void onCreate() {
		if (mp != null) {
			mp.reset();
			mp.release();
		}
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
	}

	@Override
	public void onDestroy() {
		if (mp != null) {
			stop();

		}
		if (handler != null) {
			handler.removeMessages(1);
			handler = null;
		}
	}


	@Override
	public void onStart(Intent intent, int startId) {
		

		int _id = intent.getIntExtra("_id", -1);
		if (_id != -1) {
			if (id != _id) {
				id = _id;
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _id);

				try {
					mp.reset();
					mp.setDataSource(this, uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		setup();
		init();

		int op = intent.getIntExtra("op", -1);
		if (op != -1) {
			switch (op) {
			case PLAYING:
				play();
				break;
			case PAUSE:
				pause();
				break;
			case STOP:
				stop();
				break;
			case PROGRESS_CHANGE:
				int progress = intent.getExtras().getInt("progress");
				mp.seekTo(progress);
				break;
			}
		}

	}


	private void play() {
		if (mp != null) {
			mp.start();
		}
	}


	private void pause() {
		if (mp != null) {
			mp.stop();
		}
		System.out.println("�����Ѿ�ֹͣ");
	}


	private void stop() {
		if (mp != null) {
			mp.stop();
			try {
				mp.prepare();
				mp.seekTo(0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			handler.removeMessages(1);
		}
	}


	private void init() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_CURRENT);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					currentTime = mp.getCurrentPosition();
					intent.putExtra("currentTime", currentTime);
					sendBroadcast(intent);

				}
				handler.sendEmptyMessageDelayed(1, 600);
			}
		};

	}


	private void setup() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_DURATION);
		try {
			if (!mp.isPlaying()) {
				mp.prepare();
				mp.start();
			} else if (!mp.isPlaying()) {
				mp.stop();
			}
			mp.setOnPreparedListener(new OnPreparedListener() {

				public void onPrepared(MediaPlayer mp) {
					handler.sendEmptyMessage(1);

				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		duration = mp.getDuration();
		intent.putExtra("duration", duration);
		sendBroadcast(intent);

	}

	public void onCompletion(MediaPlayer arg0) {
		Intent intent = new Intent();
		intent.setAction(MUSIC_NEXT);
		sendBroadcast(intent);
		System.out.println("���ֲ�����һ��");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
