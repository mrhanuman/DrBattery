package com.androhanu.drbattery

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.AnimationDrawable
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat


class MainActivity : AppCompatActivity() {
    private lateinit var tvPercent: TextView
    private lateinit var tvChargingType: TextView
    private lateinit var tvBatteryHealth: TextView
    private lateinit var tvBatteryTech: TextView
    private lateinit var tvBatteryCapacity: TextView
    private lateinit var tvBatteryTemp: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvBatteryProgress: TextView
    private lateinit var tvVolt: TextView
    private lateinit var minSwitch: SwitchCompat
    private lateinit var maxSwitch: SwitchCompat
    private lateinit var edtMinPercent: EditText
    private lateinit var edtMaxPercent: EditText
    private lateinit var ivBatteryAnimation: ImageView
    private lateinit var batteryAnimation: AnimationDrawable
    private lateinit var intentFilter: IntentFilter
    private lateinit var batteryBroadcastReceiver: BroadcastReceiver

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        supportActionBar!!.title = "Dr. Battery"
        supportActionBar!!.elevation = 0f

        tvStatus = findViewById(R.id.tv_battery_status)
        tvPercent = findViewById(R.id.tv_battery_percent)
        tvChargingType = findViewById(R.id.tv_charging_type)
        tvBatteryCapacity = findViewById(R.id.tv_Battery_capacity)
        tvBatteryHealth = findViewById(R.id.tv_Battery_health)
        tvBatteryTech = findViewById(R.id.tv_Battery_tech)
        tvBatteryTemp = findViewById(R.id.tv_Battery_temp)
        minSwitch = findViewById(R.id.switch_min)
        maxSwitch = findViewById(R.id.switch_max)
        edtMaxPercent = findViewById(R.id.edt_max_percent)
        edtMinPercent = findViewById(R.id.edt_min_percent)
        tvBatteryProgress = findViewById(R.id.tv_battery_percent_progressbar)
        tvVolt = findViewById(R.id.tv_Battery_volt)
        ivBatteryAnimation = findViewById(R.id.battery_animation)

        intentFilterAndBroadcast()


        val capacity = getBatteryCapacity(this)
        if (capacity > 0) {
            tvBatteryCapacity.text = "$capacity mAh"
        }
        
        
        minSwitch.setOnClickListener {
            edtMinPercent.isEnabled = !minSwitch.isChecked
        }

