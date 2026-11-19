import { Furnish, Transport, View } from './models/flats';

const object = (value: unknown): value is Record<string, unknown> =>
	typeof value === 'object' && value !== null && !Array.isArray(value);
const integer = (
	value: unknown,
	min: number,
	max = Number.MAX_SAFE_INTEGER,
): value is number =>
	typeof value === 'number' &&
	Number.isSafeInteger(value) &&
	value >= min &&
	value <= max;
const optionalEnum = (value: unknown, options: string[]) =>
	value == null || (typeof value === 'string' && options.includes(value));
function date(value: unknown): boolean {
	if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value))
		return false;
	const parsed = new Date(value + 'T12:00:00Z');
	return (
		!Number.isNaN(parsed.getTime()) &&
		parsed.toISOString().slice(0, 10) === value
	);
}
function flat(value: unknown): boolean {
	if (
		!object(value) ||
		!integer(value.id, 1) ||
		typeof value.name !== 'string' ||
		!value.name.length ||
		!date(value.creationDate)
	)
		return false;
	if (
		!integer(value.area, 1, 724) ||
		!integer(value.numberOfRooms, 1, 20) ||
		!object(value.coordinates)
	)
		return false;
	const { x, y } = value.coordinates;
	if (
		typeof x !== 'number' ||
		!Number.isFinite(Math.fround(x)) ||
		x <= -288 ||
		!integer(y, -2147483648, 2147483647)
	)
		return false;
	if (
		!optionalEnum(value.furnish, Object.values(Furnish)) ||
		!optionalEnum(value.view, Object.values(View)) ||
		!optionalEnum(value.transport, Object.values(Transport))
	)
		return false;
	if (value.house != null) {
		if (
			!object(value.house) ||
			(value.house.name != null && typeof value.house.name !== 'string') ||
			!integer(value.house.year, 1, 148) ||
			!integer(value.house.numberOfFlatsOnFloor, 1, 2147483647)
		)
			return false;
	}
	return true;
}

// Generated TypeScript types do not validate the server's response at runtime.
export function validateResponse(
	value: unknown,
	url: string,
	method = 'GET',
	status = 200,
): void {
	let valid: boolean;
	if (method.toUpperCase() === 'DELETE')
		valid = status === 204 && value === undefined;
	else if (
		(url === '/flats' && method.toUpperCase() === 'GET') ||
		url.startsWith('/flats/view-greater-than/') ||
		url.startsWith('/agency/get-ordered-by-time-to-metro/')
	)
		valid = Array.isArray(value) && value.every(flat);
	else if (url === '/flats/count-by-house')
		valid = object(value) && integer(value.count, 0);
	else if (url === '/agency/get-total-cost')
		valid =
			object(value) &&
			typeof value.totalCost === 'number' &&
			Number.isFinite(value.totalCost) &&
			value.totalCost >= 0;
	else valid = flat(value);
	if (!valid)
		throw new Error(
			'Сервер вернул ответ с некорректными данными. Обновите страницу или повторите запрос позже.',
		);
}
