import { useEffect, useState } from 'react';
import type { Flat, ListFlatsParams } from '../api/models/flats';
import { errorMessage, flatApi } from './flats';

export function useFlatPage(params: ListFlatsParams, revision: number) {
	const key = JSON.stringify([params, revision]);
	const [state, setState] = useState<{
		key: string;
		flats: Flat[];
		hasNext: boolean;
		error: string;
	}>({ key: '', flats: [], hasNext: false, error: '' });
	useEffect(() => {
		const [query] = JSON.parse(key) as [ListFlatsParams, number];
		const controller = new AbortController();
		async function load() {
			try {
				const flats = await flatApi.listFlats(query, {
					signal: controller.signal,
				});
				let hasNext = false;
				if (flats.length === query.size) {
					const next = await flatApi.listFlats(
						{ ...query, page: (query.page ?? 0) + 1 },
						{ signal: controller.signal },
					);
					hasNext = next.length > 0;
				}
				if (!controller.signal.aborted)
					setState({ key, flats, hasNext, error: '' });
			} catch (cause) {
				if (!controller.signal.aborted)
					setState({
						key,
						flats: [],
						hasNext: false,
						error: errorMessage(cause),
					});
			}
		}
		void load();
		return () => controller.abort();
	}, [key]);
	return { ...state, loading: state.key !== key };
}
