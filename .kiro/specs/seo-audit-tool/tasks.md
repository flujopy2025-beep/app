# Plan de Implementación: SEO Audit Tool — Android Nativo

## Resumen

Plan de implementación en 4 fases para la aplicación nativa de Android de auditoría SEO. Cada fase construye sobre la anterior, siguiendo Clean Architecture con Kotlin, Jetpack Compose y Hilt. Las tareas están organizadas incrementalmente para entregar valor desde la Fase 1 (MVP) hasta la Fase 4 (producto completo pulido).

## Tareas

- [x] 1. Fase 1: Estructura de proyecto, Core MCP Client y Shell de UI
  - [x] 1.1 Configurar estructura de proyecto Android con Clean Architecture y Hilt
    - Crear proyecto Android con `build.gradle.kts` incluyendo todas las dependencias (Compose, Hilt, Ktor, Room, SQLCipher, Kotest, Coroutines, kotlinx.serialization)
    - Configurar la estructura de paquetes feature-by-feature: `di/`, `core/`, `feature/`, `background/`
    - Configurar módulos Hilt globales: `AppModule.kt`, `NetworkModule.kt`, `DatabaseModule.kt`, `SecurityModule.kt`
    - Configurar `NetworkModule` con Ktor Client (ContentNegotiation, Logging, Timeout, Retry)
    - _Requisitos: 17.1, 17.2, 17.4, 17.5_

  - [x] 1.2 Implementar tema Material Design 3 y navegación Compose
    - Crear tema MD3 con soporte para modo claro/oscuro automático (`core/presentation/theme/`)
    - Implementar `AppNavGraph` con todas las rutas definidas (`Screen` sealed class)
    - Crear shell de pantalla principal (`HomeScreen`) con navegación a todas las secciones
    - Crear `SplashScreen` con cold start optimizado
    - Implementar composables reutilizables base (`core/presentation/components/`)
    - _Requisitos: 18.1, 18.2, 18.5, 18.6_

  - [x] 1.3 Implementar almacenamiento seguro con Android Keystore
    - Implementar `KeystoreCredentialStore` con `EncryptedSharedPreferences` y `MasterKey AES256_GCM`
    - Implementar interfaz `CredentialRepository` (store, retrieve, delete, clearAll, exists)
    - Configurar `SecurityModule` Hilt para proveer el `CredentialRepository`
    - _Requisitos: 8.1, 20.1, 19.6_

  - [ ]* 1.4 Escribir test de propiedad para round-trip de cifrado de credenciales
    - **Propiedad 11: Round-trip de cifrado de credenciales**
    - **Valida: Requisitos 8.1, 20.1**

  - [ ]* 1.5 Escribir test de propiedad para permanencia de eliminación de credenciales
    - **Propiedad 12: Permanencia de eliminación de credenciales**
    - **Valida: Requisitos 8.3, 24.6**

  - [x] 1.6 Implementar base de datos Room con SQLCipher
    - Crear `SeoAuditDatabase` con todas las entidades: `AuditReportEntity`, `WpPageCacheEntity`, `DiagnosticReportEntity`, `AuthLockoutEntity`
    - Implementar DAOs: `AuditReportDao`, `WpPageCacheDao`, `AuthLockoutDao`
    - Configurar `DatabaseModule` Hilt con SQLCipher
    - Implementar `TypeConverters` para serialización JSON
    - _Requisitos: 20.2, 17.2_

  - [x] 1.7 Implementar autenticación biométrica/PIN
    - Implementar flujo `BiometricPrompt` para proteger acceso a credenciales
    - Implementar lógica de timeout: requerir re-autenticación tras 5 minutos en background
    - Integrar con `CredentialRepository` para gatillar verificación antes de acceder a datos sensibles
    - _Requisitos: 20.4, 20.5_

  - [ ]* 1.8 Escribir test de propiedad para autenticación biométrica por tiempo en background
    - **Propiedad 21: Requisito de autenticación biométrica por tiempo en background**
    - **Valida: Requisitos 20.4**

  - [x] 1.9 Implementar lógica de bloqueo por intentos fallidos
    - Implementar `AuthLockoutTracker` con persistencia en Room (`AuthLockoutDao`)
    - Implementar `AuthLockoutUseCase` (recordFailedAttempt, isLocked, getRemainingLockoutTime)
    - Configurar umbral de 5 intentos y ventana de bloqueo de 5 minutos
    - _Requisitos: 8.4_

  - [ ]* 1.10 Escribir test de propiedad para lógica de bloqueo por intentos fallidos
    - **Propiedad 13: Lógica de bloqueo por intentos fallidos**
    - **Valida: Requisitos 8.4**

  - [x] 1.11 Implementar Cliente MCP en Kotlin
    - Implementar `McpClientImpl` con JSON-RPC 2.0 sobre HTTP/WebSocket
    - Implementar mensajes: Initialize, ListTools, CallTool, ListResources
    - Gestionar `McpSession` y `McpSessionState` como StateFlow
    - Serializar/deserializar con kotlinx.serialization
    - _Requisitos: 19.1, 19.2, 19.4_

  - [ ]* 1.12 Escribir test de propiedad para conformidad del protocolo MCP
    - **Propiedad 10: Conformidad del protocolo MCP en validación y respuestas**
    - **Valida: Requisitos 7.2, 7.3, 7.4**

  - [x] 1.13 Implementar integración con LLM (Gemini API / Claude API)
    - Implementar `LlmApiService` con soporte para Gemini y Claude APIs
    - Implementar streaming de respuestas via SSE usando Flow
    - Implementar lógica de reintentos exponenciales (3 intentos, base 2)
    - Almacenar API keys en Android Keystore
    - _Requisitos: 19.3, 19.5, 19.6_

  - [ ]* 1.14 Escribir test de propiedad para lógica de reintentos del cliente LLM
    - **Propiedad 20: Lógica de reintentos del cliente MCP/LLM**
    - **Valida: Requisitos 19.5**

  - [ ]* 1.15 Escribir test de propiedad para detección de expiración de token
    - **Propiedad 1: Detección de expiración de token**
    - **Valida: Requisitos 1.2**

  - [x] 1.16 Implementar utilidades de fecha y helpers core
    - Implementar `DateUtils` con cálculo de rango predeterminado (28 días desde ayer)
    - Implementar extensions de Kotlin y utilidades compartidas en `core/domain/util/`
    - _Requisitos: 2.3_

  - [ ]* 1.17 Escribir test de propiedad para cómputo del rango de fechas predeterminado
    - **Propiedad 2: Cómputo del rango de fechas predeterminado**
    - **Valida: Requisitos 2.3**

