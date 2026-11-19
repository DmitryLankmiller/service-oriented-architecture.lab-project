import { fieldByKey, type FieldKey } from './flats';

export type FilterRule = {
	id: string;
	field: FieldKey;
	operator: string;
	value: string;
};
export type SortRule = {
	id: string;
	field: FieldKey;
	direction: 'asc' | 'desc';
};
export const operatorLabels: Record<string, string> = {
	eq: 'Равно',
	neq: 'Не равно',
	gt: 'Больше',
	ge: 'Не меньше',
	lt: 'Меньше',
	le: 'Не больше',
	contains: 'Содержит',
	ncontains: 'Не содержит',
	isnull: 'Не указано',
	notnull: 'Указано',
};
export function operators(field: FieldKey) {
	const definition = fieldByKey(field);
	return [
		...(definition.kind === 'text'
			? ['eq', 'neq', 'contains', 'ncontains']
			: ['eq', 'neq', 'gt', 'ge', 'lt', 'le']),
		...(definition.nullable ? ['isnull', 'notnull'] : []),
	];
}
export function filterStrings(rules: FilterRule[]) {
	return rules.map(
		(rule) =>
			`${rule.field}_${rule.operator === 'isnull' ? 'eq' : rule.operator === 'notnull' ? 'neq' : rule.operator}_${['isnull', 'notnull'].includes(rule.operator) ? 'null' : rule.value}`,
	);
}
