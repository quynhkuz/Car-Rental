package com.example.test.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class CarViewModel @Inject constructor(val repository: CarRepository) : ViewModel()
{



}