- [x] 2. Checkpoint Fase 1 — Verificar compilación, tests y navegación funcional
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 3. Fase 2: Conectores GSC y WordPress
  - [x] 3.1 Implementar conector Google Search Console con Google Sign-In
    - Implementar flujo OAuth 2.0 con Google Sign-In nativo (scope: webmasters.readonly)
    - Implementar `GscTokenManager` con renovación silenciosa de tokens
    - Implementar `GscApiService` (queryAnalytics, listSites)
    - Implementar `GscRepositoryImpl` mapeando DTOs a modelos de dominio
    - Crear UI `GscAuthScreen` con flujo de selección de cuenta estándar de Google
    - _Requisitos: 1.1, 1.2, 1.3, 1.4, 24.1, 24.2, 24.3, 24.4, 24.5_

  - [x] 3.2 Implementar obtención de datos de rendimiento GSC
    - Implementar caso de uso para obtener métricas de rendimiento (clics, impresiones, CTR, posición)
    - Implementar filtrado por página específica con consultas asociadas
    - Implementar manejo de error 429 (cuota excedida) con `QuotaExceededException`
    - Crear UI de métricas de rendimiento en `AuditDashboardScreen`
    - _Requisitos: 2.1, 2.2, 2.3, 2.4, 14.1, 14.3, 14.4_

  - [ ]* 3.3 Escribir test de propiedad para atribución de fuente en métricas
    - **Propiedad 17: Atribución de fuente en métricas**
    - **Valida: Requisitos 14.3**

  - [x] 3.4 Implementar conector WordPress con Ktor
    - Implementar `WpApiService` con autenticación HTTP Basic (Application Passwords)
    - Implementar endpoints: getPage, listPages, updatePage, listThemes, listPlugins
    - Implementar `WordPressRepositoryImpl` con mappers DTO→Domain
    - Verificar conexión y permisos de escritura al autenticar
    - Crear UI `WpAuthScreen` con formulario de conexión (URL, usuario, app password)
    - _Requisitos: 3.1, 3.2, 3.3, 3.4, 23.1, 23.2, 23.3, 23.4, 23.5_

  - [ ]* 3.5 Escribir test de propiedad para completitud del parseo de páginas WordPress
    - **Propiedad 3: Completitud del parseo de páginas WordPress**
    - **Valida: Requisitos 4.1, 4.4**

  - [x] 3.6 Implementar lectura y edición de páginas WordPress
    - Implementar recuperación de contenido completo (título, meta, HTML, slug)
    - Implementar actualización de página con preservación de contenido en caso de fallo
    - Implementar `UpdateFailedException` con rollback del contenido original
    - _Requisitos: 4.1, 4.2, 4.3, 4.4_

  - [ ]* 3.7 Escribir test de propiedad para preservación de contenido en actualizaciones fallidas
    - **Propiedad 4: Preservación de contenido en actualizaciones fallidas**
    - **Valida: Requisitos 4.3**

  - [x] 3.8 Implementar caché de WordPress en Room con TTL
    - Implementar `WpPageCacheDao` con lógica de TTL configurable
    - Implementar validación de caché: retornar datos locales si dentro de TTL, solicitar a red si expirado
    - Implementar invalidación de caché al actualizar páginas
    - _Requisitos: 23.6_

  - [ ]* 3.9 Escribir test de propiedad para caché de respuestas de API WordPress
    - **Propiedad 24: Caché de respuestas de API WordPress**
    - **Valida: Requisitos 23.6**

  - [x] 3.10 Implementar conector de Sistema de Archivos (SAF)
    - Implementar `SafFileRepository` con operaciones: readFile, writeFile, listFiles, createBackup
    - Implementar persistencia de permisos URI para sesiones futuras
    - Implementar detección de charset y preservación de codificación
    - Crear UI `FileExplorerScreen` con ActivityResultLauncher para selección de directorio
    - _Requisitos: 13.1, 13.2, 13.3, 13.4, 22.1, 22.2, 22.3, 22.4, 22.5_

  - [ ]* 3.11 Escribir test de propiedad para preservación de codificación de archivos
    - **Propiedad 16: Preservación de codificación de archivos**
    - **Valida: Requisitos 13.4**

  - [ ]* 3.12 Escribir test de propiedad para creación de respaldo antes de modificación
    - **Propiedad 23: Creación de respaldo antes de modificación de archivos**
    - **Valida: Requisitos 22.6**

  - [x] 3.13 Implementar WorkManager para sincronización periódica de GSC
    - Crear `SyncWorker` con WorkManager para sincronización de datos de GSC
    - Configurar restricciones de red y batería
    - Implementar notificaciones de progreso de sincronización
    - _Requisitos: 21.2, 21.6_

  - [x] 3.14 Implementar desconexión y revocación de cuentas
    - Implementar desconexión de GSC: revocar token OAuth, limpiar Keystore y caché
    - Implementar desconexión de WordPress: eliminar credenciales y limpiar caché
    - Implementar UI en `SettingsScreen` para gestión de cuentas conectadas
    - _Requisitos: 8.3, 24.6_

