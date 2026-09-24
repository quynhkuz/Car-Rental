package com.example.test.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.model.login.Dto_login
import com.example.test.model.login.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CarViewModel @Inject constructor(val repository: CarRepository) : ViewModel()
{


    var loginResponse : MutableLiveData<Result<Dto_login>> = MutableLiveData()
    fun login(request: LoginRequest){
        viewModelScope.launch(Dispatchers.IO) {
            loginResponse.postValue(repository.login(request))
        }
    }

}