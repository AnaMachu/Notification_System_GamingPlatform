# Notification_System_GamingPlatform

## Descripción
Sistema de notificaciones en tiempo real para una plataforma de gaming. 
Los eventos existentes se dividen en dos categorías:
#### Eventos del Juego
  - Level Up <br>
  - Item Acquired <br>
  - Challenge Completed <br>
  - PvP:Attacked or defeated<br>
#### Eventos Sociales  
  - Friend Request <br>
  - Friend accepted <br>
  - New Follower <br>

Según su tipo, los eventos son emitidos por diferentes componentes de la plataforma. Al recibir un evento, el sistema de notificaciones consulta las preferencias del usuario para verificar que tenga habilitada la recepción de notificaciones correspondientes a ese tipo de evento.

Este sistema está construido sobre una arquitectura orientada a eventos (event-driven), utilizando un Event Bus basado en el patrón Observer. Los eventos se publican en un bus central al que diferentes suscriptores reaccionan de forma independiente.
## Estructura de carpetas
````text
notification-system/
├── .gitignore
├── ARCHITECTURE.md
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── notifications/
    │   │           ├── RandomEventSystem.java       
    │   │           ├── bus/
    │   │           │   ├── EventBus.java
    │   │           │   ├── EventListener.java
    │   │           │   ├── GameEvent.java
    │   │           │   └── SimpleEventBus.java
    │   │           ├── channel/
    │   │           │   ├── ConsoleNotificationChannel.java
    │   │           │   └── NotificationChannel.java
    │   │           ├── emitters/
    │   │           │   ├── GameEngine.java
    │   │           │   ├── RandomEventGenerator.java
    │   │           │   └── SocialSystem.java
    │   │           ├── model/
    │   │           │   ├── ChallengeCompletedPayload.java
    │   │           │   ├── EventPayload.java
    │   │           │   ├── EventType.java
    │   │           │   ├── FriendAcceptedPayload.java
    │   │           │   ├── FriendRequestPayload.java
    │   │           │   ├── ItemAcquiredPayload.java
    │   │           │   ├── LevelUpPayload.java
    │   │           │   ├── NewFollowerPayload.java
    │   │           │   ├── Notification.java
    │   │           │   ├── NotificationCategory.java
    │   │           │   └── PvpDefeatedPayload.java
    │   │           └── service/
    │   │               ├── AnalyticsListener.java
    │   │               ├── NotificationServiceImpl.java
    │   │               └── UserPreferencesService.java
    │   └── resources/
    │       └── simplelogger.properties
    └── test/
        └── java/
            └── com/
                └── notifications/
                    └── RandomEventSystemTest.java
                    └──  PerObserverQueueEventBusTest.java

````

## Arquitectura
<img width="800" height="322" alt="image" src="https://github.com/user-attachments/assets/d1929ca8-0b89-4fdf-87cd-799ba1529cc5" />

## Flujo de eventos
Generación del evento: RandomEventGenerator simula que algo pasó en el juego, llamando a un método público de GameEngine o SocialSystem como lo haría cualquier parte real del juego.<br>

Traducción a evento: GameEngine/SocialSystem construyen un GameEvent tipado con su EventType y su EventPayload correspondiente y lo publican al EventBus.<br>

Reparto: con la implementación activa (PerObserverQueueEventBus), el hilo productor deja el evento en la cola dedicada de cada suscriptor interesado en ese tipo, y retorna de inmediato. Cada suscriptor tiene su propio hilo consumidor procesando su propia cola, de forma aisladas. Los suscriptores también pueden desuscribirse dinámicamente.<br>

Decisión: NotificationServiceImpl consulta UserPreferencesService y si la categoría del usuario está deshabilitada, el flujo termina ahí. <br>

Entrega: si está habilitada, se arma el Notification final con el mensaje de texto ya construido y se entrega a través de NotificationChannel el cual para este proyecto es por consola.<br>

## Instalaciones necesarias
- JDK 21 o versiones superiores<br>
- Maven<br>
- Extension Pack for Java (si se corre en VSC)<br>

