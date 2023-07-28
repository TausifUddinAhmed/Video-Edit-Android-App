package com.example.videoedit

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.arthenica.ffmpegkit.MediaInformation
import com.example.videoedit.databinding.DailogAudioStripInputBinding
import com.example.videoedit.databinding.DailogShowVideoInfoBinding


private lateinit var alertDialog: AlertDialog

fun Activity.showVideoDetails(
    mediaInformation: MediaInformation
) {


    val dialogView = DailogShowVideoInfoBinding.inflate(LayoutInflater.from(applicationContext))
    alertDialog = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        .setView(dialogView.root).setCancelable(false)
        .create()

    dialogView.tvShowVideoInfo.text = buildString {
        append("File Name :")
        append(mediaInformation.filename)
        append("\n")
        append("Duration : ")
        append(mediaInformation.duration)
        append("\n")
        append("Format : ")
        append(mediaInformation.format)
        append("\n")
        append("Size : ")
        append(mediaInformation.size)
        append("\n")
        append("BitRate : ")
        append(mediaInformation.bitrate)
        append("\n")
        append("Start Time: ")
        append(mediaInformation.startTime)
        append("\n")
        append("Stream Size : ")
        append(mediaInformation.streams.size)
        append("\n")
        append("Height : ")
        append(mediaInformation.streams[0].height)
        append("\n")
        append("Width : ")
        append(mediaInformation.streams[0].width)
    }
    dialogView.buttonOk.setOnClickListener {
        alertDialog.dismiss()

    }

    alertDialog.show()

}
interface CallBack {
    fun onOkClick(type : Int =0)
}


private lateinit var alertDialogAudioStrip: AlertDialog

fun Activity.showAudioStripTimeInputField(
    callBack: ShowAudioStripTimeInputFieldCallBack
) {

    val dialogView = DailogAudioStripInputBinding.inflate(LayoutInflater.from(applicationContext))

    alertDialogAudioStrip = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        .setView(dialogView.root).setCancelable(false)
        .create()

    dialogView.buttonOk.setOnClickListener {
        callBack.onOkClick(dialogView.editTextStartTime.text.toString(), dialogView.editTextEndTime.text.toString())
        alertDialogAudioStrip.dismiss()

    }


    alertDialogAudioStrip.show()

}


fun dismissDialog(){
    alertDialog.dismiss()

}

fun isAlertDialogShowing(): Boolean = alertDialog.isShowing

interface ShowAudioStripTimeInputFieldCallBack {
    fun onOkClick(startTime : String, endTime : String)
}






