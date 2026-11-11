# Ximilar — prendas similares

Integración con la API de Ximilar para buscar prendas parecidas a partir de las imágenes
del catálogo.

## Requisitos

- Cuenta de Ximilar con una colección de **product similarity**.
- Token de API y id de colección.

## Variables de entorno

Crear un archivo `.env` en `tienda/ximilar-product-similarity/`:

```properties
XIMILAR_API_TOKEN=""
XIMILAR_COLLECTION_ID=""
PORT=3000
```

El `.env` está ignorado por git. No commitear tokens.

## Instalación y ejecución

```bash
cd tienda/ximilar-product-similarity
npm install
node server.js
```

## Cargar imágenes a la colección

```bash
node scripts/insert-product.js
```

El script sube las imágenes del catálogo a la colección configurada.

## Buscar similares

```bash
node scripts/test-product-similarity.js
```

Dentro del script se declara `imageUrl` con la imagen de referencia. El resultado se usa
como prueba de la integración y como evidencia.

## Integración con el backend

El backend expone `GET /api/productos/{id}/similares` a través de `XimilarFacade`:

- Sube las imágenes del catálogo a la colección.
- Busca prendas similares.
- Filtra para devolver productos de **otras tiendas**.
- Usa timeout configurado.
- Tiene un perfil mock con respuestas fijas para tests y demo sin red.

## Límites del plan gratuito

- 1.000 créditos por mes.
- Subir una imagen cuesta **0,25 créditos**.
- Etiquetar una foto cuesta **15 créditos**.
- En esta etapa **no se usa tagging**.
- Cuando se agotan los créditos hay que crear otra cuenta y reemplazar el token y el id de
  colección.

## Evidencias

- Respuesta real de búsqueda de similares guardada.
- Captura de la colección cargada.
- Captura del endpoint `GET /api/productos/{id}/similares`.
- Caso de prueba con el perfil mock.
