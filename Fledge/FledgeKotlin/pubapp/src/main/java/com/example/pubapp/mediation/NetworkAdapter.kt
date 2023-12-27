package com.example.pubapp.mediation

import android.adservices.adselection.AdSelectionConfig
import android.adservices.adselection.AdSelectionManager
import android.adservices.adselection.AdSelectionOutcome
import android.adservices.adselection.ReportImpressionRequest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.OutcomeReceiver
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.concurrent.futures.CallbackToFutureAdapter
import com.example.pubapp.MainActivity
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("NewApi")
class NetworkAdapter(context: Context, val networkConfig: NetworkConfig) {

  companion object{
    val TAG = "NetworkAdapter"
  }
  private val adSelectionManager : AdSelectionManager
  private val EXECUTOR: Executor = Executors.newCachedThreadPool()
  private val adSelectionConfig: AdSelectionConfig

  init {
    adSelectionManager = context.getSystemService(AdSelectionManager::class.java)
    adSelectionConfig = AdSelectionConfig.Builder()
      .setSeller(networkConfig.sellerName)
      .setDecisionLogicUri(networkConfig.decisionLogicUrl)
      .setCustomAudienceBuyers(networkConfig.buyers)
      .build()
  }

  fun selectAds(): AdSelectionOutcome? {
    var adSelectionOutcome: AdSelectionOutcome? = null
    try{
      adSelectionOutcome = runAdSelection()[1, TimeUnit.SECONDS]!!
      Thread.sleep(1000)
    }catch (e: Exception){
    }
    return adSelectionOutcome
;  }

  fun reportImpressions(adSelectionId: Long) {
    reportImpression(adSelectionId)
  }

  @SuppressLint("NewApi")
  private fun runAdSelection(): ListenableFuture<AdSelectionOutcome?> {

    /*Log.d(TAG, "Running Ad selection with sellerName: $sellerName, decisionLogicUrl: $decisionLogicUrl," +
            "audienceBuyer: $customAudienceBuyers")*/

    return CallbackToFutureAdapter.getFuture { completer ->
      var callback: OutcomeReceiver<AdSelectionOutcome, Exception> =
        @RequiresApi(Build.VERSION_CODES.S)
        object : OutcomeReceiver<AdSelectionOutcome, Exception> {
          override fun onResult(result: AdSelectionOutcome) {
            Log.i("Ad Selection", "Completed running ad selection renderUrl: ${result.renderUri}, adSelectionId: ${result.adSelectionId}")
            completer.set(result)
          }

          override fun onError(error: Exception) {
            // Handle error
            Log.e("Ad Selection", "Error executing joinCustomAudience", error)
            completer.setException(error)
          }
        }
      adSelectionManager.selectAds(adSelectionConfig, EXECUTOR, callback)
    }
  }

  // Reports an impression, this method should be called after the ad is rendered.
  private fun reportImpression(adSelectionId: Long){
    val reportImpressionRequest = ReportImpressionRequest(adSelectionId, adSelectionConfig)
    adSelectionManager?.reportImpression(reportImpressionRequest, EXECUTOR, object :
      OutcomeReceiver<Any, java.lang.Exception> {

      override fun onResult(result: Any) {
        Log.e(MainActivity.TAG, "onResult : ${result}")
      }

      override fun onError(error: java.lang.Exception) {
        Log.e(MainActivity.TAG, "Error while executing impresion: ${error}")
      }
    })
  }
}