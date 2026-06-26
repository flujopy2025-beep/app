# Documento de Requisitos

## Introducción

Aplicación nativa de Android para auditoría SEO potenciada por inteligencia artificial que actúa como un Ingeniero de Software Full-Stack Senior, Especialista en SEO Técnico y Experto en Automatización con MCP. La aplicación funciona como un Cliente MCP y Orquestador de IA, conectándose a Google Search Console y WordPress mediante APIs, y accediendo al sistema de archivos local del dispositivo para auditar, analizar y optimizar sitios web combinando datos de rendimiento, métricas de búsqueda y código fuente. Construida con Kotlin y Jetpack Compose siguiendo Clean Architecture, la aplicación permite un flujo de trabajo interactivo completo desde un dispositivo móvil: recopilación de datos, análisis cruzado con LLMs externos (Gemini API o Anthropic Claude API), generación de informes diagnósticos y producción de código corregido seguro y optimizado para SEO.

### Stack Tecnológico

- **Lenguaje**: Kotlin
- **Framework de UI**: Jetpack Compose (Material Design 3, optimizado para escaneabilidad en pantallas de 6")
- **Concurrencia**: Kotlin Coroutines & Flow
- **Inyección de Dependencias**: Hilt o Koin
- **Almacenamiento Seguro**: Android Keystore + Room (cifrado y almacenamiento de tokens GSC y credenciales WP)
- **Procesamiento en Segundo Plano**: WorkManager o Foreground Services (prevenir que el SO cancele procesos largos de análisis IA)
- **Arquitectura**: Clean Architecture (capas Data, Domain, Presentation)
- **Organización**: Feature-by-Feature modular, optimizada para rendimiento móvil

### Plan de Desarrollo (4 Fases, MVP Primero)

- **Fase 1**: Core MCP Client + Shell básico de UI
- **Fase 2**: Conectores GSC y WordPress
- **Fase 3**: Motor de Auditoría IA + Generación de Código
- **Fase 4**: Flujo de trabajo interactivo completo + Pulido

## Glosario

- **Sistema_Auditoría_SEO**: Aplicación nativa de Android que orquesta la auditoría SEO, actúa como Cliente MCP y Orquestador de IA, gestiona la conexión con APIs externas, el acceso al sistema de archivos y la edición de páginas.
- **Cliente_MCP**: Componente que implementa el protocolo Model Context Protocol del lado cliente en Kotlin, gestionando el ciclo de vida de Tools, Prompts y Resources, e interfaz con LLMs externos.
- **Conector_GSC**: Módulo responsable de la autenticación OAuth 2.0 (Google Sign-In) y comunicación con la API de Google Search Console (GSC_API).
- **Conector_WordPress**: Módulo responsable de la autenticación mediante Application Passwords y comunicación con la API REST de WordPress (WP_API) utilizando Ktor o Retrofit.
- **Conector_FS**: Módulo responsable de la lectura y escritura de archivos del proyecto web local mediante el Storage Access Framework (SAF) de Android.
- **Motor_IA**: Componente de inteligencia artificial que se comunica con LLMs externos (Gemini API o Anthropic Claude API) para analizar datos SEO, realizar análisis cruzado de métricas y código, y generar soluciones de código.
- **Servidor_MCP**: Servidor que expone las capacidades de la herramienta mediante el protocolo Model Context Protocol.
- **Informe_Auditoría**: Documento estructurado que contiene los hallazgos, métricas y recomendaciones de la auditoría SEO.
- **Informe_Diagnóstico**: Tabla técnica concisa que presenta problemas detectados, su origen (GSC/Código), impacto y solución propuesta.
- **Credenciales_OAuth**: Tokens de autenticación OAuth 2.0 utilizados para acceder a la API de Google Search Console mediante Google Sign-In.
- **Token_WordPress**: Token de autenticación (Application Password) utilizado para acceder a la API REST de WordPress.
- **Core_Web_Vitals**: Métricas de Google que miden la experiencia de usuario en la web (LCP, FID, CLS).
- **Datos_Estructurados**: Marcado Schema.org en formato JSON-LD que facilita la comprensión del contenido por motores de búsqueda.
- **Formato_Diff**: Representación de cambios de código que muestra líneas eliminadas y líneas añadidas para facilitar la revisión.
- **Clean_Architecture**: Patrón arquitectónico con separación en capas Data, Domain y Presentation, donde las dependencias apuntan hacia el dominio.
- **Capa_Data**: Capa de Clean Architecture responsable de la implementación de repositorios, fuentes de datos (Room, APIs remotas, SAF) y mapeo de entidades.
- **Capa_Domain**: Capa de Clean Architecture que contiene los casos de uso, entidades de negocio e interfaces de repositorio (sin dependencias de framework Android).
- **Capa_Presentation**: Capa de Clean Architecture que contiene ViewModels, estados de UI y composables de Jetpack Compose.
- **SAF**: Storage Access Framework de Android, API del sistema para acceso seguro a archivos y directorios del dispositivo con permisos explícitos del usuario.
- **WorkManager**: API de Android Jetpack para programar tareas en segundo plano que deben ejecutarse de manera confiable incluso si la aplicación se cierra.
- **Foreground_Service**: Servicio de Android con notificación persistente que el sistema operativo no cancela durante procesos largos.
- **Android_Keystore**: Sistema de almacenamiento criptográfico de Android que protege claves y credenciales a nivel de hardware.

## Requisitos

### Requisito 1: Autenticación con Google Search Console

**Historia de Usuario:** Como usuario, quiero conectar mi cuenta de Google Search Console a la herramienta, para que pueda acceder a los datos de rendimiento SEO de mis sitios web.

#### Criterios de Aceptación

1. WHEN el usuario proporciona credenciales OAuth válidas, THE Conector_GSC SHALL autenticar la conexión y almacenar el token de acceso de forma segura.
2. WHEN el token de acceso ha expirado, THE Conector_GSC SHALL renovar el token automáticamente utilizando el token de refresco.
3. IF las credenciales OAuth proporcionadas son inválidas, THEN THE Conector_GSC SHALL retornar un mensaje de error descriptivo indicando el motivo del fallo de autenticación.
4. WHEN la autenticación es exitosa, THE Conector_GSC SHALL listar los sitios web disponibles asociados a la cuenta del usuario.

### Requisito 2: Obtención de Datos de Google Search Console

**Historia de Usuario:** Como usuario, quiero obtener métricas de rendimiento SEO de mis páginas desde Google Search Console, para que pueda identificar áreas de mejora.

#### Criterios de Aceptación

1. WHEN el usuario selecciona un sitio web autenticado, THE Conector_GSC SHALL recuperar los datos de rendimiento (clics, impresiones, CTR, posición media) para el rango de fechas especificado.
2. WHEN el usuario solicita datos de una página específica, THE Conector_GSC SHALL retornar las consultas de búsqueda asociadas a esa página con sus métricas correspondientes.
3. WHEN el usuario solicita datos sin especificar un rango de fechas, THE Conector_GSC SHALL utilizar los últimos 28 días como rango predeterminado.
4. IF la API de Google Search Console retorna un error de cuota excedida, THEN THE Conector_GSC SHALL informar al usuario del límite alcanzado y sugerir un tiempo de espera.

### Requisito 3: Autenticación con WordPress

**Historia de Usuario:** Como usuario, quiero conectar mi sitio WordPress a la herramienta, para que pueda editar el contenido y código de mis páginas directamente.

#### Criterios de Aceptación

1. WHEN el usuario proporciona la URL del sitio WordPress y un Token_WordPress válido, THE Conector_WordPress SHALL establecer la conexión y verificar los permisos de escritura.
2. IF la URL del sitio WordPress no es accesible, THEN THE Conector_WordPress SHALL retornar un mensaje de error indicando que el sitio no está disponible.
3. IF el Token_WordPress no tiene permisos de edición, THEN THE Conector_WordPress SHALL informar al usuario que los permisos son insuficientes para editar contenido.
4. WHEN la autenticación es exitosa, THE Conector_WordPress SHALL confirmar la conexión mostrando el nombre del sitio y la versión de WordPress.

### Requisito 4: Lectura y Edición de Páginas WordPress

**Historia de Usuario:** Como usuario, quiero leer y editar el código HTML de mis páginas WordPress, para que pueda implementar las correcciones SEO recomendadas.

#### Criterios de Aceptación

1. WHEN el usuario solicita una página específica, THE Conector_WordPress SHALL recuperar el contenido completo incluyendo título, meta descripción, contenido HTML y slug.
2. WHEN el usuario envía cambios de código para una página, THE Conector_WordPress SHALL actualizar la página en WordPress y confirmar la actualización exitosa.
3. IF la actualización de la página falla, THEN THE Conector_WordPress SHALL retornar un mensaje de error descriptivo y preservar el contenido original sin modificaciones.
4. WHEN el usuario solicita una lista de páginas, THE Conector_WordPress SHALL retornar las páginas disponibles con su título, URL y estado de publicación.

### Requisito 5: Auditoría SEO Automatizada con IA

**Historia de Usuario:** Como usuario, quiero que la IA audite automáticamente mis páginas web y detecte problemas de SEO, para que pueda mejorar el posicionamiento de mis sitios.

#### Criterios de Aceptación

1. WHEN el usuario inicia una auditoría para una página específica, THE Motor_IA SHALL analizar el contenido HTML y generar un Informe_Auditoría con los problemas detectados categorizados por severidad (crítico, advertencia, información).
2. THE Motor_IA SHALL evaluar los siguientes aspectos SEO: etiquetas de título, meta descripciones, encabezados (H1-H6), atributos alt en imágenes, enlaces internos, datos estructurados y rendimiento de carga.
3. WHEN la auditoría detecta un problema, THE Motor_IA SHALL proporcionar una explicación clara del problema, su impacto en el SEO y una recomendación de corrección.
4. WHEN el usuario solicita una auditoría completa del sitio, THE Motor_IA SHALL auditar todas las páginas disponibles y generar un resumen consolidado con las prioridades de corrección.

### Requisito 6: Generación de Soluciones de Código con IA

**Historia de Usuario:** Como usuario, quiero que la IA genere soluciones de código para corregir los problemas de SEO detectados, para que pueda implementar las correcciones de forma rápida y precisa.

#### Criterios de Aceptación

1. WHEN la auditoría identifica un problema corregible, THE Motor_IA SHALL generar una propuesta de código corregido que resuelva el problema de SEO identificado.
2. WHEN el usuario acepta una solución propuesta, THE Sistema_Auditoría_SEO SHALL aplicar los cambios a la página mediante el Conector_WordPress.
3. THE Motor_IA SHALL presentar una vista de diferencias (diff) entre el código original y el código propuesto antes de aplicar cualquier cambio.
4. IF el código propuesto contiene errores de sintaxis HTML, THEN THE Motor_IA SHALL validar el código y corregir los errores antes de proponerlo al usuario.

### Requisito 7: Servidor MCP

**Historia de Usuario:** Como usuario, quiero que la herramienta funcione como un servidor MCP, para que pueda integrarla con clientes MCP compatibles (como asistentes de IA) y utilizarla mediante comandos de lenguaje natural.

#### Criterios de Aceptación

1. THE Servidor_MCP SHALL exponer las funcionalidades de auditoría SEO, lectura de datos de Google Search Console y edición de WordPress como herramientas MCP individuales.
2. WHEN un cliente MCP envía una solicitud de herramienta, THE Servidor_MCP SHALL validar los parámetros de entrada y ejecutar la operación correspondiente.
3. THE Servidor_MCP SHALL retornar respuestas en el formato estándar del protocolo MCP con contenido estructurado (texto, JSON o ambos).
4. IF un cliente MCP envía una solicitud con parámetros inválidos, THEN THE Servidor_MCP SHALL retornar un error descriptivo siguiendo la especificación de errores del protocolo MCP.
5. THE Servidor_MCP SHALL exponer un recurso de configuración que liste las conexiones activas (Google Search Console y WordPress) y su estado.

### Requisito 8: Seguridad y Gestión de Credenciales

**Historia de Usuario:** Como usuario, quiero que mis credenciales y tokens de acceso se gestionen de forma segura, para que mi información de autenticación esté protegida.

#### Criterios de Aceptación

1. THE Sistema_Auditoría_SEO SHALL almacenar las Credenciales_OAuth y el Token_WordPress de forma cifrada utilizando un mecanismo de cifrado estándar.
2. THE Sistema_Auditoría_SEO SHALL transmitir las credenciales únicamente mediante conexiones HTTPS.
3. WHEN el usuario solicita desconectar una cuenta, THE Sistema_Auditoría_SEO SHALL eliminar las credenciales almacenadas de forma permanente e irreversible.
4. IF se detectan 5 intentos fallidos de autenticación consecutivos, THEN THE Sistema_Auditoría_SEO SHALL bloquear temporalmente los intentos de conexión durante 5 minutos.

### Requisito 9: Descubrimiento Interactivo y Recopilación de Datos

**Historia de Usuario:** Como usuario, quiero que el sistema me solicite información sobre mi sitio web de forma interactiva antes de iniciar el análisis, para que la auditoría se adapte a mi contexto y problema específico.

#### Criterios de Aceptación

1. WHEN el usuario inicia una nueva sesión de auditoría, THE Sistema_Auditoría_SEO SHALL solicitar al usuario la URL del sitio web y si el sitio utiliza WordPress.
2. WHEN el usuario proporciona la URL del sitio, THE Sistema_Auditoría_SEO SHALL solicitar al usuario que describa el problema principal (caída de tráfico, errores 404 masivos, lentitud de carga, actualización de diseño u otro problema específico).
3. WHEN el usuario describe el problema principal, THE Sistema_Auditoría_SEO SHALL solicitar al usuario qué archivos o secciones de código desea analizar prioritariamente.
4. THE Sistema_Auditoría_SEO SHALL esperar la respuesta del usuario en cada paso interactivo antes de proceder al siguiente paso del flujo de trabajo.
5. IF el usuario no proporciona información suficiente para iniciar el análisis, THEN THE Sistema_Auditoría_SEO SHALL formular preguntas de seguimiento específicas para clarificar el alcance de la auditoría.

### Requisito 10: Análisis Cruzado de Datos y Código

**Historia de Usuario:** Como usuario, quiero que el sistema cruce los datos de rendimiento de Google Search Console con el código fuente de mis páginas, para que pueda identificar la causa raíz de los problemas de SEO.

#### Criterios de Aceptación

1. WHEN el usuario ha proporcionado los datos del sitio, THE Motor_IA SHALL analizar las métricas del Conector_GSC identificando las páginas con peor rendimiento o caídas repentinas de CTR e impresiones.
2. WHEN el Motor_IA identifica páginas con bajo rendimiento, THE Conector_FS o THE Conector_WordPress SHALL recuperar el código fuente relevante de esas páginas específicas para su análisis.
3. WHEN el código fuente está disponible, THE Motor_IA SHALL identificar cuellos de botella técnicos: código JS/CSS redundante, estructura deficiente de etiquetas HTML (H1-H6), ausencia de Datos_Estructurados o errores de carga que afecten los Core_Web_Vitals.
4. THE Motor_IA SHALL correlacionar cada problema de código detectado con su impacto medible en las métricas de Google Search Console.
5. IF los datos de Google Search Console no están disponibles para una página específica, THEN THE Motor_IA SHALL solicitar al usuario que proporcione los datos de rendimiento manualmente.

### Requisito 11: Generación de Informe Diagnóstico

**Historia de Usuario:** Como usuario, quiero recibir un informe diagnóstico técnico conciso en formato de tabla, para que pueda priorizar las correcciones según su impacto y origen.

#### Criterios de Aceptación

1. WHEN el análisis cruzado se completa, THE Motor_IA SHALL generar un Informe_Diagnóstico en formato de tabla con las columnas: Problema Detectado, Origen (GSC / Código), Impacto (Alto/Medio/Bajo) y Solución Propuesta.
2. THE Motor_IA SHALL ordenar los problemas en el Informe_Diagnóstico por nivel de impacto, presentando los problemas de impacto Alto primero.
3. THE Motor_IA SHALL clasificar el origen de cada problema como "GSC" cuando se detecta a partir de métricas de rendimiento, o como "Código" cuando se detecta mediante análisis del código fuente.
4. WHEN el Informe_Diagnóstico contiene más de 10 problemas, THE Motor_IA SHALL agrupar los problemas por categoría técnica (rendimiento, estructura HTML, datos estructurados, recursos bloqueantes).

### Requisito 12: Generación de Diff y Modificación de Código

**Historia de Usuario:** Como usuario, quiero que el sistema genere bloques de código corregido en formato Diff claro para cada solución aprobada, para que pueda revisar e implementar las correcciones de forma segura.

#### Criterios de Aceptación

1. WHEN el usuario aprueba una solución del Informe_Diagnóstico, THE Motor_IA SHALL generar el bloque de código corregido en Formato_Diff mostrando líneas eliminadas y líneas añadidas.
2. WHEN el código corregido está listo para aplicar en un sitio WordPress, THE Sistema_Auditoría_SEO SHALL insertar el código mediante el Conector_WordPress.
3. WHEN el código corregido corresponde a archivos locales del proyecto, THE Sistema_Auditoría_SEO SHALL aplicar los cambios mediante el Conector_FS.
4. THE Motor_IA SHALL generar código que sea limpio, seguro contra vulnerabilidades (SQLi, XSS) y optimizado para SEO.
5. IF la modificación propuesta puede comprometer la estabilidad del sitio WordPress (por ejemplo, editar functions.php directamente), THEN THE Sistema_Auditoría_SEO SHALL advertir explícitamente al usuario del riesgo y proponer una alternativa segura (plugin de snippets o Child Theme).

### Requisito 13: Acceso al Sistema de Archivos Local

**Historia de Usuario:** Como usuario, quiero que el sistema pueda leer y escribir archivos del proyecto web local, para que pueda analizar y corregir código fuente que no está gestionado por WordPress.

#### Criterios de Aceptación

1. WHEN el usuario indica archivos específicos del proyecto local, THE Conector_FS SHALL leer el contenido de esos archivos y proporcionarlo al Motor_IA para su análisis.
2. WHEN el Motor_IA genera una corrección para un archivo local, THE Conector_FS SHALL escribir los cambios en el archivo correspondiente tras la aprobación del usuario.
3. IF el archivo solicitado no existe en la ruta especificada, THEN THE Conector_FS SHALL informar al usuario que el archivo no fue encontrado e indicar la ruta buscada.
4. THE Conector_FS SHALL preservar la codificación original del archivo (UTF-8, ISO-8859-1) al realizar modificaciones.

### Requisito 14: Integridad de Datos y Prohibición de Datos Inventados

**Historia de Usuario:** Como usuario, quiero que el sistema utilice únicamente datos reales de Google Search Console y nunca invente métricas, para que pueda confiar en los informes generados.

#### Criterios de Aceptación

1. THE Motor_IA SHALL utilizar exclusivamente datos reales obtenidos del Conector_GSC para métricas de rendimiento (impresiones, clics, CTR, errores de indexación).
2. IF los datos de Google Search Console no están disponibles o la conexión falla, THEN THE Sistema_Auditoría_SEO SHALL solicitar al usuario que proporcione los datos manualmente en lugar de generar estimaciones.
3. THE Motor_IA SHALL indicar explícitamente la fuente y el rango de fechas de cada métrica presentada en los informes.
4. IF el usuario solicita un análisis para un período sin datos disponibles en Google Search Console, THEN THE Conector_GSC SHALL informar al usuario que no existen datos para ese rango de fechas.

### Requisito 15: Estándares de Código y Seguridad

**Historia de Usuario:** Como usuario, quiero que todo código generado cumpla con estándares modernos y sea seguro contra vulnerabilidades, para que las correcciones no introduzcan problemas de seguridad ni compatibilidad.

#### Criterios de Aceptación

1. THE Motor_IA SHALL generar código PHP compatible con la versión 8.0 o superior.
2. THE Motor_IA SHALL generar código JavaScript compatible con el estándar ES6 o superior.
3. THE Motor_IA SHALL generar código HTML siguiendo el estándar semántico HTML5.
4. THE Motor_IA SHALL validar que el código generado no contenga vulnerabilidades de inyección SQL (SQLi).
5. THE Motor_IA SHALL validar que el código generado no contenga vulnerabilidades de Cross-Site Scripting (XSS) mediante el escape adecuado de salidas.
6. THE Motor_IA SHALL sanitizar todas las entradas de usuario en el código generado utilizando funciones de sanitización apropiadas para el contexto (base de datos, HTML, URL).

### Requisito 16: Tono y Comunicación Profesional

**Historia de Usuario:** Como usuario, quiero que el sistema mantenga un tono técnico, directo y profesional en todas sus comunicaciones, para que la interacción sea eficiente y enfocada en resultados de rendimiento.

#### Criterios de Aceptación

1. THE Sistema_Auditoría_SEO SHALL mantener un tono técnico, directo y profesional en todas las respuestas y reportes generados.
2. THE Sistema_Auditoría_SEO SHALL enfocar todas las recomendaciones en la eficiencia de rendimiento y el impacto medible en métricas SEO.
3. WHEN el Sistema_Auditoría_SEO presenta soluciones, THE Sistema_Auditoría_SEO SHALL incluir una justificación técnica concisa del beneficio esperado para cada corrección propuesta.

### Requisito 17: Plataforma Android Nativa y Arquitectura Clean

**Historia de Usuario:** Como desarrollador, quiero que la aplicación sea una app nativa de Android construida con Kotlin y Jetpack Compose siguiendo Clean Architecture, para que el código sea modular, testeable y optimizado para rendimiento móvil.

#### Criterios de Aceptación

1. THE Sistema_Auditoría_SEO SHALL implementarse como una aplicación nativa de Android utilizando Kotlin como lenguaje de programación principal.
2. THE Sistema_Auditoría_SEO SHALL organizar el código siguiendo Clean_Architecture con separación estricta en tres capas: Capa_Data, Capa_Domain y Capa_Presentation.
3. THE Capa_Domain SHALL contener los casos de uso y entidades de negocio sin dependencias directas de frameworks de Android (android.*, androidx.*).
4. THE Sistema_Auditoría_SEO SHALL utilizar inyección de dependencias mediante Hilt o Koin para gestionar el ciclo de vida de los componentes y facilitar la testabilidad.
5. THE Sistema_Auditoría_SEO SHALL organizar los módulos del proyecto por funcionalidad (feature-by-feature) en lugar de por tipo de componente técnico.
6. THE Sistema_Auditoría_SEO SHALL utilizar Kotlin Coroutines y Flow como mecanismo principal de concurrencia para operaciones asíncronas y flujos de datos reactivos.

### Requisito 18: Interfaz de Usuario con Jetpack Compose y Material Design 3

**Historia de Usuario:** Como usuario, quiero una interfaz moderna y escaneable optimizada para pantallas de 6 pulgadas, para que pueda realizar auditorías SEO de forma cómoda desde mi dispositivo móvil.

#### Criterios de Aceptación

1. THE Capa_Presentation SHALL implementar la interfaz de usuario utilizando Jetpack Compose con el sistema de diseño Material Design 3.
2. THE Capa_Presentation SHALL optimizar la disposición de componentes de UI para escaneabilidad en pantallas de 6 pulgadas, priorizando la legibilidad y el acceso rápido a las acciones principales.
3. THE Capa_Presentation SHALL utilizar ViewModels de Android Architecture Components para gestionar el estado de la UI de forma independiente del ciclo de vida de los composables.
4. WHEN el usuario navega entre secciones de la aplicación, THE Capa_Presentation SHALL preservar el estado de la pantalla anterior para permitir la navegación hacia atrás sin pérdida de datos.
5. THE Capa_Presentation SHALL seguir las guías de tipografía de Material Design 3 con tamaños de fuente mínimos de 14sp para texto de contenido y 12sp para texto secundario.
6. WHEN el dispositivo cambia entre modo claro y modo oscuro, THE Capa_Presentation SHALL adaptar la interfaz al tema del sistema de forma automática sin reiniciar la actividad.

### Requisito 19: Cliente MCP en Kotlin

**Historia de Usuario:** Como usuario, quiero que la aplicación funcione como un Cliente MCP capaz de comunicarse con LLMs externos, para que pueda orquestar análisis de IA avanzados directamente desde mi dispositivo móvil.

#### Criterios de Aceptación

1. THE Cliente_MCP SHALL implementar el protocolo Model Context Protocol del lado cliente en Kotlin, gestionando la comunicación bidireccional con servidores MCP.
2. THE Cliente_MCP SHALL gestionar el ciclo de vida completo de Tools (herramientas), Prompts (plantillas) y Resources (recursos) según la especificación del protocolo MCP.
3. THE Cliente_MCP SHALL proporcionar una interfaz de conexión con LLMs externos compatibles, incluyendo Gemini API y Anthropic Claude API.
4. WHEN el Cliente_MCP envía una solicitud a un LLM externo, THE Cliente_MCP SHALL incluir el contexto relevante (datos de GSC, código fuente, resultados previos) de forma estructurada según el protocolo.
5. IF la conexión con el LLM externo falla o se agota el tiempo de espera, THEN THE Cliente_MCP SHALL reintentar la conexión hasta 3 veces con espera exponencial antes de informar el error al usuario.
6. THE Cliente_MCP SHALL almacenar y gestionar las claves de API de los LLMs externos de forma segura utilizando Android_Keystore.

### Requisito 20: Almacenamiento Seguro en Android

**Historia de Usuario:** Como usuario, quiero que todas mis credenciales (tokens GSC, contraseñas de WordPress, claves API de LLMs) se almacenen de forma segura en el dispositivo, para que mi información sensible esté protegida contra accesos no autorizados.

#### Criterios de Aceptación

1. THE Sistema_Auditoría_SEO SHALL cifrar todos los tokens de OAuth, credenciales de WordPress y claves API de LLMs utilizando Android_Keystore antes de almacenarlos en la base de datos local.
2. THE Sistema_Auditoría_SEO SHALL utilizar Room como base de datos local para almacenar datos persistentes, aplicando cifrado mediante SQLCipher o EncryptedSharedPreferences para datos sensibles.
3. WHEN el usuario desinstala la aplicación, THE Sistema_Auditoría_SEO SHALL eliminar todas las credenciales almacenadas del Android_Keystore de forma automática.
4. THE Sistema_Auditoría_SEO SHALL requerir autenticación biométrica o PIN del dispositivo antes de acceder a las credenciales almacenadas cuando la aplicación se abre después de estar en segundo plano durante más de 5 minutos.
5. IF el dispositivo no dispone de hardware de seguridad (TEE/StrongBox), THEN THE Sistema_Auditoría_SEO SHALL informar al usuario del nivel de seguridad reducido y utilizar cifrado por software como alternativa.

### Requisito 21: Procesamiento en Segundo Plano y Resiliencia

**Historia de Usuario:** Como usuario, quiero que los procesos largos de análisis de IA continúen ejecutándose incluso si minimizo la aplicación, para que las auditorías completas no se interrumpan por el sistema operativo.

#### Criterios de Aceptación

1. WHEN el usuario inicia una auditoría completa del sitio o un análisis cruzado extenso, THE Sistema_Auditoría_SEO SHALL ejecutar el proceso utilizando un Foreground_Service con una notificación persistente que muestre el progreso.
2. THE Sistema_Auditoría_SEO SHALL utilizar WorkManager para programar tareas de sincronización periódica de datos de Google Search Console que se ejecuten de forma confiable incluso si la aplicación no está activa.
3. WHEN un proceso en segundo plano se interrumpe por falta de conectividad, THE Sistema_Auditoría_SEO SHALL guardar el estado del progreso y reanudar automáticamente cuando la conectividad se restablezca.
4. THE Sistema_Auditoría_SEO SHALL mostrar una notificación persistente con barra de progreso durante la ejecución de procesos largos de análisis de IA, indicando la etapa actual y el porcentaje de avance.
5. IF el sistema operativo cancela el proceso en segundo plano por restricciones de memoria, THEN THE Sistema_Auditoría_SEO SHALL notificar al usuario y ofrecer la opción de reiniciar el análisis desde el último punto de control guardado.
6. THE Sistema_Auditoría_SEO SHALL optimizar el consumo de batería durante procesos en segundo plano, limitando las operaciones de red a lotes y respetando los modos de ahorro de energía del dispositivo.

### Requisito 22: Integración con Sistema de Archivos Android (SAF)

**Historia de Usuario:** Como usuario, quiero poder seleccionar directorios de proyectos web en mi dispositivo para que la aplicación pueda leer, analizar y aplicar correcciones (Diffs) a los archivos locales de código fuente.

#### Criterios de Aceptación

1. WHEN el usuario necesita acceder a archivos del proyecto local, THE Conector_FS SHALL utilizar el Storage Access Framework (SAF) de Android para solicitar permisos de acceso al directorio del proyecto.
2. WHEN el usuario concede acceso a un directorio mediante SAF, THE Conector_FS SHALL persistir los permisos de URI para sesiones futuras sin requerir selección manual nuevamente.
3. THE Conector_FS SHALL leer archivos de código fuente (HTML, CSS, JS, PHP, JSON) del directorio seleccionado y proporcionarlos al Motor_IA para análisis.
4. WHEN el Motor_IA genera un Formato_Diff con correcciones, THE Conector_FS SHALL aplicar los cambios al archivo correspondiente en el directorio local tras la aprobación explícita del usuario.
5. IF el usuario revoca los permisos de acceso al directorio, THEN THE Conector_FS SHALL informar al usuario que el acceso fue revocado y solicitar nueva autorización antes de realizar operaciones de lectura o escritura.
6. THE Conector_FS SHALL crear una copia de respaldo del archivo original antes de aplicar cualquier modificación mediante Formato_Diff.

### Requisito 23: Conector WordPress con Cliente HTTP Nativo

**Historia de Usuario:** Como usuario, quiero que la aplicación se conecte a mi sitio WordPress mediante un cliente HTTP nativo (Ktor o Retrofit) con autenticación por Application Passwords, para que pueda gestionar temas, plugins y código de páginas desde el dispositivo móvil.

#### Criterios de Aceptación

1. THE Conector_WordPress SHALL implementar la comunicación con la API REST de WordPress utilizando un cliente HTTP nativo (Ktor o Retrofit) optimizado para Android.
2. THE Conector_WordPress SHALL autenticar las solicitudes a WordPress exclusivamente mediante Application Passwords según la especificación de la API REST de WordPress.
3. WHEN el usuario solicita la lista de temas instalados, THE Conector_WordPress SHALL recuperar y mostrar el nombre, versión, estado (activo/inactivo) y autor de cada tema.
4. WHEN el usuario solicita la lista de plugins instalados, THE Conector_WordPress SHALL recuperar y mostrar el nombre, versión, estado (activo/inactivo) y descripción de cada plugin.
5. WHEN el usuario solicita editar el código de una página, THE Conector_WordPress SHALL recuperar el contenido completo (HTML, CSS en línea, scripts) y permitir la edición mediante el Motor_IA.
6. THE Conector_WordPress SHALL implementar caché local de respuestas de la API utilizando Room para reducir el consumo de datos móviles y mejorar los tiempos de respuesta.

### Requisito 24: Integración OAuth 2.0 con Google Search Console en Android

**Historia de Usuario:** Como usuario, quiero autenticarme con Google Search Console mediante Google Sign-In nativo en Android, para que el proceso de autenticación sea seguro y familiar.

#### Criterios de Aceptación

1. THE Conector_GSC SHALL implementar la autenticación OAuth 2.0 utilizando la librería Google Sign-In nativa de Android para obtener acceso a la API de Search Console.
2. WHEN el usuario inicia el proceso de autenticación con Google, THE Conector_GSC SHALL presentar el flujo estándar de Google Sign-In con los permisos mínimos necesarios (scope: webmasters.readonly).
3. WHEN la autenticación OAuth 2.0 es exitosa, THE Conector_GSC SHALL almacenar el token de refresco en Android_Keystore cifrado y el token de acceso en memoria para la sesión activa.
4. THE Conector_GSC SHALL utilizar la API de Search Console para recuperar datos de rendimiento (clics, impresiones, CTR, posición media) y errores de indexación.
5. IF el token de acceso expira durante una operación, THEN THE Conector_GSC SHALL renovar el token de forma silenciosa utilizando el token de refresco sin interrumpir la experiencia del usuario.
6. WHEN el usuario solicita desconectar la cuenta de Google, THE Conector_GSC SHALL revocar el token OAuth, eliminar las credenciales del Android_Keystore y limpiar los datos en caché de Search Console.

### Requisito 25: Plan de Desarrollo por Fases (MVP Primero)

**Historia de Usuario:** Como desarrollador, quiero que el proyecto siga un plan de desarrollo en 4 fases con enfoque MVP primero, para que pueda entregar valor incremental y validar la arquitectura tempranamente.

#### Criterios de Aceptación

1. THE Sistema_Auditoría_SEO SHALL completar la Fase 1 (Core MCP Client + Shell básico de UI) como prerequisito antes de iniciar la implementación de las Fases 2, 3 o 4.
2. WHEN la Fase 1 se completa, THE Sistema_Auditoría_SEO SHALL proporcionar un Cliente_MCP funcional capaz de conectarse a un LLM externo y un shell de interfaz de usuario con navegación básica implementada en Jetpack Compose.
3. WHEN la Fase 2 se completa, THE Sistema_Auditoría_SEO SHALL proporcionar conectores funcionales para Google Search Console (OAuth 2.0 + lectura de datos) y WordPress (Application Passwords + lectura/escritura de páginas).
4. WHEN la Fase 3 se completa, THE Motor_IA SHALL ser capaz de ejecutar auditorías SEO completas utilizando datos reales de GSC y código fuente, generando informes diagnósticos y propuestas de código corregido.
5. WHEN la Fase 4 se completa, THE Sistema_Auditoría_SEO SHALL ofrecer el flujo de trabajo interactivo completo (descubrimiento, análisis, informe, corrección, aplicación) con transiciones fluidas entre cada etapa.
6. THE Sistema_Auditoría_SEO SHALL mantener cada fase independientemente desplegable y testeable, permitiendo pruebas de integración al finalizar cada fase sin depender de fases posteriores.

### Requisito 26: Rendimiento y Optimización Móvil

**Historia de Usuario:** Como usuario, quiero que la aplicación sea rápida, fluida y eficiente en el uso de recursos del dispositivo, para que pueda realizar auditorías SEO sin degradar el rendimiento general de mi teléfono.

#### Criterios de Aceptación

1. THE Capa_Presentation SHALL renderizar las pantallas principales en menos de 16ms por frame para mantener una tasa de 60 FPS durante la navegación y desplazamiento.
2. THE Sistema_Auditoría_SEO SHALL limitar el consumo de memoria RAM a un máximo de 256MB durante operaciones de análisis activo, liberando recursos cuando la aplicación pasa a segundo plano.
3. WHEN el Sistema_Auditoría_SEO ejecuta operaciones de red (llamadas a API de GSC, WordPress o LLMs), THE Sistema_Auditoría_SEO SHALL ejecutar todas las operaciones de red fuera del hilo principal utilizando Kotlin Coroutines con dispatchers apropiados (IO para red, Default para cómputo).
4. THE Sistema_Auditoría_SEO SHALL implementar paginación para listas de páginas, resultados de auditoría e informes diagnósticos cuando el conjunto de datos supere los 50 elementos.
5. WHEN la aplicación se inicia en frío, THE Sistema_Auditoría_SEO SHALL mostrar la pantalla principal interactiva en menos de 2 segundos en dispositivos con especificaciones mínimas (Android 8.0, 3GB RAM).
6. THE Sistema_Auditoría_SEO SHALL utilizar carga diferida (lazy loading) para imágenes, gráficos y datos secundarios que no sean visibles en el viewport inicial de la pantalla.
