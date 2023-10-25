package com.example.advertiserapp.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson


class CustomAudienceRepository (context: Context): CustomAudienceDataSource {

  private val sharedPref: SharedPreferences
  init {
    sharedPref = context.getSharedPreferences("${context.packageName}_custom_audience", Context.MODE_PRIVATE)
  }
  override fun getAll(): List<CustomAudienceModel> {
    return fetchAudienceFromSharedPreference()
  }

  override fun save(customAudienceModel: CustomAudienceModel): Boolean {
    return saveAudience(customAudienceModel)
  }

  fun fetchAudienceFromSharedPreference(): List<CustomAudienceModel>{
    val gson = Gson()
    val customAudienceModelList: MutableList<CustomAudienceModel> = ArrayList()
    sharedPref.all.entries.forEach{
      customAudienceModelList.add(gson.fromJson(it.value as String, CustomAudienceModel::class.java))
    }
    return customAudienceModelList
  }

  fun saveAudience(customAudienceModel: CustomAudienceModel): Boolean{
    val gson = Gson()
    val editor = sharedPref.edit()
    editor.putString(customAudienceModel.audienceName, gson.toJson(customAudienceModel))
    editor.apply()
    return true
  }

  companion object{
    var customAudience: CustomAudienceRepository? = null
    fun getInstance(context: Context): CustomAudienceRepository{
      if(customAudience == null){
        customAudience = CustomAudienceRepository(context = context)
      }
      return customAudience as CustomAudienceRepository
    }
  }
}