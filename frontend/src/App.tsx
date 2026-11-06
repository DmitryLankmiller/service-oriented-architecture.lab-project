import { isAxiosError } from 'axios';
import { useEffect, useState } from 'react';
import { getFlatAPI } from './api/flats-api';
import type { Error as ApiError, Flat } from './api/models/flats';
import './App.css';

const flatApi = getFlatAPI();

function App() {
	const [flats, setFlats] = useState<Flat[]>([]);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState<string | null>(null);
	const [reloadCount, setReloadCount] = useState(0);

	useEffect(() => {
		const controller = new AbortController();
		flatApi
			.listFlats(
				{ page: 0, size: 10, sort: ['id_asc'] },
				{
					signal: controller.signal,
				},
			)
			.then((page) => {
				if (controller.signal.aborted) return;
				setFlats(page);
				setLoading(false);
			})
			.catch((cause: unknown) => {
				if (controller.signal.aborted) return;
				const message = isAxiosError<ApiError>(cause)
					? cause.response?.data?.message
					: undefined;
				setError(
					message ||
						'Не удалось загрузить квартиры. Проверьте доступность API.',
				);
				setLoading(false);
			});
		return () => controller.abort();
	}, [reloadCount]);

	return (
		<main className="flats-page">
			<header className="flats-header">
				<div>
					<h1>Квартиры</h1>
				</div>
				<button
					type="button"
					disabled={loading}
					onClick={() => {
						setLoading(true);
						setError(null);
						setReloadCount((count) => count + 1);
					}}>
					Обновить
				</button>
			</header>

			{loading && <p role="status">Загрузка квартир…</p>}
			{error && (
				<p className="error-message" role="alert">
					{error}
				</p>
			)}
			{!loading &&
				!error &&
				(flats.length === 0 ? (
					<p>Квартир пока нет.</p>
				) : (
					<div className="table-scroll">
						<table>
							<caption>Квартиры на первой странице: {flats.length}</caption>
							<thead>
								<tr>
									<th scope="col">ID</th>
									<th scope="col">Название</th>
									<th scope="col">Площадь, м²</th>
									<th scope="col">Комнаты</th>
									<th scope="col">Координаты</th>
									<th scope="col">Дом</th>
									<th scope="col">Вид</th>
								</tr>
							</thead>
							<tbody>
								{flats.map((flat) => (
									<tr key={flat.id}>
										<td>{flat.id}</td>
										<td>{flat.name}</td>
										<td>{flat.area}</td>
										<td>{flat.numberOfRooms}</td>
										<td>
											{flat.coordinates.x}, {flat.coordinates.y}
										</td>
										<td>
											{flat.house ? flat.house.name || 'Без названия' : '—'}
										</td>
										<td>{flat.view ?? '—'}</td>
									</tr>
								))}
							</tbody>
						</table>
					</div>
				))}
		</main>
	);
}

export default App;
