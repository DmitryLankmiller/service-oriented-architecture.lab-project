import { useState, type FormEvent } from 'react';
import type { Flat, View } from '../api/models/flats';
import {
	agencyApi,
	errorMessage,
	fieldByKey,
	flatApi,
	numberFormat,
	validateValue,
	viewLabels,
} from '../lib/flats';
import { FlatsTable } from './FlatsTable';
import { focusInvalid } from '../lib/forms';
import { ErrorNotice, Field, Icon, Modal, type IconName } from './ui';

const operations: {
	id: string;
	title: string;
	description: string;
	icon: IconName;
	action: string;
}[] = [
	{
		id: 'cost',
		title: 'Общая стоимость',
		description: 'Стоимость всех квартир в коллекции.',
		icon: 'home',
		action: 'Рассчитать',
	},
	{
		id: 'metro',
		title: 'Время до метро',
		description: 'Квартиры по времени пути пешком или на транспорте.',
		icon: 'sort',
		action: 'Показать квартиры',
	},
	{
		id: 'count',
		title: 'Квартиры в доме',
		description: 'Количество квартир с указанными характеристиками дома.',
		icon: 'grid',
		action: 'Посчитать',
	},
	{
		id: 'view',
		title: 'Квартиры по виду',
		description: 'Квартиры со значением вида больше выбранного.',
		icon: 'filter',
		action: 'Показать квартиры',
	},
	{
		id: 'redirect',
		title: 'Найти через агентство',
		description: 'Открыть карточку квартиры по её ID.',
		icon: 'arrow',
		action: 'Открыть квартиру',
	},
	{
		id: 'delete',
		title: 'Удаление по комнатам',
		description: 'Удалить одну из квартир с выбранным числом комнат.',
		icon: 'trash',
		action: 'Удалить одну квартиру',
	},
];
type Result =
	| { kind: 'number'; value: number; label: string }
	| { kind: 'list'; flats: Flat[] }
	| { kind: 'deleted'; flat: Flat };