- [x] 4. Checkpoint Fase 2 — Verificar conectividad, autenticación y caché
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [ ] 5. Fase 3: Motor de Auditoría IA + Generación de Código
  - [~] 5.1 Implementar motor de auditoría SEO con 7 categorías
    - Implementar análisis de: etiquetas de título, meta descripciones, encabezados H1-H6, atributos alt, enlaces internos, datos estructurados, rendimiento de carga
    - Implementar clasificación de severidad (CRITICAL, WARNING, INFO) para cada problema
    - Implementar `AuditPageUseCase` con integración de datos WP + GSC + LLM
    - Generar `AuditReport` con score 0-100 y lista de `AuditIssue`
    - _Requisitos: 5.1, 5.2, 5.3_

  - [ ]* 5.2 Escribir test de propiedad para validez estructural del informe de auditoría
    - **Propiedad 5: Validez estructural del informe de auditoría**
    - **Valida: Requisitos 5.1, 5.3**

  - [ ]* 5.3 Escribir test de propiedad para cobertura de categorías del motor de auditoría
    - **Propiedad 6: Cobertura de categorías del motor de auditoría**
    - **Valida: Requisitos 5.2**

  - [~] 5.4 Implementar auditoría de sitio completo
    - Implementar `AuditSiteUseCase` con procesamiento iterativo de páginas
    - Emitir `SiteAuditProgress` (InProgress, Complete, Error) como Flow
    - Generar `SiteAuditReport` consolidado con `AuditSummary` (críticos, advertencias, info, corregibles)
    - _Requisitos: 5.4_

  - [ ]* 5.5 Escribir test de propiedad para completitud de auditoría de sitio
    - **Propiedad 7: Completitud de auditoría de sitio**
    - **Valida: Requisitos 5.4**

  - [~] 5.6 Implementar motor de diferencias (DiffEngine)
    - Implementar `DiffEngineImpl` con algoritmo LCS para generar diffs
    - Implementar `applyDiff` para aplicar hunks al texto original
    - Implementar `validateDiff` para verificar round-trip
    - Crear modelos `DiffResult`, `DiffHunk`, `DiffLine`
    - _Requisitos: 6.3, 12.1_

  - [ ]* 5.7 Escribir test de propiedad para corrección de generación de diferencias (Round-Trip)
    - **Propiedad 8: Corrección de la generación de diferencias (Round-Trip)**
    - **Valida: Requisitos 6.3, 12.1**

  - [~] 5.8 Implementar generación de código corregido con validación
    - Implementar `GenerateCodeFixUseCase` que invoca al LLM para generar código corregido
    - Implementar validador de sintaxis HTML (detección de etiquetas sin cerrar, atributos mal formados)
    - Implementar validador de seguridad (detección de SQLi y XSS)
    - Validar código PHP 8.0+ y JS ES6+ generado
    - _Requisitos: 6.1, 6.4, 12.4, 15.1, 15.2, 15.3, 15.4, 15.5, 15.6_

  - [ ]* 5.9 Escribir test de propiedad para detección de errores de sintaxis HTML
    - **Propiedad 9: Detección de errores de sintaxis HTML**
    - **Valida: Requisitos 6.4**

  - [ ]* 5.10 Escribir test de propiedad para detección de vulnerabilidades de seguridad
    - **Propiedad 18: Detección de vulnerabilidades de seguridad en código generado**
    - **Valida: Requisitos 15.4, 15.5**

  - [~] 5.11 Implementar aplicación de correcciones (WordPress y archivos locales)
    - Implementar `ApplyFixUseCase` con rutas para WordPress (vía API) y archivos locales (vía SAF)
    - Crear backup automático antes de aplicar cambios en archivos locales
    - Implementar advertencia para archivos de riesgo (functions.php) con propuesta de alternativa segura
    - _Requisitos: 6.2, 12.2, 12.3, 12.5, 22.6_

  - [~] 5.12 Implementar informe diagnóstico con tabla de problemas
    - Implementar `DiagnosticReport` con columnas: Problema, Origen (GSC/Código), Impacto, Solución
    - Implementar ordenamiento por impacto descendente (Alto → Medio → Bajo)
    - Implementar agrupación por categoría técnica cuando hay más de 10 problemas
    - Crear UI `DiagnosticReportScreen` con tabla de problemas
    - _Requisitos: 11.1, 11.2, 11.3, 11.4_

  - [ ]* 5.13 Escribir test de propiedad para estructura y ordenamiento del informe diagnóstico
    - **Propiedad 15: Estructura y ordenamiento del informe diagnóstico**
    - **Valida: Requisitos 11.1, 11.2, 11.3, 11.4**

  - [~] 5.14 Implementar identificación de páginas con peor rendimiento
    - Implementar algoritmo de detección de páginas con bajo CTR/posición respecto al promedio
    - Implementar detección de caídas repentinas de impresiones
    - Integrar con UI de dashboard de auditoría
    - _Requisitos: 10.1_

  - [ ]* 5.15 Escribir test de propiedad para identificación de páginas con peor rendimiento
    - **Propiedad 14: Identificación de páginas con peor rendimiento**
    - **Valida: Requisitos 10.1**

  - [~] 5.16 Implementar UI de auditoría y vista de diferencias
    - Crear `PageAuditScreen` con visualización de informe (score, problemas por severidad)
    - Crear UI de vista de diferencias (diff view) con colores para líneas añadidas/eliminadas
    - Crear `ApplyFixConfirmationDialog` con botón de aceptar/rechazar corrección
    - Crear `AuditDashboardScreen` con lista de páginas auditadas y progreso de auditoría de sitio
    - _Requisitos: 5.1, 5.3, 6.3, 18.2_

  - [~] 5.17 Implementar Foreground Service para auditorías largas
    - Implementar `AuditForegroundService` con notificación persistente de progreso
    - Implementar `NotificationHelper` con barra de progreso (etapa actual + porcentaje)
    - Manejar interrupción por OS: guardar checkpoint, notificar al usuario
    - _Requisitos: 21.1, 21.4, 21.5_

  - [~] 5.18 Implementar paginación para listas extensas
    - Implementar paginación con Jetpack Paging 3 para listas de páginas, resultados y problemas
    - Aplicar umbral de 50 elementos para activar paginación
    - Implementar metadatos correctos (totalPages, totalItems, currentPage)
    - _Requisitos: 26.4_

  - [ ]* 5.19 Escribir test de propiedad para umbral de paginación
    - **Propiedad 25: Umbral de paginación**
    - **Valida: Requisitos 26.4**

