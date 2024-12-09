package com.example.mysocialproject.networking.pref

interface PrefHelper {
   suspend fun setUserId(id:String?)
   suspend fun getUserId():String?
   suspend fun clearApp()
}