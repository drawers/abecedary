// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import io.github.drawers.abecedary.lint.Stubs.ALPHABETICAL
import org.junit.Test

class EnumEntryOrderDetectorTest {
    @Test
    fun inOrder() {
        lint()
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        enum class Fruit {
                            APPLE,
                            BANANA,
                        }
                    """,
                ),
            )
            .issues(EnumEntryOrderDetector.ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun simpleOutOfOrder() {
        lint()
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        enum class Fruit {
                            BANANA,
                            APPLE,
                        }
                    """,
                ),
            )
            .issues(EnumEntryOrderDetector.ISSUE)
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that APPLE is before BANANA")
    }

    @Test
    fun annotationOnSuperInterface() {
        lint()
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        @Alphabetical
                        interface Edible

                        interface Delicious: Edible

                        enum class Fruit: Delicious {
                            BANANA,
                            APPLE,
                        }
                    """,
                ),
            )
            .issues(EnumEntryOrderDetector.ISSUE)
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("Rearrange so that APPLE is before BANANA")
    }

    @Test
    fun noAnnotation() {
        lint()
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        enum class Fruit {
                            BANANA,
                            APPLE,
                        }
                    """,
                ),
            )
            .issues(EnumEntryOrderDetector.ISSUE)
            .allowMissingSdk()
            .run()
            .expectClean()
    }
}
