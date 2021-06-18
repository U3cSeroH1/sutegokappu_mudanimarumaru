package com.example.sutegokappu_mudanimarumaru

import android.os.Bundle
import androidx.wear.ambient.AmbientModeSupport

class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {

    override fun onEnterAmbient(ambientDetails: Bundle?) {
        // Handle entering ambient mode
    }

    override fun onExitAmbient() {
        // Handle exiting ambient mode
    }

    override fun onUpdateAmbient() {
        // Update the content
    }
}