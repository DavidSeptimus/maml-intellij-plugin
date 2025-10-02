package com.davidseptimus.maml

import com.davidseptimus.maml.highlighting.MamlSyntaxHighlighter
import com.davidseptimus.maml.lang.MamlFileType
import com.davidseptimus.maml.lang.MamlLanguage
import com.davidseptimus.maml.lang.psi.MamlFile
import com.davidseptimus.maml.lang.psi.MamlObject
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture

class MamlIntegrationTest : BasePlatformTestCase() {

    // Editor Integration Tests

    fun testOpenMamlFileInEditor() {
        val file = myFixture.configureByText(
            "test.maml",
            """
            {
              name: "Test"
              version: 1.0
            }
            """.trimIndent()
        )

        assertNotNull("File should be opened in editor", file)
        assertTrue("File should be recognized as MAML", file is MamlFile)
    }

    fun testSmallFileLoadsWithoutIssues() {
        val content = """
            {
              key: "value"
              number: 123
              boolean: true
            }
        """.trimIndent()

        val file = myFixture.configureByText("small.maml", content)
        assertNotNull("Small file should load", file)

        val psiFile = file as MamlFile
        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Small file should parse correctly", obj)
    }

    fun testMediumFileLoadsWithoutIssues() {
        val content = buildString {
            appendLine("{")
            for (i in 1..100) {
                appendLine("  key$i: \"value$i\"")
            }
            appendLine("}")
        }

        val file = myFixture.configureByText("medium.maml", content)
        assertNotNull("Medium file should load", file)

        val psiFile = file as MamlFile
        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Medium file should parse correctly", obj)
    }

    fun testLargeFileLoadsWithoutErrors() {
        val content = buildString {
            appendLine("{")
            appendLine("  data: [")
            for (i in 1..1000) {
                appendLine("    { id: $i, name: \"item$i\" }${if (i < 1000) "," else ""}")
            }
            appendLine("  ]")
            appendLine("}")
        }

        val file = myFixture.configureByText("large.maml", content)
        assertNotNull("Large file should load", file)

        val psiFile = file as MamlFile
        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Large file should parse correctly", obj)
    }

    // PSI Integration Tests

    fun testPsiFileCreatedFromVirtualFile() {
        val virtualFile = myFixture.createFile(
            "test.maml",
            """{ key: "value" }"""
        )

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        assertNotNull("PSI file should be created from virtual file", psiFile)
        assertTrue("PSI file should be MamlFile", psiFile is MamlFile)
    }

    fun testPsiTreeIsValid() {
        val file = myFixture.configureByText(
            "test.maml",
            """
            {
              name: "Test"
              items: [1, 2, 3]
            }
            """.trimIndent()
        )

        val psiFile = file as MamlFile
        assertTrue("PSI tree should be valid", psiFile.isValid)

        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Should find object in PSI tree", obj)
        assertTrue("Object should be valid", obj!!.isValid)
    }

    // Multiple File Tests

    fun testMultipleMamlFilesInProject() {
        val file1 = myFixture.configureByText("file1.maml", """{ a: 1 }""")
        val file2 = myFixture.configureByText("file2.maml", """{ b: 2 }""")
        val file3 = myFixture.configureByText("file3.maml", """{ c: 3 }""")

        assertTrue("File 1 should be MAML", file1 is MamlFile)
        assertTrue("File 2 should be MAML", file2 is MamlFile)
        assertTrue("File 3 should be MAML", file3 is MamlFile)

        assertNotSame("Files should be different instances", file1, file2)
        assertNotSame("Files should be different instances", file2, file3)
    }

    fun testSwitchingBetweenMamlFiles() {
        val file1 = myFixture.addFileToProject("file1.maml", """{ a: 1 }""")
        val file2 = myFixture.addFileToProject("file2.maml", """{ b: 2 }""")

        // Open first file
        myFixture.openFileInEditor(file1.virtualFile)
        var currentFile = myFixture.file
        assertTrue("Current file should be file1", currentFile is MamlFile)
        assertEquals("Current file name should be file1.maml", "file1.maml", currentFile.name)

        // Switch to second file
        myFixture.openFileInEditor(file2.virtualFile)
        currentFile = myFixture.file
        assertTrue("Current file should be file2", currentFile is MamlFile)
        assertEquals("Current file name should be file2.maml", "file2.maml", currentFile.name)
    }

    // Editing Tests

    fun testFileRemainsValidAfterEdit() {
        myFixture.configureByText("test.maml", """{ key: "value" }""")

        myFixture.editor.caretModel.moveToOffset(15)
        myFixture.type("123")

        val psiFile = myFixture.file as MamlFile
        assertTrue("File should remain valid after edit", psiFile.isValid)

        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Should still find object after edit", obj)
    }

