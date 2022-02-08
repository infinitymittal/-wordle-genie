package com.wordlegenie

data class Move (
    val word: String,
    val wordCount: Int,
    val moveNumber: Int,
    val bucketToMove: Map<Int,Move>,
)