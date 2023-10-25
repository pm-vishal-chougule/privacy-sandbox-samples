package com.example.advertiserapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.advertiserapp.model.CustomAudienceModel
import com.example.advertiserapp.model.CustomAudienceRepository
import kotlinx.coroutines.launch

class CustomAudienceViewModel(): ViewModel() {

  var customAudienceRepository: CustomAudienceRepository? = null
  private val customAudiencesModel = MutableLiveData<List<CustomAudienceModel>>()
  val customAudienceModelList: LiveData<List<CustomAudienceModel>> = customAudiencesModel
  fun fetchCustomAudience(){
    viewModelScope.launch {
      try {
        val audiences = customAudienceRepository?.getAll()
        customAudiencesModel.value = audiences
      }catch (e: Exception){

      }
    }
  }

  fun saveCustomAudience(customAudienceModel: CustomAudienceModel){
    customAudienceRepository?.saveAudience(customAudienceModel)
    fetchCustomAudience()
  }
}