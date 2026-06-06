package com.example.satwalaya.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.satwalaya.data.repository.BookingRepository

class HomeViewModel : ViewModel() {

    private val repository = BookingRepository()

    private val _activeCount = MutableLiveData<Int>()
    val activeCount: LiveData<Int> = _activeCount

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    fun loadActiveBookings(userId: String) {
        repository.countActiveBookings(userId) { count ->
            _activeCount.postValue(count)
        }
    }

    fun setUserName(name: String) {
        _userName.value = name.ifEmpty { "Pemilik Hewan" }
    }
}