package com.example.sutegokappu_mudanimarumaru

import android.os.AsyncTask


class Async(_callBacks: AsyncTaskCallbacks?) :
    AsyncTask<Int?, Void?, Int?>() {
    private var callbacks_: AsyncTaskCallbacks? = null
    protected override fun doInBackground(vararg params: Int?): Int? {
        //何かの処理

        onPostExecute(1)



        return null
    }

    override fun onPostExecute(result: Int?) {
        callbacks_!!.onTaskFinished() //非同期処理が終了したらonTaskFinished()を呼ぶ
    }

    override fun onCancelled() {
        callbacks_!!.onTaskCancelled() //非同期処理がキャンセルされたらonTaskCancelled()を呼ぶ
    }

    //コールバック登録用コンストラクタ
    init {
        callbacks_ = _callBacks //コールバック登録
    }

}