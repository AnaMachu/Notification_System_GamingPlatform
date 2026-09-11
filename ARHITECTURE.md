## Por qué Java

Java es una opción adecuada para este proyecto por las siguientes características:

- **Tipado estático:** detecta muchos errores durante la compilación, aumentando la seguridad y estabilidad del sistema.
- **Programación orientada a objetos:** facilita modelar el dominio y aplicar patrones como Observer y Productor-Consumidor.
- **JVM:** permite ejecutar el mismo código en diferentes sistemas operativos sin recompilar.
- **Gestión automática de memoria:** reduce errores relacionados con la administración manual de memoria.
- **Concurrencia:** ofrece herramientas maduras para manejar múltiples eventos y usuarios simultáneamente.
- **Rendimiento:** la JVM y su compilador JIT proporcionan un rendimiento adecuado para aplicaciones backend de alta demanda.
- **Ecosistema:** cuenta con un amplio ecosistema de herramientas y frameworks para sistemas backend, además de una trayectoria comprobada en aplicaciones de gran escala.

En conjunto, Java ofrece seguridad de tipos, concurrencia, portabilidad y un ecosistema maduro características adecuadas para un sistema de notificaciones en tiempo real.
## Tecnologías usadas

- **Maven:** Gestiona dependencias y automatiza la compilación, pruebas y empaquetado de proyectos Java.

- **SLF4J:** Proporciona una API estándar para registrar logs, permitiendo cambiar la implementación de logging sin modificar el código.

- **JUnit 5:** Framework para crear y ejecutar pruebas automatizadas en Java mediante aserciones.

- **Mockito:** Permite crear mocks para aislar y probar unidades de código sin depender de sus implementaciones reales.

## Arquitectura

### Patrones de diseño usados

**Observer (publish/subscribe).** Los componentes que publican eventos (`GameEngine`, `SocialSystem`) nunca conocen a quienes reaccionan a ellos. En su lugar, cualquier interesado se **suscribe** a un tipo de evento a través de un intermediario central (el bus), y es notificado automáticamente cuando ese tipo de evento ocurre. Ni el publicador conoce a los suscriptores, ni los suscriptores conocen entre sí.

**Productor-Consumidor.** Determina como viaja un evento entre dos hilos de forma segura, sin que uno tenga que esperar al otro innecesariamente: un hilo productor deja el evento en una cola; un hilo consumidor, de forma independiente, lo saca y lo procesa.

`NotificationServiceImpl` es un observer, cumple el rol de suscriptor, y lo logra implementando la interfaz `EventListener`.

### Organización en paquetes

El código se organiza en 5 paquetes, cada uno con una única responsabilidad:

| Paquete | Responsabilidad |
|---|---|
| `model` | Define la forma de los datos: los tipos de evento, sus categorías, los payloads específicos de cada tipo, y la notificación final ya construida. |
| `bus` | Transporta los eventos entre quien los origina y quien reacciona a ellos, gestionando la suscripción, desuscripción y entrega — sin conocer el contenido ni el significado de esos eventos. |
| `channel` | Entrega la notificación final al usuario, a través de un contrato intercambiable que abstrae el medio de entrega real (consola, push, email, etc.). |
| `service` | Contiene la lógica de decisión: qué hacer con cada evento, si debe convertirse en una notificación según las preferencias del usuario, y cómo se construye el mensaje. |
| `emitters` | Representa las partes del juego que anuncian que algo ocurrió, traduciendo esos sucesos al formato de evento que el resto del sistema entiende. |

### Flujo de eventos

#### 1. `model` 
Define las estructuras principales del sistema:
- `EventType`: enum con los 7 tipos de eventos.
- `NotificationCategory`: clasifica los eventos en `GAME_EVENTS` y `SOCIAL_EVENTS`.
- `EventPayload`: interfaz sellada con un `record` específico para cada tipo de evento.
- `Notification`: representa la notificación final que será entregada.

Los `payload` son inmutables y el uso de `sealed interface` permite que Java verifique que todos los tipos de eventos sean contemplados.

#### 2. `emitters` Generación de eventos
`RandomEventGenerator` simula acciones del juego y las comunica a `GameEngine` o `SocialSystem`.

Estos componentes traducen las acciones del juego a un `GameEvent` tipado y lo publican en el `EventBus`.

#### 3. `bus` Distribución de eventos
Los listeners (`NotificationServiceImpl` y `AnalyticsListener`) se suscriben a los tipos de eventos que les interesan.

El `PerObserverQueueEventBus` utiliza una cola y un hilo independiente por suscriptor, permitiendo procesar eventos de forma asíncrona y aislada.

Los listeners pueden suscribirse y desuscribirse dinámicamente.

