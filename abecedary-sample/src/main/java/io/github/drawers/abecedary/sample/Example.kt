// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("InvalidPackageDeclaration", "MagicNumber", "MatchingDeclarationName")

package io.github.drawers.abecedary.sample

import io.github.drawers.abecedary.Alphabetical

@Alphabetical
interface Identifiable {
    val id: Int
}

@Alphabetical
interface Edible

enum class Fruit(override val id: Int) : Identifiable {
    CHERRY(1),
    APPLE(2),
    BANANA(3),
}

sealed class Vegetable : Edible {
    object Daikon : Vegetable()
    object Carrot : Vegetable()
}

val letters = @Alphabetical listOf("b", "c", "a")
val myMeal = @Alphabetical Meal.tastyListOf("carrot", "beetroot")

val portion = @Alphabetical Portion().tastyListOf("a", "b", "c")

fun printLetters() {
    @Alphabetical listOf("a", "c", "b").forEach {
        println(it)
    }
}

object Meal {
    fun tastyListOf(vararg s: String) = listOf<String>()
}

class Portion {
    fun tastyListOf(vararg s: String) = listOf<String>()
}
