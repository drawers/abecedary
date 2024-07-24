// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import io.github.drawers.abecedary.lint.Stubs.ALPHABETICAL
import org.junit.Test

class SealedSubtypeOrderDetectorTest {
    @Test
    fun subclassOutOfOrder() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        sealed class Fruit {
                            object Cherry: Fruit()
                            object Banana : Fruit()
                            object Apple : Fruit()
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("it is annotated with @Alphabetical")
            .expectContains("Rearrange so that Apple is before Cherry")
    }

    @Test
    fun interfaceOutOfOrder() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        sealed interface Fruit {
                            object Cherry: Fruit
                            object Banana : Fruit
                            interface Apple : Fruit
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that Apple is before Cherry")
    }

    @Test
    fun otherDeclarations() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        sealed class Fruit {
                            object Acai
                            object Apple : Fruit()
                            val cherry = "Cherry"
                            object Banana : Fruit()
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun nested() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        sealed class Fruit {
                            sealed class Apple: Fruit()  {
                                object RedDelicious: Apple()
                                object GrannySmith: Apple()
                            }
                            object Banana : Fruit()
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that GrannySmith is before RedDelicious")
    }

    @Test
    fun declaredOutside() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        sealed class Fruit

                        object Banana: Fruit()
                        object Apple: Fruit()
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun noAnnotation() {
        lint()
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        sealed class Fruit {

                            object Banana: Fruit()
                            object Apple: Fruit()
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun annotationOnSuperType() {
        lint()
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        interface Edible

                        interface Delicious: Edible

                        sealed class Fruit: Delicious {
                            object Banana: Fruit()
                            object Apple: Fruit()
                        }
                    """,
                ),
            )
            .issues(SealedSubtypeOrderDetector.ISSUE)
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("its super type Edible is annotated with @Alphabetical")
            .expectContains("Rearrange so that Apple is before Banana")
    }
}
