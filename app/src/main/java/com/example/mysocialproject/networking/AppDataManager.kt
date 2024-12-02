package com.example.mysocialproject.networking

import com.example.mysocialproject.networking.pref.PrefHelper
import com.example.mysocialproject.networking.repository.UserRepository
import javax.inject.Inject

class AppDataManager @Inject constructor(
    private val sharePreference: PrefHelper,
):AppDataHelper{

}