#### 4. `service` Consulta preferencias y entrega
`NotificationServiceImpl`:
1. Traduce el `EventType` a una `NotificationCategory`.
2. Consulta las preferencias del usuario.
3. Si las notificaciones están habilitadas, construye el mensaje correspondiente.
4. Crea la `Notification` y la entrega mediante `NotificationChannel`.

Si la entrega falla, el error se registra sin interrumpir el procesamiento de otros eventos.

Actualmente `NotificationServiceImpl` utiliza un solo `NotificationChannel`, aunque el diseño permite extenderlo fácilmente para soportar múltiples canales.

### Principios SOLID 

**Single Responsibility Principle (SRP)** — cada paquete, y cada clase dentro de él, tiene un único motivo para cambiar. `UserPreferencesService` solo cambiaría si cambia cómo se almacenan o consultan preferencias; `NotificationServiceImpl` solo si cambia la lógica de qué hacer con un evento; `ConsoleNotificationChannel` solo si cambia cómo se entrega una notificación. Ningún cambio en una de estas razones obliga a tocar las demás clases — por ejemplo, cambiar el formato del mensaje de una notificación (una responsabilidad de `service`) nunca requiere modificar `bus` ni `channel`.<br>

**Open/Closed Principle (OCP)** — el sistema está abierto a extensión, mas no requiere modificar código ya existente y probado para incorporar comportamiento nuevo. Agregar un `EventType` nuevo con su payload correspondiente no modifica `EventBus`, `GameEngine` ni `AnalyticsListener`. Agregar un canal de entrega nuevo se logra creando una clase nueva que implemente `NotificationChannel`, sin tocar `NotificationServiceImpl`. Agregar un listener nuevo no requiere modificar ni `EventBus` ni los emisores existentes.<br>

**Liskov Substitution Principle (LSP)** — cualquier implementación de una interfaz debe poder sustituir a otra sin alterar la corrección del programa que las usa. `RandomEventSystem.main()` puede intercambiar una implementación por otra cambiando una sola línea, sin que `GameEngine`, `SocialSystem`, `NotificationServiceImpl` ni `AnalyticsListener` necesiten ningún ajuste mientras la implementaciones cumplan.<br>

**Interface Segregation Principle (ISP)** — ningún cliente debería verse forzado a depender de métodos que no usa. `EventListener` expone un único método (`onEvent`); `NotificationChannel` expone un único método (`send`). Esto evita que una implementación simple, tenga que implementar métodos irrelevantes para su propósito a diferencia de lo que ocurriría si, por ejemplo, `EventListener` obligara a implementar también lógica de entrega o de preferencias, que no le corresponden a todo suscriptor.<br>

**Dependency Inversion Principle (DIP)** — los módulos de alto nivel no deben depender de módulos de bajo nivel; ambos deben depender de abstracciones. `GameEngine` y `SocialSystem` no conocen ninguna clase concreta de `service` y dependen únicamente de la interfaz `EventBus`. `NotificationServiceImpl` no conoce `ConsoleNotificationChannel` directamente, solo depende de la interfaz `NotificationChannel`, recibida por inyección de dependencias en su constructor. Esta inversión es lo que permite que `RandomEventSystem.main()`  decida qué implementación usar en cada caso, sin que el resto del sistema necesite saberlo.<br>

## Cuestionamientos

El diseño del bus pasó por tres iteraciones, cada una resolviendo el problema que dejaba pendiente la anterior.<br>

**Idea inicial → SimpleEventBus:** un hilo, sin concurrencia, síncrono. Si un evento se atora, deja pendientes todos los demás y no solo los del propio listener atorado, sino también los del resto de los observers, porque todos comparten el mismo hilo y la misma llamada de método directa. El sistema completo queda congelado hasta que ese único listener responda.<br>

**Segunda idea → QueuedEventBus:** se introduce asincronía mediante una cola compartida y un hilo consumidor dedicado, para que el hilo productor main nunca tenga que esperar a que un evento se procese. Esto desacopla al productor del tiempo de procesamiento pero como ambos listeners (`NotificationServiceImpl`, `AnalyticsListener`) siguen compartiendo la misma cola y el mismo único hilo consumidor, si uno de los dos se atora procesando un evento, el otro listener tampoco recibe sus eventos. Se ganó desacoplar al productor, pero no se logró aislamiento entre observers.<br>

**Implementación final → PerObserverQueueEventBus:** cada observer tiene su propia cola y su propio hilo consumidor dedicado. Un listener atorado bloquea únicamente su propio hilo y el resto de los observers, con su infraestructura independiente, siguen procesando con normalidad, sin enterarse del problema.<br> 

Este aislamiento se verificó experimentalmente con una prueba de tiempos: un listener que se demora artificialmente 3 segundos por evento no impide que otro listener, suscrito al mismo tipo de evento, procese los suyos en milisegundos.(contribución de IA)