- [~] 6. Checkpoint Fase 3 — Verificar motor de auditoría, diff engine y validadores
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [ ] 7. Fase 4: Flujo Interactivo Completo + Pulido
  - [~] 7.1 Implementar flujo interactivo de descubrimiento
    - Implementar secuencia de preguntas: URL del sitio → ¿usa WordPress? → problema principal → archivos/secciones prioritarias
    - Implementar espera de respuesta del usuario en cada paso
    - Implementar preguntas de seguimiento cuando la información es insuficiente
    - Crear UI conversacional (`AiChatScreen`) para el flujo de descubrimiento
    - _Requisitos: 9.1, 9.2, 9.3, 9.4, 9.5_

  - [~] 7.2 Implementar análisis cruzado GSC + código fuente
    - Implementar `CrossAnalysisUseCase` que correlaciona métricas GSC con problemas de código
    - Identificar cuellos de botella técnicos: JS/CSS redundante, estructura HTML deficiente, ausencia de datos estructurados
    - Correlacionar cada problema con impacto medible en métricas GSC
    - Solicitar datos manuales si GSC no disponible para una página
    - _Requisitos: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [~] 7.3 Implementar MCP Server expuesto
    - Implementar `McpServerImpl` con todas las herramientas definidas: gsc_authenticate, gsc_list_sites, gsc_get_performance, wp_authenticate, wp_list_pages, audit_page, audit_site, generate_fix, apply_fix, disconnect_account
    - Implementar recurso de configuración con conexiones activas y su estado
    - Validar parámetros de entrada y retornar errores según especificación MCP
    - Crear UI `McpConsoleScreen` para visualizar estado del servidor
    - _Requisitos: 7.1, 7.2, 7.3, 7.4, 7.5_

  - [~] 7.4 Implementar checkpoint/resume para procesos interrumpidos
    - Implementar guardado de estado de progreso en Room al perder conectividad
    - Implementar reanudación automática desde último punto de control
    - No reprocesar elementos ya completados
    - _Requisitos: 21.3_

  - [ ]* 7.5 Escribir test de propiedad para checkpoint de progreso ante interrupciones
    - **Propiedad 22: Checkpoint de progreso ante interrupciones**
    - **Valida: Requisitos 21.3**

  - [~] 7.6 Implementar preservación de estado en navegación
    - Asegurar que ViewModels preservan UiState al navegar hacia atrás (pop)
    - Implementar `SavedStateHandle` para restauración tras process death
    - _Requisitos: 18.4_

  - [ ]* 7.7 Escribir test de propiedad para preservación de estado en navegación
    - **Propiedad 19: Preservación de estado en navegación**
    - **Valida: Requisitos 18.4**

  - [~] 7.8 Optimización de rendimiento
    - Implementar lazy loading para imágenes, gráficos y datos secundarios fuera del viewport
    - Optimizar cold start para < 2 segundos (dispositivos Android 8.0, 3GB RAM)
    - Verificar renderizado a 60 FPS (< 16ms por frame)
    - Limitar consumo de RAM a < 256MB durante análisis activo
    - Asegurar uso de Dispatchers.IO para red y Dispatchers.Default para cómputo
    - _Requisitos: 26.1, 26.2, 26.3, 26.5, 26.6_

  - [~] 7.9 Implementar tono profesional y comunicación
    - Configurar prompts del Motor_IA para mantener tono técnico, directo y profesional
    - Incluir justificación técnica en cada recomendación
    - Enfocar recomendaciones en eficiencia de rendimiento e impacto medible
    - _Requisitos: 16.1, 16.2, 16.3_

  - [~] 7.10 Implementar manejo de integridad de datos
    - Asegurar que Motor_IA usa exclusivamente datos reales de GSC (nunca datos inventados)
    - Implementar solicitud de datos manuales cuando GSC no disponible
    - Incluir fuente y rango de fechas en cada métrica presentada
    - _Requisitos: 14.1, 14.2, 14.3, 14.4_

  - [ ]* 7.11 Escribir tests de UI con Compose Testing para pantallas principales
    - Test de `HomeScreen`, `AuditDashboardScreen`, `PageAuditScreen`, `GscAuthScreen`, `WpAuthScreen`
    - Test de estados de carga, error y éxito
    - Test de accesibilidad y tamaños de fuente mínimos
    - _Requisitos: 18.1, 18.2, 18.5_

