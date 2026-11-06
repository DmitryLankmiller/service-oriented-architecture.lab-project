import axios, {
	type AxiosError,
	type AxiosRequestConfig,
} from 'axios';

const numericFields = new Set([
	'id',
	'x',
	'y',
	'area',
	'numberOfRooms',
	'year',
	'numberOfFlatsOnFloor',
	'count',
	'totalCost',
	'status',
]);
const objectFields = new Set([
	'flat',
	'coordinates',
	'house',
	'error',
	'countResult',
	'totalCostResult',
]);

function readElement(element: Element): unknown {
	if (
		element.getAttributeNS(
			'http://www.w3.org/2001/XMLSchema-instance',
			'nil',
		) === 'true'
	) {
		return null;
	}
	const children = Array.from(element.children);
	if (children.length > 0 || objectFields.has(element.localName)) {
		return Object.fromEntries(
			children.map((child) => [child.localName, readElement(child)]),
		);
	}
	const text = element.textContent ?? '';
	if (numericFields.has(element.localName)) {
		const value = Number(text);
		if (!text.trim() || !Number.isFinite(value)) {
			throw new Error(`Invalid numeric XML field: ${element.localName}`);
		}
		return value;
	}
	return text;
}

function fromXml(text: string): unknown {
	if (!text.trim()) return undefined;
	const document = new DOMParser().parseFromString(text, 'application/xml');
	if (document.querySelector('parsererror')) {
		throw new Error('The API returned malformed XML');
	}
	const root = document.documentElement;
	if (root.localName === 'flats') {
		return Array.from(root.children).map((flat) => readElement(flat));
	}
	if (!objectFields.has(root.localName)) {
		throw new Error(`Unexpected XML response root: ${root.localName}`);
	}
	return readElement(root);
}

function toXml(data: unknown): string | undefined {
	if (data === undefined || data === null) return undefined;
	if (typeof data === 'string') return data;
	const document = window.document.implementation.createDocument(null, 'flat');
	function append(parent: Element, value: unknown): void {
		if (typeof value === 'object' && value !== null) {
			for (const [name, childValue] of Object.entries(value)) {
				if (childValue === undefined || childValue === null) continue;
				const child = document.createElement(name);
				append(child, childValue);
				parent.appendChild(child);
			}
		} else {
			parent.textContent = String(value);
		}
	}
	append(document.documentElement, data);
	return new XMLSerializer().serializeToString(document);
}

function serializeParams(
	params: Record<string, unknown> | URLSearchParams,
): string {
	if (params instanceof URLSearchParams) return params.toString();
	const query = new URLSearchParams();
	for (const [name, value] of Object.entries(params)) {
		if (value === undefined || value === null) continue;
		if (Array.isArray(value)) {
			if (name === 'sort' && value.length > 0) {
				query.append(name, value.join(','));
			} else {
				for (const item of value) query.append(name, String(item));
			}
		} else {
			query.append(name, String(value));
		}
	}
	return query.toString();
}

export async function xmlConverter<T>(
	config: AxiosRequestConfig,
	options?: AxiosRequestConfig,
): Promise<T> {
	const request = { ...config, ...options };
	const headers = {
		...request.headers,
		Accept: 'application/xml',
		...(request.data !== undefined && request.data !== null
			? { 'Content-Type': 'application/xml' }
			: {}),
	};
	try {
		const response = await axios.request<string>({
			baseURL: import.meta.env.VITE_API_BASE_URL || '/~s373305/soa/api',
			...request,
			headers,
			data: toXml(request.data),
			responseType: 'text',
			transformResponse: [(data) => data],
			paramsSerializer: { serialize: serializeParams },
		});
		return fromXml(response.data) as T;
	} catch (error) {
		if (axios.isAxiosError(error) && typeof error.response?.data === 'string') {
			try {
				error.response.data = fromXml(error.response.data);
			} catch {
				// Keep non-XML proxy errors available with their original HTTP status.
			}
		}
		throw error;
	}
}

export type ErrorType<T> = AxiosError<T>;
