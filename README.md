## Arquitectura

Para esta prueba, he utilizado la arquitectura **_Model-View-ViewModel_** (MVVM).
La capa **_Model_** (o DataModel) contiene toda la lógica y tareas para la persistencia
y obtención de la información de la App, en esta prueba, se encarga de obtener
películas y series (ya sea desde la base de datos o desde la red), almacenarlas
en cache y cuidar la integridad de los datos. Las clases más importantes de esta
capa son `MoviesRepository`, `TasksRepository`, `AppDatabase` (con sus `Dao`) y `WebService`.

El **_ViewModel_** es quien ejecuta la lógica de negocio, se encarga de obtener la
información desde la capa de persistencia (Model), la prepara para luego
exponer un flujo de datos que pueda ser escuchado y entendido por la capa vistas.
Las clases que pertenecen a esta capa son las que extienden de `ViewModel`, como
`MainViewModel`, `ItemDetailViewModel`, `OfflineSearchViewModel`, `OnlineSearchViewModel`.

Finalmente, la capa de vista (**_View_**) sólo tiene la tarea de mostrar información
en la interfaz de usuario, la cual es previamente preparada por el ViewModel.
Esta capa se "suscribe" a flujos de datos del ViewModel para reaccionar ante
cambios y mostrar la información correcta. A esta capa pertenecen todas las
clases que extienden de `Activity` y `Fragment`, como `MainActivity`,
`PaginatedItemsFragment`, `ItemDetailActivity`, `OfflineSearchActivity` y
`OnlineSearchActivity`.

## Responsabilidades de las clases:

Detallo las clases más relevantes del proyecto:

- `AppDatabase`: Abstracción alrededor de una base de datos SQLite,

- `WebService`: Encapsula todos los metodos para la comunicacion con el servicio web.

- `MoviesRepository`: Obtiene y almacena en cache la información de películas, individual o e forma paginada.

- `TvSeriesRepository`: Obtiene y almacena en cache la información de series de TV, , individual o e forma paginada.

- `MainViewModel`: Obtiene las datos de las distintas categorias y las prepara para
el UI, tambien llega el control de la paginación y restauración de estado.

- `ItemDetailViewModel`: Recolecta los datos de alguna película o serie para mostrar
al usuario, incluyendo los videos relacionados.

- `OfflineSearchViewModel`: obtiene películas y series desde la base de datos local
(usando los repositorios) basado en la busqueda realizada por el usuario, al igual que se encarga del manejo de errores.

- `OfflineSearchViewModel`: obtiene películas y series desde el servicio web
(usando los repositorios) basado en la busqueda realizada por el usuario, al igual que se encarga del manejo de errores.

- `MainActivity`: Ordena los distintos fragments que representan las categorias,

- `PaginatedItemsFragment`: Renderiza el resultado de alguna de las categorias.

- `PaginatedItemsAdapter`: Añade secuencialmente (segun la página actual) nuevo 
contenido a la lista de el películas o series.

- `OfflineSearchActivity` y `OnlineSearchActivity`: Obtienen input de usuario para
realizar la busqueda con los parametros especificados.

## Principio de Responsabilidad Única (SRP):

Este principio, llamado SRP en inglés, es el primero de los principios SOLID y pregona
que una clase sólo debe tener una única responsabilidad y una única razón para
que esta deba cambiar (basado en su responsabilidad). Al una clase tener una única
responsabilidad, es mucho más mantenible y fácil de explicar y entender, y al
entenderla mejor, reduce sustancualmente el numero de posibles bugs y efectos
colaterales que puedan ocurrir al modificar la clase. En resumen, el correcto
uso de esto reduce las probabilidades de bugs, y facilita su mantenimiento y
extensión.


## Principios de código:

- Estilo coherente siguiendo normas internacionales.
- Escrito de manera de que cualquiera pueda entenderlo, no solo quien lo escribe, debe ser claro y consiso, con buenos nombres de metodos, clases, variables, etc.
- Comentado cuidadosamente siempre que sea necesario para mejorar la mantenibilidad y el paso de información entre el equipo.
- Que siga en lo posible los principios SOLID, con una arquitectura bien definida y conocida que permita extender fácilmente.
- Testeable: modular y conciso de manera que permite escribir test unitarios para cada pequeña función preferiblemente siguiendo el proncipio de inversión de control (IoC).


## Lo que hubiese hecho con más tiempo:

- Dagger para injeccion de dependencias.
- Manejo de errores más detallado.
- Pruebas unitarias: a pesar de que no hay ninguna actualmente, el código está muy bien modularizado y pensado para prubarlo fácil y extensivamente.
- Un buen ícono para el launcher.
