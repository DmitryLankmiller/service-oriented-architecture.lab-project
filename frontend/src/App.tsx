import { useEffect, useState, type FormEvent } from 'react';
import type { Flat } from './api/models/flats';
import { FlatDetails } from './components/FlatDetails';
import { FlatEditor } from './components/FlatEditor';
import { FlatsTable } from './components/FlatsTable';
import { Operations } from './components/Operations';
import { QueryBuilder } from './components/QueryBuilder';
import { ErrorNotice, Field, Icon } from './components/ui';
import { focusInvalid, xmlTextError } from './lib/forms';
import {
	filterStrings,
	operatorLabels,
	type FilterRule,
	type SortRule,
} from './lib/query';
import { fieldByKey, validateValue } from './lib/flats';
import { useFlatPage } from './lib/useFlatPage';
import './App.css';

type Section = 'flats' | 'operations';
type Dialog =
	| { kind: 'create' }
	| { kind: 'edit'; flat: Flat }
	| { kind: 'details'; id: number; viaAgency?: boolean }
	| null;
const currentSection = (): Section =>
	window.location.hash === '#operations' ? 'operations' : 'flats';

function App() {
	const [section, setSection] = useState<Section>(currentSection);
	const [dialog, setDialog] = useState<Dialog>(null);
	const [page, setPage] = useState(0);
	const [size, setSize] = useState(10);
	const [pageInput, setPageInput] = useState('1');
	const [pageError, setPageError] = useState('');
	const [search, setSearch] = useState('');
	const searchError = xmlTextError(search);
	const [appliedSearch, setAppliedSearch] = useState('');
	const [filters, setFilters] = useState<FilterRule[]>([]);
	const [sorts, setSorts] = useState<SortRule[]>([]);
	const [builder, setBuilder] = useState<'filter' | 'sort' | null>(null);
	const [lookup, setLookup] = useState('');
	const [lookupError, setLookupError] = useState('');
	const [revision, setRevision] = useState(0);
	const [toast, setToast] = useState('');
	const params = {
		page,
		size,
		filter: [
			...filterStrings(filters),
			...(appliedSearch ? [`name_contains_${appliedSearch}`] : []),
		],
		sort: sorts.length
			? sorts.map((sort) => `${sort.field}_${sort.direction}`)
			: ['id_asc'],
	};
	const list = useFlatPage(params, revision);
	useEffect(() => {
		const change = () => {
			setSection(currentSection());
			window.scrollTo(0, 0);
		};
		window.addEventListener('hashchange', change);
		return () => window.removeEventListener('hashchange', change);
	}, []);
	useEffect(() => {
		if (!toast) return;
		const timer = setTimeout(() => setToast(''), 4500);
		return () => clearTimeout(timer);
	}, [toast]);
	function goToPage(next: number) {
		setPage(next);
		setPageInput(String(next + 1));
		setPageError('');
	}
	function changed(message: string) {
		setRevision((previous) => previous + 1);
		setToast(message);
	}
	function open(id: number, viaAgency = false) {
		setDialog({ kind: 'details', id, viaAgency });
	}
	function lookupFlat(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		const message = validateValue(fieldByKey('id'), lookup);
		setLookupError(message ?? '');
		if (message) {
			focusInvalid(event.currentTarget);
			return;
		}
		open(Number(lookup));
	}
	function jump(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		const numeric = Number(pageInput);
		const max = Math.floor(2147483647 / size);
		if (!Number.isSafeInteger(numeric) || numeric < 1 || numeric - 1 > max) {
			setPageError(`Укажите страницу от 1 до ${max + 1}.`);
			focusInvalid(event.currentTarget);
			return;
		}
		goToPage(numeric - 1);
	}
	return (
		<div className="app-shell">
			<aside className="sidebar">
				<a className="brand" href="#flats">
					<span className="brand-icon">
						<Icon name="home" size={23} />
					</span>
					<span>
						<i>flats</i>
					</span>
				</a>
				<div className="workspace">
					<strong>Меню</strong>
				</div>
				<nav aria-label="Главное меню">
					<a
						href="#flats"
						className={section === 'flats' ? 'active' : ''}
						aria-current={section === 'flats' ? 'page' : undefined}
						title="Квартиры"
					>
						<Icon name="grid" />
						<span>Квартиры</span>
					</a>
					<a
						href="#operations"
						className={section === 'operations' ? 'active' : ''}
						aria-current={section === 'operations' ? 'page' : undefined}
						title="Специальные операции"
					>
						<Icon name="functions" />
						<span>Специальные операции</span>
					</a>
				</nav>
			</aside>
			<div className="main-shell">
				<main>
					{section === 'flats' ? (
						<>
							<header className="page-heading">
								<h1>Квартиры</h1>
								<button
									type="button"
									className="button primary"
									onClick={() => setDialog({ kind: 'create' })}
								>
									<Icon name="plus" size={18} />
									Добавить квартиру
								</button>
							</header>
							<div className="view-toolbar">
								<div className="view-label">
									<Icon name="grid" size={17} />
									<strong>Все квартиры</strong>
								</div>
								<button
									className="icon-button"
									type="button"
									disabled={list.loading}
									title="Обновить"
									aria-label="Обновить квартиры"
									onClick={() => setRevision((value) => value + 1)}
								>
									<Icon name="refresh" size={17} />
								</button>
							</div>
							<div className="filters-toolbar">
								<form
									className="search-input"
									role="search"
									onSubmit={(event) => {
										event.preventDefault();
										if (searchError) return;
										setAppliedSearch(search.trim());
										goToPage(0);
									}}
								>
									<Icon name="search" size={18} />
									<input
										aria-label="Поиск по названию"
										aria-invalid={!!searchError}
										placeholder="Поиск по названию…"
										value={search}
										onChange={(event) => setSearch(event.target.value)}
									/>
									{search && (
										<button
											type="button"
											className="icon-button small"
											aria-label="Очистить поиск"
											onClick={() => {
												setSearch('');
												setAppliedSearch('');
												goToPage(0);
											}}
										>
											<Icon name="close" size={14} />
										</button>
									)}
									<button type="submit" className="text-button">
										Найти
									</button>
								</form>
								<button
									className={`button secondary ${filters.length ? 'selected' : ''}`}
									type="button"
									onClick={() => setBuilder('filter')}
								>
									<Icon name="filter" size={17} />
									Фильтры
									{filters.length > 0 && (
										<span className="count-badge">{filters.length}</span>
									)}
								</button>
								<button
									className={`button secondary ${sorts.length ? 'selected' : ''}`}
									type="button"
									onClick={() => setBuilder('sort')}
								>
									<Icon name="sort" size={17} />
									Сортировка
									{sorts.length > 0 && (
										<span className="count-badge">{sorts.length}</span>
									)}
								</button>
							</div>
							{(filters.length > 0 || sorts.length > 0 || appliedSearch) && (
								<div className="query-chips">
									{appliedSearch && (
										<span className="query-chip">
											Название содержит «{appliedSearch}»
											<button
												type="button"
												aria-label="Убрать поиск"
												onClick={() => {
													setSearch('');
													setAppliedSearch('');
													goToPage(0);
												}}
											>
												<Icon name="close" size={13} />
											</button>
										</span>
									)}
									{filters.map((rule) => (
										<span className="query-chip" key={rule.id}>
											{fieldByKey(rule.field).label} ·{' '}
											{operatorLabels[rule.operator].toLocaleLowerCase('ru')}{' '}
											{!['isnull', 'notnull'].includes(rule.operator) &&
												(fieldByKey(rule.field).options?.[rule.value] ??
													(rule.value || '«»'))}
											<button
												type="button"
												aria-label={`Убрать фильтр ${fieldByKey(rule.field).label}`}
												onClick={() => {
													setFilters(
														filters.filter((item) => item.id !== rule.id),
													);
													goToPage(0);
												}}
											>
												<Icon name="close" size={13} />
											</button>
										</span>
									))}
									{sorts.map((rule, index) => (
										<span className="query-chip" key={rule.id}>
											{index + 1}. {fieldByKey(rule.field).label}{' '}
											{rule.direction === 'asc' ? '↑' : '↓'}
											<button
												type="button"
												aria-label={`Убрать сортировку ${fieldByKey(rule.field).label}`}
												onClick={() => {
													setSorts(sorts.filter((item) => item.id !== rule.id));
													goToPage(0);
												}}
											>
												<Icon name="close" size={13} />
											</button>
										</span>
									))}
									<button
										className="text-button"
										type="button"
										onClick={() => {
											setFilters([]);
											setSorts([]);
											setSearch('');
											setAppliedSearch('');
											goToPage(0);
										}}
									>
										Сбросить всё
									</button>
								</div>
							)}
							{searchError && <ErrorNotice message={searchError} />}
							{list.error && !list.loading ? (
								<ErrorNotice
									message={list.error}
									onRetry={() => setRevision((value) => value + 1)}
								/>
							) : (
								<FlatsTable
									flats={list.flats}
									loading={list.loading}
									onOpen={open}
									onEdit={(flat) => setDialog({ kind: 'edit', flat })}
								/>
							)}
							<div className="pagination">
								<span className="pagination-range muted">
									{list.loading
										? 'Загрузка…'
										: list.error
											? 'Не удалось загрузить страницу'
											: list.flats.length
												? `${page * size + 1}–${page * size + list.flats.length}`
												: 'Нет записей'}
								</span>
								<div className="pagination-controls">
									<label>
										На странице
										<select
											aria-label="Квартир на странице"
											value={size}
											disabled={list.loading}
											onChange={(event) => {
												setSize(Number(event.target.value));
												goToPage(0);
											}}
										>
											<option>10</option>
											<option>20</option>
											<option>50</option>
											<option>100</option>
										</select>
									</label>
									<button
										className="icon-button"
										aria-label="Предыдущая страница"
										type="button"
										disabled={page === 0 || list.loading}
										onClick={() => goToPage(page - 1)}
									>
										<Icon name="left" size={16} />
									</button>
									<form onSubmit={jump} className="page-jump" noValidate>
										<label className="sr-only" htmlFor="page-number">
											Номер страницы
										</label>
										<input
											id="page-number"
											type="number"
											min="1"
											value={pageInput}
											aria-invalid={!!pageError}
											disabled={list.loading}
											onChange={(event) => {
												setPageInput(event.target.value);
												setPageError('');
											}}
										/>
										<button className="text-button" disabled={list.loading}>
											Перейти
										</button>
									</form>
									<button
										className="icon-button"
										aria-label="Следующая страница"
										type="button"
										disabled={!list.hasNext || list.loading || !!list.error}
										onClick={() => goToPage(page + 1)}
									>
										<Icon name="right" size={16} />
									</button>
								</div>
							</div>
							{pageError && (
								<p className="field-error-text" role="alert">
									{pageError}
								</p>
							)}
							<section className="lookup-panel">
								<h2>
									<Icon name="search" size={18} />
									Найти квартиру
								</h2>
								<form onSubmit={lookupFlat} noValidate>
									<Field label="ID квартиры" error={lookupError}>
										<input
											type="number"
											min="1"
											step="1"
											placeholder="Например, 7"
											value={lookup}
											aria-invalid={!!lookupError}
											onChange={(event) => {
												setLookup(event.target.value);
												setLookupError('');
											}}
										/>
									</Field>
									<button className="button secondary">
										Открыть
										<Icon name="arrow" size={16} />
									</button>
								</form>
							</section>
						</>
					) : (
						<Operations onOpen={open} onChanged={changed} />
					)}
				</main>
			</div>
			{toast && (
				<div className="toast" role="status">
					<Icon name="check" size={18} />
					{toast}
					<button
						type="button"
						className="icon-button small"
						aria-label="Закрыть уведомление"
						onClick={() => setToast('')}
					>
						<Icon name="close" size={14} />
					</button>
				</div>
			)}
			{builder && (
				<QueryBuilder
					mode={builder}
					initialFilters={filters}
					initialSorts={sorts}
					onClose={() => setBuilder(null)}
					onApply={(nextFilters, nextSorts) => {
						setFilters(nextFilters);
						setSorts(nextSorts);
						goToPage(0);
						setBuilder(null);
					}}
				/>
			)}
			{dialog?.kind === 'create' && (
				<FlatEditor
					onClose={() => setDialog(null)}
					onSaved={(flat) => {
						changed('Квартира добавлена');
						goToPage(0);
						open(flat.id);
					}}
				/>
			)}
			{dialog?.kind === 'edit' && (
				<FlatEditor
					key={`edit-${dialog.flat.id}`}
					flat={dialog.flat}
					onClose={() => open(dialog.flat.id)}
					onSaved={(flat) => {
						changed('Изменения сохранены');
						open(flat.id);
					}}
				/>
			)}
			{dialog?.kind === 'details' && (
				<FlatDetails
					key={`${dialog.id}-${dialog.viaAgency}`}
					id={dialog.id}
					viaAgency={dialog.viaAgency}
					onClose={() => setDialog(null)}
					onEdit={(flat) => setDialog({ kind: 'edit', flat })}
					onChanged={changed}
					onDeleted={() => {
						changed('Квартира удалена');
						goToPage(0);
						setDialog(null);
					}}
				/>
			)}
		</div>
	);
}
export default App;
