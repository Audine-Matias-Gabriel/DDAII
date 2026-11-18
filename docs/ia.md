# Componente de IA: recomendaciones por contenido

Requisito de la consigna: **un componente de IA consumido por REST o por cola**. Lo cumplimos con un
recomendador por contenido que se usa de las dos formas (P4).

| Forma | Endpoint | Dónde se ve |
|---|---|---|
| REST (sincrónico) | `GET /api/recomendaciones?productoId=&limite=&estrategia=` | Bloque "Te puede gustar" del detalle |
| Cola (asincrónico) | `POST /api/recomendaciones/solicitudes` → 202 y publica `RecomendacionSolicitada`; el consumidor calcula y guarda; `GET /api/recomendaciones/solicitudes/{id}` devuelve el resultado | Panel de RabbitMQ (`recomendacion.solicitada.queue`) |

Código: `tienda/backend/src/main/java/com/ddaii/services/RecomendacionService.java`.

## Qué modelo se usa y por qué

Un **modelo basado en contenido con puntaje ponderado** (*content-based filtering*): se compara la
prenda de entrada con cada prenda disponible del catálogo según sus atributos y se ordena por puntaje.

**Por qué este modelo y no otro:**
- No hay historial de usuarios (compras, clics), así que no se puede usar filtrado colaborativo.
- Los atributos que normaliza el scraper (categoría, género, marca, precio) son justamente los que
  usa una persona para decir "esto se parece a aquello".
- Es explicable: cada recomendación trae su `puntaje` y se puede decir por qué apareció.
- No necesita entrenamiento ni infraestructura, y responde en milisegundos con ~540 productos.
- El parecido **visual** lo resuelve Ximilar en "Prendas similares en otras tiendas"; este componente
  cubre el parecido por atributos y el armado de conjuntos.

### Puntaje (0 a 1)

| Atributo | Estrategia `contenido` | Estrategia `complementaria` |
|---|---|---|
| Categoría | misma: **0,4** | tiene que ser una que combine (no suma) |
| Género | mismo: **0,2** · uno es Unisex: **0,1** · otro: **se descarta** | mismo: **0,5** · Unisex: **0,25** · otro: **se descarta** |
| Marca | misma: **0,2** | misma: **0,3** |
| Precio | hasta 30 % de diferencia: **0,2** · hasta 60 %: **0,1** | igual |
| Disponibilidad | sin stock vale 0: **no se recomienda** | igual |

Reglas comunes: nunca se recomienda la misma prenda ni variantes repetidas (mismo nombre en otro color),
no se recomiendan prendas con puntaje 0, y los empates se ordenan por el precio más cercano.

### Estrategias (patrón Strategy + Factory)

- `EstrategiaRecomendacion`: interfaz con `recomendar(entrada, candidatos, limite)`.
- `RecomendacionPorContenido` (por defecto): las prendas más parecidas.
- `RecomendacionComplementaria`: arma un **conjunto** con la mejor prenda de cada categoría que combina,
  después la segunda de cada una, y así:

  | Entrada | Combina con |
  |---|---|
  | REMERA | PANTALON y CALZADO |
  | PANTALON | REMERA y CALZADO |
  | CALZADO | REMERA y PANTALON |
  | VESTIDO | CALZADO |

- `RecomendacionFactory.crear("contenido" | "complementaria")` elige la estrategia; otro valor responde 400.

## Cómo se valida

1. **Tests unitarios con resultado esperado** (`RecomendacionServiceTest`, 16 casos, sin base de datos):
   puntajes por atributo, Unisex, sin stock, desempate por precio, conjunto complementario, vestido con
   calzado, géneros que no combinan, variantes repetidas, factory y límites.
