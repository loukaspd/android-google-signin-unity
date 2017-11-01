using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AndroidGoogleSignIn : MonoBehaviour 
{

	#region Initialization

	public static AndroidGoogleSignIn Init(GameObject attachTo) {
		AndroidGoogleSignIn instance = attachTo.GetComponent<AndroidGoogleSignIn> ();
		if (instance != null) return instance;

		return attachTo.AddComponent<AndroidGoogleSignIn>();
	}

	#endregion

    private Action<AndroidGoogleSignInAccount> _successCallback;
    private Action<string> _errorCallback;

    public void SignIn(string webClientId, Action<AndroidGoogleSignInAccount> successCallback, Action<string> errorCallback)
    {
        _successCallback = successCallback;
        _errorCallback = errorCallback;
        using (var unityPlayerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
        using (var unityActivity = unityPlayerClass.GetStatic<AndroidJavaObject>("currentActivity"))
        using (var signInFragmentClass = new AndroidJavaClass("gr.loukaspd.googlesignin.GoogleSignInFragment"))
        {
            signInFragmentClass.SetStatic("UnityGameObjectName", this.gameObject.name);

            signInFragmentClass.CallStatic("SignIn", unityActivity, webClientId);
        }
    }


    public void UnityGoogleSignInSuccessCallback(string googleSignInAccountJson)
    { 
    	var account = JsonUtility.FromJson<AndroidGoogleSignInAccount>(googleSignInAccountJson);
    	if (account == null) {
    		UnityGoogleSignInErrorCallback("");
    		return;
    	}

		if (_successCallback != null) _successCallback.Invoke(account);
		ClearReferences ();
    }

    public void UnityGoogleSignInErrorCallback(string errorMsg) {
		if (_errorCallback != null) _errorCallback.Invoke(errorMsg);

		ClearReferences ();
    }


	private void ClearReferences() {
		_successCallback = null;
		_errorCallback = null;
	}
}