## Ejecución del proyecto

- Desde VS Code: 
abrir src/main/java/com/notifications/RandomEventSystem.java y usar el enlace Run que aparece sobre public static void main().

- Desde terminal con Maven:

mvn compile exec:java -Dexec.mainClass="com.notifications.RandomEventSystem"

La simulación genera 15 eventos aleatorios con una pequeña pausa entre cada uno, imprimiendo cada notificación enviada u omitida por preferencia del usuario.

## Ejecución de las pruebas

Desde terminal, con Maven:

mvn test

Se ejecutan 9 pruebas con JUnit 5 + Mockito, repartidas en dos archivos:
- **RandomEventSystemTest** (5 pruebas) — preferencias por defecto y explícitas, construcción de mensajes por tipo de payload, filtrado por categoría deshabilitada.
- **PerObserverQueueEventBusTest** (4 pruebas) — el contrato del bus contra la implementación activa: entrega selectiva de eventos por tipo, unsubscribe, y conteo de AnalyticsListener.


## Proceso de desarrollo 
El proyecto se construyó por capas, cada etapa se apoya en la anterior sin necesitar rehacer lo ya construido.

###### Día 1 Diseño base y modelo de dominio

Análisis del reto y elección de Java como lenguaje, lo que implicó repasar principios de Java y POO. 
Construcción de primeras clases: Notification, EventType, NotificationCategory, UserPreferencesService, NotificationChannel, GameEngine/SocialSystem con llamadas directas al servicio de notificaciones (sin la implementación del bus todavía).

###### Día 2 Desacoplamiento con un Event Bus real

Introducción de GameEvent, EventListener, EventBus y SimpleEventBus. NotificationServiceImpl pasa de ser llamado directamente a ser un EventListener suscrito al bus. Se agrega AnalyticsListener como segundo suscriptor independiente (solamente por demostración concreta de que el bus permite observadores nuevos sin tocar el resto del sistema). Se agregó RandomEventGenerator para simular eventos en tiempo real sin datos hardcodeados, y la capacidad de unsubscribe.

###### Día 3 Pruebas y estructura de proyecto con Maven
Logging real con SLF4J en vez de System.out.println, con niveles diferenciados (info/debug/error).
Pruebas unitarias con JUnit 5 + Mockito.
Payloads tipados: Uso de sealed interface EventPayload con un record por tipo de evento.
Javadoc en interfaces y clases para documentación en el código.
Separación del código en paquetes por responsabilidad (model, bus, channel, service, emitters), siguiendo la convención estándar src/main/java / src/test/java. Creación del pom.xml con las dependencias reales (SLF4J, JUnit 5, Mockito). Verificación de que el proyecto compila y las 9 pruebas siguen pasando igual tras la reorganización.

###### Día 4 Concurrencia y aislamiento
Implementación de QueuedEventBus que era asíncrona, cola compartida con un solo hilo consumidor y análisis de sus ventajas y desventajas frente al bus síncrono original. Identificación de que una cola compartida no aísla a los observers entre sí. 
Implementación de PerObserverQueueEventBus, con cola e hilo dedicados por cada observer, logrando aislamiento real
 Auditoría de riesgos de concurrencia en el nuevo diseño. Swap de SimpleEventBus a PerObserverQueueEventBus como implementación por defecto. Actualización de las pruebas para cubrir la implementación activa.

###### Día 5 Documentación final
Consolidé documentación recopilada durante el proyecto para generar este README y ARCHITECTURE.md, incluyendo decisiones de diseño, principios SOLID aplicados, análisis comparativo de las tres implementaciones del bus, y una propuesta de escalamiento con Amazon SNS + SQS.

## Documentación adicional 
Puede consultar [ARCHITECTURE.md](ARCHITECTURE.md) para  mayor información sobre decisiones de diseño y su justificación, errores contemplados y su prevención, concurrencia, puntos de extensión, mantenibilidad y escalabilidad y limitaciones conocidas.
