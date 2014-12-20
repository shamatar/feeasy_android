package me.feeasy.test;

import com.google.zxing.client.android.CaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class InitialActivity extends Activity {
	private static final String TAG_RESTORED = "restored";
	public static final String TAG_NO_SCAN = "noscan";
	public static final int TAG_KILL_ALL = 8888;
	public static final int TAG_SHOW_PAY = 8889;

	@Override protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.initial);
		FeeasyApp.instance.setupActivity(this);
		
		//String s = decryptSign("nb6q7cpkhpdxbv5cwggwknwqaloj6ch3rfpy3nrk6tizxg4wp6w5s5dqb2u7lmwkrnn3fygk63rcmxcfaqam2umd2c454fbwn25q3l25vx45gzj3zbmbkgwb2eqbh5ow3wbbrihcj726rqesgq4iksl6vhmi2yxfci52w6w2i5yxzakqmejd2gyv4ywehypo4du3zradujsgo000");
		
		findViewById(R.id.logoHolder).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				runQr();
			}
		});
		
		boolean scan = true;
		Bundle extras = getIntent().getExtras();
		if( extras!=null && extras.getBoolean(TAG_NO_SCAN, false) ) {
			scan = false;
		}
		if( savedState!=null && savedState.getBoolean(TAG_RESTORED,false) ) {
			scan = false;
		}
		
		if( scan ) {
	        // run QR scanner
	        runQr();
		}
	}
	
	private void runQr() {
		Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
	}
	
	@Override protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(TAG_RESTORED, true);
		super.onSaveInstanceState(outState);
	}
	
	@Override public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if( resultCode==TAG_KILL_ALL ) {
			setResult(TAG_KILL_ALL);
			finish();
			
			return;
		}
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // Handle successful scan 

            	Intent payIntent = new Intent(getApplicationContext(), ActivityPay.class);
            	payIntent.putExtra(ActivityPay.TAG_URI, intent.getStringExtra("SCAN_RESULT"));
            	startActivity(payIntent);
            } else if (resultCode == RESULT_CANCELED) {
               // Handle cancel
               Log.i("App","Scan unsuccessful");
            }
        }
    }
}
