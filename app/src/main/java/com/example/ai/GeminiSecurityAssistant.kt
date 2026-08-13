package com.example.ai

import com.example.BuildConfig
import com.example.data.ScanResultEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

object GeminiSecurityAssistant {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeThreatOrQuestion(
        userPrompt: String,
        threatContext: ScanResultEntity? = null,
        installedAppsSummary: String? = null,
        overallScore: Int = 90
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val systemPrompt = """
            You are ShieldAI, a world-class mobile cybersecurity expert and anti-scam AI security assistant.
            Provide concise, accurate, objective, and highly actionable advice on cybersecurity, mobile privacy, phishing, scams, malicious APKs, bad permissions, and suspicious links/numbers.
            Current Device Security Context:
            - Device Overall Security Score: $overallScore/100
            ${if (threatContext != null) "- Active Threat Under Query: ${threatContext.target} (${threatContext.scanType}) | Level: ${threatContext.threatLevel} | Risk Score: ${threatContext.riskScore}% | Details: ${threatContext.summaryReason}" else ""}
            ${if (installedAppsSummary != null) "- High Risk Apps Summary: $installedAppsSummary" else ""}

            Instructions:
            - If the user asks "Why is this dangerous?" or "Explain like I'm not technical", break down technical concepts into simple real-world analogies.
            - Never invent fake scan findings if none exist; speak strictly based on verified evidence or general cybersecurity principles.
            - Use bullet points and clear headings when outlining steps to secure a device or remediate a threat.
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline heuristic AI response fallback
            return@withContext generateOfflineSecurityAdvice(userPrompt, threatContext, overallScore)
        }

        try {
            val requestBodyObj = GenerateContentRequest(
                contents = listOf(
                    Content(parts = listOf(Part(text = userPrompt)))
                ),
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val jsonString = json.encodeToString(GenerateContentRequest.serializer(), requestBodyObj)
            val url = "$BASE_URL?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val parsed = json.decodeFromString(GenerateContentResponse.serializer(), responseBody)
                val reply = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!reply.isNullOrBlank()) {
                    return@withContext reply
                }
            }

            // Fallback if API returned error or empty
            return@withContext generateOfflineSecurityAdvice(userPrompt, threatContext, overallScore)
        } catch (e: Exception) {
            return@withContext generateOfflineSecurityAdvice(userPrompt, threatContext, overallScore)
        }
    }

    private fun generateOfflineSecurityAdvice(prompt: String, threat: ScanResultEntity?, score: Int): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("why") || lower.contains("dangerous") || lower.contains("explain") -> {
                if (threat != null) {
                    """
                    ShieldAI Threat Breakdown:
                    • **Target**: ${threat.target} (${threat.scanType})
                    • **Severity Level**: ${threat.threatLevel} (Risk Score: ${threat.riskScore}/100)
                    • **Primary Reason**: ${threat.summaryReason}
                    
                    **Plain Language Explanation**:
                    This item exhibits behaviors or requests permissions that allow unauthorized data extraction, UI overlays, or suspicious server communications.
                    
                    **Recommended Action**:
                    We strongly recommend clicking **Quarantine** or **Delete** to isolate this item immediately.
                    """.trimIndent()
                } else {
                    """
                    Security Principle Explanation:
                    Suspicious mobile items usually pose risks through three vectors:
                    1. **Dangerous Permissions**: Requesting access to SMS, Call Logs, Camera, or Accessibility without needing them.
                    2. **Unverified Sources**: Installing APKs outside official app stores that bypass Google Play Protect.
                    3. **Social Engineering**: Phishing links or SMS urgency designed to steal passcodes or payment tokens.
                    """.trimIndent()
                }
            }
            lower.contains("secure") || lower.contains("protect") || lower.contains("fix") -> {
                """
                ShieldAI Security Hardening Checklist:
                1. **Audit High-Risk App Permissions**: Go to ShieldAI > Permission Auditor and revoke Accessibility & Overlay permissions for unknown apps.
                2. **Enable Daily Background Scans**: Keep automated daily scans active in ShieldAI Settings.
                3. **Inspect Copied Links**: Always run suspicious URLs through the ShieldAI Link Scanner before tapping.
                4. **Update System & Play Protect**: Ensure Android system updates and Google Play Protect are updated.
                """.trimIndent()
            }
            else -> {
                """
                ShieldAI Security Engine Analysis:
                • Current Security Score: $score/100
                • Offline Rule Engine: Active
                
                I have analyzed your query. To stay protected against scams and malicious apps:
                - Do not share OTPs or click links in unsolicited SMS messages.
                - Review any app requesting Accessibility or Draw Over Apps permissions.
                - Keep real-time alerts enabled in ShieldAI settings.
                """.trimIndent()
            }
        }
    }
}