function OperationCard({
	operation,
	onOpen,
	onChanged,
}: {
	operation: (typeof operations)[number];
	onOpen: (id: number, viaAgency?: boolean) => void;
	onChanged: (message: string) => void;
}) {
	const [values, setValues] = useState({
		rooms: '',
		id: '',
		houseName: '',
		houseYear: '',
		houseFlats: '',
		withoutHouse: true,
		withoutHouseName: false,
		view: 'STREET',
		mode: 'by-feet',
		order: 'asc',
	});
	const [errors, setErrors] = useState<Record<string, string>>({});
	const [busy, setBusy] = useState(false);
	const [error, setError] = useState('');
	const [result, setResult] = useState<Result | null>(null);
	const [confirm, setConfirm] = useState(false);
	function change<K extends keyof typeof values>(
		key: K,
		value: (typeof values)[K],
	) {
		setValues((previous) => ({ ...previous, [key]: value }));
		setErrors((previous) => ({ ...previous, [key]: '' }));
		setError('');
		setResult(null);
	}
	async function run() {
		if (busy) return;
		if (operation.id === 'redirect') {
			onOpen(Number(values.id), true);
			return;
		}
		setBusy(true);
		setError('');
		setResult(null);
		try {
			switch (operation.id) {
				case 'cost': {
					const response = await agencyApi.getTotalCost();
					setResult({
						kind: 'number',
						value: response.totalCost,
						label: 'Суммарная стоимость',
					});
					break;
				}
				case 'metro': {
					const flats = await agencyApi.getFlatsOrderedByTimeToMetro(
						values.mode as 'by-feet' | 'by-transport',
						values.order as 'asc' | 'desc',
					);
					setResult({ kind: 'list', flats });
					break;
				}
				case 'count': {
					const response = await flatApi.countFlatsByHouse(
						values.withoutHouse
							? { house: 'null' }
							: {
									'house.name': values.withoutHouseName
										? undefined
										: values.houseName,
									'house.year': Number(values.houseYear),
									'house.numberOfFlatsOnFloor': Number(values.houseFlats),
								},
					);
					setResult({
						kind: 'number',
						value: response.count,
						label: 'Найдено квартир',
					});
					break;
				}
				case 'view': {
					const flats = await flatApi.getFlatsWithGreaterView(
						values.view as View,
					);
					setResult({ kind: 'list', flats });
					break;
				}
				case 'delete': {
					const flat = await flatApi.deleteOneFlatByNumberOfRooms({
						numberOfRooms: Number(values.rooms),
					});
					setResult({ kind: 'deleted', flat });
					setConfirm(false);
					onChanged(`Удалена квартира «${flat.name}»`);
					break;
				}
			}
		} catch (cause) {
			setError(errorMessage(cause));
		} finally {
			setBusy(false);
		}
	}
	function submit(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		const validation: Record<string, string> = {};
		const checks: [string, string | undefined][] = [];
		if (operation.id === 'delete')
			checks.push([
				'rooms',
				validateValue(fieldByKey('numberOfRooms'), values.rooms),
			]);
		if (operation.id === 'redirect')
			checks.push(['id', validateValue(fieldByKey('id'), values.id)]);
		if (operation.id === 'count' && !values.withoutHouse)
			checks.push(
				[
					'houseYear',
					validateValue(fieldByKey('house.year'), values.houseYear),
				],
				[
					'houseFlats',
					validateValue(
						fieldByKey('house.numberOfFlatsOnFloor'),
						values.houseFlats,
					),
				],
			);
		for (const [key, message] of checks) if (message) validation[key] = message;
		if (
			operation.id === 'count' &&
			!values.withoutHouse &&
			!values.withoutHouseName
		) {
			const message = validateValue(fieldByKey('house.name'), values.houseName);
			if (message) validation.houseName = message;
		}
		setErrors(validation);
		if (Object.keys(validation).length) {
			focusInvalid(event.currentTarget);
			return;
		}
		if (operation.id === 'delete') {
			setConfirm(true);
			setError('');
		} else void run();
	}
	return (
		<article className="operation-card">
			<span
				className={`operation-icon ${operation.id === 'delete' ? 'danger-quiet' : ''}`}
			>
				<Icon name={operation.icon} size={23} />
			</span>
			<div className="operation-copy">
				<h2>{operation.title}</h2>
				<p>{operation.description}</p>
			</div>
			<form onSubmit={submit} noValidate>
				<fieldset disabled={busy}>
					{operation.id === 'metro' && (
						<div className="form-grid">
							<Field label="Способ передвижения">
								<select
									value={values.mode}
									onChange={(event) => change('mode', event.target.value)}
								>
									<option value="by-feet">Пешком</option>
									<option value="by-transport">На транспорте</option>
								</select>
							</Field>
							<Field label="Порядок">
								<select
									value={values.order}
									onChange={(event) => change('order', event.target.value)}
								>
									<option value="asc">Сначала ближе</option>
									<option value="desc">Сначала дальше</option>
								</select>
							</Field>
						</div>
					)}
					{operation.id === 'count' && (
						<>
							<label className="check-label">
								<input
									type="checkbox"
									checked={values.withoutHouse}
									onChange={(event) =>
										change('withoutHouse', event.target.checked)
									}
								/>
								Квартиры без дома
							</label>
							{!values.withoutHouse && (
								<div className="count-fields">
									<Field label="Название дома" error={errors.houseName}>
										<input
											value={values.houseName}
											aria-invalid={!!errors.houseName}
											disabled={values.withoutHouseName}
											onChange={(event) =>
												change('houseName', event.target.value)
											}
										/>
									</Field>
									<label className="check-label">
										<input
											type="checkbox"
											checked={values.withoutHouseName}
											onChange={(event) =>
												change('withoutHouseName', event.target.checked)
											}
										/>
										Название не указано
									</label>
									<div className="form-grid">
										<Field
											label="Возраст дома, лет"
											required
											error={errors.houseYear}
										>
											<input
												type="number"
												min="1"
												max="148"
												step="1"
												value={values.houseYear}
												aria-invalid={!!errors.houseYear}
												onChange={(event) =>
													change('houseYear', event.target.value)
												}
											/>
										</Field>
										<Field
											label="Квартир на этаже"
											required
											error={errors.houseFlats}
										>
											<input
												type="number"
												min="1"
												max="2147483647"
												step="1"
												value={values.houseFlats}
												aria-invalid={!!errors.houseFlats}
												onChange={(event) =>
													change('houseFlats', event.target.value)
												}
											/>
										</Field>
									</div>
								</div>
							)}
						</>
					)}
					{operation.id === 'view' && (
						<Field label="Вид" hint="На улицу → На парк → Обычный">
							<select
								value={values.view}
								onChange={(event) => change('view', event.target.value)}
							>
								{Object.entries(viewLabels).map(([key, label]) => (
									<option key={key} value={key}>
										{label}
									</option>
								))}
							</select>
						</Field>
					)}
					{operation.id === 'redirect' && (
						<Field label="ID квартиры" required error={errors.id}>
							<input
								type="number"
								min="1"
								max={Number.MAX_SAFE_INTEGER}
								step="1"
								value={values.id}
								aria-invalid={!!errors.id}
								onChange={(event) => change('id', event.target.value)}
							/>
						</Field>
					)}
					{operation.id === 'delete' && (
						<Field label="Количество комнат" required error={errors.rooms}>
							<input
								type="number"
								min="1"
								max="20"
								step="1"
								value={values.rooms}
								aria-invalid={!!errors.rooms}
								onChange={(event) => change('rooms', event.target.value)}
							/>
						</Field>
					)}
					<button
						className={`button ${operation.id === 'delete' ? 'danger-quiet' : 'secondary'} operation-submit`}
						disabled={busy}
					>
						{busy ? 'Выполнение…' : operation.action}
						<Icon
							name={operation.id === 'delete' ? 'trash' : 'arrow'}
							size={17}
						/>
					</button>
				</fieldset>
			</form>
			{error && !confirm && <ErrorNotice message={error} />}
			{result && (
				<div className="operation-result" aria-live="polite">
					{result.kind === 'number' ? (
						<>
							<span className="eyebrow">{result.label}</span>
							<strong className="result-number">
								{numberFormat.format(result.value)}
							</strong>
						</>
					) : result.kind === 'deleted' ? (
						<div className="deleted-result">
							<Icon name="check" />
							<span>
								Удалена: <strong>{result.flat.name}</strong>
								<small>
									ID {result.flat.id} · {result.flat.numberOfRooms} комн. ·{' '}
									{result.flat.area} м²
								</small>
							</span>
						</div>
					) : (
						<>
							<div className="result-heading">
								Найдено квартир: <strong>{result.flats.length}</strong>
							</div>
							<FlatsTable flats={result.flats} onOpen={onOpen} />
						</>
					)}
				</div>
			)}
			{confirm && (
				<Modal
					title="Удалить одну квартиру?"
					onClose={() => setConfirm(false)}
					busy={busy}
				>
					<div className="modal-body">
						<p>
							Будет удалена одна из квартир с числом комнат {values.rooms}.
							Выбрать конкретную квартиру в этой операции нельзя.
						</p>
						{error && <ErrorNotice message={error} />}
					</div>
					<div className="modal-footer">
						<button
							type="button"
							className="button secondary"
							onClick={() => setConfirm(false)}
							disabled={busy}
						>
							Отмена
						</button>
						<button
							className="button danger"
							type="button"
							disabled={busy}
							onClick={run}
						>
							{busy ? 'Удаление…' : 'Удалить'}
						</button>
					</div>
				</Modal>
			)}
		</article>
	);
}
export function Operations({
	onOpen,
	onChanged,
}: {
	onOpen: (id: number, viaAgency?: boolean) => void;
	onChanged: (message: string) => void;
}) {
	return (
		<>
			<header className="page-heading">
				<h1>Специальные операции</h1>
			</header>
			<div className="operations-list">
				{operations.map((operation) => (
					<OperationCard
						key={operation.id}
						operation={operation}
						onOpen={onOpen}
						onChanged={onChanged}
					/>
				))}
			</div>
		</>
	);
}
