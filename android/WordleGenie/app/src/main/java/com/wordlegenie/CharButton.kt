package com.wordlegenie

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.appcompat.widget.AppCompatButton

class CharButton(context: Context, char:Char): AppCompatButton(context) {
    var state: Int
    private val stateToColor = arrayOf(Color.BLACK, Color.YELLOW, Color.GREEN)
    private val stateToTextColor = arrayOf(Color.WHITE, Color.BLACK, Color.BLACK)

    constructor(context: Context): this(context, ' ')

    init {
        this.text = char.toString()
        this.state = 0
        this.setBackgroundColor(stateToColor[state])
        this.setTextColor(stateToTextColor[state])
        this.setOnClickListener { onCharClick() }
    }

    private fun onCharClick() {
        this.state = (this.state+1)%stateToColor.size
        this.setBackgroundColor(stateToColor[state])
        this.setTextColor(stateToTextColor[state])
        Log.d(LOG_TAG, "state set to "+this.state)
    }
}