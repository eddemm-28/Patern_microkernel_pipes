# Banco de Preguntas Saber PRO — Microkernel + Tuberías y Filtros

Taller 5 del **Laboratorio de Ingeniería de Software II** — Universidad del Cauca, 2026.2
**Grupo 3:** Johan López y Eddy Sánchez.

Implementación del proyecto de clase (Banco de Preguntas Saber PRO) con una arquitectura
**Microkernel** (plugins cargados dinámicamente por **reflexión**) combinada con el patrón
**Tuberías y Filtros** para la validación de las preguntas. Aplicación de escritorio en **Java + Swing**.

## Requisitos

- JDK 17 o superior
- Maven 3.8 o superior (o abrir el proyecto como proyecto Maven en NetBeans, IntelliJ IDEA, Eclipse o VS Code)

## Cómo ejecutar

```bash
mvn clean test     # compila y ejecuta las 98 pruebas unitarias (JUnit 5)
mvn exec:java      # abre la aplicación de escritorio (Swing)
mvn package        # genera el jar ejecutable en target/
java -jar target/taller05-microkernel-1.0.0.jar            # interfaz gráfica
java -jar target/taller05-microkernel-1.0.0.jar --consola   # demostración por consola
```

En el IDE, la clase principal es `co.edu.unicauca.microkernel.app.Main`.

## Estructura

```
src/main/java/co/edu/unicauca/microkernel/
├── app/           Main, ConsoleDemo
├── presentation/  Interfaz Swing (MainWindow, MainPanel, QuestionFormPanel,
│                  QuestionBankPanel, PluginPanel)
├── common/        Contrato (QuestionPlugin) y entidades (Question, QuestionRequest)
├── core/          Núcleo: QuestionMicrokernel, PluginLoader, PluginDescriptor
├── pipeline/      Tuberías y filtros (QuestionPipeline + 6 filtros)
└── plugins/       MultipleChoice, Case y Multimedia
src/main/resources/plugins.properties   Registro de plugins
src/test/java/...                       15 clases de prueba (98 pruebas)
```

## Microkernel

El núcleo almacena el banco en un `Map<String, Question>`, registra los plugins listados en
`plugins.properties`, los instancia por reflexión
(`Class.forName(clase).getDeclaredConstructor().newInstance()`), los ejecuta según el tipo de
pregunta y administra su ciclo de vida (activar, desactivar, descargar, recargar).
El núcleo solo conoce la interfaz `QuestionPlugin`.

### Agregar un plugin nuevo (sin modificar el núcleo)

1. Crear una clase que implemente `QuestionPlugin` (o que extienda `AbstractPipelineQuestionPlugin`).
2. Registrarla en `src/main/resources/plugins.properties`:

```properties
plugin.mi_tipo = co.edu.unicauca.microkernel.plugins.MiNuevoPlugin
```

También puede registrarse en caliente desde la pestaña **Plugins del núcleo** de la aplicación.

## Tuberías y Filtros

Toda pregunta atraviesa el pipeline antes de entrar al banco:

`ContentValidationFilter → OptionsValidationFilter → ClassificationFilter → CorrectAnswerValidationFilter`

Los plugins de caso y multimedia reutilizan esos filtros y agregan
`CaseContextValidationFilter` y `MediaResourceValidationFilter`.
Si un filtro rechaza la solicitud, la tubería se detiene e informa el filtro y el motivo.
