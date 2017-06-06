/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.EditText

import com.carbonylgroup.schoolpower.R
import com.carbonylgroup.schoolpower.classes.Utils.Utils
import com.carbonylgroup.schoolpower.classes.Utils.postData


class LoginActivity : Activity() {

    private var utils: Utils? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.Design)
        super.onCreate(savedInstanceState)
        checkIfLoggedIn()
        setContentView(R.layout.login_content)

        initValue()
    }

    private fun checkIfLoggedIn() {

        val sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(getString(R.string.loggedIn), false))
            startMainActivity()
    }

    private fun initValue() {

        utils = Utils(this)

        val input_username = findViewById(R.id.input_username) as EditText
        val input_password = findViewById(R.id.input_password) as EditText

        findViewById(R.id.login_fab).setOnClickListener {
            saveUserId(input_username.text.toString())
            loginAction(input_username.text.toString(), input_password.text.toString())
        }
    }
    private fun saveUserId(stringId:String) {
        val spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit()
        spEditor.putString(getString(R.string.user_id), stringId)
        spEditor.apply()
    }

    fun loginAction(username: String, password: String) {

        val progressDialog = ProgressDialog(this@LoginActivity)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage(getString(R.string.authenticating))
        progressDialog.show()

        val publicKey = Utils.restorePublicKey(publicKeyString)

        val encryptedArgument = Utils.RSAEncode(publicKey, username + ";" + password)

        Thread(postData(
                getString(R.string.postURL),
                getString(R.string.token_equals) + encryptedArgument + "&filter=",
                object : Handler() {
                    override fun handleMessage(msg: Message) {

                        progressDialog.dismiss()
                        val strMessage = msg.obj.toString()
                        val messages = strMessage.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        if (strMessage.contains(getString(R.string.error_wrong_password)))
                            utils!!.showSnackBar(this@LoginActivity, findViewById(R.id.login_coordinate_layout), getString(R.string.wrong_password), true)
                        else if (strMessage.contains("[]")) {

                            val spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit()
                            spEditor.putString(getString(R.string.token), encryptedArgument)
                            spEditor.putBoolean(getString(R.string.loggedIn), true)
                            spEditor.putString(getString(R.string.student_name), messages[1])
                            spEditor.apply()

                            startMainActivity()

                        } else utils!!.showSnackBar(this@LoginActivity, findViewById(R.id.login_coordinate_layout), getString(R.string.no_connection), true)
                    }
                })).start()

    }

    private fun startMainActivity() {

        startActivity(Intent(application, MainActivity::class.java))
        this@LoginActivity.finish()
    }

    companion object {

        private val publicKeyString = "" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDtILvI2l7yH/9yn/qKmpbqGqSi" +
                "sawP42X1Js4zykHgGdqSyQ4PsKEbEEWh8KLOdaCeBkxMRzqhKS3WLI78oKijNgg7" +
                "6z0/jHJoCrOEJmCeWA2ugcgUOrw5i2siFHo/ogHQhtCf0fa1a+6PUjwOvhhaU4yW" +
                "EXfsfWVY0iJA4qO58wIDAQAB"
    }
}