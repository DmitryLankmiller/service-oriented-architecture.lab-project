import { useEffect, useState, type FormEvent } from 'react';
import type { Flat } from '../api/models/flats';
import {
	agencyApi,
	errorMessage,
	fieldByKey,
	fields,
	flatApi,
	formatDate,
	furnishLabels,
	numberFormat,
	toPatch,
	transportLabels,
	validateValue,
	viewLabels,
	type FieldKey,
} from '../lib/flats';
import { focusInvalid } from '../lib/forms';
import { ErrorNotice, Field, Icon, Modal } from './ui';

export function FlatDetails({
	id,
	viaAgency = false,
	onClose,
	onEdit,
	onChanged,
	onDeleted,
}: {
	id: number;
	viaAgency?: boolean;
	onClose: () => void;
	onEdit: (flat: Flat) => void;
	onChanged: (message: string) => void;
	onDeleted: () => void;
}) {
	const [flat, setFlat] = useState<Flat | null>(null);
	const [error, setError] = useState('');
	const [revision, setRevision] = useState(0);
	const [patch, setPatch] = useState(false);
	const [field, setField] = useState<FieldKey>('name');
	const [value, setValue] = useState('');
	const [valueError, setValueError] = useState('');
	const [operationError, setOperationError] = useState('');
	const [busy, setBusy] = useState(false);
	const [confirm, setConfirm] = useState(false);
	useEffect(() => {
		const controller = new AbortController();
		// Both APIs return the same flat after the agency redirect has been followed.
		const request = viaAgency
			? agencyApi.redirectToFlat(id, { signal: controller.signal })
			: flatApi.getFlat(id, { signal: controller.signal });
		request
			.then((result) => {
				if (!controller.signal.aborted) {
					setFlat(result as Flat);
					setError('');
				}
			})
			.catch((cause) => {
				if (!controller.signal.aborted) setError(errorMessage(cause));
			});
		return () => controller.abort();
	}, [id, viaAgency, revision]);
	async function submitPatch(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		if (busy) return;
		const message = validateValue(fieldByKey(field), value);
		setValueError(message ?? '');
		if (message) {
			focusInvalid(event.currentTarget);
			return;
		}
		setBusy(true);
		setOperationError('');
		try {
			const saved = await flatApi.patchFlat(id, toPatch(field, value));
			setFlat(saved);
			setPatch(false);
			onChanged('Квартира обновлена');
		} catch (cause) {
			setOperationError(errorMessage(cause));
		} finally {
			setBusy(false);
		}
	}
	async function remove() {
		if (busy) return;
		setBusy(true);
		setOperationError('');
		try {
			await flatApi.deleteFlat(id);
			onDeleted();
		} catch (cause) {
			setOperationError(errorMessage(cause));
		} finally {
			setBusy(false);
		}
	}
	const definition = fieldByKey(field);
	const patchFields = fields.filter(
		(item) =>
			item.key !== 'id' &&
			item.key !== 'creationDate' &&
			(!item.key.startsWith('house.') || flat?.house),
	);
	return (
		<>
			<Modal title={`Квартира #${id}`} onClose={onClose} drawer busy={busy}>
				<div className="modal-body">
					{error ? (
						<ErrorNotice
							message={error}
							onRetry={() => setRevision((n) => n + 1)}
						/>
					) : !flat ? (
						<div className="table-loading" role="status">
							<span className="spinner" />
							Загрузка квартиры…
						</div>
					) : (
						<>
							<div className="detail-hero">
								<span className="detail-icon">
									<Icon name="home" size={28} />
								</span>
								<div>
									<h3>{flat.name}</h3>
									<p>
										{numberFormat.format(flat.area)} м²{' '}
										<span className="dot-separator">·</span> Комнат:{' '}
										{flat.numberOfRooms}
									</p>
								</div>
							</div>
							<h3 className="form-section-title">Свойства</h3>
							<dl className="detail-grid">
								{[
									['ID', String(flat.id)],
									['Дата создания', formatDate(flat.creationDate)],
									['Площадь', `${numberFormat.format(flat.area)} м²`],
									['Количество комнат', String(flat.numberOfRooms)],
									['Мебель', flat.furnish ? furnishLabels[flat.furnish] : '—'],
									['Вид', flat.view ? viewLabels[flat.view] : '—'],
									[
										'Транспорт',
										flat.transport ? transportLabels[flat.transport] : '—',
									],
									['Координата X', numberFormat.format(flat.coordinates.x)],
									['Координата Y', String(flat.coordinates.y)],
								].map(([label, text]) => (
									<div key={label}>
										<dt>{label}</dt>
										<dd>{text}</dd>
									</div>
								))}
							</dl>
							<section className="detail-house">
								<h3>
									<Icon name="home" size={17} />
									Дом
								</h3>
								{flat.house ? (
									<dl className="detail-grid">
										<div>
											<dt>Название</dt>
											<dd>{flat.house.name ?? '—'}</dd>
										</div>
										<div>
											<dt>Возраст</dt>
											<dd>{flat.house.year} лет</dd>
										</div>
										<div>
											<dt>Квартир на этаже</dt>
											<dd>{flat.house.numberOfFlatsOnFloor}</dd>
										</div>
									</dl>
								) : (
									<p className="muted">Дом не указан</p>
								)}
							</section>
							<button
								type="button"
								className="text-button"
								disabled={busy}
								onClick={() => {
									setPatch(!patch);
									setOperationError('');
									setValue(flat.name);
									setField('name');
									setValueError('');
								}}>
								Изменить одно поле
							</button>
							{patch && (
								<form className="patch-form" onSubmit={submitPatch} noValidate>
									<Field label="Поле">
										<select
											value={field}
											onChange={(event) => {
												setField(event.target.value as FieldKey);
												setValue('');
												setValueError('');
											}}>
											{patchFields.map((item) => (
												<option key={item.key} value={item.key}>
													{item.label}
												</option>
											))}
										</select>
									</Field>
									<Field label="Новое значение" error={valueError} required>
										{definition.options ? (
											<select
												value={value}
												aria-invalid={!!valueError}
												disabled={busy}
												onChange={(event) => {
													setValue(event.target.value);
													setValueError('');
												}}>
												<option value="">Выберите значение</option>
												{Object.entries(definition.options).map(
													([key, text]) => (
														<option key={key} value={key}>
															{text}
														</option>
													),
												)}
											</select>
										) : (
											<input
												type={definition.kind === 'text' ? 'text' : 'number'}
												step={definition.kind === 'number' ? 'any' : '1'}
												value={value}
												aria-invalid={!!valueError}
												disabled={busy}
												onChange={(event) => {
													setValue(event.target.value);
													setValueError('');
												}}
											/>
										)}
									</Field>
									<button className="button primary" disabled={busy}>
										{busy ? 'Сохранение…' : 'Применить'}
									</button>
								</form>
							)}
							{operationError && !confirm && (
								<ErrorNotice message={operationError} />
							)}
						</>
					)}
				</div>
				{flat && (
					<div className="modal-footer">
						<button
							type="button"
							className="button danger-quiet"
							disabled={busy}
							onClick={() => {
								setConfirm(true);
								setOperationError('');
							}}>
							<Icon name="trash" size={17} />
							Удалить
						</button>
						<button
							type="button"
							className="button primary"
							disabled={busy}
							onClick={() => onEdit(flat)}>
							<Icon name="edit" size={17} />
							Редактировать
						</button>
					</div>
				)}
			</Modal>
			{confirm && (
				<Modal
					title="Удалить квартиру?"
					onClose={() => setConfirm(false)}
					busy={busy}>
					<div className="modal-body">
						<p>«{flat?.name}» будет удалена. Отменить это действие нельзя.</p>
						{operationError && <ErrorNotice message={operationError} />}
					</div>
					<div className="modal-footer">
						<button
							type="button"
							className="button secondary"
							disabled={busy}
							onClick={() => setConfirm(false)}>
							Отмена
						</button>
						<button
							type="button"
							className="button danger"
							disabled={busy}
							onClick={remove}>
							{busy ? 'Удаление…' : 'Удалить квартиру'}
						</button>
					</div>
				</Modal>
			)}
		</>
	);
}
