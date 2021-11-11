package gr.loukaspd.googlesignin;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.unity3d.player.UnityPlayer;

public class GoogleSignInFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String Tag = "GoogleSingInTag";
    private static final int RC_SIGN_IN = 100;
    private static final String UnitySuccessCallbackName = "UnityGoogleSignInSuccessCallback";
    private static final String UnityErrorCallbackName = "UnityGoogleSignInErrorCallback";

    //region Public Api

    public static String WebClientId;
    public static String UnityGameObjectName;

    public static void SignIn(Activity unityActivity, String webClientId)
    {
        // Creating an intent with the current activity and the activity we wish to start
        WebClientId = webClientId;
        SignIn(unityActivity);
    }

    public static void SignIn(Activity unityActivity) {
        if (WebClientId == null || WebClientId.length() == 0) {
            return;
        }

        FragmentTransaction trans = unityActivity.getFragmentManager().beginTransaction();
        trans.add(new GoogleSignInFragment(), Tag);
        trans.commitAllowingStateLoss();
    }

    //endregion

    private GoogleApiClient _gApiClient;

    @Override
    public void onStart() {
        super.onStart();

        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestProfile();
        if (WebClientId != null && WebClientId.length() > 0) {
            builder.requestIdToken(WebClientId);
        }

        GoogleSignInOptions gso = builder.build();

        _gApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        _gApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                sendAccountToUnity(acct);
            }else {
                sendErrorToUnity("");
            }
        }
    }


    private void hideFragment() {
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag(Tag)).commit();
    }

    //region Google Api Client
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Auth.GoogleSignInApi.signOut(_gApiClient);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(_gApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionSuspended(int i) {
        sendErrorToUnity("");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        sendErrorToUnity("");
    }

    //endregion

    //region Unity Integration

    private void sendAccountToUnity(GoogleSignInAccount account) {
        UnityPlayer.UnitySendMessage(UnityGameObjectName, UnitySuccessCallbackName, serializeSignInAccount(account));
        hideFragment();
    }

    private void sendErrorToUnity(String error) {
        UnityPlayer.UnitySendMessage(UnityGameObjectName, UnityErrorCallbackName, error);
        hideFragment();
    }


    private static String serializeSignInAccount(GoogleSignInAccount account) {
        String result = "";
        if (account == null) return result;

        result += "{";

        result += "\"Id\": \"" + account.getId() + "\", ";
        result += "\"Token\": \"" + account.getIdToken() + "\", ";
        result += "\"DisplayName\": \"" + account.getDisplayName() + "\", ";
        result += "\"Email\": \"" + account.getEmail() + "\", ";
        result += "\"FamilyName\": \"" + account.getFamilyName() + "\", ";
        result += "\"Code\": \"" + account.getServerAuthCode() + "\", ";
        if (account.getPhotoUrl() != null) result += "\"PhotoUrl\": \"" +  account.getPhotoUrl().toString() + "\" ";

        result += "}";

        return result;
    }

    //endregion
}