- [~] 8. Checkpoint Final — Verificar flujo completo E2E y rendimiento
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental al final de cada fase
- Los tests de propiedad validan propiedades universales de corrección definidas en el diseño
- Los tests unitarios validan ejemplos específicos y casos extremos
- La Fase 1 entrega un MVP funcional con MCP Client y navegación
- Las Fases 2-4 agregan conectividad, análisis IA y pulido progresivamente
- El proyecto utiliza Kotlin como lenguaje, Jetpack Compose para UI, y Kotest para tests de propiedad

## Grafo de Dependencias de Tareas

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "1.3", "1.6", "1.16"] },
    { "id": 2, "tasks": ["1.4", "1.5", "1.7", "1.9", "1.11", "1.13", "1.15", "1.17"] },
    { "id": 3, "tasks": ["1.8", "1.10", "1.12", "1.14"] },
    { "id": 4, "tasks": ["3.1", "3.4", "3.10"] },
    { "id": 5, "tasks": ["3.2", "3.5", "3.6", "3.8", "3.11", "3.12", "3.13", "3.14"] },
    { "id": 6, "tasks": ["3.3", "3.7", "3.9"] },
    { "id": 7, "tasks": ["5.1", "5.6", "5.14", "5.18"] },
    { "id": 8, "tasks": ["5.2", "5.3", "5.4", "5.7", "5.8", "5.15", "5.19"] },
    { "id": 9, "tasks": ["5.5", "5.9", "5.10", "5.11", "5.12", "5.16", "5.17"] },
    { "id": 10, "tasks": ["5.13"] },
    { "id": 11, "tasks": ["7.1", "7.2", "7.3", "7.4", "7.6", "7.8"] },
    { "id": 12, "tasks": ["7.5", "7.7", "7.9", "7.10", "7.11"] }
  ]
}
```
