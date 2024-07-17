package com.geeksloft.udprelay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.Arrays

class RelayService : Service() {
    private val LOG_TAG = "Relay Service"
    private var clientSocket: DatagramSocket? = null
    private var serverSocket: DatagramSocket? = null

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
        if(clientSocket == null) {
            clientSocket = DatagramSocket()
        }

        if(serverSocket == null) {
            serverSocket = DatagramSocket(8900)
        }
    }

    private fun closeSocket() {
        if(clientSocket != null) {
            clientSocket!!.close()
            clientSocket = null
        }

        if(serverSocket != null) {
            serverSocket!!.close()
            serverSocket = null
        }
    }

    inner class SocketThread: Thread() {
        override fun run() {
            super.run()

            if(clientSocket == null || serverSocket == null) {
                Log.e(LOG_TAG, "Socket not initialized!!")
                stopSelf()
            }

            if(dstIP.isNullOrEmpty() || dstPort == 0) {
                Log.e(LOG_TAG, "Invalid Destination Info!!")
                stopSelf()
            }

            Log.d(LOG_TAG, "Server Listening on ${serverSocket!!.localAddress.hostAddress}:${serverSocket!!.localPort}")
            try {
                val receiveBuffer = ByteArray(20480)
                val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                while(true) {
                    serverSocket!!.receive(receivePacket)
                    Log.i(LOG_TAG, "Received ${receivePacket.data.contentToString()}")

                    val sendPacket = DatagramPacket(receiveBuffer, receiveBuffer.size, InetAddress.getByName(dstIP), dstPort)
                    clientSocket!!.send(sendPacket)

                    Arrays.fill(receiveBuffer, 0)
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