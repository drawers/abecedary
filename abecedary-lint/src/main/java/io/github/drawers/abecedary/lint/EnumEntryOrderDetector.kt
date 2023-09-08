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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.util.childrenOfType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import java.util.EnumSet

class EnumEntryOrderDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(
        UClass::class.java,
    )

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {

            override fun visitClass(node: UClass) {
                if (!node.isEnum) {
                    return
                }

                if (!node.hasAlphabeticalAnnotation()) return

                val entries = node.kotlinEnumEntries() ?: node.javaEnumEntries()
                if (entries.isEmpty()) return

                val zipped = entries.sorted()
                    .zip(entries) { sorted, unsorted ->
                        Order(
                            expected = sorted,
                            actual = unsorted,
                        )
                    }

                val outOfOrder = zipped.firstOrNull {
                    it.expected.name != it.actual.name
                } ?: return

                context.report(
                    issue = ISSUE,
                    location = context.getLocation(node as UElement),
                    message = "`${node.name}` should declare its entries in alphabetical order. " +
                        "Rearrange so that ${outOfOrder.expected.name} is before ${outOfOrder.actual.name}.",
                    quickfixData = null,
                )
            }

            /**
             * Returns the list of Kotlin enum entries or `null` if
             * this is not applicable (e.g., because we are inspecting
             * a Java class).
             */
            private fun UClass.kotlinEnumEntries(): List<Entry>? {
                val ktClass = sourcePsi as? KtClass ?: return null
                return ktClass.body
                    ?.enumEntries
                    ?.map {
                        Entry(
                            node = it,
                            name = it.name.orEmpty(),
                        )
                    }
            }

            private fun UClass.javaEnumEntries(): List<Entry> {
                return javaPsi.childrenOfType<PsiEnumConstant>()
                    .map {
                        Entry(
                            node = it,
                            name = it.name,
                        )
                    }
            }
        }
    }

    private data class Entry(
        val node: PsiElement,
        val name: String,
    ) : Comparable<Entry> {

        override fun compareTo(other: Entry): Int {
            return name.compareTo(other.name)
        }
    }

    private data class Order(
        val expected: Entry,
        val actual: Entry,
    )

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "EnumEntryOrder",
            briefDescription = "Enum entry order",
            explanation = "Keeping enum entries in alphabetical order, where appropriate, " +
                "enables quick scanning of a file containing enums and prevents " +
                "merge conflicts.",
            implementation = Implementation(
                EnumEntryOrderDetector::class.java,
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
