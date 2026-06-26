package com.seoaudit.app.feature.ai.domain.usecase

import com.seoaudit.app.feature.ai.domain.model.DiscoveryData
import com.seoaudit.app.feature.ai.domain.model.DiscoveryStep
import javax.inject.Inject

/**
 * Manages the step-by-step interactive discovery flow.
 *
 * Returns the next question based on the current step,
 * validates user input, generates follow-up questions
 * when information is insufficient, and advances the flow.
 *
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5
 */
class DiscoveryFlowUseCase @Inject constructor() {

    /**
     * Returns the system question for the given discovery step.
     */
    fun getQuestionForStep(step: DiscoveryStep): String {
        return when (step) {
            DiscoveryStep.ASK_URL ->
                "Hola, soy tu asistente de auditoría SEO. " +
                "¿Cuál es la URL del sitio web que deseas analizar?"
            DiscoveryStep.ASK_WORDPRESS ->
                "¿Tu sitio utiliza WordPress como CMS? " +
                "(Responde sí/no)"
            DiscoveryStep.ASK_PROBLEM ->
                "¿Cuál es el problema principal que estás " +
                "experimentando? (Ej: caída de tráfico, " +
                "errores 404, lentitud de carga, " +
                "actualización de diseño, otro)"
            DiscoveryStep.ASK_FILES ->
                "¿Qué archivos o secciones de código deseas " +
                "analizar prioritariamente? (Ej: header.php, " +
                "página de inicio, plantillas de producto)"
            DiscoveryStep.COMPLETE ->
                "Gracias. Tengo toda la información necesaria. " +
                "Iniciando el análisis de tu sitio..."
        }
    }

    /**
     * Validates user input for the current step.
     * Returns null if valid, or a follow-up question if
     * the information is insufficient.
     */
    fun validateInput(
        step: DiscoveryStep,
        input: String
    ): String? {
        val trimmed = input.trim()
        return when (step) {
            DiscoveryStep.ASK_URL -> validateUrl(trimmed)
            DiscoveryStep.ASK_WORDPRESS -> validateWordPress(trimmed)
            DiscoveryStep.ASK_PROBLEM -> validateProblem(trimmed)
            DiscoveryStep.ASK_FILES -> validateFiles(trimmed)
            DiscoveryStep.COMPLETE -> null
        }
    }

    /**
     * Processes the user response and advances the flow.
     * Returns updated DiscoveryData with the next step.
     */
    fun processResponse(
        currentData: DiscoveryData,
        input: String
    ): DiscoveryData {
        val trimmed = input.trim()
        return when (currentData.currentStep) {
            DiscoveryStep.ASK_URL -> currentData.copy(
                siteUrl = normalizeUrl(trimmed),
                currentStep = DiscoveryStep.ASK_WORDPRESS
            )
            DiscoveryStep.ASK_WORDPRESS -> currentData.copy(
                isWordPress = parseWordPressResponse(trimmed),
                currentStep = DiscoveryStep.ASK_PROBLEM
            )
            DiscoveryStep.ASK_PROBLEM -> currentData.copy(
                mainProblem = trimmed,
                currentStep = DiscoveryStep.ASK_FILES
            )
            DiscoveryStep.ASK_FILES -> currentData.copy(
                priorityFiles = trimmed,
                currentStep = DiscoveryStep.COMPLETE
            )
            DiscoveryStep.COMPLETE -> currentData
        }
    }

    private fun validateUrl(input: String): String? {
        if (input.isBlank()) {
            return "Por favor, proporciona la URL de tu sitio " +
                "web. Ejemplo: https://ejemplo.com"
        }
        if (!input.contains(".")) {
            return "La URL no parece válida. Asegúrate de " +
                "incluir el dominio completo (ej: misitio.com)"
        }
        return null
    }

    private fun validateWordPress(input: String): String? {
        val lower = input.lowercase()
        val validYes = listOf("sí", "si", "yes", "s", "y")
        val validNo = listOf("no", "n", "not")
        if (lower !in validYes && lower !in validNo) {
            return "No entendí tu respuesta. ¿Tu sitio usa " +
                "WordPress? Responde 'sí' o 'no'."
        }
        return null
    }

    private fun validateProblem(input: String): String? {
        if (input.isBlank()) {
            return "Necesito conocer el problema principal. " +
                "¿Qué tipo de problema SEO estás " +
                "experimentando?"
        }
        if (input.length < 5) {
            return "¿Podrías describir el problema con más " +
                "detalle? Esto me ayudará a enfocar el " +
                "análisis correctamente."
        }
        return null
    }

    private fun validateFiles(input: String): String? {
        if (input.isBlank()) {
            return "¿Hay algún archivo o sección específica " +
                "que quieras analizar? Si no estás seguro, " +
                "escribe 'todo el sitio'."
        }
        return null
    }

    private fun normalizeUrl(url: String): String {
        return if (!url.startsWith("http://") &&
            !url.startsWith("https://")
        ) {
            "https://$url"
        } else {
            url
        }
    }

    private fun parseWordPressResponse(input: String): Boolean {
        val lower = input.lowercase()
        return lower in listOf("sí", "si", "yes", "s", "y")
    }
}
