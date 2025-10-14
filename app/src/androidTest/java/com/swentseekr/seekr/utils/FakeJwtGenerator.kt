package com.swentseekr.seekr.utils

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.util.UUID
import org.json.JSONObject

/** Generates deterministic fake Google ID tokens for testing. */
object FakeJwtGenerator {
  fun createFakeGoogleIdToken(
      sub: String = UUID.randomUUID().toString(),
      name: String = "Test User",
      email: String = "test@example.com"
  ): String {
    val header = JSONObject(mapOf("alg" to "none", "typ" to "JWT")).toString()
    val payload =
        JSONObject(
                mapOf(
                    "sub" to sub,
                    "name" to name,
                    "email" to email,
                    "iss" to "https://accounts.google.com",
                    "aud" to "test-client-id.apps.googleusercontent.com"))
            .toString()

    return listOf(header, payload, "").joinToString(".") {
      Base64.encodeToString(
          it.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
    }
  }
}
