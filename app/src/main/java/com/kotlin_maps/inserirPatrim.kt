package com.kotlin_maps

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_inserir_patrim.*
import kotlinx.android.synthetic.main.activity_maps.*
import android.app.PendingIntent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.provider.Settings
import android.widget.Toast
import kotlin.experimental.and


class inserirPatrim : AppCompatActivity(), View.OnClickListener {

    private val CAMERA_REQUEST_CODE = 12345
    private val REQUEST_GALLERY_CAMERA = 45654

    var nfcAdapter: NfcAdapter? = null
    var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inserir_patrim)
        capture.setOnClickListener(this)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "Cadê teu NFC?", Toast.LENGTH_SHORT).show()

            finish()

            return
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, this.javaClass)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )

    }

    override fun onClick(v: View?) {
        when(v){
            capture -> {
                if(Build.VERSION.SDK_INT >= 23){
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_GALLERY_CAMERA)
                    }
                    else{
                        openCamera()
                    }
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null)
            startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            Log.d("MEUAPP", "Tirei a foto!")
            //Após tirada a foto insere a imagem no ImageView
            when(requestCode){
                CAMERA_REQUEST_CODE -> {
                    val extras = data?.getExtras()
                    val imageBitmap = extras?.get("data") as Bitmap
                    image.setImageBitmap(imageBitmap)
                    capture.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        resolveIntent(intent)
    }

    override fun onResume() {
        super.onResume()

        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled)
                showWirelessSettings()

            nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show()
        intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    private fun resolveIntent(intent: Intent) {

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.action)
            || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.action)
            || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.action)) {

            val id: ByteArray = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)

            tagPatri.setText(toDec(id).toString())
            tagPatri.isEnabled = false
            tagPatri.setTextColor(Color.BLUE)
        }
    }

    private fun toDec(bytes: ByteArray): Long {
        var result: Long = 0
        var factor: Long = 1
        for (i in bytes.indices) {

            val value = bytes[i].and(0xffL.toByte())

            result += value * factor
            factor *= 256L
        }
        return result
    }
}
