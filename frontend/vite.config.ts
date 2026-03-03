import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

/**
 * Vite configuration for the React frontend.
 * 
 * Vite is our build tool and dev server. Key config:
 * - React plugin: enables JSX transformation and Fast Refresh
 * - Path alias: @ maps to src/ for cleaner imports
 * - Server port: 3000 (to match docker-compose)
 * - Proxy: forwards /api requests to Spring Boot backend
 */
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        port: 3000,
        // Proxy API requests to the Spring Boot backend during development
        // This avoids CORS issues in development
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            },
            '/ws': {
                target: 'http://localhost:8080',
                ws: true,
            },
        },
    },
});