        maxSwitch.setOnClickListener {
            edtMaxPercent.isEnabled = !maxSwitch.isChecked
        }

//
//        val sharedPreferences = getSharedPreferences("save", MODE_PRIVATE)
//        val sharedPreferences2 = getSharedPreferences("max_switch_save", MODE_PRIVATE)
//        minSwitch.isChecked = sharedPreferences.getBoolean("value", true)
//        maxSwitch.isChecked = sharedPreferences2.getBoolean("max_switch_value", true)
//
//        minSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//            // on below line we are checking
//            // if switch is checked or not.
//            if (minSwitch.isChecked) {
//                // When switch checked
//                val editor = getSharedPreferences("save", MODE_PRIVATE).edit()
//                editor.putBoolean("value", true)
//                editor.apply()
//                minSwitch.isChecked = true
//            } else {
//                // When switch unchecked
//                val editor = getSharedPreferences("save", MODE_PRIVATE).edit()
//                editor.putBoolean("value", false)
//                editor.apply()
//                minSwitch.isChecked = false
//            }
//        }
//
//
//        maxSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//            // on below line we are checking
//            // if switch is checked or not.
//            if (maxSwitch.isChecked) {
//                // When switch checked
//                val editor = getSharedPreferences("max_switch_save", MODE_PRIVATE).edit()
//                editor.putBoolean("max_switch_value", true)
//                editor.apply()
//                maxSwitch.isChecked = true
//            } else {
//                // When switch unchecked
//                val editor = getSharedPreferences("max_switch_save", MODE_PRIVATE).edit()
//                editor.putBoolean("max_switch_value", false)
//                editor.apply()
//                maxSwitch.isChecked = false
//            }
//        }


    }


    private fun intentFilterAndBroadcast() {
        intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        batteryBroadcastReceiver = object : BroadcastReceiver() {

            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                chargingType(intent)
                batteryHealth(intent)
                currentChargingStatus(intent)

                if (intent != null) {
                    if (Intent.ACTION_BATTERY_CHANGED == intent.action) {

                        val getPercent = intent.getIntExtra("level", 0)
                        tvPercent.text = "$getPercent%"

                        minSwitchService(getPercent)
                        maxSwitchService(getPercent)


                        val voltage = intent.getIntExtra("voltage", 0) * 0.001f
                        tvVolt.text = "$voltage V"

                        val tech = intent.getStringExtra("technology")
                        tvBatteryTech.text = "$tech"

                        val temperature = intent.getIntExtra("temperature", -1) / 10f
                        tvBatteryTemp.text = "$temperature °C"


                    }
                }
            }
        }


    }

    private fun minSwitchService(batteryPercent: Int) {
        val edt = edtMinPercent.text.toString()
        if (minSwitch.isChecked && edtMinPercent.text.toString().toInt() == batteryPercent) {

            startService(Intent(this@MainActivity, AlertService::class.java))
        } else {
            stopService(Intent(this@MainActivity, AlertService::class.java))

        }

    }


    private fun maxSwitchService(batteryPercent: Int) {
        val edt = edtMinPercent.text.toString()
        if (maxSwitch.isChecked && edtMaxPercent.text.toString().toInt() == batteryPercent) {

            startService(Intent(this@MainActivity, AlertService::class.java))
        } else {
            stopService(Intent(this@MainActivity, AlertService::class.java))
        }
    }

    private fun getBatteryCapacity(context: Context): Long {
        val mBatteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val chargeCounter =
            mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val fullCapacity = (chargeCounter.toFloat() / capacity.toFloat() * 100f) / 1000
        return fullCapacity.toLong()
    }

    private fun batterLevelSetter() {

        val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager

        // Get the battery percentage and store it in a INT variable
        val batLevel: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (batLevel <= 10) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_10)
        } else if (batLevel <= 20) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_20)
        } else if (batLevel <= 30) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_30)
        } else if (batLevel <= 50) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_50)
        } else if (batLevel <= 70) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_70)
        } else if (batLevel <= 90) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_90)
        } else if (batLevel <= 100) {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_100)
        } else {
            ivBatteryAnimation.setBackgroundResource(R.drawable.baseline_battery_10)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun currentChargingStatus(intent: Intent?) {
        when (intent?.getIntExtra("status", -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> {
                tvStatus.text = "Charging"
                startBatteryAnimation()
            }
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {
                tvStatus.text = "Not Charging"
                stopBatteryAnimation()
            }
            BatteryManager.BATTERY_STATUS_DISCHARGING -> {
                tvStatus.text = "Discharging"
                stopBatteryAnimation()
            }
            BatteryManager.BATTERY_STATUS_FULL -> {
                tvStatus.text = "Full"
                stopBatteryAnimation()
            }
            BatteryManager.BATTERY_STATUS_UNKNOWN -> {
                tvStatus.text = "Unknown"
                stopBatteryAnimation()
            }
            else -> {
                tvStatus.text = "Null"
                stopBatteryAnimation()

            }
        }

    }

    private fun startBatteryAnimation() {

        ivBatteryAnimation.setBackgroundResource(R.drawable.battery_animation)
        batteryAnimation = ivBatteryAnimation.background as AnimationDrawable
        batteryAnimation.start()

    }

    private fun stopBatteryAnimation() {
        ivBatteryAnimation.setBackgroundResource(R.drawable.battery_animation)
        batteryAnimation = ivBatteryAnimation.background as AnimationDrawable
        batterLevelSetter()


    }

    @SuppressLint("SetTextI18n")
    private fun chargingType(intent: Intent?) {
        when (intent?.getIntExtra("plugged", -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> {
                tvChargingType.text = "AC Source"
            }
            BatteryManager.BATTERY_PLUGGED_USB -> {
                tvChargingType.text = "USB Source"
            }
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                tvChargingType.text = "Wireless Source"
            }
            else -> {
                tvChargingType.text = "Disconnected"
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun batteryHealth(intent: Intent?) {
        when (intent?.getIntExtra("health", 0)) {
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> {
                tvBatteryHealth.text = "Over Heat"
            }
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> {
                tvBatteryHealth.text = "Unspecified Failure"
            }
            BatteryManager.BATTERY_HEALTH_DEAD -> {
                tvBatteryHealth.text = "Dead"
            }
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> {
                tvBatteryHealth.text = "Over Voltage"
            }
            BatteryManager.BATTERY_HEALTH_COLD -> {
                tvBatteryHealth.text = "Cold"
            }
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> {
                tvBatteryHealth.text = "Unknown"
            }
            BatteryManager.BATTERY_HEALTH_GOOD -> {
                tvBatteryHealth.text = "Good"
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_app -> {}
            R.id.rate_us -> {}
            R.id.setting -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStart() {
        super.onStart()
        registerReceiver(batteryBroadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(batteryBroadcastReceiver)
    }

}

