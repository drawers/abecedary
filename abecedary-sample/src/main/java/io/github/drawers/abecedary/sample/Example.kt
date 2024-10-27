// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.sample

import io.github.drawers.abecedary.Alphabetical

@Alphabetical
interface Identifiable {
    val id: Int
}

@Alphabetical
interface Edible

enum class Fruit(override val id: Int) : Identifiable {
    APPLE(2),
    BANANA(3),
    CHERRY(1),
}

sealed class Vegetable : Edible {
    data object Carrot : Vegetable()

    data object Daikon : Vegetable()
}

val letters = @Alphabetical listOf("a", "b", "c")
val myMeal = @Alphabetical Meal.tastyListOf("beetroot", "carrot")

val portion = @Alphabetical Portion().tastyListOf("a", "b", "c")

fun printLetters() {
    @Alphabetical listOf("a", "b", "c").forEach {
        println(it)
    }
}

object Meal {
    @Suppress("UNUSED_PARAMETER")
    fun tastyListOf(vararg s: String) = listOf<String>()
}

class Portion {
    @Suppress("UNUSED_PARAMETER")
    fun tastyListOf(vararg s: String) = listOf<String>()
}
