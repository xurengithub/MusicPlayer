package org.music.tools;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScanSDCardReceiver extends BroadcastReceiver {
	private AlertDialog.Builder  builder = null;
	private AlertDialog ad = null;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){
			System.out.println("ɨ��SD���С��������Ժ�");
			builder = new AlertDialog.Builder(context);
			builder.setMessage("����ɨ��洢��...");
			ad = builder.create();
			ad.show();
			
		}else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){
			ad.cancel();
		}

	}

}
