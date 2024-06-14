package com.bitapps.snake

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity(), GameListener {

    private lateinit var gameView: GameView
    private lateinit var info: TextView
    private lateinit var moreButton: Button
    private lateinit var buttonLeft: Button
    private lateinit var buttonUp: Button
    private lateinit var buttonRight: Button
    private lateinit var buttonDown: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        info = findViewById(R.id.info)
        moreButton = findViewById(R.id.moreButton)
        buttonLeft = findViewById(R.id.buttonLeft)
        buttonUp = findViewById(R.id.buttonUp)
        buttonRight = findViewById(R.id.buttonRight)
        buttonDown = findViewById(R.id.buttonDown)

        moreButton.setOnClickListener { button ->
            gameView.startGame()
            info.visibility = View.GONE
            button.visibility = View.GONE
        }

        buttonLeft.setOnClickListener { deliverEvent(KeyEvent.KEYCODE_DPAD_LEFT) }
        buttonUp.setOnClickListener { deliverEvent(KeyEvent.KEYCODE_DPAD_UP) }
        buttonRight.setOnClickListener { deliverEvent(KeyEvent.KEYCODE_DPAD_RIGHT) }
        buttonDown.setOnClickListener { deliverEvent(KeyEvent.KEYCODE_DPAD_DOWN) }

        gameView.listener = this
        gameView.startGame()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        deliverEvent(keyCode)
        return super.onKeyDown(keyCode, event)
    }

    private fun deliverEvent(keyCode: Int) {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> gameView.direction = KeyEvent.KEYCODE_DPAD_LEFT
            KeyEvent.KEYCODE_DPAD_UP -> gameView.direction = KeyEvent.KEYCODE_DPAD_UP
            KeyEvent.KEYCODE_DPAD_RIGHT -> gameView.direction = KeyEvent.KEYCODE_DPAD_RIGHT
            KeyEvent.KEYCODE_DPAD_DOWN -> gameView.direction = KeyEvent.KEYCODE_DPAD_DOWN
        }
    }

    override fun onFail() {
        info.visibility = View.VISIBLE
        moreButton.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.finish()
    }
}