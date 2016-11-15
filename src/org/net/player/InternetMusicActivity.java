package org.net.player;

import com.music.player.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class InternetMusicActivity extends Activity {
	private static final int START = 1;
	private static final int HELP = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, START, 1, "��ʼʹ��").setIcon(R.drawable.menu_start)
				.setAlphabeticShortcut('S');
		menu.add(0, HELP, 2, "�鿴����").setIcon(R.drawable.helps)
				.setAlphabeticShortcut('H');
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == START) {
			
		} else if (item.getItemId() == HELP) {
			Intent intent=new Intent();
			intent.setClass(InternetMusicActivity.this,Internet_help.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

}
