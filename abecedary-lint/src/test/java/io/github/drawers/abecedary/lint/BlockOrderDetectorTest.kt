// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import io.github.drawers.abecedary.lint.Stubs.ALPHABETICAL
import org.junit.Test

class BlockOrderDetectorTest {
    @Test
    fun inOrder() {
        lint()
            .issues(BlockOrderDetector.ISSUE)
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        fun main() {
                            @Alphabetical
                            run {
                                println("a")
                                println("b")
                            }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun outOfOrder() {
        lint()
            .issues(BlockOrderDetector.ISSUE)
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        fun main() {
                            @Alphabetical
                            run {
                                println("c")
                                println("b")
                                println("a")
                            }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("""println("c"""")
    }

    @Test
    fun with() {
        lint()
            .issues(BlockOrderDetector.ISSUE)
            .testModes(TestMode.DEFAULT)
            .files(
                ALPHABETICAL,
                kotlin(
                    """
                        import io.github.drawers.abecedary.Alphabetical

                        fun main() {
                            @Alphabetical
                            with(Unit) {
                                println("b")
                                println("a")
                            }
                        }
                    """,
                ),
            )
            .allowMissingSdk()
            .run()
            .expectErrorCount(1)
            .expectContains("""println("b"""")
    }
}
