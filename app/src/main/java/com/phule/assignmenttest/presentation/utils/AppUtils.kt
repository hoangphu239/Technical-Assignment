package com.phule.assignmenttest.presentation.utils

import android.content.Context
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.sqrt

object AppUtils {
    private fun isPerfectSquare(num: Int): Boolean {
        val sqrtNum = sqrt(num.toDouble()).toInt()
        return sqrtNum * sqrtNum == num
    }

    fun isFibonacci(n: Int): Boolean {
        return isPerfectSquare(5 * n * n + 4) || isPerfectSquare(5 * n * n - 4)
    }

    fun loadJsonFromRaw(context: Context, rawResId: Int): String? {
        return try {
            val inputStream = context.resources.openRawResource(rawResId)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()
            jsonString
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun <T> parseJson(jsonString: String?, classType: Class<T>): T? {
        return try {
            Gson().fromJson(jsonString, classType)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}