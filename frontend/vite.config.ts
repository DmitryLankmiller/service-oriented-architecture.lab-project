import react from '@vitejs/plugin-react';
import { defineConfig, loadEnv, type ProxyOptions } from 'vite';

// https://vite.dev/config/
export default defineConfig(({ command, mode, isPreview }) => {
	const env = loadEnv(mode, process.cwd(), 'VITE_');
	const target = env.VITE_API_PROXY_TARGET || 'https://se.ifmo.ru';
	const apiPath = env.VITE_API_BASE_URL || '/~s373305/soa/api';
	const proxy: Record<string, ProxyOptions> = {
		[apiPath]: {
			target,
			changeOrigin: true,
			configure(server) {
				server.on('proxyRes', (response) => {
					const location = response.headers.location;
					const origin = new URL(target).origin;
					// Keep redirected API requests on the local origin during development.
					if (location?.startsWith(origin + apiPath + '/'))
						response.headers.location = location.slice(origin.length);
				});
			},
		},
	};
	return {
		plugins: [react()],
		base:
			command === 'build' || isPreview
				? env.VITE_APP_BASE || '/~s373305/soa/app/'
				: '/',
		server: { proxy },
		preview: { proxy },
	};
});
