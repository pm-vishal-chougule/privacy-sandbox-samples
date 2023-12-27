package com.example.pubapp.mediation

import android.adservices.adselection.AdSelectionOutcome

interface Mediator {

  fun register3PSDK(networkConfig: NetworkConfig);

  fun requestAd(): Pair<AdSelectionOutcome, NetworkAdapter>


}