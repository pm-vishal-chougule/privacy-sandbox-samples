package com.example.pubapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubapp.model.AdSelectionOutcomeModel
import kotlinx.coroutines.launch

class AdSelectionOutcomeViewModel: ViewModel() {
  private val adSelectionOutcomeViewModel = MutableLiveData<AdSelectionOutcomeModel>()
  val adSelectionViewModel: LiveData<AdSelectionOutcomeModel> = adSelectionOutcomeViewModel

  fun updateUrl(renderUrl: String, width: Int, height: Int){
    Log.d("AdSelectionOutcomeViewModel", "updateUrl: render url ${renderUrl}")
    val adSelectionOutcome = AdSelectionOutcomeModel(renderUrl, width, height)
    adSelectionOutcomeViewModel.value = adSelectionOutcome
  }

}