# ============================================================
# Frontend Dockerfile
# ============================================================
# Builds the React application and serves it with a simple
# Node.js server. For production, you'd typically use Nginx.
# ============================================================

FROM node:20-alpine

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy source code
COPY . .

# Expose port 3000
EXPOSE 3000

# Start the Vite dev server (for Docker development)
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
