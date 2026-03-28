# AREP — Taller de Seguridad Web con Autenticación Distribuida

Sistema web distribuido en tres máquinas que implementa autenticación segura mediante tokens de sesión. El proyecto incluye un micro-framework HTTP personalizado en Java puro, un servidor de autenticación Spring Boot y un proxy como punto de entrada único.

---

## Arquitectura

```
Usuario / Navegador
        │
        ▼
┌─────────────────────────┐
│  Máquina 1 — Proxy      │  Puerto 80
│  Punto de entrada único │
└────────┬────────────────┘
         │
    ┌────┴────┐
    ▼          ▼
┌──────────────────────┐   ┌──────────────────────┐
│    Máquina 2         │   │    Máquina 3          │
│  Micro HTTP Server   │──►│  Spring Boot          │
│  Puerto 35000        │   │  Puerto 5000          │
│  Archivos + API      │   │  Login · Tokens       │
└──────────────────────┘   └──────────────────────┘
```

El servidor Java (Máquina 2) actúa como proxy de autenticación: en cada petición protegida consulta al Spring Server (Máquina 3) si el token es válido antes de responder.

---

## Comenzando

Estas instrucciones te permitirán obtener una copia del proyecto funcionando en tu máquina local para desarrollo y pruebas. Consulta la sección de **Despliegue** para instrucciones sobre cómo correrlo en AWS.

### Prerrequisitos

- **Java 17** o superior
- **Maven 3.8+**
- **Git**

```bash
java -version
mvn -version
git --version
```

---

## Instalación local

### 1. Clonar el repositorio

```bash
git clone https://github.com/Yojhan-Toro/AREP_LAB08.git
cd AREP_LAB08
```

### 2. Compilar el Spring Server

```bash
cd "Spring Server"
mvn clean package -DskipTests
```

### 3. Compilar el Apache Server

```bash
cd "../Apache Server"
mvn clean package -DskipTests
```

### 4. Levantar el Spring Server

```bash
cd "Spring Server"
java -jar target/lab08-0.0.1-SNAPSHOT.jar --server.port=5000
```

### 5. Levantar el Apache Server

En otra terminal:

```bash
cd "Apache Server"
java -jar target/Lab06-0.0.1-SNAPSHOT.jar
```

### 6. Abrir en el navegador

```
http://localhost:35000
```

Credenciales de prueba:

| Usuario | Contraseña |
|---------|------------|
| admin   | admin123   |
| user    | user123    |

---

## Ejecutar las pruebas

### Pruebas unitarias

```bash
cd "Apache Server"
mvn test
```

Las pruebas verifican el registro de rutas, el parsing de query params y la respuesta de los endpoints matemáticos:

```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

### Pruebas de integración manuales

Con ambos servidores corriendo:

```bash
curl -X POST http://localhost:5000/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Respuesta esperada:

```json
{"token": "uuid-generado", "username": "admin"}
```

```bash
curl -H "Authorization: Bearer <token>" http://localhost:35000/App/pi
```

---

## Despliegue en AWS

Se asume que las tres instancias EC2 ya están creadas y tienen Java 17 instalado.

### Máquina 3 — Spring Server

```bash
java -jar lab08-0.0.1-SNAPSHOT.jar --server.port=5000 > spring.log 2>&1 &
```

### Máquina 2 — Apache Server

Antes de empaquetar, actualiza las IPs en `HttpServer.java`, `login.html` e `index.html` para que apunten a la IP real de la Máquina 3. Luego:

```bash
mvn package -DskipTests
java -jar Lab06-0.0.1-SNAPSHOT.jar > apache.log 2>&1 &
```

---
## Video demostrativo

![demo](VideoAREP.gif)

## Construido con

- **Java 17** — Lenguaje principal del micro-framework HTTP
- **Spring Boot 3** — Servidor de autenticación REST
- **Maven** — Gestión de dependencias y empaquetado
- **BCrypt** — Hash seguro de contraseñas
- **JUnit 5** — Pruebas unitarias

---

## Autor

**Yojhan Toro Rivera** — [AREP_LAB08](https://github.com/Yojhan-Toro/AREP_LAB08)

---

