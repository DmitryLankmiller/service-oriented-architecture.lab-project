import type { Flat } from '../api/models/flats';
import {
	formatDate,
	furnishLabels,
	numberFormat,
	transportLabels,
	viewLabels,
} from '../lib/flats';
import { EmptyState, Icon } from './ui';

export function FlatsTable({
	flats,
	onOpen,
	onEdit,
	loading = false,
}: {
	flats: Flat[];
	onOpen: (id: number) => void;
	onEdit?: (flat: Flat) => void;
	loading?: boolean;
}) {
	if (loading)
		return (
			<div className="table-loading" role="status">
				<span className="spinner" />
				Загрузка квартир…
			</div>
		);
	if (flats.length === 0)
		return (
			<EmptyState>
				<p>Попробуйте изменить фильтры или добавьте квартиру.</p>
			</EmptyState>
		);
	return (
		<div className="table-scroll">
			<table className="data-table">
				<thead>
					<tr>
						<th scope="col" className="id-cell">
							ID
						</th>
						<th scope="col" className="name-cell">
							<span>
								<Icon name="home" size={15} />
								Название
							</span>
						</th>
						<th scope="col">Площадь</th>
						<th scope="col">Комнаты</th>
						<th scope="col">Мебель</th>
						<th scope="col">Вид</th>
						<th scope="col">Транспорт</th>
						<th scope="col">Дом</th>
						<th scope="col">Возраст дома</th>
						<th scope="col">Квартир на этаже</th>
						<th scope="col">X</th>
						<th scope="col">Y</th>
						<th scope="col">Создана</th>
						{onEdit && (
							<th scope="col" className="actions-cell">
								<span className="sr-only">Действия</span>
							</th>
						)}
					</tr>
				</thead>
				<tbody>
					{flats.map((flat) => (
						<tr key={flat.id}>
							<td className="id-cell muted">{flat.id}</td>
							<td className="name-cell">
								<button
									className="record-link"
									type="button"
									onClick={() => onOpen(flat.id)}>
									<span className="record-icon">
										<Icon name="home" size={16} />
									</span>
									<span title={flat.name}>{flat.name}</span>
								</button>
							</td>
							<td className="numeric">
								{numberFormat.format(flat.area)}{' '}
								<span className="muted">м²</span>
							</td>
							<td className="numeric">{flat.numberOfRooms}</td>
							<td>
								{flat.furnish ? (
									<span className={`tag tag-${flat.furnish.toLowerCase()}`}>
										{furnishLabels[flat.furnish]}
									</span>
								) : (
									<span className="muted">—</span>
								)}
							</td>
							<td>
								{flat.view ? (
									<span
										className={`tag ${flat.view === 'PARK' ? 'tag-green' : 'tag-neutral'}`}>
										{viewLabels[flat.view]}
									</span>
								) : (
									<span className="muted">—</span>
								)}
							</td>
							<td>
								{flat.transport ? (
									transportLabels[flat.transport]
								) : (
									<span className="muted">—</span>
								)}
							</td>
							<td className="house-cell" title={flat.house?.name ?? ''}>
								{flat.house ? (
									(flat.house.name ?? 'Без названия')
								) : (
									<span className="muted">—</span>
								)}
							</td>
							<td className="numeric">{flat.house?.year ?? '—'}</td>
							<td className="numeric">
								{flat.house?.numberOfFlatsOnFloor ?? '—'}
							</td>
							<td className="numeric">
								{numberFormat.format(flat.coordinates.x)}
							</td>
							<td className="numeric">{flat.coordinates.y}</td>
							<td>{formatDate(flat.creationDate)}</td>
							{onEdit && (
								<td className="actions-cell">
									<button
										className="icon-button"
										type="button"
										title="Изменить квартиру"
										aria-label={`Изменить: ${flat.name}`}
										onClick={() => onEdit(flat)}>
										<Icon name="edit" size={16} />
									</button>
								</td>
							)}
						</tr>
					))}
				</tbody>
			</table>
		</div>
	);
}
