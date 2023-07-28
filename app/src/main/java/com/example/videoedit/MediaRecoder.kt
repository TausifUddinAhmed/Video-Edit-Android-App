package com.example.videoedit

import android.app.Activity
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File


private var mediaRecorder: MediaRecorder? = null
@RequiresApi(Build.VERSION_CODES.O)
fun Activity.initializeMediaRecorder(output : File) : MediaRecorder {
    mediaRecorder = MediaRecorder()
    mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
    mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    mediaRecorder?.setOutputFile(output)
    return mediaRecorder as MediaRecorder
}