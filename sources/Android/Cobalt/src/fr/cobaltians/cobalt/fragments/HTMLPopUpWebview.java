package fr.cobaltians.cobalt.fragments;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.util.Log;
import android.webkit.JavascriptInterface;
import fr.cobaltians.cobalt.activities.HTMLActivity;

/**
 * Special {@link HTMLFragment} presented over the current HTMLFragment as a Web layer.
 * @author Diane
 * @details This class should not be instantiated directly. {@link HTMLFragment} manages it directly with Web layer messages.
 */
public class HTMLPopUpWebview extends HTMLFragment {	

	/******************************************************
	 * LIFECYCLE
	 *****************************************************/
	@Override
	public void onStart() {
		super.onStart();
		
		if(mWebView != null) {
			mWebView.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	@Override
	public void onDestroy() {
		onDismiss();
		
		super.onDestroy();
	}
	
	/*************************************************************************************************************************************
	 * COBALT
	 ************************************************************************************************************************************/
	@Override
	@JavascriptInterface
	public boolean handleMessageSentByJavaScript(String message) {
		JSONObject jsonObj;
		
		try {
			jsonObj = new JSONObject(message);
			if (jsonObj != null) {
				// TODO: catch data to pass to Web when onDismiss event is fired
				String type = jsonObj.optString(kJSType);
				if (type.equals(JSTypeWebLayer)) {
					String action = jsonObj.optString(kJSAction);
					if (action.equals(JSActionWebLayerDismiss)) {
						final JSONObject data = jsonObj.optJSONObject(kJSData);
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								dismissWebAlert(data);
							}
						});
						return true;
					}
				}
			}
		} 
		catch (JSONException exception) {
			if (mDebug) Log.e(getClass().getSimpleName(), "handleMessageSentByJavaScript: cannot handle message for JSON \n" + message);
			exception.printStackTrace();
		}
		
		return super.handleMessageSentByJavaScript(message);
	}

	@Override
	protected void onUnhandledMessage(JSONObject message) {
		
	}

	@Override
	protected void handleBackButtonPressed(boolean allowedToGoBack) {
		if(allowedToGoBack) {
			dismissWebAlert(null);
		}
	}
	
	/********************************************************************************************
	 * DISMISS
	 *******************************************************************************************/
	protected void dismissWebAlert(JSONObject jsonObject) {
		android.support.v4.app.FragmentTransaction fTransition;
		fTransition = getActivity().getSupportFragmentManager().beginTransaction();
		
		if(jsonObject != null) {
			double fadeDuration = jsonObject.optDouble(kJSWebLayerFadeDuration, 0);
			
			if(fadeDuration > 0) {
				fTransition.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, 
												android.R.anim.fade_in, android.R.anim.fade_out);
			}
			else {
				fTransition.setTransition(FragmentTransaction.TRANSIT_NONE);
			}
		}
		else {
			fTransition.setTransition(FragmentTransaction.TRANSIT_NONE);
		}
		
		fTransition.remove(this);
		fTransition.commit();
	}

	private void onDismiss() {
		if (HTMLActivity.class.isAssignableFrom(getActivity().getClass())) {
			HTMLActivity activity = (HTMLActivity) getActivity();
			activity.onWebPopupDismiss(pageName, getDataForDismiss());
		}
	}
	
	public JSONObject getDataForDismiss() {
		return null;
	}
}
