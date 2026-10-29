package com.rabbyx.newtonixairevivededition.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()
    val text: LiveData<String> = _index.map {
        when (it) {
            1 -> "Welcome to Tab 1"
            2 -> "Welcome to Tab 2"
            else -> "Hello world from section: $it"
        }
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}