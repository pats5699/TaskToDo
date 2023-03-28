package com.abulnes16.purrtodo.utils

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.*

object DataTransformationUtil {

    fun getMonthFromString(date: String): Int {
        val parsedDate = SimpleDateFormat("MMM", Locale.getDefault()).parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate!!
        return calendar.get(Calendar.MONTH)
    }

    fun hideKeyboard(activity: FragmentActivity?) {
        activity?.currentFocus?.let { view ->
            val imm =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun getGreetingFromHour(): String {
        val calendar = Calendar.getInstance();
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 6..11 -> {
                "Good morning"
            }
            in 13..18 -> {
                "Good afternoon"
            }
            in 18..20 -> {
                "Good evening"
            }
            else -> {
                "Good night"
            }
        }

        return greeting

    }

}