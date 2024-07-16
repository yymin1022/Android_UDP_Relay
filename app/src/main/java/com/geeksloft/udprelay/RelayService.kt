package com.geeksloft.udprelay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.Arrays

class RelayService : Service() {
    private val LOG_TAG = "Relay Service"
    private var serverSocket: DatagramSocket? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Starting Service")

        initSocket()
        SocketThread().start()

        Log.d(LOG_TAG, "Started Service")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "Stopping Service")

        SocketThread().join()
        while(SocketThread().isAlive)
        closeSocket()

        Log.d(LOG_TAG, "Stopped Service")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initSocket() {
        if(serverSocket == null){
            serverSocket = DatagramSocket(8900)
        }
    }

    private fun closeSocket() {
        if(serverSocket != null){
            serverSocket!!.close()
            serverSocket = null
        }
    }

    inner class SocketThread: Thread() {
        override fun run() {
            super.run()

            if(serverSocket == null) {
                Log.e(LOG_TAG, "Socket not initialized!!")
                return
            }

            Log.d(LOG_TAG, "Server Listening on ${serverSocket!!.localAddress.hostAddress}:${serverSocket!!.localPort}")
            try {
                val receiveBuffer = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                while(true) {
                    serverSocket!!.receive(receivePacket)
                    Log.i(LOG_TAG, "Received ${receivePacket.data.contentToString()}")
                    Arrays.fill(receiveBuffer, 0)
                }
            } catch(e: Exception) {
                Log.e(LOG_TAG, e.toString())
            }
        }
    }
}