package com.example.advertiserapp.model

import android.adservices.common.AdData
import android.adservices.common.AdSelectionSignals
import android.adservices.customaudience.TrustedBiddingData
import java.time.Instant

data class CustomAudienceModel(
  val audienceName: String,
  val buyerName: String,
  val biddingLogicUrl: String,
  val dailyUpdateUrl: String,
  val ads: List<AdData>,
  val trustedBiddingUrl: String? = null,
  val activationTime: Instant? = null,
  val expirtationTime: Instant? = null,
  val userBiddingSignals: AdSelectionSignals? = null,
  val trustedBiddingData: TrustedBiddingData? = null,
)