2. **Tabla de validación con 10 casos reales** del catálogo scrapeado (Sportline y Grid). Para cada
   caso se escribió antes **qué se espera** y una regla automática que decide si pasa:
   - `contenido`: los 4 resultados son de la **misma categoría** y de un **género compatible**.
   - `complementaria`: trae **todas las categorías que combinan** y ninguna prenda de la misma categoría
     ni de un género que no combina.

   Se corre con `node docs/evidencias/validar-recomendaciones.js docs/evidencias/ia-validacion.json filas.md`
   (con el backend levantado). El resultado completo queda en `docs/evidencias/ia-validacion.json`.

### Iteración 1: 8 de 10

| Caso | Qué falló | Corrección |
|---|---|---|
| 4. Calza adidas Mujer | Recomendaba 2 shorts adidas **de hombre**: misma marca + categoría + precio sumaban 0,8, más que otras prendas de mujer | El género tiene que combinar (mismo o Unisex); si no, se descarta |
| 5. Short adidas Hombre | Recomendaba una calza adidas **de mujer** (mismo motivo) | Ídem |
| 2 y 4 | Aparecía **dos veces la misma prenda** (otro color, otro id) y hasta la misma que la de entrada | Se descartan nombres repetidos y el de la entrada |

Cada error quedó como test (`contenidoNoMezclaGenerosQueNoCombinan`, `noRepiteVariantesConElMismoNombreNiLaPrendaDeEntrada`).

### Iteración 2 (actual): 10 de 10

Fecha: 2026-11-18. `limite=4`. Formato del obtenido: nombre (categoría, género, precio, tienda, puntaje).

