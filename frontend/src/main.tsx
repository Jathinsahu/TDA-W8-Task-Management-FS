import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

/**
 * Main entry point for the React application.
 * 
 * ReactDOM.createRoot creates a React root on the DOM element with id "root"
 * (defined in index.html). All our React components are rendered inside this root.
 * 
 * React.StrictMode is a development tool that:
 * - Highlights potential problems in the application
 * - Warns about unsafe lifecycle methods
 * - Double-invokes certain functions to detect side effects
 * 
 * StrictMode doesn't affect the production build at all.
 */
ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);