### Ventajas de la implementación final

- **Aislamiento real entre observers** : la falla o lentitud de un suscriptor no se propaga a los demás, a diferencia de las dos iteraciones anteriores.<br>
- **El productor nunca espera**: `publish()` retorna de inmediato independientemente de qué tan ocupado esté cualquier observer, heredando la ventaja que ya había logrado `QueuedEventBus` frente a la versión síncrona.<br>
- **Backpressure acotado por observer**: si un observer específico no da abasto, solo sus propios eventos se ven afectados; no compromete la entrega a los demás observers, cuyas colas están intactas.<br>
- **El contrato `EventListener` no cambia**: ningún suscriptor necesita saber que existe una cola o un hilo dedicado detrás; la complejidad de concurrencia queda encapsulada por completo dentro del bus.<br>

### Consideraciones

- **La cola se puede saturar si el consumidor no procesa tan rápido como se publica.** Cada cola tiene capacidad fija y si el observer correspondiente no da abasto, los eventos nuevos para ese observer específico dejan de caber.<br>

- **Qué pasa con los eventos que no pudieron entrar a la cola.** `BlockingQueue` ofrece varias estrategias posibles: esperar hasta que haya espacio (reintroduce backpressure real, a costa de volver a bloquear al productor), rechazar de inmediato y descartar (la política usada actualmente, registrando una advertencia), o desviarlos a un mecanismo secundario de reintento para revisión posterior. La decisión correcta depende de qué tan tolerable es perder ese evento específico porque no es la misma respuesta para una notificación al jugador que para un evento de analítica.<br>

- **Muchos observers implican muchos hilos permanentes.** Cada suscripción nueva crea un hilo dedicado que vive mientras el observer esté suscrito a algo, consumiendo memoria y un recurso del sistema operativo incluso en los momentos en que no tiene nada que procesar. Con pocos observers (como en este proyecto) el costo es insignificante; a mayor escala, sería necesario un punto intermedio (un pool de hilos compartido, en vez de un hilo dedicado por cada observer) para no pagar ese costo de forma descontrolada.<br>

- **Concurrencia: manejo y posibilidad de errores.** El mapa de suscriptores y consumidores del bus se protege con `ConcurrentHashMap`/`CopyOnWriteArrayList`, evitando corrupción si varios hilos lo tocan a la vez. Dentro del bucle de cada hilo consumidor, la llamada al listener está envuelta en un `try/catch`, un listener que lanza una excepción no debe matar permanentemente su propio hilo consumidor. <br>

## Implementación de escalabilidad

Para escalar de un solo proceso Java a un sistema con muchos usuarios concurrentes, la pieza que cambiaría es la implementación detrás de la interfaz `EventBus`, reemplazando la cola en memoria por un servicio de mensajería distribuido, como Amazon SNS combinado con Amazon SQS.<br>

Este patrón conocido como SNS fan-out to SQS es, a nivel de infraestructura administrada, el equivalente del diseño ya construido. SNS cumple el rol del Observer, y cada suscriptor tiene su propia cola de SQS sería el mismo principio de aislamiento por observer que ya se implementó a mano con `PerObserverQueueEventBus`, ahora administrado y distribuido entre múltiples máquinas en vez de vivir dentro de un único proceso Java.<br>

## Pruebas
El proyecto tiene 9 pruebas con JUnit 5 + Mockito, repartidas en dos archivos según qué verifican.

### `RandomEventSystemTest` (5 pruebas)  comportamiento de `NotificationServiceImpl` y `UserPreferencesService`

- **`preferenciaPorDefectoEsHabilitada`** — si un usuario nunca configuró ninguna preferencia, `isEnabled()` responde `true` por defecto, garantizando que nadie se queda sin notificaciones solo por no haber tocado su configuración.
- **`preferenciaExplicitaSeRespeta`** — al deshabilitar explícitamente una categoría para un usuario, esa categoría responde `false`, mientras que la otra categoría (no tocada) sigue en `true` y confirma que las preferencias son independientes por categoría, no un interruptor global.
- **`dispatchEnviaNotificacionConMensajeCorrecto`** — dispara un evento `LEVEL_UP` y usa `ArgumentCaptor` de Mockito para capturar el `Notification` resultante, verificando el mensaje de texto exacto, el destinatario y la categoría.
- **`dispatchNoEnviaSiCategoriaDesactivada`** — con la categoría social deshabilitada para un usuario, dispara un evento `FRIEND_REQUEST` para ese mismo usuario y verifica con `verify(channel, never()).send(any())` que el canal nunca fue invocado.
- **`todosLosTiposDePayloadGeneranMensajeSinExcepcion`** — dispara los 5 tipos de evento restantes y verifica con `assertDoesNotThrow` que ninguno lanza excepción al construir su mensaje, confirmando que el `switch` exhaustivo sobre `EventPayload` maneja los 7 casos correctamente.

