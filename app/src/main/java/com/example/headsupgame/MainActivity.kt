package com.example.headsupgame

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Surface
import androidx.core.view.isVisible
import com.example.headsupgame.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    lateinit var celebrities: ArrayList<JSONObject>

    private var celebrity = 0

    private var playing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        celebrities = arrayListOf()

        binding.playBtn.setOnClickListener { requestAPI() }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val rotation = windowManager.defaultDisplay.rotation
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { // That means the phone in Portrait
            if(playing) {
                celebrity++
                displayCelebrity(celebrity)
                showUI(false)
            } else {
                showUI(false)
            }
        } else {
            if (playing) {
                showUI(true)
            } else {
                showUI(false)
            }

        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayTimer() {
        if (!playing) {
            playing = true
            binding.introTV.text = "Rotate the device to display a celebrity "
            binding.playBtn.isVisible = false
            val rotation = windowManager.defaultDisplay.rotation
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
                showUI(false)
            } else {
                showUI(true)
            }

            object : CountDownTimer(60000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    binding.timerTV.text = "Time: ${millisUntilFinished/1000}s"
                }

                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    playing = false
                    binding.timerTV.text = "Time: 00s"
                    binding.introTV.text = "Heads Up!"
                    binding.playBtn.isVisible = true
                    showUI(false)
                }
            }.start()
        }
    }

    private  fun displayCelebrity(id: Int) {
        if (id < celebrities.size){
            binding.celebrityName.text = celebrities[id].getString("name")
            binding.taboo1.text = celebrities[id].getString("taboo1")
            binding.taboo2.text = celebrities[id].getString("taboo2")
            binding.taboo3.text = celebrities[id].getString("taboo3")
        }
    }
    
    private fun requestAPI() {
        //Note: Coroutine is used instead of Retrofit because it's a simple API request and Retrofit isn't needed in this case
        CoroutineScope(IO).launch {
            val data = async {
                fetchCelebrities()
            }.await()
            if (data.isNotEmpty()){
                withContext(Main){
                    parseCeleb(data)
                    celebrities.shuffle()
                    displayCelebrity(0)
                    displayTimer()
                }
            }
        }
    }

    private suspend fun parseCeleb(result: String) {
        withContext(Main) {
            celebrities.clear()
            val jsonArray = JSONArray(result)
            for(cele in 0 until jsonArray.length() ) {
                celebrities.add(jsonArray.getJSONObject(cele))
            }
        }
    }

    private fun fetchCelebrities(): String{
        var response = ""
        try {
            response = URL("https://dojo-recipes.herokuapp.com/celebrities/")
                .readText(Charsets.UTF_8)
        }catch (exce: Exception){
            println("ERROR: $exce")
        }
        return response
    }

    private fun showUI(showCele: Boolean) {
        if (showCele){
            binding.celebrityLL.isVisible = true
            binding.introLL.isVisible = false
        } else {
            binding.celebrityLL.isVisible = false
            binding.introLL.isVisible = true
        }
    }
}
