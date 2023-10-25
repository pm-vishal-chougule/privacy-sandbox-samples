package com.example.advertiserapp.model

interface CustomAudienceDataSource {

  fun getAll(): List<CustomAudienceModel>
  fun save(customAudienceModel: CustomAudienceModel): Boolean
}