package com.example.mysocialproject.networking

import com.example.mysocialproject.networking.pref.PrefHelper
import com.example.mysocialproject.networking.repository.FriendRepository
import com.example.mysocialproject.networking.repository.MessageRepository
import com.example.mysocialproject.networking.repository.PostRepository
import com.example.mysocialproject.networking.repository.UserRepository


interface AppDataHelper:PrefHelper,UserRepository, PostRepository, FriendRepository,MessageRepository{
}