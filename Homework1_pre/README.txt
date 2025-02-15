# Backend de Gestión de Incidencias y Sugerencias

Este proyecto es un backend que gestiona una base de datos de incidencias y sugerencias reportadas en distintos municipios. Implementa una estructura de datos basada en entidades JPA y está diseñado para integrarse con una aplicación más amplia.

## Tecnologías utilizadas

- **Java EE / Jakarta EE**
- **JPA (Java Persistence API)**
- **Hibernate** como implementación de JPA
- **REST API** para la comunicación con el frontend
- **JSON-B** para la serialización y deserialización de datos
- **Base de datos relacional** (PostgreSQL, MySQL u otra)

## Estructura de la Base de Datos

El proyecto maneja varias entidades principales:

### Entidades

- **Estado** (`Estado`): Representa el estado de una incidencia.
- **Incidencia** (`Incidencia`): Contiene información sobre un problema reportado, como fecha, descripción, ubicación, estado y tipo.
- **Municipio** (`Municipio`): Representa un municipio en el cual pueden existir incidencias y sugerencias.
- **Sugerencia** (`Sugerencia`): Una sugerencia de mejora o cambio dentro de un municipio.
- **Tipos de Incidencia** (`TiposIncidencia`): Clasifica las incidencias según su naturaleza.
- **Usuario** (`Usuario`): Representa a un usuario que interactúa con el sistema, incluyendo su autenticación.

### Relaciones entre entidades

- Un **Estado** tiene muchas **Incidencias**.
- Un **Municipio** tiene muchas **Incidencias** y **Sugerencias**.
- Una **Incidencia** pertenece a un **Estado**, un **Municipio** y un **Tipo de Incidencia**.
- Una **Sugerencia** pertenece a un **Municipio**.
- Un **Usuario** tiene credenciales de autenticación.

EstadoFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad Estado.
    Métodos principales:
        getAllState(): Obtiene todos los estados registrados.

IncidenciaFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad Incidencia.
    Métodos principales:
        getAllIncidencias(): Obtiene todas las incidencias.
        getIncidencia(): Obtiene una incidencia específica por su ID.
        deleteIncidencia(): Elimina una incidencia específica.
        createIncidencia(): Crea una nueva incidencia.
        updateIncidencia(): Actualiza una incidencia existente.

MunicipioFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad Municipio.
    Métodos principales:
        getAllMunicipality(): Obtiene todos los municipios registrados.

SugerenciaFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad Sugerencia.
    Métodos principales:
        getAllSugerencias(): Obtiene todas las sugerencias.
        getSugerencia(): Obtiene una sugerencia específica por su ID.
        deleteSugerencia(): Elimina una sugerencia específica.
        createSugerencia(): Crea una nueva sugerencia.
        updateSugerencia(): Actualiza una sugerencia existente.

TiposFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad TiposIncidencia.
    Métodos principales:
        getAllTypes(): Obtiene todos los tipos de incidencia registrados.

UsuarioFacadeREST
    Propósito: Gestiona las operaciones CRUD para la entidad Usuario.
    Métodos principales:
        getAllUsers(): Obtiene todos los usuarios registrados.
        updateUser(): Actualiza un usuario específico.
        createUser(): Crea un nuevo usuario.
        deleteUser(): Elimina un usuario específico.
        login(): Endpoint para que los usuarios inicien sesión y se genere un token JWT.
