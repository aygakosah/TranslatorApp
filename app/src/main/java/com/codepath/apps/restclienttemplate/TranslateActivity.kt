package com.codepath.apps.restclienttemplate

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.*
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import okhttp3.Headers
import java.io.IOException

class TranslateActivity : AppCompatActivity() {
    private var translate:
            Translate? = null
    lateinit var client:TwitterClient
    lateinit var inputToTranslate: EditText
    private var translatedTv: TextView? = null
    val lang = mapOf("English" to "en", "French" to "fr", "German" to "de", "Hindu" to "hn")
    lateinit var selectedlang : String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)

        inputToTranslate = findViewById(R.id.inputToTranslate)
        translatedTv = findViewById(R.id.translatedTv)
        val translateButton: Button = findViewById(R.id.translateButton)
        val postTweetButton : Button = findViewById(R.id.postTweet)

        // get reference to the string array that we just created
        val languages = resources.getStringArray(R.array.programming_languages)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_menu, languages)
        // get reference to the autocomplete text view
        val autocompleteTV = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)
        Log.i(TAG, autocompleteTV.text.toString())

        autocompleteTV.setOnItemClickListener { _, _, position, _ ->
            // You can get the label or item that the user clicked:
            val value = arrayAdapter.getItem(position) ?: ""
            selectedlang = lang[value].toString()
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }


        translateButton.setOnClickListener {
            if (checkInternetConnection()) {

                //If there is internet connection, get translate service and start translation:
                getTranslateService()
                translate()

            } else {

                //If not, display "no connection" warning:
                translatedTv!!.text = resources.getString(R.string.no_connection)
            }
        }

        postTweetButton.setOnClickListener {
            val tweetcontent  = translatedTv?.text.toString()
            client = TwitterApplication.getRestClient(this)
            client.postTweet(tweetcontent, object: JsonHttpResponseHandler(){
                override fun onFailure(
                    statusCode: Int,
                    headers: Headers?,
                    response: String?,
                    throwable: Throwable?
                ) {
                    Log.i(TAG, "Failed $response")
                }

                override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON?) {
                    Log.i(TAG, "Succeeded")
                    val value ="Tweet is posted"
                    Toast.makeText(this@TranslateActivity, value, Toast.LENGTH_LONG).show();
                }

            })
        }
    }

    private fun getTranslateService() {

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            System.setProperty("GOOGLE_API_KEY", "AIzaSyAwGAWP_qqrIvfmgOxDCWzoHMZz7uPu7fs    ")
//            val translateOptions = TranslateOptions.newBuilder().setApiKey("").setCredentials(
//                NoCredentials.getInstance()).build()
            translate = TranslateOptions.getDefaultInstance().service

        } catch (ioe: IOException) {
            ioe.printStackTrace()

        }

    }

    private fun translate() {

        //Get input text to be translated:
        val originalText: String = inputToTranslate!!.text.toString()
        Log.i(TAG, selectedlang)
        val translation = translate!!.translate(originalText, Translate.TranslateOption.targetLanguage(selectedlang), Translate.TranslateOption.model("base"))
        Log.i(TAG, "Code reached here")
        //Translated text and original text are set to TextViews:
        translatedTv!!.text = translation.translatedText

    }

    private fun checkInternetConnection():Boolean{
        val connectivityManager=this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=connectivityManager.activeNetworkInfo
        return  networkInfo!=null && networkInfo.isConnected
    }

    companion object {
        val TAG = "MainActivity"
    }
}