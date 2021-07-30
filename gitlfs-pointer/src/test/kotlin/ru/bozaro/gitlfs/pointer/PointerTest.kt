package ru.bozaro.gitlfs.pointer

import com.google.common.collect.ImmutableMap
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import ru.bozaro.gitlfs.pointer.Pointer.createPointer
import ru.bozaro.gitlfs.pointer.Pointer.parsePointer
import ru.bozaro.gitlfs.pointer.Pointer.serializePointer
import java.io.IOException

/**
 * Pointer parser test.
 *
 * @author Artem V. Navrotskiy
 */
class PointerTest {
    @Test(dataProvider = "parseValidProvider")
    @Throws(IOException::class)
    fun parseValid(fileName: String, expected: Map<String, String>) {
        javaClass.getResourceAsStream(fileName).use { stream ->
            Assert.assertEquals(parsePointer(stream!!), expected)
        }
    }

    @Test
    fun parseAndSerialize() {
        val pointer = createPointer("sha256:4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", 12345)
        Assert.assertEquals(pointer["oid"], "sha256:4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393")
        Assert.assertEquals(pointer["size"], "12345")
        Assert.assertEquals(pointer.size, 3)
        val bytes = serializePointer(pointer)
        val parsed: Map<String, String>? = parsePointer(bytes)
        Assert.assertEquals(parsed, pointer)
    }

    @Test(dataProvider = "parseInvalidProvider")
    @Throws(IOException::class)
    fun parseInvalid(fileName: String, description: String) {
        javaClass.getResourceAsStream(fileName).use { stream ->
            Assert.assertNotNull(stream)
            Assert.assertNull(parsePointer(stream!!), description)
        }
    }

    @DataProvider(name = "parseValidProvider")
    fun parseValidProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        "pointer-valid-01.dat",
                        ImmutableMap.builder<Any, Any>()
                                .put("version", "https://git-lfs.github.com/spec/v1")
                                .put("oid", "sha256:4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393")
                                .put("size", "12345")
                                .build()
                ), arrayOf(
                "pointer-valid-02.dat",
                ImmutableMap.builder<Any, Any>()
                        .put("version", "https://git-lfs.github.com/spec/v1")
                        .put("oid", "sha256:4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393")
                        .put("name", "Текст в UTF-8")
                        .put("size", "12345")
                        .build()
        ), arrayOf(
                "pointer-valid-03.dat",
                ImmutableMap.builder<Any, Any>()
                        .put("version", "https://git-lfs.github.com/spec/v1")
                        .put("object-name", " Foo")
                        .put("object.id", "F1")
                        .put("object0123456789", ":)")
                        .put("oid", "sha256:4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393")
                        .put("name", "Текст в UTF-8")
                        .put("size", "12345")
                        .build()
        )
        )
    }

    @DataProvider(name = "parseInvalidProvider")
    fun parseInvalidProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("pointer-invalid-01.dat", "Version is not in first line"),
                arrayOf("pointer-invalid-02.dat", "Two empty lines at end of file"),
                arrayOf("pointer-invalid-03.dat", "Size not found"),
                arrayOf("pointer-invalid-04.dat", "Oid not found"),
                arrayOf("pointer-invalid-05.dat", "Version not found"),
                arrayOf("pointer-invalid-06.dat", "Invalid items order"),
                arrayOf("pointer-invalid-07.dat", "Non utf-8"),
                arrayOf("pointer-invalid-08.dat", "Size is not number"),
                arrayOf("pointer-invalid-09.dat", "Duplicate line"),
                arrayOf("pointer-invalid-10.dat", "Duplicate version")
        )
    }
}
