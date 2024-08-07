package com.geeksloft.udprelay

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var btnStart: Button? = null
    private var btnStop: Button? = null
    private var inputIP_1: EditText? = null
    private var inputIP_2: EditText? = null
    private var inputPort_1: EditText? = null
    private var inputPort_2: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnStart = findViewById(R.id.main_btn_start)
        btnStop = findViewById(R.id.main_btn_stop)
        btnStart!!.setOnClickListener(btnListener)
        btnStop!!.setOnClickListener(btnListener)

        inputIP_1 = findViewById(R.id.main_input_ip_1)
        inputIP_2 = findViewById(R.id.main_input_ip_2)
        inputPort_1 = findViewById(R.id.main_input_port_1)
        inputPort_2 = findViewById(R.id.main_input_port_2)
    }

    private val btnListener = View.OnClickListener {
        when(it.id) {
            R.id.main_btn_start -> {
                val intent = Intent(applicationContext, RelayService::class.java)
                intent.putExtra("DST_IP_1", if(inputIP_1!!.text.isNotEmpty()) inputIP_1!!.text.toString() else "")
                intent.putExtra("DST_PORT_1", if(inputPort_1!!.text.isNotEmpty()) inputPort_1!!.text.toString().toInt() else 0)
                intent.putExtra("DST_IP_2", if(inputIP_2!!.text.isNotEmpty()) inputIP_2!!.text.toString() else "")
                intent.putExtra("DST_PORT_2", if(inputPort_2!!.text.isNotEmpty()) inputPort_2!!.text.toString().toInt() else 0)
                startForegroundService(intent)
            }
            R.id.main_btn_stop -> stopService(Intent(applicationContext, RelayService::class.java))
        }
    }
}