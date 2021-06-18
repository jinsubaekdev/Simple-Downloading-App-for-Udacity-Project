package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 0
    private val NOTIFICATION_ID = 0

    private var downloadID: Long = 0
    private var downloadTitle: String = ""
    private var downloadStatus :String = ""

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if(radioGroup.checkedRadioButtonId > 0){
                download()
                custom_button.downloadStarted()
            } else {
                Toast.makeText(this, getString(R.string.message_radio_not_selected), Toast.LENGTH_SHORT).show()
                custom_button.downloadFailed()
            }

        }

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            Log.i(TAG, "id: $id")
            custom_button.downloadCompleted()
            id?.let {
                getDownloadInfo(id)
                sendNotification()
            }
        }
    }

    private fun download() {
        val title = when(radioGroup.checkedRadioButtonId){
            radio_button1.id -> getString(R.string.radio_glide)
            radio_button2.id -> getString(R.string.radio_load_app)
            else -> getString(R.string.radio_glide)
        }

        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(title)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"

        const val DOWNLOAD_TITLE = "DOWNLOAD_TITLE"
        const val DOWNLOAD_STATUS = "DOWNLOAD_STATUS"
    }

    private fun sendNotification() {
        val contentIntent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DOWNLOAD_TITLE, downloadTitle)
            putExtra(DOWNLOAD_STATUS, downloadStatus)
        }
        val contentPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.icon_download)
            .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
            .setAutoCancel(true)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.icon_download, getString(R.string.notification_button), contentPendingIntent)

        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel(channel_id: String, channel_name: String) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channel_id,
                channel_name,
            NotificationManager.IMPORTANCE_DEFAULT).apply {
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun getDownloadInfo(id: Long) {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val query: DownloadManager.Query = DownloadManager.Query()
        query.setFilterById(id)

        val cursor = downloadManager.query(query)
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Empty row")
            return
        }

        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)
        downloadStatus = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> "Success"
            DownloadManager.STATUS_FAILED -> "Fail"
            else -> ""
        }
        val columnTitle = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        downloadTitle = cursor.getString(columnTitle)

    }

}