| # | Producto de entrada | Estrategia | Resultado esperado | Resultado obtenido | ¿Pasa? |
|---|---|---|---|---|---|
| 1 | Remera Puma Essential Script Mujer — REMERA, Mujer, Puma, $49999 (Sportline) | `contenido` | Remeras de mujer (o unisex), Puma o precio cercano | Remera adidas Adizero Essentials Mujer (REMERA, Mujer, $59999, Sportline, 0.8)<br>REMERA NIKE SPORTSWEAR ESSENTIALS MUJER (REMERA, Mujer, $39999, Grid, 0.8)<br>REMERA JORDAN MVP MUJER (REMERA, Mujer, $39999, Grid, 0.8)<br>REMERA NBA CHICAGO BULLS NIÑOS (REMERA, Unisex, $39999, Grid, 0.7) | ✅ misma categoría y género compatible; dif. de precio media 20 % |
| 2 | REMERA NBA CHICAGO BULLS HOMBRE — REMERA, Hombre, NBA, $24999 (Grid) | `contenido` | Remeras de hombre, NBA, precio cercano (~$25.000) | REMERA NBA LOS ANGELES LAKERS HOMBRE (REMERA, Hombre, $24999, Grid, 1)<br>REMERA NBA DALLAS MAVERICKS HOMBRE (REMERA, Hombre, $24999, Grid, 1)<br>REMERA NBA BOSTON CELTICS HOMBRE (REMERA, Hombre, $19999, Grid, 1)<br>REMERA NBA MIAMI HEAT HOMBRE (REMERA, Hombre, $19999, Grid, 1) | ✅ misma categoría y género compatible; dif. de precio media 10 % |
| 3 | Camiseta De Fútbol adidas Titular Boca Juniors 26 Hombre — REMERA, Hombre, ADIDAS, $159999 (Sportline) | `contenido` | Remeras/camisetas de hombre, adidas, precio alto | Camiseta De Fútbol adidas Afa X Messi 2026 Hombre (REMERA, Hombre, $159999, Sportline, 1)<br>Camiseta De Fútbol adidas Titular Boca Juniors 26 Niño (REMERA, Unisex, $119999, Sportline, 0.9)<br>Camiseta De Fútbol adidas Originals Suplente River Plate 26 Hombre (REMERA, Hombre, $159999, Sportline, 0.8)<br>Camiseta De Fútbol adidas Originals Suplente Boca Juniors 26 Hombre (REMERA, Hombre, $159999, Sportline, 0.8) | ✅ misma categoría y género compatible; dif. de precio media 6 % |
| 4 | Calza adidas Optime Essential Mujer — PANTALON, Mujer, ADIDAS, $59999 (Sportline) | `contenido` | Pantalones/calzas de mujer, adidas | Calza adidas Essentials Logo Mujer (PANTALON, Mujer, $54999, Sportline, 1)<br>Pantalón adidas Dayready Mujer (PANTALON, Mujer, $79999, Sportline, 0.9)<br>Calza Atomik Manifest Biker Mujer (PANTALON, Mujer, $44900, Sportline, 0.8)<br>Pantalón adidas Seasonal Essentials Mujer (PANTALON, Mujer, $99999, Sportline, 0.8) | ✅ misma categoría y género compatible; dif. de precio media 33 % |
| 5 | Short adidas Tiro 26 Essentials Hombre — PANTALON, Hombre, ADIDAS, $49999 (Sportline) | `contenido` | Pantalones/shorts de hombre, adidas | SHORT NBA CHICAGO BULLS HOMBRE (PANTALON, Hombre, $49999, Grid, 0.8)<br>SHORT NBA BOSTON CELTICS HOMBRE (PANTALON, Hombre, $54999, Grid, 0.8)<br>Pantalón NBA CARGO POCKET REGULAR FIT JOGGERS LOS ANGELES LAKERS HOMBRE (PANTALON, Hombre, $44999, Grid, 0.8)<br>SHORT NBA LOS ANGELES LAKERS HOMBRE (PANTALON, Hombre, $54999, Grid, 0.8) | ✅ misma categoría y género compatible; dif. de precio media 8 % |
| 6 | ZAPATILLAS NIKE AIR FORCE 1 '07 DE MUJER — CALZADO, Mujer, Nike Sportswear, $169999 (Grid) | `contenido` | Calzado de mujer, Nike, precio cercano (~$170.000) | ZAPATILLAS NIKE SPORTSWEAR DUNK LOW MUJER (CALZADO, Mujer, $169999, Grid, 1)<br>ZAPATILLAS NIKE SPORTSWEAR AIR FORCE 1 '07 MUJER (CALZADO, Mujer, $169999, Grid, 1)<br>ZAPATILLAS NIKE SPORTSWEAR AIR MAX 90 MUJER (CALZADO, Mujer, $149999, Grid, 1)<br>ZAPATILLAS NIKE SPORTSWEAR GAMMA FORCE MUJER (CALZADO, Mujer, $199999, Grid, 1) | ✅ misma categoría y género compatible; dif. de precio media 7 % |
| 7 | Sandalias Crocs Crocband Unisex — CALZADO, Unisex, Crocs, $74999 (Sportline) | `contenido` | Calzado unisex o de cualquier género, Crocs o precio cercano | Sandalias Crocs Classic Unisex (CALZADO, Unisex, $59999, Sportline, 1)<br>Sandalias Crocs Crocband Kids Niño (CALZADO, Unisex, $59999, Sportline, 1)<br>Zapatillas Puma Court Classic Clean Adp Unisex (CALZADO, Unisex, $75999, Sportline, 0.8)<br>Zapatillas Topper Strong Pace 4 Cs Unisex (CALZADO, Unisex, $72999, Sportline, 0.8) | ✅ misma categoría y género compatible; dif. de precio media 11 % |
| 8 | Short Topper Mix Niño — PANTALON, Unisex, Topper, $15599 (Sportline) | `contenido` | Pantalones unisex/niños, precio bajo | SHORT NBA LOS ANGELES LAKERS MUJER (PANTALON, Mujer, $19999, Grid, 0.7)<br>SHORT NBA CHICAGO BULLS MUJER (PANTALON, Mujer, $19999, Grid, 0.7)<br>Short NBA LOGO PRINT REGULAR FIT BASKETBALL CHICAGO BULLS NIÑOS (PANTALON, Unisex, $34999, Grid, 0.6)<br>SHORT NBA CHICAGO BULLS NIÑOS (PANTALON, Unisex, $34999, Grid, 0.6) | ✅ misma categoría y género compatible; dif. de precio media 76 % |
| 9 | Remera Puma Essential Script Mujer — REMERA, Mujer, Puma, $49999 (Sportline) | `complementaria` | Conjunto: un pantalón y un calzado de mujer (o unisex) | Calza adidas Essentials Logo Mujer (PANTALON, Mujer, $54999, Sportline, 0.7)<br>Ojotas Puma Cool Cat 3.0 Sport Woman Slide Mujer (CALZADO, Mujer, $54999, Sportline, 1)<br>Calza Atomik Manifest Biker Mujer (PANTALON, Mujer, $44900, Sportline, 0.7)<br>Ojotas Puma Cool Cat 3.0 Mujer (CALZADO, Mujer, $54999, Sportline, 1) | ✅ trae todas las categorías que combinan; dif. de precio media 10 % |
| 10 | Zapatillas Topper Boris Hombre — CALZADO, Hombre, Topper, $40000 (Sportline) | `complementaria` | Conjunto: una remera y un pantalón de hombre (o unisex) | Remera Topper Brand Hombre (REMERA, Hombre, $22199, Sportline, 0.9)<br>SHORT MLB NEW YORK YANKEES HOMBRE (PANTALON, Hombre, $39999, Grid, 0.7)<br>Remera Under Armour Ua Tech 2.0 Hombre (REMERA, Hombre, $39999, Sportline, 0.7)<br>SHORT NBA CHICAGO BULLS HOMBRE (PANTALON, Hombre, $39999, Grid, 0.7) | ✅ trae todas las categorías que combinan; dif. de precio media 11 % |

