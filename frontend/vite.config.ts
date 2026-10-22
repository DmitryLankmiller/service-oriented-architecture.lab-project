import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// https://vite.dev/config/
export default defineConfig({
	plugins: [react()],
	server: {
		proxy: {
			'/~s373305/soa/api/': {
				target: 'https://se.ifmo.ru',
				changeOrigin: true,
			},
		},
	},
});
