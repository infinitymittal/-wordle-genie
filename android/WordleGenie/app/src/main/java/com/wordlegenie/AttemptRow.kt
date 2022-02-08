package com.wordlegenie

import android.text.Html
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.math.roundToInt

class AttemptRow(
    private var mainActivity: MainActivity,
    private var rowNumber: Int,
    private var move: Move
): ConstraintLayout(mainActivity) {
    private var charButtons: Array<CharButton>
    private var goButton: Button

    init {
        this.id = View.generateViewId()
        this.layoutParams = LayoutParams(dpToPx(0), dpToPx(50))
        this.visibility = View.INVISIBLE

        this.charButtons = Array(WORD_LENGTH) { CharButton(mainActivity,' ') }
        for (i in charButtons.indices) {
            this.charButtons[i].id = View.generateViewId()
            this.charButtons[i].text = move.word[i].toString()
            this.charButtons[i].layoutParams =
                LayoutParams(dpToPx(50), dpToPx(50))
            this.addView(this.charButtons[i])
        }

        this.goButton = Button(mainActivity)
        this.goButton.id = View.generateViewId()
        this.goButton.setText(R.string.button_go)
        this.goButton.layoutParams = LayoutParams(dpToPx(100), dpToPx(50))
        this.goButton.setOnClickListener{ onGoClick() }
        this.addView(goButton)
    }

    private fun onGoClick() {
        if(this.rowNumber+1>=mainActivity.attemptRows!!.size)
            return
        val bucket = calculateBucket()
        if(bucket !in move.bucketToMove) {
            Toast.makeText(context,
                Html.fromHtml("<font color='#ff0000' ><b>" + "No possible word." + "</b></font>"),
                Toast.LENGTH_SHORT).show()
            return
        }
        val nextMove: Move = move.bucketToMove.getValue(bucket)
        val nextRow = mainActivity.attemptRows!![this.rowNumber + 1]
        nextRow.setWord(nextMove.word)
        Log.d(LOG_TAG, bucket.toString())
        Log.d(LOG_TAG, nextMove.word)
        nextRow.move = nextMove
        nextRow.visibility = View.VISIBLE
    }

    private fun calculateBucket(): Int {
        var bucket = 0
        for(charButton in this.charButtons) {
            bucket = bucket*3 + charButton.state
        }
        return bucket
    }

    private fun setWord(word:String) {
        for(i in 0 until WORD_LENGTH)
            this.charButtons[i].text = word[i].toString()
    }

    private fun dpToPx(value:Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(), resources.displayMetrics).roundToInt()
    }

    fun addConstraints(mainLayout: ConstraintLayout, prevView: View) {
        val mainConstraints = ConstraintSet()
        mainConstraints.clone(mainLayout)
        mainConstraints.connect(this.id, ConstraintSet.TOP,
            prevView.id, ConstraintSet.BOTTOM, 32)
        mainConstraints.connect(this.id, ConstraintSet.START,
            ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
        mainConstraints.connect(this.id, ConstraintSet.END,
            ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
        mainConstraints.setHorizontalBias(this.id, 0.0F)

        mainConstraints.applyTo(mainLayout)

        val rowConstraints = ConstraintSet()
        rowConstraints.clone(this)

        var leftView: View = this
        var leftLocation = ConstraintSet.START
        for(button in charButtons) {
            rowConstraints.connect(button.id, ConstraintSet.START,
            leftView.id, leftLocation, 4)
            leftView = button
            leftLocation = ConstraintSet.END
        }

        rowConstraints.connect(this.goButton.id, ConstraintSet.END,
            this.id, ConstraintSet.END, 16)

        rowConstraints.applyTo(this)
    }
}