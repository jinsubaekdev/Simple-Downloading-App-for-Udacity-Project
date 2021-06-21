package com.udacity

import android.app.NotificationManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        notificationManager.cancelAll()

        val downloadTitle = intent.getStringExtra(MainActivity.DOWNLOAD_TITLE)
        val downloadStatus = intent.getStringExtra(MainActivity.DOWNLOAD_STATUS)

        text_file_name.text = downloadTitle
        text_status.text = downloadStatus
        if(text_status.text == "Fail")
            text_status.setTextColor(Color.RED)
        else
            text_status.setTextColor(Color.BLUE)

        val motionListener = object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}

            override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                if(currentId == R.id.end)
                    finish()
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}
        }
        motion_layout.addTransitionListener(motionListener)
    }
}
