package com.websitebeaver.documentscanner

import com.websitebeaver.documentscanner.constants.ResponseType
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit test to make sure ResponseType constants are the right value */
class ResponseTypeUnitTest {
  @Test
  fun constantValue_isCorrect() {
    assertEquals(ResponseType.BASE64, "base64")
  }
}
