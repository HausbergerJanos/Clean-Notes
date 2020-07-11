package com.codingwithmitch.cleannotes.framework.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingwithmitch.cleannotes.R
import com.codingwithmitch.cleannotes.util.printLogD
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

























