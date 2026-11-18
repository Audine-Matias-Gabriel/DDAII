FROM node:18-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY src ./src
COPY output ./output
ENV TZ=America/Argentina/Buenos_Aires
CMD ["node", "src/scheduler.js", "--run-now"]
