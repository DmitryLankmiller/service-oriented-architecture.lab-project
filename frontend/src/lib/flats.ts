import { isAxiosError } from 'axios';
import { getAgencyAPI } from '../api/agency-api';
import { getFlatAPI } from '../api/flats-api';
import { xmlTextError } from './forms';
import type {
	Error as ApiError,
	Flat,
	FlatInput,
	FlatPatch,
	Furnish,
	Transport,
	View,
} from '../api/models/flats';

export const flatApi = getFlatAPI();
export const agencyApi = getAgencyAPI();

export const furnishLabels: Record<Furnish, string> = {
	DESIGNER: 'Дизайнерская',
	NONE: 'Без мебели',
	FINE: 'Хорошая',
	BAD: 'Требует замены',
	LITTLE: 'Частично',
};
export const viewLabels: Record<View, string> = {
	STREET: 'На улицу',
	PARK: 'На парк',
	NORMAL: 'Обычный',
};
export const transportLabels: Record<Transport, string> = {
	FEW: 'Мало',
	LITTLE: 'Немного',
	NORMAL: 'Средне',
	ENOUGH: 'Достаточно',
};
export const numberFormat = new Intl.NumberFormat('ru-RU', {
	maximumFractionDigits: 2,
});
export function formatDate(value: string) {
	const date = new Date(value.slice(0, 10) + 'T12:00:00');
	return Number.isNaN(date.getTime())
		? value
		: date.toLocaleDateString('ru-RU');
}
export function errorMessage(cause: unknown): string {
	if (isAxiosError<ApiError>(cause)) {
		if (cause.code === 'ECONNABORTED' || cause.code === 'ETIMEDOUT')
			return 'Сервер не ответил вовремя. Обновите данные перед повторной отправкой изменений.';
		const body = cause.response?.data;
		if (typeof body?.message === 'string' && body.message.trim())
			return body.message;
		if (cause.response)
			return `Не удалось выполнить запрос (HTTP ${cause.response.status}). Попробуйте ещё раз.`;
		return 'Не удалось связаться с сервером. Проверьте соединение и повторите запрос.';
	}
	return cause instanceof Error &&
		typeof cause.message === 'string' &&
		cause.message.trim()
		? cause.message
		: 'Не удалось выполнить операцию.';
}

export type FieldKey =
	| 'id'
	| 'name'
	| 'coordinates.x'
	| 'coordinates.y'
	| 'creationDate'
	| 'area'
	| 'numberOfRooms'
	| 'furnish'
	| 'view'
	| 'transport'
	| 'house.name'
	| 'house.year'
	| 'house.numberOfFlatsOnFloor';
export type FieldDefinition = {
	key: FieldKey;
	label: string;
	kind: 'text' | 'integer' | 'number' | 'date' | 'enum';
	nullable?: boolean;
	options?: Record<string, string>;
	min?: number;
	max?: number;
};
export const fields: FieldDefinition[] = [
	{
		key: 'id',
		label: 'ID',
		kind: 'integer',
		min: 1,
		max: Number.MAX_SAFE_INTEGER,
	},
	{ key: 'name', label: 'Название', kind: 'text' },
	{ key: 'area', label: 'Площадь', kind: 'integer', min: 1, max: 724 },
	{ key: 'numberOfRooms', label: 'Комнаты', kind: 'integer', min: 1, max: 20 },
	{ key: 'coordinates.x', label: 'Координата X', kind: 'number', min: -288 },
	{
		key: 'coordinates.y',
		label: 'Координата Y',
		kind: 'integer',
		min: -2147483648,
		max: 2147483647,
	},
	{ key: 'creationDate', label: 'Дата создания', kind: 'date' },
	{
		key: 'furnish',
		label: 'Мебель',
		kind: 'enum',
		nullable: true,
		options: furnishLabels,
	},
	{
		key: 'view',
		label: 'Вид',
		kind: 'enum',
		nullable: true,
		options: viewLabels,
	},
	{
		key: 'transport',
		label: 'Транспорт',
		kind: 'enum',
		nullable: true,
		options: transportLabels,
	},
	{ key: 'house.name', label: 'Название дома', kind: 'text', nullable: true },
	{
		key: 'house.year',
		label: 'Возраст дома',
		kind: 'integer',
		min: 1,
		max: 148,
	},
	{
		key: 'house.numberOfFlatsOnFloor',
		label: 'Квартир на этаже',
		kind: 'integer',
		min: 1,
		max: 2147483647,
	},
];
export const fieldByKey = (key: FieldKey) =>
	fields.find((field) => field.key === key)!;

