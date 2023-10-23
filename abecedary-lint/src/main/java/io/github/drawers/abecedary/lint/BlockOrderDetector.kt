// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.isUastChildOf
import java.util.EnumSet

class BlockOrderDetector : Detector(), SourceCodeScanner {
    /**
     * Note that we cannot currently use the [Detector.applicableAnnotations] API
     * for detecting annotations applied to call expressions
     */
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(
            UCallExpression::class.java,
            UBlockExpression::class.java,
        )

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            private val annotatedCallExpressions = mutableSetOf<UCallExpression>()

            override fun visitBlockExpression(node: UBlockExpression) {
                if (annotatedCallExpressions.none {
                        node.isUastChildOf(it)
                    }
                ) {
                    return
                }

                val source = node.sourcePsi?.text?.lineSequence()?.map { it.trimIndent() } ?: return
                val sortedLines = source.sorted()

                sortedLines.zip(source) { sorted, unsorted ->
                    Entry(
                        expected = sorted,
                        actual = unsorted,
                    )
                }.find {
                    it.expected != it.actual
                } ?: return

                context.report(
                    issue = ISSUE,
                    location = context.getLocation(node),
                    message = "Block is out of alphabetical order",
                )
            }

            override fun visitCallExpression(node: UCallExpression) {
                node.findAnnotation(Annotation.FULLY_QUALIFIED_NAME) ?: return
                annotatedCallExpressions.add(node)
            }
        }
    }

    private data class Entry(
        val expected: String,
        val actual: String,
    )

    companion object {
        @JvmField
        val ISSUE =
            Issue.create(
                id = "BlockOrder",
                briefDescription = "Block order",
                explanation =
                    "Looks for blocks annotated with `@Alphabetical` and reports when the trimmed " +
                        "lines in the block are not in alphabetical order.",
                implementation =
                    Implementation(
                        BlockOrderDetector::class.java,
                        EnumSet.of(
                            Scope.JAVA_FILE,
                            Scope.TEST_SOURCES,
                        ),
                        EnumSet.of(Scope.JAVA_FILE),
                        EnumSet.of(Scope.TEST_SOURCES),
                    ),
                category = Category.PRODUCTIVITY,
                priority = 5,
                severity = Severity.ERROR,
            )
    }
}
