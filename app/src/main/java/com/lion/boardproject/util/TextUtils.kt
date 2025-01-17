package com.lion.boardproject.util

import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun TextView.setRelativeTimeText(timestamp: Long) {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time
    val seconds = abs(diff) / 1000

    val relativeTime = when {
        seconds < 60 -> "방금 전"
        seconds < 60 * 60 -> "${seconds / 60}분 전"
        seconds < 60 * 60 * 24 -> "${seconds / (60 * 60)}시간 전"
        seconds < 60 * 60 * 24 * 30 -> "${seconds / (60 * 60 * 24)}일 전"
        seconds < 60 * 60 * 24 * 365 -> "${seconds / (60 * 60 * 24 * 30)}달 전"
        else -> "${seconds / (60 * 60 * 24 * 365)}년 전"
    }

    text = relativeTime
}