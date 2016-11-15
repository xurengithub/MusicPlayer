package org.net.player;

import org.music.local.TabMusicActivity;

import com.music.player.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Internet_help extends Activity {
	Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.net_helps);
		back = (Button) findViewById(R.id.back_btn);
		back.setOnClickListener(new OnClickListener() {

	
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Internet_help.this, TabMusicActivity.class);
				startActivity(intent);
				finish();

			}
		});
	}

}
