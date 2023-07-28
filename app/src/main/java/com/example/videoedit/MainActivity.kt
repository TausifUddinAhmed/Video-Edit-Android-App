package com.example.videoedit

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.example.videoedit.databinding.ActivityMainBinding
import getSecond
import isTimeFormatValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity(), ShowAudioStripTimeInputFieldCallBack {

    lateinit var binding: ActivityMainBinding

    private var isVideoPlaying = false
    private var audioRecordOutputFileString: String? = null
    private var clippedAudioFileString: String? = null
    private var finalAudioFileString: String? = null
    private var finalVideoFileString: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var recordingState: Boolean = false
    private var recordingStopped: Boolean = false
    private var progressDialog: ProgressDialog? = null
    private var audioExtractFromVideoFileString: String? = null
    private var mStartTime: Int = 0
    private var mEndTime: Int = 0


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        checkPermission()
        progressDialog = ProgressDialog(this@MainActivity)
        binding.videoView.setOnClickListener {
            try {
                if (inputVideoUri != null) {
                    if (isAppliedFilter) {
                        binding.videoView.setVideoPath(updatedVideoFilePath)
                    } else {
                        binding.videoView.setVideoURI(inputVideoUri)
                    }
                    binding.videoView.start()
                    binding.selectVideo.text = getString(R.string.stop_playing)
                    isVideoPlaying = true
                }
            } catch (e: java.lang.Exception) {
                Log.e("Error", e.toString())
            }

        }


        binding.selectVideo.setOnClickListener {
            if (!isVideoPlaying) {
                selectVideoLauncher.launch("video/*")
            } else {
                binding.videoView.stopPlayback()
                binding.selectVideo.text = getString(R.string.select_video)
                isVideoPlaying = false
            }
        }

        binding.videoView.setOnCompletionListener {
            binding.selectVideo.text = getString(R.string.select_video)
            isVideoPlaying = false
        }

        binding.buttonRecord.setOnClickListener {
            if (inputVideoUriString != null) {
                if (!recordingState) {
                    val appSpecificInternalStorageDirectory: File = filesDir
                    val audioRecordOutputFile =
                        File(appSpecificInternalStorageDirectory, "recording.mp3")
                    audioRecordOutputFileString = audioRecordOutputFile.toString()
                    mediaRecorder = initializeMediaRecorder(audioRecordOutputFile)
                    binding.videoView.stopPlayback()
                    startRecording()
                    binding.buttonRecord.text = getString(R.string.stop_recording)
                } else {
                    stopRecording()
                    binding.buttonRecord.text = getString(R.string.start_recording)
                }
            } else {
                Toast.makeText(this, "Please Select Video First", Toast.LENGTH_SHORT).show()
            }


        }

        binding.buttonPlayRecordedAudio.setOnClickListener {
            try {
                if (audioRecordOutputFileString != null) {
                    mp = startMediaPlayer(audioRecordOutputFileString.toString())
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.no_recorded_audio_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: java.lang.Exception) {
                Log.e("Error", e.stackTraceToString())
            }

        }

        binding.buttonShowVideoDetails.setOnClickListener {
            Log.e("buttonShowVideoDetails ", "input_video_uri " + inputVideoUriString.toString())
            if (inputVideoUriString != null) {
                inputVideoUriString = FFmpegKitConfig.getSafParameterForRead(this, inputVideoUri)
                val mediaInformation =
                    FFprobeKit.getMediaInformation(inputVideoUriString).mediaInformation
                showVideoDetails(mediaInformation)
            } else {
                Toast.makeText(this, "Please Select Video First", Toast.LENGTH_SHORT).show()
            }

        }

        binding.buttonAddAudioInVideo.setOnClickListener {

            showAudioStripTimeInputField(this)


            // for separated audio
            /*      val appSpecificInternalStorageDirectory: File = filesDir
                  val audioRecordOutputFile = File(appSpecificInternalStorageDirectory, "separatedAudio.aac")
                  audioExtractFromVideoFileString = audioRecordOutputFile.toString()

                 // val folder = cacheDir
                //  val separatedAudioFile = File(folder, System.currentTimeMillis().toString() +"separatedAudio.aac")
                  binding.videoView.stopPlayback()
                  val exe = ("-y -i $inputVideoUriString -vn -acodec copy ${audioRecordOutputFile.absolutePath}")
               //   val exe = ("-y -i $inputVideoUriString -c:v copy -an ${file.absolutePath}")
               //   val exe = ("-y -i $inputVideoUriString -filter_complex [0:v]trim=0:${2000 / 1000},setpts=PTS-STARTPTS[v1];[0:v]trim=${2000 / 1000}:${4000/ 1000},setpts=2*(PTS-STARTPTS)[v2];[0:v]trim=${4000 / 1000},setpts=PTS-STARTPTS[v3];[0:a]atrim=0:${2000 / 1000},asetpts=PTS-STARTPTS[a1];[0:a]atrim=${2000 / 1000}:${4000 / 1000},asetpts=PTS-STARTPTS,atempo=0.5[a2];[0:a]atrim=${4000 / 1000},asetpts=PTS-STARTPTS[a3];[v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1 -b:v 2097k -vcodec mpeg4 -crf 0 -preset superfast ${file.absolutePath}")
                  executeFfmpegCommand(exe, audioRecordOutputFile.absolutePath, FFMpegOperationType.EXTRACT_AUDIO)*/

        }


    }


    private var inputVideoUriString: String? = null
    private var inputVideoUri: Uri? = null


    private val selectVideoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                inputVideoUri = it
                inputVideoUriString = it.toString()
                Log.e("input_video_uriTT", "input_video_uri " + inputVideoUriString)
                inputVideoUriString = FFmpegKitConfig.getSafParameterForRead(this, it)
                // Log.e("input_video_uri", "input_video_uri "+ input_video_uri)
                binding.videoView.setVideoURI(it)
                //after successful retrieval of the video and properly setting up the retried video uri in
                //VideoView, Start the VideoView to play that video
                binding.videoView.start()
                binding.selectVideo.text = getString(R.string.stop_playing)
                isVideoPlaying = true
            }
        }


    private fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            recordingState = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        if (recordingState) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            recordingState = false
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }


    private var extractedClippedAudioFilePath: String = ""
    private var mergedFinalAudioFilePath: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeExtractedAudioFromVideoFfmpegCommand(exe: String, filePath: String) {
        progressDialog?.show()

        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    Log.e("filePathTT", "filePath " + filePath)
                    extractedAudioOutputFileString = filePath
               //     progressDialog?.dismiss()
                    val appSpecificInternalStorageDirectory: File = filesDir
                    val clippedAudioOutputFile = File(appSpecificInternalStorageDirectory, "clipped.mp3")
                    clippedAudioFileString = clippedAudioOutputFile.toString()

                    Toast.makeText(this@MainActivity,
                        "Extracted audio from video",
                        Toast.LENGTH_SHORT
                    ).show()
                    val exe_clip_command = ("-y -ss  $mStartTime -t $mEndTime -i $extractedAudioOutputFileString ${clippedAudioOutputFile.absolutePath}")
                    executeFfmpegClippedCommand(
                        exe_clip_command,
                        clippedAudioOutputFile.absolutePath)



                } else {
                    progressDialog?.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(this@MainActivity, "Something Went Wrong!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog?.setMessage("Applying Filter..${log.message}")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeFfmpegClippedCommand(
        exe: String,
        filePath: String) {
        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    //after successful execution of ffmpeg command,
                    //again set up the video Uri in VideoView
                    Log.e("filePathTTT", "filePath " + filePath)

                        extractedClippedAudioFilePath = filePath
                         mp = startMediaPlayer(filePath)
                        Log.e("extractedClippedAudioFilePath", extractedClippedAudioFilePath)
                         //progressDialog?.dismiss()
                        Toast.makeText(this@MainActivity, "Audio Clipped", Toast.LENGTH_SHORT).show()


                        val appSpecificInternalStorageDirectory: File = filesDir
                        val finalAudioOutputFile =
                            File(appSpecificInternalStorageDirectory, "final.mp3")
                        finalAudioFileString = finalAudioOutputFile.toString()

                        val appSpecificInternalStorageDirectoryV2: File = filesDir
                        val fileV2 = File(appSpecificInternalStorageDirectoryV2, "file_test.mp3")
                        var fileV2String = finalAudioOutputFile.toString()
                        val exe_audio_merge_command = ("-y -i concat:$audioRecordOutputFileString|$clippedAudioFileString -i $audioRecordOutputFileString -acodec copy ${finalAudioOutputFile.absolutePath} -map_metadata 0:1")
                        executeFfmpegMergeWithRecordedAudio(
                            exe_audio_merge_command,
                            finalAudioOutputFile.absolutePath,
                        )




                } else {
                    progressDialog?.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(this@MainActivity, "Something Went Wrong!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog?.setMessage("Applying Filter..${log.message}")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun executeFfmpegMergeWithRecordedAudio(
        exe: String,
        filePath: String
    ) {
        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    //after successful execution of ffmpeg command,
                    //again set up the video Uri in VideoView
                    Log.e("filePathTTT", "filePath " + filePath)
                        mergedFinalAudioFilePath = filePath
                           mp = startMediaPlayer(filePath)
                      //  audioRecordOutputFileString = filePath
                        Log.e("mergedFinalAudioFilePath", mergedFinalAudioFilePath)
                        Toast.makeText(
                            this@MainActivity,
                            "Merge Record Applied",
                            Toast.LENGTH_SHORT
                        ).show()

                        progressDialog?.dismiss()

                        /*   val appSpecificInternalStorageDirectory: File = filesDir
                           val finalVideoOutputFile = File(appSpecificInternalStorageDirectory, "final.mp3")
                           finalAudioFileString = finalVideoOutputFile.toString()

                           val exe_audio_video_merge_command = ("-y -i $inputVideoUriString -i $audioRecordOutputFileString -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 RecordingB.mp4" )
                           executeFfmpegMergeWithVideo(exe_audio_video_merge_command, finalVideoOutputFile.absolutePath, FFMpegOperationType.MERGE_WITH_VIDEO)
   */


                } else {
                    progressDialog?.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(this@MainActivity, "Something Went Wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog?.setMessage("Applying Filter..${log.message}")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }


    private fun executeFfmpegMergeWithVideo(
        exe: String,
        filePath: String,
        ffmpegOperationType: FFMpegOperationType
    ) {
        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    //after successful execution of ffmpeg command,
                    //again set up the video Uri in VideoView
                    Log.e("filePathTTT", "filePath " + filePath)
                    /*     binding.videoView.setVideoPath(filePath)
                         //change the video_url to filePath, so that we could do more manipulations in the
                         //resultant video. By this we can apply as many effects as we want in a single video.
                         //Actually there are multiple videos being formed in storage but while using app it
                         //feels like we are doing manipulations in only one video
                         audioRecordOutputFileString = filePath
                         //play the result video in VideoView
                         binding.videoView.start()*/

                    if (ffmpegOperationType == FFMpegOperationType.MERGE_WITH_VIDEO) {

                        inputVideoUriString = filePath
                        Log.e("inputVideoUriString", inputVideoUriString.toString())
                        progressDialog?.dismiss()
                        Toast.makeText(
                            this@MainActivity,
                            "Final Merge With Video Applied",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                } else {
                    progressDialog?.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(this@MainActivity, "Something Went Wrong!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog?.setMessage("Applying Filter..${log.message}")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_MEDIA_AUDIO,
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }


    private var extractedAudioOutputFileString = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOkClick(startTime: String, endTime: String) {

        try {

            if (startTime.isEmpty() && endTime.isEmpty()) {
                Toast.makeText(this, "Field Can't be empty", Toast.LENGTH_SHORT).show()
            } else if (!isTimeFormatValid(startTime) && !isTimeFormatValid(endTime)) {
                Toast.makeText(this, "Please provide correct time format", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mStartTime = getSecond(startTime)
                mEndTime = getSecond((endTime))
                Log.e("mStartTime ", mStartTime.toString())
                Log.e("mEndTime ", mEndTime.toString())
                /*val appSpecificInternalStorageDirectory: File = filesDir
                val extractedAudioOutputFile =
                    File(appSpecificInternalStorageDirectory, "extractedAudio.mp3")
                extractedAudioOutputFileString = extractedAudioOutputFile.toString()
                val exe_command_1 = ("-y -i $inputVideoUriString -vn -acodec copy ${extractedAudioOutputFile.absolutePath}")
                executeExtractedAudioFromVideoFfmpegCommand(
                    exe_command_1,
                    extractedAudioOutputFile.absolutePath
                )*/

                val appSpecificInternalStorageDirectory: File = filesDir
               val finalVideoOutputFile = File(appSpecificInternalStorageDirectory, "final.mp4")

               val exe_command = ("-y -i $inputVideoUriString -i $audioRecordOutputFileString -c:v copy -map 0:v:0 -map 1:a:0 ${finalVideoOutputFile.absolutePath}")
               executeFfmpegToReplaceAudio(exe_command,finalVideoOutputFile.toString())

            }
        } catch ( e: java.lang.Exception){
            Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_SHORT)
                .show()
        }

    }

    override fun onStop() {
        super.onStop()
        stopMediaPlayer()
    }


    private var isAppliedFilter = false;
    private var updatedVideoFilePath = "";

    private fun executeFfmpegToReplaceAudio(exe: String, filePath: String) {
        progressDialog?.show()
        FFmpegKit.executeAsync(exe, { session ->
            val returnCode = session.returnCode
            lifecycleScope.launch(Dispatchers.Main) {
                if (returnCode.isValueSuccess) {
                    //after successful execution of ffmpeg command,
                    //again set up the video Uri in VideoView
                    Log.e("filePathTTT", "filePath " + filePath)
                    isAppliedFilter = true
                    updatedVideoFilePath = filePath
                    binding.videoView.setVideoPath(filePath)
                    binding.videoView.start()
                    Toast.makeText(this@MainActivity, "  Replace audio Applied", Toast.LENGTH_SHORT)
                        .show()

                    progressDialog?.dismiss()

                } else {
                    progressDialog?.dismiss()
                    Log.d("TAG", session.allLogsAsString)
                    Toast.makeText(this@MainActivity, "Something Went Wrong4", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, { log ->
            lifecycleScope.launch(Dispatchers.Main) {
                progressDialog?.setMessage("Applying Filter..${log.message}")
            }
        }) { statistics -> Log.d("STATS", statistics.toString()) }
    }


}