## Limitaciones

- **No distingue chicos de adultos.** El scraper normaliza "Niño" como `Unisex` (el front solo tiene
  `Niña`), así que a un short de niño le recomienda shorts de mujer adulta (caso 8). Se resolvería con
  un atributo de edad en el modelo.
- **Precio de chicos vs. adultos:** en el caso 8 la diferencia de precio media es 76 %, porque hay pocos
  productos infantiles baratos en la misma categoría.
- **Marca por texto exacto:** `adidas`, `ADIDAS` y `Adidas Originals` no son la misma marca para el
  modelo (las mayúsculas sí se ignoran).
- **No mira la imagen ni el estilo:** una camiseta de fútbol y una remera básica son "REMERA" igual. El
  parecido visual lo cubre Ximilar en "similares".
- **Sin personalización:** no hay historial de usuario; todos ven las mismas recomendaciones para un producto.
- **Categorías limitadas** a las cuatro que normaliza el scraper; buzos y camperas no entran.
- **La cola usa siempre la estrategia `contenido`:** el evento `RecomendacionSolicitada` no tiene campo
  de estrategia (es contrato con P5; agregarlo es un cambio aparte).

## Evidencias

- [docs/evidencias/ia-validacion.json](evidencias/ia-validacion.json): los 10 casos con entrada, esperado, obtenido y resultado.
- [docs/evidencias/ia-cola.txt](evidencias/ia-cola.txt): pedido por cola (202), resultado guardado por el consumidor y la cola en 0.
- [docs/evidencias/ia-recomendaciones.txt](evidencias/ia-recomendaciones.txt): llamado REST y por cola.
- [docs/evidencias/07-front-recomendaciones-y-similares.jpg](evidencias/07-front-recomendaciones-y-similares.jpg): "Te puede gustar" en el detalle.

## Cómo probarlo

```powershell
curl.exe "http://localhost:8080/api/recomendaciones?productoId=36&limite=4"
```

```powershell
curl.exe "http://localhost:8080/api/recomendaciones?productoId=36&limite=4&estrategia=complementaria"
```

```powershell
curl.exe -X POST http://localhost:8080/api/recomendaciones/solicitudes -H "Content-Type: application/json" -d '{\"productoId\":36,\"limite\":3}'
```

Con el `solicitudId` de la respuesta: `GET http://localhost:8080/api/recomendaciones/solicitudes/<solicitudId>`.

Tests: `cd tienda/backend && mvn test -Dtest=RecomendacionServiceTest`.
