/**
 * Sep 8, 2013
 * PostFragment.java
 * @author fengxiang
 */
package com.weibo.sdk.android.demo.Fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.demo.FriendListActivity;
import com.weibo.sdk.android.demo.SocialNetworkRequest.SocialNetworkRequest;
import com.weibo.sdk.android.demo.SocialNetworkRequest.WeiboUserInfoPO;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.util.FileManager;
import com.weibo.sdk.android.util.GetImage;

/**
 * 4:09:12 PM
 * Sep 8, 2013
 * PostFragment.java
 * @author fengxiang
 */
public class PostFragment extends Fragment{
	
	private int mHeight;
	private int mWidth;
	
	public static final int INPUTEDITTEXTID = 999999;
	public static final int BUTTONSGROUP = 999998;
	public static final String TAG = "sinasdk";
	public static final String USERID = "userId";
	public static final String FRIENDLIST = "friendList";
	public static List<WeiboUserInfoPO> list;


	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			
			
		}
		final AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... params) {
				list = new ArrayList<WeiboUserInfoPO>();
				JSONObject json;
				try {
					json = new JSONObject(params[0]);
					JSONArray array = json.getJSONArray("users");
					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonWeiboUser = array.getJSONObject(i);
						WeiboUserInfoPO weiboUserInfoPO = new WeiboUserInfoPO();
						weiboUserInfoPO.setId(jsonWeiboUser.getString("id"));
						weiboUserInfoPO.setAvatar_large(jsonWeiboUser
								.getString("avatar_large"));
						weiboUserInfoPO.setName(jsonWeiboUser.getString("name"));
						weiboUserInfoPO.setScreen_name(jsonWeiboUser
								.getString("screen_name"));
						weiboUserInfoPO.setPic_path(getActivity().getFilesDir()
								+ "/filmApp/" + weiboUserInfoPO.getId()
								+ ".jpg");
						list.add(weiboUserInfoPO);
						Bitmap bitmap = GetImage.getHttpBitmap(weiboUserInfoPO.getAvatar_large());
						FileManager.saveBitmapToFile(bitmap, weiboUserInfoPO.getPic_path());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return "";
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
			}
		};
		
		try {
			SocialNetworkRequest.getUserId(getActivity(),new RequestListener() {
				@Override
				public void onIOException(IOException e) {
					Log.i(TAG, e.toString());
					new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getActivity(),
									"userTimeline IOException!!!",
									Toast.LENGTH_LONG).show();
						}
					};
				}

				@Override
				public void onError(WeiboException e) {
					Log.i(TAG, e.toString());
					new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getActivity(),
									"userTimeline Error!!!",
									Toast.LENGTH_LONG).show();
						}
					};
				}

				@Override
				public void onComplete(String response) {
					Log.i(TAG, response);
					try {
						JSONObject json = new JSONObject(response);
						JSONArray array = json.getJSONArray("statuses");
						JSONObject firstFeed = array.getJSONObject(0);
						JSONObject user = firstFeed.getJSONObject("user");
						String userId = user.getString("id");
						Log.i("userTimeline", firstFeed.toString());
						Log.i("userTimeline", user.toString());
						Log.i("userTimeline", userId);
						SharedPreferences pref = getActivity()
								.getSharedPreferences(USERID,
										Context.MODE_APPEND);
						Editor editor = pref.edit();
						editor.putString("userId", userId);
						editor.commit();
						new Thread(new Runnable() {
							@Override
							public void run() {
								Looper.prepare();
								try {
									SocialNetworkRequest.getFriendList(getActivity(),new RequestListener() {
										@Override
										public void onIOException(IOException e) {
											Log.i(TAG, e.toString());
											new Runnable() {
												@Override
												public void run() {
													Toast.makeText(getActivity(), "GETFRIENDLIST IOException!!!",
															Toast.LENGTH_LONG).show();
												}
											};
										}

										@Override
										public void onError(WeiboException e) {
											Log.i(TAG, e.toString());
											new Runnable() {
												@Override
												public void run() {
													Toast.makeText(getActivity(), "GETFRIENDLIST Error!!!",
															Toast.LENGTH_LONG).show();
												}
											};
										}

										@Override
										public void onComplete(final String response) {

											Log.i(TAG, response);
											SharedPreferences pref = getActivity().getSharedPreferences(
													FRIENDLIST, Context.MODE_APPEND);
											Editor editor = pref.edit();
											editor.putString(FRIENDLIST, response);
											editor.commit();
											asyncTask.execute(response);
										}
									});
								} catch (JSONException e) {
									e.printStackTrace();
								}
								Looper.loop();
							}
						}).start();
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		
		DisplayMetrics dm = new DisplayMetrics();    
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);    
	    mWidth = dm.widthPixels;              
		mHeight = dm.heightPixels;  
		
		
		
		// construct the RelativeLayout
		RelativeLayout v = new RelativeLayout(getActivity());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mWidth,mHeight *2/5);
		
		
		
		EditText editText = new EditText(getActivity());
		editText.setHint("Thinking in Java");
		editText.setTextSize(20);
		editText.setFocusable(true);   
		editText.setFocusableInTouchMode(true);   
		editText.requestFocus();  
		editText.setLayoutParams(lp);
		editText.setId(INPUTEDITTEXTID);
		editText.setGravity(Gravity.LEFT | Gravity.TOP);
		
		//Group buttons
		FrameLayout frameLayout = new FrameLayout(getActivity());
		frameLayout.setId(BUTTONSGROUP);
		Button button_at,button_emoji,button_pic,button_tag,button_location;
		button_at = new Button(getActivity());
		button_emoji = new Button(getActivity());
		button_pic = new Button(getActivity());
		button_tag = new Button(getActivity());
		button_location = new Button(getActivity());
		Button button_post = new Button(getActivity());
		
		button_post.setText("POST");
		button_at.setText("@");
		button_emoji.setText("E");
		button_pic.setText("P");
		button_tag.setText("#");
		button_location.setText("L");
		
		button_at.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Looper.prepare();
						getFriendListActivity("");
						Looper.loop();
					}
				}).start();
			}
		});
		
		
		int button_width = 100;
		int button_height = 63;
		
		FrameLayout.LayoutParams layoutParamspost = new FrameLayout.LayoutParams(220,button_height);
		layoutParamspost.gravity = Gravity.RIGHT | Gravity.TOP;
		FrameLayout.LayoutParams layoutParamsat = new FrameLayout.LayoutParams(button_width,button_height);
		layoutParamsat.gravity = Gravity.RIGHT | Gravity.TOP;
		layoutParamsat.rightMargin = 230;
		FrameLayout.LayoutParams layoutParamstag = new FrameLayout.LayoutParams(button_width,button_height);
		layoutParamstag.gravity = Gravity.RIGHT | Gravity.TOP;
		layoutParamstag.rightMargin = 350;
		FrameLayout.LayoutParams layoutParamsemoji = new FrameLayout.LayoutParams(button_width,button_height);
		layoutParamsemoji.gravity = Gravity.RIGHT | Gravity.TOP;
		layoutParamsemoji.rightMargin = 350;
		FrameLayout.LayoutParams layoutParamspic = new FrameLayout.LayoutParams(button_width,button_height);
		layoutParamspic.gravity = Gravity.RIGHT | Gravity.TOP;
		layoutParamspic.rightMargin =240;
		FrameLayout.LayoutParams layoutParamslocation = new FrameLayout.LayoutParams(button_width,button_height);
		layoutParamslocation.gravity = Gravity.RIGHT | Gravity.TOP;
		layoutParamslocation.rightMargin = 480;
		
		button_post.setLayoutParams(layoutParamspost);
		button_at.setLayoutParams(layoutParamsat);
		button_emoji.setLayoutParams(layoutParamsemoji);
		button_pic.setLayoutParams(layoutParamspic);
		button_tag.setLayoutParams(layoutParamstag);
		button_location.setLayoutParams(layoutParamslocation);
		
		frameLayout.addView(button_post);
		frameLayout.addView(button_at);
		frameLayout.addView(button_tag);
//		frameLayout.addView(button_emoji);
//		frameLayout.addView(button_pic);
//		frameLayout.addView(button_location);
		
		FrameLayout postButtonFrameLayout = new FrameLayout(getActivity());
		

		
		
		
		
		
		RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.LayoutParams(mWidth,60);
		frameLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, INPUTEDITTEXTID);
		frameLayout.setLayoutParams(frameLayoutParams);
		
		
		
		v.addView(editText);
		v.addView(frameLayout);
		v.addView(postButtonFrameLayout);
		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//todo
	}
	
	
	
	public void getFriendListActivity(String s) {
		Intent intent = new Intent(getActivity(), FriendListActivity.class);
//	    intent.putExtra(MOVIE_MESSAGE, s);
	    startActivity(intent);
	}
}
