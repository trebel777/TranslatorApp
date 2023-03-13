package ru.netology.translatorapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var sourceLanguage: EditText
    private lateinit var targetLanguage: TextView
    private lateinit var sourceLanguageChooseBtn: MaterialButton
    private lateinit var targetLanguageBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton

    companion object{
        private const val TAG = "MAIN_TAG"
    }

    private var languageArrayList: ArrayList<ModelLanguage>? = null

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "ru"
    private var targetLanguageTitle = "Русский"


    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceLanguage = findViewById(R.id.sourceLanguage)
        targetLanguage = findViewById(R.id.targetLanguage)
        sourceLanguageChooseBtn = findViewById(R.id.sourceLanguageChooseBtn)
        targetLanguageBtn = findViewById(R.id.targetLanguageBtn)
        translateBtn = findViewById(R.id.translateBtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvailableLanguages()

        sourceLanguageChooseBtn.setOnClickListener {
            sourceLanguageChoose()

        }
        targetLanguageBtn.setOnClickListener {
            targetLanguageChoose()

        }
        translateBtn.setOnClickListener {
            validateData()

        }

    }
    private var sourceLanguageText = ""

    private fun validateData() {

        sourceLanguageText = sourceLanguage.text.toString().trim()

        Log.d(TAG, "validateData: sourceLanguageText: $sourceLanguageText")

        if (sourceLanguageText.isEmpty()){
            showToast("Enter text to translate...")
        }else{
            startTranslation()
        }
    }

    private fun startTranslation() {
        progressDialog.setMessage("Processing language model...")
        progressDialog.show()

        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()
        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()


        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                Log.d(TAG, "startTranslation: model ready, start translation...")
                progressDialog.setMessage("Translating...")

                translator.translate(sourceLanguageText)
                    .addOnSuccessListener { translatedText ->
                        Log.d(TAG, "startTranslation: translatedText: $translatedText")
                        progressDialog.dismiss()
                        targetLanguage.text = translatedText

                    }
                    .addOnFailureListener { e ->

                        progressDialog.dismiss()
                        Log.d(TAG, "startTranslation: ", e)
                        showToast(" Failed to translate due to ${e.message}")

                    }
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Log.d(TAG, "startTranslation: ", e)
                showToast(" Failed due to ${e.message}")

            }
    }

    private fun loadAvailableLanguages(){
        languageArrayList = ArrayList()

        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList){

            val languageTitle = Locale(languageCode).displayLanguage

            Log.d(TAG, "loadAvailableLanguage: languageCode: $languageCode")
            Log.d(TAG, "loadAvailableLanguage: languageTitle: $languageTitle")

            val modelLanguage = ModelLanguage(languageCode, languageTitle)

            languageArrayList!!.add(modelLanguage)


        }
    }
    private fun sourceLanguageChoose(){
        val popupMenu = PopupMenu(this, sourceLanguageChooseBtn)
        for (i in languageArrayList!!.indices){
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            sourceLanguageCode = languageArrayList!![position].languageCode
            sourceLanguageTitle = languageArrayList!![position].languageTitle

            sourceLanguageChooseBtn.text = sourceLanguageTitle
            sourceLanguage.hint = "Enter $sourceLanguageTitle"

            Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: $sourceLanguageCode")
            Log.d(TAG, "sourceLanguageChoose: sourceLanguageTitle: $sourceLanguageTitle")

            false
        }
    }
    private fun targetLanguageChoose(){

        val popupMenu = PopupMenu(this, targetLanguageBtn)
        for (i in languageArrayList!!.indices){
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            targetLanguageCode = languageArrayList!![position].languageCode
            targetLanguageTitle = languageArrayList!![position].languageTitle

            targetLanguageBtn.text = targetLanguageTitle


            Log.d(TAG, "sourceLanguageChoose: sourceLanguageCode: $targetLanguageCode")
            Log.d(TAG, "sourceLanguageChoose: sourceLanguageTitle: $targetLanguageTitle")

            false
        }
    }
    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}