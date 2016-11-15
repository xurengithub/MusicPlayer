package org.music.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.TreeMap;

import org.music.tools.LRCbean;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.music.player.R;

public class PlayMusicActivity extends Activity {
	private int[] _ids;
	private String _titles[] = null;
	private String _artists[] = null;
	private int position;
	private ImageButton playbtn = null;
	private ImageButton latestBtn = null;
	private ImageButton nextBtn = null;
	private TextView playtime = null;
	private TextView durationTime = null;
	private SeekBar seekbar = null;
	private int currentPosition;
	private int duration;
	private TextView name = null;
	private TextView artist = null;
	private TextView lrcText = null;
	private static final String MUSIC_CURRENT = "com.music.currentTime";
	private static final String MUSIC_DURATION = "com.music.duration";
	private static final String MUSIC_NEXT = "com.music.next";
	private static final String MUSIC_UPDATE = "com.music.update";
	private static final int PLAY = 1;
	private static final int PAUSE = 2;
	private static final int STOP = 3;
	private static final int PROGRESS_CHANGE = 4;

	private static final int STATE_PLAY = 1;̬
	private static final int STATE_PAUSE = 2;
	private int flag;
	private Cursor cursor;
	private TreeMap<Integer, LRCbean> lrc_map = new TreeMap<Integer, LRCbean>();
	private ImageView albumpic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.playmusic);
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		_ids = bundle.getIntArray("_ids");
		position = bundle.getInt("position");
		_titles = bundle.getStringArray("_titles");
		_artists = bundle.getStringArray("_artists");
		name = (TextView) findViewById(R.id.name);
		artist = (TextView) findViewById(R.id.singer);
		lrcText = (TextView) findViewById(R.id.lrc);
		playtime = (TextView) findViewById(R.id.playtime);
		durationTime = (TextView) findViewById(R.id.duration);
		albumpic = (ImageView) findViewById(R.id.albumPic);
		ShowPlayBtn();
		ShowLastBtn();
		ShowNextBtn();
		ShowSeekBar();

	}

	private void ShowPlayBtn() {
		playbtn = (ImageButton) findViewById(R.id.playBtn);
		playbtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				switch (flag) {
				case STATE_PLAY:
					pause();
					break;

				case STATE_PAUSE:
					play();
					break;
				}

			}
		});

	}

	private void ShowSeekBar() {
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			
			public void onStopTrackingTouch(SeekBar seekBar) {
				play();

			}

			
			public void onStartTrackingTouch(SeekBar seekBar) {
				pause();

			}

			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					seekbar_change(progress);
				}

			}
		});

	}

	private void ShowNextBtn() {
		nextBtn = (ImageButton) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				nextOne();

			}
		});

	}

	private void ShowLastBtn() {
		latestBtn = (ImageButton) findViewById(R.id.lastBtn);
		latestBtn.setOnClickListener(new View.OnClickListener() {

			
			public void onClick(View v) {
				latestOne();
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		setup();
		play();
	}

	
	protected void onStop() {
		super.onStop();
		unregisterReceiver(musicreceiver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(PlayMusicActivity.this,
					TabMusicActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
	}


	protected void play() {
		flag = PLAY;
		playbtn.setImageResource(R.drawable.pause_button);
		Intent intent = new Intent();
		intent.setAction("org.music.service.LocalMusicService");
		intent.putExtra("op", PLAY);
		startService(intent);

	}


	protected void pause() {
		flag = PAUSE;
		playbtn.setImageResource(R.drawable.play_button);
		Intent intent = new Intent();
		intent.setAction("org.music.service.LocalMusicService");
		intent.putExtra("op", PAUSE);
		startService(intent);

	}


	protected void latestOne() {
		if (position == 0) {
			position = _ids.length - 1;
		} else if (position > 0) {
			position--;
		}
		stop();
		setup();
		play();

	}


	private void stop() {
		Intent intent = new Intent();
		intent.setAction("org.music.service.LocalMusicService");
		intent.putExtra("op", STOP);
		startService(intent);
	}


	protected void nextOne() {
		if (position == _ids.length - 1) {
			position = 0;
		} else if (position < _ids.length - 1) {
			position++;
		}
		stop();
		setup();
		play();

	}


	protected void seekbar_change(int progress) {
		Intent intent = new Intent();
		intent.setAction("org.music.service.LocalMusicService");
		intent.putExtra("op", PROGRESS_CHANGE);
		intent.putExtra("progress", progress);
		startService(intent);

	}


	private void setup() {
		loadclip();
		init();
		ReadSDLrc();

	}


	private void ReadSDLrc() {
		cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media.ALBUM,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.ALBUM_ID }, "_id=?",
				new String[] { _ids[position] + "" }, null);
		cursor.moveToFirst();
		albumpic.setImageBitmap(getArtwork(this, _ids[position], cursor.getInt(5), true));
		String name = cursor.getString(4);
		read("/sdcard/" + name.substring(0, name.indexOf(".")) + ".lrc");
		System.out.println(cursor.getString(4));

	}


	private void init() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(MUSIC_CURRENT);
		filter.addAction(MUSIC_DURATION);
		filter.addAction(MUSIC_NEXT);
		filter.addAction(MUSIC_UPDATE);
		registerReceiver(musicreceiver, filter);

	}


	private void loadclip() {
		seekbar.setProgress(0);
		int pos = _ids[position];
		name.setText(_titles[position]);
		artist.setText(_artists[position]);
		Intent intent = new Intent();
		intent.putExtra("_id", pos);
		intent.putExtra("_titles", _titles);
		intent.putExtra("position", position);
		intent.setAction("org.music.service.LocalMusicService");
		startService(intent);

	}


	private BroadcastReceiver musicreceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MUSIC_CURRENT)) {
				currentPosition = intent.getExtras().getInt("currentTime");
				playtime.setText(toTime(currentPosition));
				seekbar.setProgress(currentPosition);
				Iterator<Integer> iterator = lrc_map.keySet().iterator();
				while (iterator.hasNext()) {
					Object o = iterator.next();
					LRCbean val = lrc_map.get(o);
					if (val != null) {

						if (currentPosition > val.getBeginTime()
								&& currentPosition < val.getBeginTime()
										+ val.getLineTime()) {
							lrcText.setText(val.getLrcBody());
							break;
						}
					}
				}

			} else if (action.equals(MUSIC_DURATION)) {
				duration = intent.getExtras().getInt("duration");
				seekbar.setMax(duration);
				durationTime.setText(toTime(duration));
			} else if (action.equals(MUSIC_NEXT)) {
				System.out.println("....");
				nextOne();
			} else if (action.equals(MUSIC_UPDATE)) {
				position = intent.getExtras().getInt("position");
				setup();
			}

		}
	};

	/**
	 * ʱ���ת��
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * �������
	 * 
	 * @param path
	 */
	private void read(String path) {
		lrc_map.clear();
		TreeMap<Integer, LRCbean> lrc_read = new TreeMap<Integer, LRCbean>();
		String data = "";
		BufferedReader br = null;
		File file = new File(path);
		System.out.println(path);
		if (!file.exists()) {
			lrcText.setText("...");
			return;
		}
		FileInputStream stream = null;
		try {

			stream = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			while ((data = br.readLine()) != null) {
				if (data.length() > 6) {
					if (data.charAt(3) == ':' && data.charAt(6) == '.') {
						data = data.replace("[", "");
						data = data.replace("]", "@");
						data = data.replace(".", ":");
						String lrc[] = data.split("@");
						String lrcContent = null;
						if (lrc.length == 2) {
							lrcContent = lrc[lrc.length - 1];
						} else {
							lrcContent = "";
						}

						for (int i = 0; i < lrc.length - 1; i++) {
							String lrcTime[] = lrc[0].split(":");

							int m = Integer.parseInt(lrcTime[0]);
							int s = Integer.parseInt(lrcTime[1]);
							int ms = Integer.parseInt(lrcTime[2]);

							int begintime = (m * 60 + s) * 1000 + ms;
							LRCbean lrcbean = new LRCbean();
							lrcbean.setBeginTime(begintime);
							lrcbean.setLrcBody(lrcContent);
							lrc_read.put(begintime, lrcbean);

						}

					}
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ����ÿ������Ҫ��ʱ��
		lrc_map.clear();
		data = "";
		Iterator<Integer> iterator = lrc_read.keySet().iterator();
		LRCbean oldval = null;
		int i = 0;

		while (iterator.hasNext()) {
			Object ob = iterator.next();
			LRCbean val = lrc_read.get(ob);
			if (oldval == null) {
				oldval = val;
			} else {
				LRCbean item1 = new LRCbean();
				item1 = oldval;
				item1.setLineTime(val.getBeginTime() - oldval.getBeginTime());
				lrc_map.put(new Integer(i), item1);
				i++;
				oldval = val;
			}
		}
	}


	public static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefault) {
		if (album_id < 0) {

			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.inSampleSize = 1;

				options.inJustDecodeBounds = true;

				BitmapFactory.decodeStream(in, null, options);

				options.inSampleSize = computeSampleSize(options, 200);

				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {

				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}


 static int computeSampleSize(BitmapFactory.Options options,
			int target) {

		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0)
			return 1;
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target)
				candidate -= 1;
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target)
				candidate -= 1;
		}
		Log.v("ADW", "candidate:" + candidate);
		return candidate;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid,
			long albumid) {
		Bitmap bm = null;

		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
					
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
					
				}
			}
			options.inSampleSize = 1;

			options.inJustDecodeBounds = true;

			BitmapFactory.decodeFileDescriptor(fd, null, options);

			options.inSampleSize = 200;

			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (FileNotFoundException ex) {

		}
		if (bm != null) {

		}
		return bm;
	}

	private static Bitmap getDefaultArtwork(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;

		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.music), null, opts);
	}

	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
	
}
