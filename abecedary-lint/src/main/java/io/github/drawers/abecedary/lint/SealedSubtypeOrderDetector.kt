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
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import java.util.EnumSet

class SealedSubtypeOrderDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                if (!context.evaluator.isSealed(node)) {
                    return
                }

                val qualifiedName = node.qualifiedName ?: return

                if (!node.hasAlphabeticalAnnotation()) return

                val classDeclarations = node.uastDeclarations
                    .filterIsInstance<UClass>()
                    .filter {
                        context.evaluator.extendsClass(it, qualifiedName)
                    }

                val zipped = classDeclarations.sortedBy { it.name }
                    .zip(
                        classDeclarations,
                    ) { sorted, unsorted ->
                        Entry(expected = sorted, actual = unsorted)
                    }

                val outOfOrder = zipped.firstOrNull {
                    it.expected.name != it.actual.name
                } ?: return

                context.report(
                    issue = ISSUE,
                    location = context.getLocation(node as UElement),
                    message = "`${node.name}` should declare its sealed subtypes in alphabetical order. " +
                        "Rearrange so that ${outOfOrder.expected.name} is before ${outOfOrder.actual.name}",
                )
            }
        }
    }

    private data class Entry(
        val expected: UClass,
        val actual: UClass,
    ) {
        override fun toString(): String {
            return "Entry(expected=${expected.name}, actual=${actual.name})"
        }
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "SealedSubtypeOrder",
            briefDescription = "Sealed subtype order",
            explanation = "Keeping sealed subtype declarations in alphabetical order, where appropriate, " + "enables quick scanning of a file containing sealed types and prevents " + "merge conflicts.",
            implementation = Implementation(
                SealedSubtypeOrderDetector::class.java,
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
