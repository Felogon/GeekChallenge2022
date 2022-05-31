package com.exampl.accountsilent

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.huawei.hmf.tasks.Task
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.Internal.instance

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //ads
        HwAds.init(this)
        var bannerView: BannerView? = findViewById(R.id.hw_banner_view)
        // Create an ad request to load an ad.
        val adParam = AdParam.Builder().build()
        bannerView?.loadAd(adParam)
        //analytics
        val instance = HiAnalytics.getInstance(applicationContext);
        //deauth()
        //silentlogin()
        click_btnID()
        silentlogin()
    }

    fun click_btnID(){
        btnID.setOnClickListener(){

            val authParams : AccountAuthParams =  AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams()
            val service : AccountAuthService = AccountAuthManager.getService(this@MainActivity, authParams)
            startActivityForResult(service.signInIntent, 8888)

            val instance = HiAnalytics.getInstance(applicationContext);
            val bundle = Bundle()
            bundle.putString("exam_difficulty", "high")
            bundle.putString("exam_level", "1-1")
            bundle.putString("exam_time", "20190520-08")
            instance.onEvent("begin_examination", bundle)
        }//fin click
    }//fin botón

    fun silentlogin(){
        val authParams : AccountAuthParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).createParams()
        val service : AccountAuthService = AccountAuthManager.getService(this@MainActivity, authParams)
        val task : Task<AuthAccount>? = service.silentSignIn()
        task?.addOnSuccessListener { authAccount ->
            // Obtain the user's ID information.
            Log.i(TAG, "displayName:" + authAccount.displayName)
            // Obtain the ID type (0: HUAWEI ID; 1: AppTouch ID).
            Log.i(TAG, "accountFlag:");
        }
        task?.addOnFailureListener { e ->
            // The sign-in failed. Try to sign in explicitly using getSignInIntent().
            if (e is ApiException) {
                Log.i(TAG, "sign failed status:" + e.statusCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Process the authorization result to obtain the authorization code from AuthAccount.
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 8888) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                val authAccount = authAccountTask.result
                Log.i(TAG, "serverAuthCode:" + authAccount.authorizationCode)
            } else {
                // The sign-in failed.
                Log.e(TAG, "sign in failed:" + (authAccountTask.exception as ApiException).statusCode)
            }
        }
    }
    fun deauth (){
        // service indicates the AccountAuthService instance generated using the getService method during the sign-in authorization.
        val authParams : AccountAuthParams =  AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().createParams()
        val service : AccountAuthService = AccountAuthManager.getService(this@MainActivity, authParams)
        service.cancelAuthorization().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Processing after a successful authorization cancellation.
                Log.i(TAG, "onSuccess: ")
            } else {
                // Handle the exception.
                val exception = task.exception
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.i(TAG, "onFailure: $statusCode")
                }
            }
        }
        Toast.makeText(applicationContext,"listo",Toast.LENGTH_SHORT).show()

    }
}