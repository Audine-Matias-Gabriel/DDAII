-- Esquema inicial Tienda (creado si las tablas no existen; JPA con ddl-auto=update las gestiona)
CREATE TABLE IF NOT EXISTS tienda (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    url_base VARCHAR(500) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    premium BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS sesion_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expira_en TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS favorito (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES producto(id) ON DELETE CASCADE,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_favorito_usuario_producto UNIQUE (usuario_id, producto_id)
);

CREATE TABLE IF NOT EXISTS producto (
    id BIGSERIAL PRIMARY KEY,
    id_externo VARCHAR(200) NOT NULL,
    tienda_id BIGINT NOT NULL REFERENCES tienda(id),
    nombre VARCHAR(200) NOT NULL,
    descripcion VARCHAR(1000),
    precio DOUBLE PRECISION NOT NULL,
    categoria VARCHAR(50),
    genero VARCHAR(50),
    marca VARCHAR(100),
    url_original VARCHAR(500),
    imagen_url VARCHAR(500),
    disponible BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_producto_tienda_id_externo UNIQUE (tienda_id, id_externo)
);

CREATE TABLE IF NOT EXISTS suscripcion (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    estado VARCHAR(50) NOT NULL,
    id_pago VARCHAR(200),
    fecha TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Talles disponibles (uno por fila)
CREATE TABLE IF NOT EXISTS talle_disponible (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES producto(id) ON DELETE CASCADE,
    talle VARCHAR(10) NOT NULL
);

-- Resultado asincrónico de recomendaciones
CREATE TABLE IF NOT EXISTS recomendacion (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id VARCHAR(100) NOT NULL UNIQUE,
    producto_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS recomendacion_producto (
    recomendacion_id BIGINT NOT NULL REFERENCES recomendacion(id) ON DELETE CASCADE,
    producto_recomendado_id BIGINT NOT NULL
);
