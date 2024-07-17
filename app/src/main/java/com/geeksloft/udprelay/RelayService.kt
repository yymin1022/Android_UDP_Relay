package com.geeksloft.udprelay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class RelayService : Service() {
    private val LOG_TAG = "Relay Service"
    private var clientChannel: DatagramChannel? = null
    private var serverChannel: DatagramChannel? = null

    private var dstIP: String? = ""
    private var dstPort: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Starting Service")

        dstIP = intent!!.getStringExtra("DST_IP")
        dstPort = intent.getIntExtra("DST_PORT", 0)

        initSocket()
        SocketThread().start()
        startForegroundService()

        Log.d(LOG_TAG, "Started Service")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Stopping Service")

        SocketThread().join()
        while(SocketThread().isAlive) {
            continue
        }
        closeSocket()

        Log.d(LOG_TAG, "Stopped Service")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initSocket() {
        if(clientChannel == null) {
            clientChannel = DatagramChannel.open()
            clientChannel!!.socket().bind(InetSocketAddress(8900))
        }

        if(serverChannel == null) {
            serverChannel = DatagramChannel.open()
        }
    }

    private fun closeSocket() {
        if(clientChannel != null) {
            clientChannel!!.close()
            clientChannel = null
        }

        if(serverChannel != null) {
            serverChannel!!.close()
            serverChannel = null
        }
    }

    inner class SocketThread: Thread() {
        override fun run() {
            super.run()

            if(clientChannel == null || serverChannel == null) {
                Log.e(LOG_TAG, "Client/Server Channel not initialized!!")
                stopSelf()
            }

            if(dstIP.isNullOrEmpty() || dstPort == 0) {
                Log.e(LOG_TAG, "Invalid Destination Info!!")
                stopSelf()
            }

            Log.d(LOG_TAG, "Server Listening on ${serverChannel!!.localAddress}")
            try {
                while(true) {
                    val receiveBuffer = ByteBuffer.allocateDirect(65507)
                    clientChannel!!.receive(receiveBuffer)
                    receiveBuffer.flip()


                    val tmpBuffer = ByteArray(receiveBuffer.limit())
                    receiveBuffer.get(tmpBuffer)
                    Log.i(LOG_TAG, "Received ${tmpBuffer.contentToString()}")

                    val sendBuffer = ByteBuffer.wrap(tmpBuffer)
                    val targetAddress = InetSocketAddress(InetAddress.getByName(dstIP), dstPort)
                    serverChannel!!.send(sendBuffer, targetAddress)
                }
            } catch(e: Exception) {
                Log.e(LOG_TAG, e.toString())
                stopSelf()
            }
        }
    }

    private fun startForegroundService(){
        val serviceChannel = NotificationChannel(
            "FOREGROUND_SERVICE",
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            createNotificationChannel(serviceChannel)
        }

        startForeground(1234, Notification.Builder(this, "FOREGROUND_SERVICE")
            .setContentTitle("Foreground Service")
            .build())
    }
}