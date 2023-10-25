package com.example.advertiserapp.model

data class CustomAudienceModel(
  val audienceName: String,
  val buyerName: String,
  val biddingLogicUrl: String,
  val dailyUpdateUrl: String,
  val renderUrl: String,
  val trustedBiddingUrl: String?
)
