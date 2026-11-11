

Ejecutar en terminal:

cd tienda
npm init -y

npm install express axios dotenv

node server.js

cd ximilar-product-similarity

cd scripts

Crear el archivo .env (instalar los plugins que pida)
XIMILAR_API_TOKEN=""
XIMILAR_COLLECTION_ID=""
PORT=3000

Las credenciales valen para 1000 usos, cuando se acaben hay que hacer otra cuenta, crear una nueva coleccion de product similarity y reemplazarlas

#Cargar datos a la coleccion
node insert-product.js 

#Buscar similitudes
node test-product-similarity.js (dentro del archivo esta declarado en imageUrl la imagen con la que buscar similares en la coleccion)


