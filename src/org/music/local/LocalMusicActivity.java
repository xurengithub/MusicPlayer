package org.music.local;

import org.music.tools.MusicListAdapter;
import org.music.tools.ScanSDCardReceiver;

import com.music.player.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * ֮ǰ�����Զ����������б������������ǾͿ�����Activity��ContentResolver��query��������/����ý����Ϣ�ˡ�
 * ������ContentResolver�Ĳ��ҷ���: 
 * URI��ָ��Ҫ��ѯ����ݿ���Ƽ��ϱ����� 
 * Projection��ָ����ѯ��ݿ���е��ļ��У����ص��α��н�������Ӧ����Ϣ��Null�򷵻�������Ϣ�� 
 * selection: ָ����ѯ����
 * selectionArgs������selection���У��������ǣ����������ʵ��ֵ��������ʺš����selection���û�У��Ļ�����ô���String�������Ϊnull
 * SortOrder��ָ����ѯ��������˳�� ����ʾ��һ���б��� ���Կؼ���ListView��ʵ����Ҫ�ġ�
 *
 */
public class LocalMusicActivity extends Activity {
	private int[] _ids;// ��������ID��ʱ����
	private String[] _artists;// ����������
	private String[] _titles;// ������ʱ����
	private ListView listview;// �б����
	private ScanSDCardReceiver receiver = null;// ɨ��SD����ʵ��
	private static final int SCAN = Menu.FIRST;//��д�˵��ĳ���
	private static final int ABOUT = Menu.FIRST + 1;
	/**
	 * �������������Ϣ���飬1.���⣬2����ʱ��,3.������,4.����id��5.��ʾ����,6.��ݡ�
	 */
	String[] media_info = new String[] { MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.localmusic);
		listview = (ListView) findViewById(R.id.music_list);// ��ListView��ID
		listview.setOnItemClickListener(new MusicListOnClickListener());// ����һ��ListView����������
		ShowMp3List();// ��ʾ����

	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SCAN, 0, "ɨ��SD��");
		menu.add(1, ABOUT, 1, "����");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == SCAN) {
			ScanSDCard();
		}
		return true;
	}

	/**
	 * ��ʾ�����б�
	 */
	private void ShowMp3List() {
		Cursor cursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_info, null,
				null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		cursor.moveToFirst();// ���α��ƶ�����ʼλ��
		_ids = new int[cursor.getCount()];// ����INT��һ����
		_artists = new String[cursor.getCount()];// ����String��һ����
		_titles = new String[cursor.getCount()];// ����String��һ����
		for (int i = 0; i < cursor.getCount(); i++) {
			_ids[i] = cursor.getInt(3);
			_titles[i] = cursor.getString(0);
			_artists[i] = cursor.getString(2);
			cursor.moveToNext();// ���α��Ƶ���һ��
		}
		listview.setAdapter(new MusicListAdapter(this, cursor));// ��setAdapterװ�����
	}

	// ����б��¼�
	public class MusicListOnClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long id) {
			playMusic(position);// ��ݵ��λ������������

		}

	}

	// �������ַ���
	public void playMusic(int position) {
		Intent intent = new Intent(LocalMusicActivity.this,
				PlayMusicActivity.class);
		intent.putExtra("_ids", _ids);
		intent.putExtra("_titles", _titles);
		intent.putExtra("_artists", _artists);
		intent.putExtra("position", position);
		startActivity(intent);
		finish();

	}

	// ɨ��SD��
	private void ScanSDCard() {
		IntentFilter filter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		receiver = new ScanSDCardReceiver();
		filter.addDataScheme("file");
		registerReceiver(receiver, filter);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
				Uri.parse("file://"
						+ Environment.getExternalStorageDirectory()
								.getAbsolutePath())));

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(receiver!=null)
				unregisterReceiver(receiver);
           AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("�˳����");
			builder.setIcon(R.drawable.dialog_alert_icon);
			builder.setMessage("ȷʵҪ�˳�������")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).setNegativeButton("ȡ��", null).show();

		}
		return false;
	}

}