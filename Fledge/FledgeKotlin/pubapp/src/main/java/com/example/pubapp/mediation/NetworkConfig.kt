package com.example.pubapp.mediation

import android.adservices.common.AdTechIdentifier
import android.net.Uri

data class NetworkConfig(val sellerName: AdTechIdentifier,
                         val decisionLogicUrl: Uri,
                         val buyers: List<AdTechIdentifier>,
                         val floorValue: Double)