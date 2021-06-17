package com.example.sutegokappu_mudanimarumaru

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : WearableActivity(), SensorEventListener {

    private var mSensorManager: SensorManager? = null


    private var heartBeatTextView: TextView? = null
    private var pressureTextView: TextView? = null
    private var lightTextView: TextView? = null
    private var accelerometerTextView: TextView? = null

    private var speed = 0f

    private val id = "mudanimarumaru_01"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        heartBeatTextView = findViewById<TextView>(R.id.heartBeat_ui_text)
        pressureTextView = findViewById<TextView>(R.id.ambientTemperature_ui_text)
        lightTextView = findViewById<TextView>(R.id.light_ui_text)
        accelerometerTextView = findViewById<TextView>(R.id.accelerometer_ui_text)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorHeartRate: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val sensorPressure: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val sensorTypeLight: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sensorAccelerometer: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager!!.registerListener(this, sensorHeartRate, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorTypeLight, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        // Enables Always-on
        setAmbientEnabled()
        createNotificationChannel()

        var hoges = hoge()
        Log.d(hoges,lightTextView!!.text as String)

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onSensorChanged(event: SensorEvent?) {

        if(event != null){

            if(event!!.sensor.stringType == Sensor.STRING_TYPE_HEART_RATE){
                heartBeatTextView!!.text = event!!.values[0].toString()
                //pushNotificationCustom("心拍数が変わった")
//                Log.d("心拍数が変わった！",heartBeatTextView!!.text as String)
            }

            if(event!!.sensor.stringType == Sensor.STRING_TYPE_PRESSURE){
                pressureTextView!!.text = event!!.values[0].toString()
                //pushNotificationCustom("温度が変わった")
//                Log.d("気圧が変わった！",pressureTextView!!.text as String)
            }

            if(event!!.sensor.stringType == Sensor.STRING_TYPE_LIGHT){
                lightTextView!!.text = event!!.values[0].toString()
                //pushNotificationCustom("温度が変わった")
//                Log.d("照度が変わった！",lightTextView!!.text as String)
            }

//            if(event!!.sensor.stringType == Sensor.STRING_TYPE_LINEAR_ACCELERATION){
//                accelerometerTextView!!.text = event!!.values[0].toString()
//                //pushNotificationCustom("温度が変わった")
//                Log.d("速度が変わった！",accelerometerTextView!!.text as String)
//            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    private fun pushNotificationCustom(string:String){
        val notificationId = 1

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)


        var builder = NotificationCompat.Builder(this, id)
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle(getString(R.string.channel_name))
            .setContentText(string)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val vibrationPattern = longArrayOf(0, 500, 50, 300)
        //-1 - don't repeat
        //-1 - don't repeat
        val indexInPatternToRepeat = -1
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat)

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hoge(): String{
        Log.d("照度が変わった！",lightTextView!!.text as String)
        return "unko"
    }


}