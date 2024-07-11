// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.java
import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import io.github.drawers.abecedary.lint.Stubs.ALPHABETICAL
import org.junit.Test

class VarargOrderDetectorTest {
    @Test
    fun inOrder() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                // TestMode.PARENTHESIZED introduces a syntax error, let's skip it
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical listOf(
                            "apple",
                            "banana",
                        )
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun strings() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical listOf(
                            "banana",
                            "apple",
                        )
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"apple\" is before \"banana\"")
    }

    @Test
    fun enums() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        enum class Fruit {
                            APPLE,
                            BANANA,;
                        }

                        val FRUITS = @Alphabetical listOf(
                            Fruit.BANANA,
                            Fruit.APPLE,
                        )
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that Fruit.APPLE is before Fruit.BANANA")
    }

    @Test
    fun javaFile() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                java(
                    """
                        package com.example;

                        class FruitList {

                            public static List<String> getFruits(String...fruits) {
                                return Arrays.asList(fruits);
                            }
                        }
                    """,
                ),
                kotlin(
                    """
                        import com.example.FruitList.getFruits
                        import io.github.drawers.abecedary.Alphabetical

                        val fruits = @Alphabetical getFruits(
                            "banana",
                            "apple",
                        )
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"apple\" is before \"banana\"")
    }

    @Test
    fun inlineFunction() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical tastyListOf(
                            "banana",
                            "apple",
                        )

                        private inline fun tastyListOf(vararg fruit: String): List<String> {
                            return listOf(*fruit).filterNot { it == "durian" }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"apple\" is before \"banana\"")
    }

    @Test
    fun noAnnotation() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        val FRUITS = listOf(
                            "banana",
                            "apple",
                        )
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun userFunction() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical tastyListOf(
                            "banana",
                            "apple",
                        )

                        private fun tastyListOf(vararg fruit: String): List<String> {
                            return listOf(*fruit).filterNot { it == "durian" }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun functionInObject() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical Meal.tastyListOf(
                            "banana",
                            "apple",
                        )

                        object Meal {
                            fun tastyListOf(vararg fruit: String): List<String> {
                                return listOf(*fruit).filterNot { it == "durian" }
                            }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun functionInClass() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical Meal().tastyListOf(
                            "banana",
                            "apple",
                        )

                        class Meal {
                            fun tastyListOf(vararg fruit: String): List<String> {
                                return listOf(*fruit).filterNot { it == "durian" }
                            }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
    }

    @Test
    fun chain() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        fun x() {
                            @Alphabetical listOf("c", "b", "a").forEach {
                                listOf("f", "e", "d").forEach {
                                    letter -> println(letter)
                                }
                             }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"a\" is before \"c\"")
    }

    @Test
    fun chainChoosesFirstVarargParameter() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        class SpecialList(
                            vararg element: String
                        ) {
                            fun addAll(vararg element: String)
                        }

                        fun x() {
                            // should target the constructor because its the first
                            // call expression where the method has a single param
                            // marked vararg
                            @Alphabetical SpecialList("c", "b", "a").addAll("f", "e", "d")
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"a\" is before \"c\"")
    }

    @Test
    fun chainWithVarargAndNonVararg() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        class SpecialList() {
                            fun doNothing(): SpecialList
                            fun addAll(vararg element: String)
                        }

                        fun x() {
                            @Alphabetical SpecialList().doNothing().addAll("c", "b", "a")
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that \"a\" is before \"c\"")
    }

    @Test
    fun ignoresMoreThanOneParam() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical tastyListOf(
                            yuckyFruit = "durian",
                            "banana",
                            "apple",
                        )

                        private fun tastyListOf(yuckyFruit: String, vararg fruit: String): List<String> {
                            return listOf(*fruit).filterNot { it == yuckyFruit }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun zeroParams() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical tastyListOf()

                        private fun tastyListOf(): List<String> {
                            return listOf("banana", "apple")
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun notVararg() {
        lint()
            .issues(VarargOrderDetector.ISSUE)
            .skipTestModes(
                TestMode.PARENTHESIZED,
            )
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        val FRUITS = @Alphabetical tastyListOf(
                            yuckyFruit = "durian",
                            "banana",
                        )

                        private fun tastyListOf(yuckyFruit: String, tastyFruit: String): List<String> {
                            return listOf(*fruit).filterNot { it == yuckyFruit }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }
}
