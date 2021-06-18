package com.example.sutegokappu_mudanimarumaru

interface AsyncTaskCallbacks {
    fun onTaskFinished() //終了
    fun onTaskCancelled() //キャンセル
}