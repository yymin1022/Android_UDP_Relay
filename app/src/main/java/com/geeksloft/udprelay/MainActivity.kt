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
    private var inputIP: EditText? = null
    private var inputPort: EditText? = null

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

        inputIP = findViewById(R.id.main_input_ip)
        inputPort = findViewById(R.id.main_input_port)
    }

    private val btnListener = View.OnClickListener {
        when(it.id) {
            R.id.main_btn_start -> {
                val intent = Intent(applicationContext, RelayService::class.java)
                intent.putExtra("DST_IP", if(inputIP!!.text.isNotEmpty()) inputIP!!.text.toString() else "")
                intent.putExtra("DST_PORT", if(inputPort!!.text.isNotEmpty()) inputPort!!.text.toString().toInt() else 0)
                startForegroundService(intent)
            }
            R.id.main_btn_stop -> stopService(Intent(applicationContext, RelayService::class.java))
        }
    }
}