import { useState, type FormEvent } from 'react';
import { fieldByKey, fields, validateValue, type FieldKey } from '../lib/flats';
import { focusInvalid } from '../lib/forms';
import {
	operators,
	operatorLabels,
	type FilterRule,
	type SortRule,
} from '../lib/query';
import { ErrorNotice, Field, Icon, Modal } from './ui';
export function QueryBuilder({
	mode,
	initialFilters,
	initialSorts,
	onClose,
	onApply,
}: {
	mode: 'filter' | 'sort';
	initialFilters: FilterRule[];
	initialSorts: SortRule[];
	onClose: () => void;
	onApply: (filters: FilterRule[], sorts: SortRule[]) => void;
}) {
	const [filters, setFilters] = useState(initialFilters);
	const [sorts, setSorts] = useState(initialSorts);
	const [errors, setErrors] = useState<Record<string, string>>({});
	function updateFilter(id: string, values: Partial<FilterRule>) {
		setFilters((previous) =>
			previous.map((rule) => (rule.id === id ? { ...rule, ...values } : rule)),
		);
		setErrors((previous) => ({ ...previous, [id]: '' }));
	}
	function save(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		const validation: Record<string, string> = {};
		if (mode === 'filter')
			for (const rule of filters) {
				if (!['isnull', 'notnull'].includes(rule.operator)) {
					const error = validateValue(fieldByKey(rule.field), rule.value, true);
					if (error) validation[rule.id] = error;
				}
			}
		if (
			mode === 'sort' &&
			new Set(sorts.map((sort) => sort.field)).size !== sorts.length
		)
			validation.sort = 'Выберите разные поля сортировки.';
		setErrors(validation);
		if (Object.keys(validation).length) {
			focusInvalid(event.currentTarget);
			return;
		}
		onApply(filters, sorts);
	}
	function move(index: number, step: number) {
		setSorts((previous) => {
			const next = [...previous];
			[next[index], next[index + step]] = [next[index + step], next[index]];
			return next;
		});
	}
	return (
		<Modal
			title={mode === 'filter' ? 'Фильтры' : 'Сортировка'}
			onClose={onClose}
			wide>
			<form onSubmit={save} noValidate>
				<div className="modal-body query-builder">
					{mode === 'filter' ? (
						<>
							<p className="muted panel-caption">
								Все условия должны выполняться одновременно.
							</p>
							{filters.length === 0 && (
								<p className="builder-empty">Фильтры не заданы</p>
							)}
							{filters.map((rule, index) => {
								const definition = fieldByKey(rule.field);
								const noValue = ['isnull', 'notnull'].includes(rule.operator);
								return (
									<div className="query-rule" key={rule.id}>
										<Field label={`Поле ${index + 1}`}>
											<select
												value={rule.field}
												onChange={(event) => {
													const field = event.target.value as FieldKey;
													updateFilter(rule.id, {
														field,
														operator: operators(field)[0],
														value: '',
													});
												}}>
												{fields.map((field) => (
													<option value={field.key} key={field.key}>
														{field.label}
													</option>
												))}
											</select>
										</Field>
										<Field label="Условие">
											<select
												value={rule.operator}
												onChange={(event) =>
													updateFilter(rule.id, {
														operator: event.target.value,
													})
												}>
												{operators(rule.field).map((operator) => (
													<option key={operator} value={operator}>
														{operatorLabels[operator]}
													</option>
												))}
											</select>
										</Field>
										{!noValue ? (
											<Field label="Значение" error={errors[rule.id]}>
												{definition.options ? (
													<select
														value={rule.value}
														aria-invalid={!!errors[rule.id]}
														onChange={(event) =>
															updateFilter(rule.id, {
																value: event.target.value,
															})
														}>
														<option value="">Выберите значение</option>
														{Object.entries(definition.options).map(
															([key, label]) => (
																<option key={key} value={key}>
																	{label}
																</option>
															),
														)}
													</select>
												) : (
													<input
														value={rule.value}
														type={
															definition.kind === 'text'
																? 'text'
																: definition.kind === 'date'
																	? 'date'
																	: 'number'
														}
														step={definition.kind === 'number' ? 'any' : '1'}
														aria-invalid={!!errors[rule.id]}
														onChange={(event) =>
															updateFilter(rule.id, {
																value: event.target.value,
															})
														}
													/>
												)}
											</Field>
										) : (
											<div className="null-value">Без значения</div>
										)}
										<button
											className="icon-button rule-remove"
											type="button"
											aria-label={`Удалить фильтр ${index + 1}`}
											onClick={() =>
												setFilters(
													filters.filter((item) => item.id !== rule.id),
												)
											}>
											<Icon name="close" size={16} />
										</button>
									</div>
								);
							})}
							<button
								type="button"
								className="button secondary compact"
								onClick={() =>
									setFilters([
										...filters,
										{
											id: crypto.randomUUID(),
											field: 'name',
											operator: 'contains',
											value: '',
										},
									])
								}>
								<Icon name="plus" size={16} />
								Добавить условие
							</button>
						</>
					) : (
						<>
							<p className="muted panel-caption">
								Поля применяются сверху вниз.
							</p>
							{sorts.length === 0 && (
								<p className="builder-empty">По умолчанию — по ID</p>
							)}
							{sorts.map((rule, index) => (
								<div className="sort-rule" key={rule.id}>
									<span className="rule-number">{index + 1}</span>
									<Field label="Поле">
										<select
											value={rule.field}
											onChange={(event) => {
												setSorts(
													sorts.map((item) =>
														item.id === rule.id
															? {
																	...item,
																	field: event.target.value as FieldKey,
																}
															: item,
													),
												);
												setErrors({});
											}}>
											{fields.map((field) => (
												<option
													value={field.key}
													key={field.key}
													disabled={sorts.some(
														(item) =>
															item.id !== rule.id && item.field === field.key,
													)}>
													{field.label}
												</option>
											))}
										</select>
									</Field>
									<Field label="Порядок">
										<select
											value={rule.direction}
											onChange={(event) =>
												setSorts(
													sorts.map((item) =>
														item.id === rule.id
															? {
																	...item,
																	direction: event.target.value as
																		| 'asc'
																		| 'desc',
																}
															: item,
													),
												)
											}>
											<option value="asc">По возрастанию</option>
											<option value="desc">По убыванию</option>
										</select>
									</Field>
									<div className="rule-move">
										<button
											className="icon-button"
											type="button"
											disabled={index === 0}
											aria-label={`Поднять сортировку ${index + 1}`}
											onClick={() => move(index, -1)}>
											<span className="rotate-up">
												<Icon name="right" size={15} />
											</span>
										</button>
										<button
											className="icon-button"
											type="button"
											disabled={index === sorts.length - 1}
											aria-label={`Опустить сортировку ${index + 1}`}
											onClick={() => move(index, 1)}>
											<span className="rotate-down">
												<Icon name="right" size={15} />
											</span>
										</button>
									</div>
									<button
										className="icon-button"
										type="button"
										aria-label={`Удалить сортировку ${index + 1}`}
										onClick={() =>
											setSorts(sorts.filter((item) => item.id !== rule.id))
										}>
										<Icon name="close" size={16} />
									</button>
								</div>
							))}
							{errors.sort && <ErrorNotice message={errors.sort} />}
							<button
								className="button secondary compact"
								type="button"
								disabled={sorts.length === fields.length}
								onClick={() => {
									const field = fields.find(
										(item) => !sorts.some((rule) => rule.field === item.key),
									);
									if (field)
										setSorts([
											...sorts,
											{
												id: crypto.randomUUID(),
												field: field.key,
												direction: 'asc',
											},
										]);
								}}>
								<Icon name="plus" size={16} />
								Добавить поле
							</button>
						</>
					)}
				</div>
				<div className="modal-footer">
					<button
						className="text-button"
						type="button"
						onClick={() => {
							if (mode === 'filter') setFilters([]);
							else setSorts([]);
							setErrors({});
						}}>
						Сбросить
					</button>
					<div className="button-group">
						<button
							className="button secondary"
							type="button"
							onClick={onClose}>
							Отмена
						</button>
						<button className="button primary">Применить</button>
					</div>
				</div>
			</form>
		</Modal>
	);
}
