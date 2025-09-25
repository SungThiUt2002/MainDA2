
FROM node:20-alpine AS build
WORKDIR /app
# ENV CI=true
# Copy lockfile first to leverage Docker layer cache
COPY package*.json ./
RUN npm ci
# Copy the rest of the source
COPY . .
RUN npm run build

# run stage
FROM nginx:alpine
# Optional: install brotli for better static compression (enable via nginx.conf if desired)
RUN apk add --no-cache brotli && rm -rf /var/cache/apk/*

# Use custom nginx config optimized for SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy build artifacts
COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

