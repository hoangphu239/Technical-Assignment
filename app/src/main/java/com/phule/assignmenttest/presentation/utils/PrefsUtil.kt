package com.phule.assignmenttest.presentation.utils

import android.content.Context

class PrefsUtil(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AD_INDEX = "key_ad_index"
    }

    fun saveAdIndex(index: Int) {
        sharedPreferences.edit().putInt(KEY_AD_INDEX, index).apply()
    }

    fun getAdIndex(): Int {
        return sharedPreferences.getInt(KEY_AD_INDEX, 0)
    }

    fun resetAdIndex() {
        sharedPreferences.edit().putInt(KEY_AD_INDEX, 0).apply()
    }
}