    // Virtual File Tests

    fun testVirtualFileIsValid() {
        val virtualFile = myFixture.createFile("test.maml", """{ key: "value" }""")

        assertTrue("Virtual file should be valid", virtualFile.isValid)
        assertEquals("Virtual file extension should be 'maml'", "maml", virtualFile.extension)
        assertEquals("Virtual file name should be 'test.maml'", "test.maml", virtualFile.name)
    }

    fun testVirtualFileContents() {
        val content = """{ key: "value" }"""
        val virtualFile = myFixture.createFile("test.maml", content)

        val fileContent = String(virtualFile.contentsToByteArray())
        assertEquals("Virtual file contents should match", content, fileContent)
    }

    // Error Handling Tests

    fun testInvalidSyntaxDoesNotCrashEditor() {
        val file = myFixture.configureByText(
            "test.maml",
            """{ key: "value" """ // Unclosed string and object
        )

        assertNotNull("File should still load with errors", file)
        assertTrue("File should be MAML file", file is MamlFile)
    }

    fun testMalformedFileStillCreatesValidPsiFile() {
        val file = myFixture.configureByText(
            "test.maml",
            """
            { key: value
              broken: [1, 2,
            """.trimIndent()
        )

        val psiFile = file as MamlFile
        assertTrue("PSI file should be valid despite errors", psiFile.isValid)
    }

    fun testEmptyFileIsValid() {
        val file = myFixture.configureByText("empty.maml", "")

        val psiFile = file as MamlFile
        assertTrue("Empty file should be valid", psiFile.isValid)
    }

    fun testFileWithOnlyWhitespace() {
        val file = myFixture.configureByText("whitespace.maml", "   \n\t\n   ")

        val psiFile = file as MamlFile
        assertTrue("File with only whitespace should be valid", psiFile.isValid)
    }

    fun testFileWithOnlyComments() {
        val file = myFixture.configureByText(
            "comments.maml",
            """
            # Just a comment
            # Another comment
            """.trimIndent()
        )

        val psiFile = file as MamlFile
        assertTrue("File with only comments should be valid", psiFile.isValid)
    }

    // Complex Document Tests

    fun testComplexRealWorldDocument() {
        val file = myFixture.configureByText(
            "complex.maml",
            """
            {
              # Application configuration
              app: {
                name: "My Application"
                version: 2.1.0
                debug: false
              }

              # Database settings
              database: {
                host: "localhost"
                port: 5432
                credentials: {
                  username: "admin"
                  password: "secret"
                }
              }

              # Feature flags
              features: [
                "feature-a"
                "feature-b"
                "feature-c"
              ]

              # Nested configuration
              services: {
                api: {
                  enabled: true
                  endpoints: [
                    { path: "/users", method: "GET" }
                    { path: "/posts", method: "POST" }
                  ]
                }
              }
            }
            """.trimIndent()
        )

        val psiFile = file as MamlFile
        assertTrue("Complex document should be valid", psiFile.isValid)

        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Should parse complex document", obj)
    }

    fun testDocumentWithAllFeatures() {
        val file = myFixture.configureByText(
            "features.maml",
            """
            {
              # Comments
              unquoted-key: "value"
              "quoted-key": 123

              # Different value types
              string: "hello"
              multiline: ""${'"'}
                Multi
                line
                string
              ""${'"'}
              number: 42
              decimal: 3.14
              scientific: 1.5e-3
              boolean_true: true
              boolean_false: false
              null_value: null

              # Nested structures
              object: {
                nested: "value"
              }

              array: [1, 2, 3]

              # Mixed separators
              a: 1, b: 2
              c: 3
              d: 4
            }
            """.trimIndent()
        )

        val psiFile = file as MamlFile
        assertTrue("Document with all features should be valid", psiFile.isValid)

        val obj = PsiTreeUtil.findChildOfType(psiFile, MamlObject::class.java)
        assertNotNull("Should parse document with all features", obj)
    }

    fun testMultipleEditsToSameFile() {
        myFixture.configureByText("test.maml", """{ key: "value" }""")

        // First edit
        myFixture.editor.caretModel.moveToOffset(7)
        myFixture.type("1")

        // Second edit
        myFixture.editor.caretModel.moveToOffset(8)
        myFixture.type("2")

        // Third edit
        myFixture.editor.caretModel.moveToOffset(9)
        myFixture.type("3")

        val psiFile = myFixture.file as MamlFile
        assertTrue("File should remain valid after multiple edits", psiFile.isValid)
    }
}