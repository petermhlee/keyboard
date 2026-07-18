package com.cheonjiin.kbd

import android.app.Activity
import android.os.Build
import android.os.Bundle

// 마이크 권한 요청용 투명 액티비티 (IME 서비스는 직접 권한 요청 불가)
class VoicePermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        } else {
            finish()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        finish()
    }
}