export function validateValue(
	field: FieldDefinition,
	value: string,
	filter = false,
): string | undefined {
	const textError = xmlTextError(value);
	if (textError) return textError;
	if (field.kind === 'text') {
		if (!filter && field.key === 'name' && !value.trim())
			return 'Введите название квартиры.';
		return;
	}
	if (!value.trim()) return 'Заполните поле.';
	if (field.kind === 'enum')
		return field.options && Object.hasOwn(field.options, value)
			? undefined
			: 'Выберите значение из списка.';
	if (field.kind === 'date') {
		const date = new Date(value + 'T12:00:00Z');
		return /^\d{4}-\d{2}-\d{2}$/.test(value) &&
			!Number.isNaN(date.getTime()) &&
			date.toISOString().slice(0, 10) === value
			? undefined
			: 'Укажите корректную дату.';
	}
	const numeric = Number(value);
	if (!Number.isFinite(numeric)) return 'Введите конечное число.';
	if (field.kind === 'integer' && !Number.isSafeInteger(numeric))
		return 'Введите целое число без потери точности.';
	if (
		field.key === 'coordinates.x' &&
		(!Number.isFinite(Math.fround(numeric)) ||
			(!filter && Math.fround(numeric) <= -288))
	)
		return 'X должен быть больше −288 и помещаться в тип float.';
	if (filter) {
		if (
			[
				'area',
				'numberOfRooms',
				'coordinates.y',
				'house.numberOfFlatsOnFloor',
			].includes(field.key) &&
			(numeric < -2147483648 || numeric > 2147483647)
		)
			return 'Число выходит за допустимый диапазон.';
	} else if (
		field.key !== 'coordinates.x' &&
		((field.min !== undefined && numeric < field.min) ||
			(field.max !== undefined && numeric > field.max))
	) {
		return `Допустимо: ${field.min ?? '−∞'}–${field.max ?? '∞'}.`;
	}
}

export type FlatDraft = Record<
	| 'name'
	| 'x'
	| 'y'
	| 'area'
	| 'numberOfRooms'
	| 'furnish'
	| 'view'
	| 'transport'
	| 'houseName'
	| 'houseYear'
	| 'houseFlats',
	string
> & { hasHouse: boolean; houseNameNull: boolean };
export function flatDraft(flat?: Flat): FlatDraft {
	return {
		name: flat?.name ?? '',
		x: flat ? String(flat.coordinates.x) : '',
		y: flat ? String(flat.coordinates.y) : '',
		area: flat ? String(flat.area) : '',
		numberOfRooms: flat ? String(flat.numberOfRooms) : '',
		furnish: flat?.furnish ?? '',
		view: flat?.view ?? '',
		transport: flat?.transport ?? '',
		hasHouse: !!flat?.house,
		houseNameNull: flat?.house ? flat.house.name == null : false,
		houseName: flat?.house?.name ?? '',
		houseYear: flat?.house ? String(flat.house.year) : '',
		houseFlats: flat?.house ? String(flat.house.numberOfFlatsOnFloor) : '',
	};
}
const draftFields: [keyof FlatDraft, FieldKey][] = [
	['name', 'name'],
	['x', 'coordinates.x'],
	['y', 'coordinates.y'],
	['area', 'area'],
	['numberOfRooms', 'numberOfRooms'],
];
export function validateDraft(draft: FlatDraft): Record<string, string> {
	const errors: Record<string, string> = {};
	const checks = [...draftFields];
	if (draft.hasHouse && !draft.houseNameNull)
		checks.push(['houseName', 'house.name']);
	if (draft.hasHouse)
		checks.push(
			['houseYear', 'house.year'],
			['houseFlats', 'house.numberOfFlatsOnFloor'],
		);
	for (const [key, field] of checks) {
		const error = validateValue(fieldByKey(field), String(draft[key]));
		if (error) errors[key] = error;
	}
	for (const key of ['furnish', 'view', 'transport'] as const) {
		if (draft[key]) {
			const error = validateValue(fieldByKey(key), draft[key]);
			if (error) errors[key] = error;
		}
	}
	return errors;
}
export function toFlatInput(draft: FlatDraft): FlatInput {
	return {
		name: draft.name.trim(),
		coordinates: { x: Number(draft.x), y: Number(draft.y) },
		area: Number(draft.area),
		numberOfRooms: Number(draft.numberOfRooms),
		furnish: draft.furnish ? (draft.furnish as Furnish) : undefined,
		view: draft.view ? (draft.view as View) : undefined,
		transport: draft.transport ? (draft.transport as Transport) : undefined,
		house: draft.hasHouse
			? {
					name: draft.houseNameNull ? undefined : draft.houseName,
					year: Number(draft.houseYear),
					numberOfFlatsOnFloor: Number(draft.houseFlats),
				}
			: undefined,
	};
}
export function toPatch(field: FieldKey, value: string): FlatPatch {
	const definition = fieldByKey(field);
	const parsed =
		definition.kind === 'integer' || definition.kind === 'number'
			? Number(value)
			: value;
	const [parent, child] = field.split('.');
	return (
		child ? { [parent]: { [child]: parsed } } : { [parent]: parsed }
	) as FlatPatch;
}
