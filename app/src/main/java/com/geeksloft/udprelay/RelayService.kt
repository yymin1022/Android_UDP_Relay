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
    private var clientAddr_1: InetSocketAddress? = null
    private var clientAddr_2: InetSocketAddress? = null
    private var clientChannel_1: DatagramChannel? = null
    private var clientChannel_2: DatagramChannel? = null
    private var serverAddr_1: InetSocketAddress? = null
    private var serverAddr_2: InetSocketAddress? = null
    private var serverChannel_1: DatagramChannel? = null
    private var serverChannel_2: DatagramChannel? = null

    private var dstIP_1: String? = ""
    private var dstIP_2: String? = ""
    private var dstPort_1: Int = 0
    private var dstPort_2: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Starting Service")

        dstIP_1 = intent!!.getStringExtra("DST_IP_1")
        dstPort_1 = intent.getIntExtra("DST_PORT_1", 0)
        dstIP_2 = intent!!.getStringExtra("DST_IP_2")
        dstPort_2 = intent.getIntExtra("DST_PORT_2", 0)

        initSocket()
        SocketThread_1().start()
        SocketThread_2().start()
        startForegroundService()

        Log.d(LOG_TAG, "Started Service")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Stopping Service")

        SocketThread_1().join()
        SocketThread_2().join()
        while(SocketThread_1().isAlive || SocketThread_2().isAlive) {
            continue
        }
        closeSocket()

        Log.d(LOG_TAG, "Stopped Service")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initSocket() {
        if(clientChannel_1 == null) {
            clientAddr_1 = InetSocketAddress(8900)
            clientChannel_1 = DatagramChannel.open()
            clientChannel_1!!.socket().bind(clientAddr_1)
        }

        if(clientChannel_2 == null) {
            clientAddr_2 = InetSocketAddress(8901)
            clientChannel_2 = DatagramChannel.open()
            clientChannel_2!!.socket().bind(clientAddr_2)
        }

        if(serverChannel_1 == null) {
            serverAddr_1 = InetSocketAddress(InetAddress.getByName(dstIP_1), dstPort_1)
            serverChannel_1 = DatagramChannel.open()
        }

        if(serverChannel_2 == null) {
            serverAddr_2 = InetSocketAddress(InetAddress.getByName(dstIP_2), dstPort_2)
            serverChannel_2 = DatagramChannel.open()
        }
    }

    private fun closeSocket() {
        if(clientChannel_1 != null) {
            clientChannel_1!!.close()
            clientChannel_1 = null
        }

        if(clientChannel_2 != null) {
            clientChannel_2!!.close()
            clientChannel_2 = null
        }

        if(serverChannel_1 != null) {
            serverChannel_1!!.close()
            serverChannel_1 = null
        }

        if(serverChannel_2 != null) {
            serverChannel_2!!.close()
            serverChannel_2 = null
        }
    }

    inner class SocketThread_1: Thread() {
        override fun run() {
            super.run()

            if(clientChannel_1 == null || serverChannel_1 == null) {
                Log.e(LOG_TAG, "Client/Server Channel not initialized!!")
                stopSelf()
            }

            if(dstIP_1.isNullOrEmpty() || dstPort_1 == 0) {
                Log.e(LOG_TAG, "Invalid Destination Info!!")
                stopSelf()
            }

            Log.d(LOG_TAG, "Server Listening on ${clientAddr_1!!.address.hostAddress}:${clientAddr_1!!.port}")
            try {
                while(true) {
                    val receiveBuffer = ByteBuffer.allocateDirect(65507)
                    clientChannel_1!!.receive(receiveBuffer)
                    receiveBuffer.flip()
                    
                    val tmpBuffer = ByteArray(receiveBuffer.limit())
                    receiveBuffer.get(tmpBuffer)

                    val sendBuffer = ByteBuffer.wrap(tmpBuffer)
                    serverChannel_1!!.send(sendBuffer, serverAddr_1)
                }
            } catch(e: Exception) {
                Log.e(LOG_TAG, e.toString())
                stopSelf()
            }
        }
    }

    inner class SocketThread_2: Thread() {
        override fun run() {
            super.run()

            if(clientChannel_2 == null || serverChannel_2 == null) {
                Log.e(LOG_TAG, "Client/Server Channel not initialized!!")
                stopSelf()
            }

            if(dstIP_2.isNullOrEmpty() || dstPort_2 == 0) {
                Log.e(LOG_TAG, "Invalid Destination Info!!")
                stopSelf()
            }

            Log.d(LOG_TAG, "Server Listening on ${clientAddr_2!!.address.hostAddress}:${clientAddr_2!!.port}")
            try {
                while(true) {
                    val receiveBuffer = ByteBuffer.allocateDirect(65507)
                    clientChannel_2!!.receive(receiveBuffer)
                    receiveBuffer.flip()

                    val tmpBuffer = ByteArray(receiveBuffer.limit())
                    receiveBuffer.get(tmpBuffer)

                    val sendBuffer = ByteBuffer.wrap(tmpBuffer)
                    serverChannel_2!!.send(sendBuffer, serverAddr_2)
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