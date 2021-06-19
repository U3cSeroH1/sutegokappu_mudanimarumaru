package com.example.sutegokappu_mudanimarumaru

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.ambient.AmbientModeSupport
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


class MainActivity : WearableActivity(),  SensorEventListener, AsyncTaskCallbacks {

    private var mSensorManager: SensorManager? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var heartBeatTextView: TextView? = null
    private var pressureTextView: TextView? = null
    private var lightTextView: TextView? = null
    private var accelerometerTextView: TextView? = null

    private var speed = 0f

    private val id = "mudanimarumaru_01"


    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates: Boolean = true

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    val REQUEST_CHECK_SETTINGS:Int = 1

    private var mActivityTransitionsPendingIntent : PendingIntent? = null
    private var mTransitionsReceiver: TransitionsReceiver? = null
    private val TRANSITIONS_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

    inner class TransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)!!
                Log.e("アクティビティ", "uwaa")
                for (event in result.transitionEvents) {
                    // chronological sequence of events....
                    Log.e("アクティビティ", event.activityType.toString())
                    pushNotificationCustom("動いた！！！！" + event.activityType.toString())
                }

                pushNotificationCustom("なにかが動いた！！！！")

            }

            Log.e("なんか来てるよ！！", ActivityTransitionResult.hasResult(intent).toString())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        heartBeatTextView = findViewById<TextView>(R.id.heartBeat_ui_text)
        pressureTextView = findViewById<TextView>(R.id.ambientTemperature_ui_text)
        lightTextView = findViewById<TextView>(R.id.light_ui_text)
        accelerometerTextView = findViewById<TextView>(R.id.accelerometer_ui_text)

        //センサー初期化
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorHeartRate: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val sensorPressure: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val sensorTypeLight: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        val sensorAccelerometer: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager!!.registerListener(this, sensorHeartRate, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorPressure, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorTypeLight, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager!!.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)


        //アクティビティトランジションのWIP

        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        mActivityTransitionsPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)

        mTransitionsReceiver = TransitionsReceiver()
        registerReceiver(mTransitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))


        val transitions = mutableListOf<ActivityTransition>()

        transitions.add(            ActivityTransition.Builder()
            .setActivityType(DetectedActivity.IN_VEHICLE)
            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
            .build())


        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build())

        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())

        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build())

        val request = ActivityTransitionRequest(transitions)

        val activityTask = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, mActivityTransitionsPendingIntent)

        activityTask.addOnSuccessListener {
            // Handle success
            Log.e("アクティビティ", "成功")
            //myPendingIntent.cancel()
        }
        activityTask.addOnFailureListener { e: Exception ->
            // Handle error
            Log.e("アクティビティ", e.localizedMessage)
        }

        // Enables Always-on
        //ambientController = AmbientModeSupport.attach(this)
        setAmbientEnabled()

        //通知周り
        createNotificationChannel()

        //位置情報らへん
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...
                    //Log.d("リアルタイム更新で位置情報を取れた！！！！！！", location!!.latitude.toString())
                    accelerometerTextView!!.text = "緯度" + location!!.latitude.toString() + "軽度" + location!!.longitude.toString()
                }
            }
        }
        if (requestingLocationUpdates) startLocationUpdates()
        //end

        //位置情報権限の要求
        val builder = LocationSettingsRequest.Builder()
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        //end


        //汎用
        val handler = Handler()
        val r: Runnable = object : Runnable {
            var count = 0
            override fun run() {
                // UIスレッド
                //                count++
                //                if (count > 5) { // 5回実行したら終了
                //                    return
                //                }
                //doSomething() // 何かやる

                Log.d("くりかえす！", "一分ごとに！！")

                //ここに書いてけ

                //pushNotificationCustom("一分後との奴")

                handler.postDelayed(this, 60000)


            }
        }
        handler.post(r)


        val async = Async(this)
        async.execute() //非同期処理呼び出し

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

    //override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()

    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun onTaskFinished() {
        Log.d("ko-rubakku!", "わーーーーー！！！")
    }

    override fun onTaskCancelled() {

    }


}