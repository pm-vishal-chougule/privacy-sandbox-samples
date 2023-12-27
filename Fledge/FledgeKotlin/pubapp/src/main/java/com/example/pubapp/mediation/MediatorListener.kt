package com.example.pubapp.mediation

import android.adservices.adselection.AdSelectionConfig
import android.adservices.adselection.AdSelectionOutcome

interface MediatorListener {

  fun onSuccess(adSelectionOutCome: AdSelectionOutcome, adSelectionConfig: AdSelectionConfig)

  fun onFailure(exception: Exception)
}