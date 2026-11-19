import { useState, type FormEvent } from 'react';
import type { Flat } from '../api/models/flats';
import {
	errorMessage,
	flatApi,
	flatDraft,
	furnishLabels,
	toFlatInput,
	transportLabels,
	validateDraft,
	viewLabels,
	type FlatDraft,
} from '../lib/flats';
import { focusInvalid } from '../lib/forms';
import { ErrorNotice, Field, Icon, Modal } from './ui';

export function FlatEditor({
	flat,
	onClose,
	onSaved,
}: {
	flat?: Flat;
	onClose: () => void;
	onSaved: (flat: Flat) => void;
}) {
	const [draft, setDraft] = useState(() => flatDraft(flat));
	const [errors, setErrors] = useState<Record<string, string>>({});
	const [error, setError] = useState('');
	const [busy, setBusy] = useState(false);
	function update<K extends keyof FlatDraft>(key: K, value: FlatDraft[K]) {
		setDraft((previous) => ({ ...previous, [key]: value }));
		setErrors((previous) => {
			const copy = { ...previous };
			delete copy[key];
			return copy;
		});
	}
	async function save(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		if (busy) return;
		const validation = validateDraft(draft);
		setErrors(validation);
		if (Object.keys(validation).length) {
			focusInvalid(event.currentTarget);
			return;
		}
		setBusy(true);
		setError('');
		try {
			const input = toFlatInput(draft);
			const saved = flat
				? await flatApi.replaceFlat(flat.id, input)
				: await flatApi.createFlat(input);
			onSaved(saved);
		} catch (cause) {
			setError(errorMessage(cause));
		} finally {
			setBusy(false);
		}
	}
	function input(
		key: Exclude<keyof FlatDraft, 'hasHouse' | 'houseNameNull'>,
		label: string,
		options: {
			min?: number;
			max?: number;
			step?: string;
			type?: string;
			hint?: string;
			required?: boolean;
		} = {},
	) {
		const { hint, required = true, type = 'number', ...attributes } = options;
		return (
			<Field label={label} error={errors[key]} required={required} hint={hint}>
				<input
					name={key}
					value={draft[key]}
					type={type}
					{...attributes}
					required={required}
					aria-invalid={!!errors[key]}
					onChange={(event) => update(key, event.target.value)}
				/>
			</Field>
		);
	}
	function select(
		key: 'furnish' | 'view' | 'transport',
		label: string,
		values: Record<string, string>,
	) {
		return (
			<Field label={label} error={errors[key]}>
				<select
					name={key}
					value={draft[key]}
					aria-invalid={!!errors[key]}
					onChange={(event) => update(key, event.target.value)}
				>
					<option value="">Не указано</option>
					{Object.entries(values).map(([value, text]) => (
						<option key={value} value={value}>
							{text}
						</option>
					))}
				</select>
			</Field>
		);
	}
	return (
		<Modal
			title={flat ? 'Редактировать квартиру' : 'Новая квартира'}
			onClose={onClose}
			drawer
			busy={busy}
		>
			<form className="editor-form" onSubmit={save} noValidate>
				<div className="modal-body">
					<fieldset disabled={busy}>
						<h3 className="form-section-title">Основное</h3>
						{input('name', 'Название', { type: 'text' })}
						<div className="form-grid">
							{input('area', 'Площадь, м²', {
								min: 1,
								max: 724,
								step: '1',
								hint: 'От 1 до 724 м²',
							})}
							{input('numberOfRooms', 'Количество комнат', {
								min: 1,
								max: 20,
								step: '1',
								hint: 'От 1 до 20',
							})}
						</div>
						<h3 className="form-section-title">Координаты</h3>
						<div className="form-grid">
							{input('x', 'X', { min: -288, step: 'any', hint: 'Больше −288' })}
							{input('y', 'Y', {
								min: -2147483648,
								max: 2147483647,
								step: '1',
								hint: 'Целое число',
							})}
						</div>
						<h3 className="form-section-title">Характеристики</h3>
						<div className="form-grid three">
							{select('furnish', 'Мебель', furnishLabels)}
							{select('view', 'Вид', viewLabels)}
							{select('transport', 'Транспорт', transportLabels)}
						</div>
						<div className="section-heading-row">
							<h3 className="form-section-title">Дом</h3>
							<label className="check-label">
								<input
									type="checkbox"
									checked={draft.hasHouse}
									onChange={(event) => update('hasHouse', event.target.checked)}
								/>
								Указать дом
							</label>
						</div>
						{draft.hasHouse && (
							<div className="house-fields">
								<Field label="Название дома" error={errors.houseName}>
									<input
										type="text"
										name="houseName"
										aria-invalid={!!errors.houseName}
										value={draft.houseName}
										disabled={draft.houseNameNull}
										onChange={(event) =>
											update('houseName', event.target.value)
										}
									/>
								</Field>
								<label className="check-label">
									<input
										type="checkbox"
										checked={draft.houseNameNull}
										onChange={(event) =>
											update('houseNameNull', event.target.checked)
										}
									/>
									Название не указано
								</label>
								<div className="form-grid">
									{input('houseYear', 'Возраст дома, лет', {
										min: 1,
										max: 148,
										step: '1',
										hint: 'От 1 до 148',
									})}
									{input('houseFlats', 'Квартир на этаже', {
										min: 1,
										max: 2147483647,
										step: '1',
									})}
								</div>
							</div>
						)}
					</fieldset>
					{error && <ErrorNotice message={error} />}
				</div>
				<div className="modal-footer">
					<button
						className="button secondary"
						type="button"
						onClick={onClose}
						disabled={busy}
					>
						Отмена
					</button>
					<button className="button primary" type="submit" disabled={busy}>
						<Icon name={flat ? 'check' : 'plus'} size={17} />
						{busy ? 'Сохранение…' : flat ? 'Сохранить' : 'Добавить квартиру'}
					</button>
				</div>
			</form>
		</Modal>
	);
}
