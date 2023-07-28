package com.example.videoedit

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi


var mp  : MediaPlayer? = null
@RequiresApi(Build.VERSION_CODES.O)
fun Activity.startMediaPlayer(output : String) : MediaPlayer {
    mp = MediaPlayer()
    mp?.setDataSource(output)
    mp?.prepare()
    mp?.start()
    return mp as MediaPlayer
}

fun stopMediaPlayer(){
    mp?.stop()
}