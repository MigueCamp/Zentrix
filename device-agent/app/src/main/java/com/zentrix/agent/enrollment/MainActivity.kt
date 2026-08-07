package com.zentrix.agent.enrollment

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.zentrix.agent.R
import com.zentrix.agent.sync.ApiClient
import com.zentrix.agent.sync.EnrollRequest
import com.zentrix.agent.sync.TokenStore
import com.zentrix.agent.telemetry.HeartbeatWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (TokenStore.isEnrolled()) {
            showEnrolledView()
        } else {
            showEnrollmentForm()
        }
    }

    private fun showEnrollmentForm() {
        val tokenInput = EditText(this).apply { hint = getString(R.string.enrollment_token_hint) }
        val imeiInput = EditText(this).apply { hint = getString(R.string.enrollment_imei_hint) }
        statusText = TextView(this).apply { text = getString(R.string.enrollment_pending) }

        val submitButton = Button(this).apply {
            text = getString(R.string.enrollment_submit)
            setOnClickListener {
                enroll(tokenInput.text.toString().trim(), imeiInput.text.toString().trim())
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 128, 64, 64)
            addView(statusText)
            addView(tokenInput)
            addView(imeiInput)
            addView(submitButton)
        }
        setContentView(layout)
    }

    private fun enroll(enrollmentToken: String, imei: String) {
        if (enrollmentToken.isEmpty() || imei.isEmpty()) {
            statusText.text = getString(R.string.enrollment_missing_fields)
            return
        }
        lifecycleScope.launch {
            try {
                val response = ApiClient.instance.enroll(EnrollRequest(enrollmentToken, imei, null))
                if (response.isSuccessful && response.body() != null) {
                    TokenStore.saveDeviceToken(response.body()!!.deviceToken)
                    scheduleHeartbeat()
                    showEnrolledView()
                } else {
                    statusText.text = getString(R.string.enrollment_failed)
                }
            } catch (e: Exception) {
                statusText.text = getString(R.string.enrollment_failed)
            }
        }
    }

    private fun scheduleHeartbeat() {
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            HeartbeatWorker.WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private fun showEnrolledView() {
        val view = TextView(this).apply {
            text = getString(R.string.enrollment_done)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(48, 96, 48, 48)
        }
        setContentView(view)
    }
}
