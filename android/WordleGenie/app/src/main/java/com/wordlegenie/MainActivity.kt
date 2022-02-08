package com.wordlegenie

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.gson.Gson

const val WORD_LENGTH = 5
const val MAX_TRIES = 6
const val RESULT_FILE = "bestresult.json"
const val LOG_TAG = "Anant"

class MainActivity : AppCompatActivity() {
    var attemptRows: Array<AttemptRow>? = null
    private var rootMove: Move? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        this.rootMove = populateMovesFromJson()
        Log.d(LOG_TAG, rootMove!!.word)

        populateRows(mainLayout)
        createResetButton(mainLayout)
    }

    private fun createResetButton(mainLayout: ConstraintLayout) {
        val resetButton = Button(this)
        resetButton.id = View.generateViewId()
        resetButton.setText(R.string.button_reset)
        resetButton.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        resetButton.setOnClickListener{onResetClick(mainLayout)}
        resetButton.setBackgroundColor(Color.CYAN)
        mainLayout.addView(resetButton)

        val mainConstraints = ConstraintSet()
        mainConstraints.clone(mainLayout)
        mainConstraints.connect(resetButton.id, ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 32)
        mainConstraints.connect(resetButton.id, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START)
        mainConstraints.connect(resetButton.id, ConstraintSet.END,
            ConstraintSet.PARENT_ID, ConstraintSet.END)

        mainConstraints.applyTo(mainLayout)

    }

    private fun populateRows(mainLayout: ConstraintLayout) {
        this.attemptRows =
            Array(MAX_TRIES) { x -> AttemptRow(this, x, rootMove!!) }

        val topTextView = findViewById<TextView>(R.id.textView)
        var topView: View = topTextView
        for (attemptRow in attemptRows!!) {
            mainLayout.addView(attemptRow)
            attemptRow.addConstraints(mainLayout, topView)
            topView = attemptRow
        }
        attemptRows!![0].visibility = View.VISIBLE
    }

    private fun populateMovesFromJson(): Move? {
        val jsonString = application.assets.open(RESULT_FILE)
            .bufferedReader().use { it.readText() }
        val gson = Gson()
        return gson.fromJson(jsonString, Move::class.java)
    }

    private fun onResetClick(mainLayout: ConstraintLayout) {
        for (attemptRow in attemptRows!!) {
            mainLayout.removeView(attemptRow)
        }
        populateRows(mainLayout)
    }
}