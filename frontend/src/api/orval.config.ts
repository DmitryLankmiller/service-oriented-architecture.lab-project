import { defineConfig, type OutputOptions } from 'orval';

const commonOutputConfig: OutputOptions = {
	client: 'axios',
	httpClient: 'axios',
	override: {
		mutator: {
			path: './xml-converter.ts',
			name: 'xmlConverter',
		},
	},
};

export default defineConfig({
	flats: {
		input: '../../openapi/flat-service.yml',
		output: {
			target: './flats-api.ts',
			schemas: './models/flats',
			...commonOutputConfig,
		},
	},
	agency: {
		input: '../../openapi/agency-service.yml',
		output: {
			target: './agency-api.ts',
			schemas: './models/agency',
			...commonOutputConfig,
		},
	},
});
