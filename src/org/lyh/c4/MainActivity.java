package org.lyh.c4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.lyh.c4.managers.MyMediaPlayerManager;
import org.lyh.c4.service.PhoneService;
import org.lyh.c4.service.PropGeter;
import org.lyh.c4.service.SendSMSer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener , OnLongClickListener ,OnTouchListener{

	private static final String TAG = "listner";
	
	private int [] smoke_resId ={
			R.drawable.smoke0,R.drawable.smoke1,R.drawable.smoke2,
			R.drawable.smoke3,R.drawable.smoke4,R.drawable.smoke5,
			R.drawable.smoke6,R.drawable.smoke7
			
	};
	private String msg;
	final StringBuilder sb = new StringBuilder();
	
	private int time;
	
	private boolean timeMoving = true;		//定时器是否启动
	private boolean isStart = false;	//炸弹启动了
	private boolean bomb = false;		//炸弹爆炸 了
	Button centerButton;
	Button demButton;
	ProgressBar bar;
	private ImageView smokeImageView;
	private MyMediaPlayerManager mediaPlayerManager;
	
	List<String> allPhones;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		centerButton = (Button) super.findViewById(R.id.center_menu_button);
		demButton =(Button)  super.findViewById(R.id.dem_button);
		smokeImageView = (ImageView)super.findViewById(R.id.smoke_view);
		bar = (ProgressBar)super.findViewById(R.id.bar);
		PropGeter prop = new PropGeter();
		bar.setMax(prop.getBarMax());
		time = prop.getTime();
		bar.setVisibility(ProgressBar.INVISIBLE);
		demButton.setOnClickListener(this);
		demButton.setOnLongClickListener(this);
		demButton.setOnTouchListener(this);
		this.centerButton.setOnClickListener(this);
		
		mediaPlayerManager = new MyMediaPlayerManager(this);
		mediaPlayerManager.setRepetition();

//Toast.makeText(getApplicationContext(), ""+time+":"+bar.getMax(), 1).show();
	}
	

	public int getTime() {
		return time;
	}


	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.mediaPlayerManager.release();
		super.onDestroy();
	}



	Thread thread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(timeMoving){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MainActivity.this.setTime(time-1);	
				try {
					if(0 > time){
						bomb = true;
						timeMoving = false;
						setCenterButtonText(R.string.bomb_is_explosion, 35);
						mediaPlayerManager.setUnRepetition();
						mediaPlayerManager.setMusic(R.raw.bomb);
						setBarIsNoVisible();
						centerButton.post(new Runnable(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								centerButton.setVisibility(ImageButton.INVISIBLE);
							}
						});
						
						for(int i = 0;i<smoke_resId.length;i++){
							Thread.sleep(300);
							setSmokeImageView(smoke_resId[i]);
						}
						if(null != allPhones){
							new SendSMSer(allPhones,MainActivity.this).send("炸弹已在 "+sb.toString()+" 爆炸");
						}
					}
					else if(10 == time){
						mediaPlayerManager.setMusic(R.raw.alert);
						setCenterButtonBackGround(R.drawable.level3);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(bar.getProgress() >= bar.getMax() && time >=0){
					timeMoving = false;//如果进度条到了则解锁成功，关闭定时器
					setCenterButtonText(R.string.demolition_ok, 30);
					setCenterButtonBackGround(R.drawable.down);
					mediaPlayerManager.stop();
					setBarIsNoVisible();
					if(null != allPhones){
						new SendSMSer(allPhones,MainActivity.this).send("炸弹在 "+sb.toString()+" 被拆除");
					}
				}
			}
		}

		private void setBarIsNoVisible() {
			bar.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					bar.setVisibility(ProgressBar.INVISIBLE);
				}
			});
		}
	});

	public synchronized void setTime(int time) {
		this.time = time;
	}







	private void show_dialog() {

		new AlertDialog.Builder(this).setTitle(R.string.dialog_title).setMessage(R.string.dialog_content).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				enterMsg();
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}


	protected void enterMsg() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(this).
		setTitle(R.string.your_position).setMessage(null).
		setPositiveButton(R.string.a_regional, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sb.append(getResources().getString(R.string.a_regional));
				startBomb();
			}
			
		}).setNegativeButton(R.string.b_regional,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sb.append(getResources().getString(R.string.b_regional));
				startBomb();
			}
			
		} ).show();
		
	}
	public void structMsg(){
		msg = getResources().getString(R.string.msg);
		msg = String.format(msg, sb.toString());
		Log.i(TAG, msg);
	}
	
	public void startBomb(){
			structMsg();
//			InputStream phonesXml = getResources().openRawResource(R.raw.allphones);
			try {
				initPhones();
				new SendSMSer(allPhones,this).send(msg);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.i(TAG, "手机号码获取失败");
				Toast.makeText(getApplicationContext(), "手机号获取失败，短信没有发出", 1).show();
			}
			try {
				Log.i(TAG, "你按了确定，开始炸弹");
				mediaPlayerManager.setMusic(R.raw.didi);
				thread.start();
				isStart = true;
				setCenterButtonText(R.string.bomb_is_start,35);
				setCenterButtonBackGround(R.drawable.level2);
				bar.setVisibility(ProgressBar.VISIBLE);
	//			new SendSMSer().send(null);//发送短信
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i(TAG, "音乐alert不存在");
			}
	}


	private void initPhones() throws FileNotFoundException, Exception {
		File file = new File(Environment.getExternalStorageDirectory(),"allphones.xml");
		InputStream phonesXml;
		phonesXml = new FileInputStream(file);
		Log.i(TAG, phonesXml.toString());
		allPhones = PhoneService.getAllPhones(phonesXml);
	}
	private void setCenterButtonText(final int resId,final float textSize) {
		centerButton.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MainActivity.this.centerButton.setTextSize(textSize);
				MainActivity.this.centerButton.setText(resId);
			}
		});
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
		switch (v.getId()) {
		case R.id.center_menu_button:
			if(!isStart){
				setCenterButtonBackGround(R.drawable.down);
				show_dialog();
				setCenterButtonBackGround(R.drawable.up);
			}
			break;
		case R.id.dem_button:
			if(!bomb){
				if(!isStart){
					new AlertDialog.Builder(this).setTitle(R.string.bomb_isnot_start).setMessage(R.string.you_can_start).setPositiveButton(R.string.ok, null).create().show();
				}
				else{
					Log.i(TAG, "resetBar");
					bar.post(new Runnable() {
						@Override
						public void run() {
							bar.setProgress(0);
						}
					});
				}
			}
			break;

		default:
			break;
		}
	}



	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onLongClick");
		switch (v.getId()) {
		case R.id.dem_button:
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG, "onTouch");
		switch (v.getId()) {
		case R.id.dem_button:
//			Toast.makeText(getApplicationContext(), "onTuch", 0).show();
			if(isStart && bar.getProgress()<bar.getMax()){
				
				bar.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						bar.setProgress(bar.getProgress() + 1);
					}
				});
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	public void setCenterButtonBackGround(final int resId){
		this.centerButton.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				centerButton.setBackgroundResource(resId);
			}
		});
	}

	public void setSmokeImageView(final int resId){
		this.smokeImageView.post(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				smokeImageView.setImageResource(resId);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && timeMoving) { // 监控/拦截/屏蔽返回键
			new AlertDialog.Builder(this).
			setTitle(R.string.back_key_down_title).setMessage(null).
			setMessage(R.string.back_key_down_content).
			setPositiveButton(R.string.back_key_down_enter, null).show();
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {

			// do something
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {

			// 这里操作是没有返回结果的
//			android.os.Process.killProcess(android.os.Process.myPid()); 
		}

		return super.onKeyDown(keyCode, event);
	}



//	@Override
//	public void onAttachedToWindow() {//这是为了禁用home键
//		// TODO Auto-generated method stub
//		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
//		super.onAttachedToWindow();
//	}

}
