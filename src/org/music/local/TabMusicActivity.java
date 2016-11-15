package org.music.local;

import org.net.player.InternetMusicActivity;

import com.music.player.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class TabMusicActivity extends TabActivity implements
		OnCheckedChangeListener {
	private TabHost tabhost;
	private RadioGroup maintab;
	private Intent musiclist;
	private Intent net;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tablist_bottom);
		maintab = (RadioGroup) findViewById(R.id.main_tab);
		maintab.setOnCheckedChangeListener(this);
		tabhost = getTabHost();
		musiclist = new Intent(this, LocalMusicActivity.class);
		tabhost.addTab(tabhost
				.newTabSpec("musiclist")
				.setIndicator("�����б�",
						getResources().getDrawable(R.drawable.icon1))
				.setContent(musiclist));
		net=new Intent(this,InternetMusicActivity.class);
		tabhost.addTab(tabhost
				.newTabSpec("net")
				.setIndicator("��������",
						getResources().getDrawable(R.drawable.icon_2_n))
				.setContent(net));
	}


	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_musiclist:
			tabhost.setCurrentTabByTag("musiclist");
			break;

		case R.id.radio_net:
			tabhost.setCurrentTabByTag("net");
			break;
		}

	}
	
}
