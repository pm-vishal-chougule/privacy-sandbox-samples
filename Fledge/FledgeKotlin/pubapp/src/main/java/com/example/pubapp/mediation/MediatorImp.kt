package com.example.pubapp.mediation

import android.adservices.adselection.AdSelectionFromOutcomesConfig
import android.adservices.adselection.AdSelectionOutcome
import android.adservices.common.AdSelectionSignals
import android.adservices.common.AdTechIdentifier
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log

@SuppressLint("NewApi")
class MediatorImp(private val context: Context) : Mediator {

  companion object {
    val TAG = "MediatorImp"
    private const val AD_SELECTION_PREBUILT_SCHEMA = "ad-selection-prebuilt"
    private const val AD_SELECTION_FROM_OUTCOMES_USE_CASE = "ad-selection-from-outcomes"
    private const val AD_OUTCOME_SELECTION_WATERFALL_MEDIATION_TRUNCATION = "waterfall-mediation-truncation"
    private const val BID_FLOOR_SIGNALS_FORMAT = "{%s:%s}"
    private const val BID_FLOOR_PARAM_KEY = "bidFloor"
    private const val BID_FLOOR_SIGNAL_KEY = "bid_floor"


    private const val DEFAULT_SCORING_LOGIC_URL = "https://pm-vishal-chougule.github.io/privacy-sandbox-samples/Fledge/FledgeServerSpec/api/ScoringLogic.js"
    private const val OW_SCORING_LOGIC_URL = "https://owsdk-stagingams.pubmatic.com:8443/protected-audiance/api/ScoringLogic.js"

  }

  val mediationChain: MutableList<NetworkAdapter> = ArrayList()

  val defaultAdapter: NetworkAdapter

  init {
    val defaultSellerStr = Uri.parse(DEFAULT_SCORING_LOGIC_URL).host.toString()
    val defaultSeller = AdTechIdentifier.fromString(defaultSellerStr)
    val defaultBuyers: MutableList<AdTechIdentifier> = ArrayList()
    defaultBuyers.add(defaultSeller)

    val defaultNetworkConfig = NetworkConfig(defaultSeller, Uri.parse(DEFAULT_SCORING_LOGIC_URL),
      defaultBuyers, 1.00)
    defaultAdapter = NetworkAdapter(context, defaultNetworkConfig)
  }
  override fun register3PSDK(networkConfig: NetworkConfig) {
    val networkAdapter = NetworkAdapter(context, networkConfig)
    mediationChain.add(networkAdapter)
  }

  override fun requestAd(): Pair<AdSelectionOutcome, NetworkAdapter> {
    val defaultNetworkOutcome: AdSelectionOutcome? = defaultAdapter.selectAds()
    var outcome: AdSelectionOutcome? = null
    for (network3p in mediationChain) {
      if (defaultNetworkOutcome?.hasOutcome()== true) {
        outcome = runSelectOutcome(defaultNetworkOutcome, network3p)
        if (outcome.hasOutcome() == true) {
          return  Pair(outcome!!, defaultAdapter)
        }
      }
      if (network3p.selectAds()?.hasOutcome() == true) {
        return Pair(network3p.selectAds()!!, network3p)
      }
    }
    return Pair(defaultNetworkOutcome!!, defaultAdapter)
  }

  @Throws(Exception::class)
  fun runSelectOutcome(
    outcome1p: AdSelectionOutcome,
    network3p: NetworkAdapter,
  ): AdSelectionOutcome {
    val config = prepareWaterfallConfig(outcome1p.adSelectionId, network3p.networkConfig.floorValue, network3p.networkConfig.sellerName)

    var result: AdSelectionOutcome? = null
    try {
      result = network3p.selectAds()
      Thread.sleep(1000)
    } catch (e: Exception) {
      Log.e(TAG, "Exception calling selectAds(AdSelectionFromOutcomesConfig)", e)
      throw e
    }
    return result!!
  }

  private fun prepareWaterfallConfig(
    outcome1pId: Long,
    bidFloor: Double,
    seller: AdTechIdentifier
  ): AdSelectionFromOutcomesConfig {
    // inject a flag to run only with "Additional ad selection ids from the UX"
    val outcomeIds: MutableList<Long> = ArrayList()
    outcomeIds.add(outcome1pId)
    return AdSelectionFromOutcomesConfig.Builder()
      .setSeller(seller)
      .setAdSelectionIds(outcomeIds)
      .setSelectionSignals(getSignalsForPrebuiltUri(bidFloor))
      .setSelectionLogicUri(getPrebuiltUriForWaterfallTruncation())
      .build()
  }

  private fun getSignalsForPrebuiltUri(bidFloor: Double): AdSelectionSignals {
    return AdSelectionSignals.fromString(
      String.format(
        BID_FLOOR_SIGNALS_FORMAT,
        BID_FLOOR_SIGNAL_KEY,
        bidFloor
      )
    )
  }

  private fun getPrebuiltUriForWaterfallTruncation(): Uri {
    return Uri.parse(
      String.format(
        "%s://%s/%s/?%s=%s",
        AD_SELECTION_PREBUILT_SCHEMA,
        AD_SELECTION_FROM_OUTCOMES_USE_CASE,
        AD_OUTCOME_SELECTION_WATERFALL_MEDIATION_TRUNCATION,
        BID_FLOOR_PARAM_KEY,
        BID_FLOOR_SIGNAL_KEY
      )
    )
  }

}