### `PerObserverQueueEventBusTest` (4 pruebas)

- **`busEntregaSoloAListenersSuscritosAEseTipo`** — suscribe dos listeners a dos tipos de evento distintos, publica un evento de un solo tipo, y verifica que solo el listener correspondiente fue invocado, prueba el enrutamiento correcto por tipo.
- **`unsubscribeDetieneLaEntregaDeEventosFuturos`** — publica un evento, desuscribe al listener, publica un segundo evento, y verifica que el listener fue llamado exactamente una vez.
- **`analyticsListenerCuentaCorrectamentePorTipo`** — publica tres eventos de dos tipos distintos, llama `shutdown()` para garantizar que el hilo consumidor terminó de procesar todo, y verifica los tres conteos exactos, incluyendo que un tipo nunca publicado quede en 0.
- **`analyticsListenerDejaDeContarTrasDesuscribirse`** — publica, desuscribe, publica otra vez, y confirma que el conteo se detuvo exactamente donde debía.

## Uso de IA

**Documentación, Javadoc** La IA explicó qué es Javadoc: un sistema de comentarios especiales (`/** */`, con etiquetas como `@param`, `@return`, `{@link}`) que Java lee para generar documentación en HTML navegable. En index.html en la carpeta docs/ se encuentra documentación navegable

**`AnalyticsListener`.** La idea de agregar un segundo observador independiente, solo para demostrar con código real que el bus permite sumar suscriptores nuevos fue mía pero la IA se encargó de la implementación concreta de la clase.

**Crear y correr pruebas.** La IA escribió la suite de pruebas con JUnit 5 y Mockito (`RandomEventSystemTest`, `PerObserverQueueEventBusTest`), incluyendo cómo sincronizar aserciones contra código asíncrono usando `shutdown()`/`unsubscribe()` en vez de `Thread.sleep()`. También corrió, a mi pedido, una prueba manual con un listener lento (`SLOW`) y uno rápido (`FAST`) para demostrar en la práctica que un observer atorado no bloquea a otro en `PerObserverQueueEventBus`la evidencia de tiempos que confirmó el aislamiento antes de darlo por hecho solo en teoría.

**Auditoría de código y troubleshooting.** Este fue uno de los usos más recurrentes ya que en varios momentos le pedí a la IA que revisara mi propio diseño buscando riesgos que yo no había considerado, en vez de solo escribir código nuevo. Le pregunté explícitamente qué errores de concurrencia tenía el sistema al volverse asíncrono, la IA auditó cada pieza de estado compartido del proyecto (mapas de suscriptores, preferencias, contadores) confirmando cuáles estaban protegidas y por qué, y en ese proceso identificó un riesgo latente en el diseño del bus, no presente en el uso actual: si un listener llamara `unsubscribe()` sobre sí mismo desde dentro de su propio `onEvent()`, se produciría un *deadlock* , el mecanismo de apagado ordenado espera a que el hilo termine, pero sería ese mismo hilo el que estaría bloqueado pidiendo esa espera. Ninguno de los listeners del proyecto hace esto, pero para confirmar que el riesgo era real y no solo hipotético, la IA escribió un caso de prueba aparte que reproduce deliberadamente ese escenario y lo ejecutó, confirmando con el proceso realmente colgado (forzado a matar con timeout) que el deadlock ocurre tal como se predijo.

El mismo enfoque se usó para resolver problemas concretos del entorno a medida que aparecían, siempre empezando por diagnosticar la causa exacta antes de proponer una corrección.

**Implementación de métodos.** La IA implementó las  versiones del `EventBus` (`SimpleEventBus`, `QueuedEventBus`, `PerObserverQueueEventBus`), los payloads tipados con `sealed interface` y `record`, las estructuras de concurrencia (`ConcurrentHashMap`, `CopyOnWriteArrayList`, `BlockingQueue`), y la lógica de apagado ordenado (`shutdown()`).

**Sintaxis y recursos de Java que no conocía.** A lo largo del proyecto, la IA me explicó características del lenguaje que no había usado antes `sealed interface` y `record`, el `switch` exhaustivo sobre tipos sellados, `ConcurrentHashMap`/`CopyOnWriteArrayList`/`BlockingQueue` de `java.util.concurrent`, y el uso de SLF4J como fachada de logging y siempre explicando el porqué de cada uno antes de aplicarlo, no solo entregando el código.
- El **reemplazo de `System.out.println` por logging real** con SLF4J, con niveles diferenciados (`info`/`debug`/`error`) en cada clase.