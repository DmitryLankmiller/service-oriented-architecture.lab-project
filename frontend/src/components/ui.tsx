import { useEffect, useId, useRef, type ReactNode } from 'react';

const paths = {
	grid: (
		<>
			<rect x="3" y="3" width="18" height="18" rx="3" />
			<path d="M3 9h18M9 9v12" />
		</>
	),
	home: (
		<>
			<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1Z" />
		</>
	),
	plus: <path d="M12 5v14M5 12h14" />,
	close: <path d="m6 6 12 12M18 6 6 18" />,
	filter: (
		<>
			<path d="M4 6h16M7 12h10M10 18h4" />
			<circle cx="8" cy="6" r="2" fill="currentColor" stroke="none" />
		</>
	),
	sort: (
		<>
			<path d="M8 4v16m-4-4 4 4 4-4M15 5h5M15 10h4M15 15h3" />
		</>
	),
	search: (
		<>
			<circle cx="10.5" cy="10.5" r="6.5" />
			<path d="m16 16 4 4" />
		</>
	),
	refresh: (
		<>
			<path d="M20 7v5h-5M4 17v-5h5" />
			<path d="M6 7a7 7 0 0 1 12-1l2 3M4 15l2 3a7 7 0 0 0 12-1" />
		</>
	),
	left: <path d="m14 6-6 6 6 6" />,
	right: <path d="m10 6 6 6-6 6" />,
	edit: (
		<>
			<path d="m15 4 5 5-11 11H4v-5Z" />
			<path d="m12 7 5 5" />
		</>
	),
	trash: (
		<>
			<path d="M3 6h18M9 6V3h6v3M6 6l1 15h10l1-15M10 10v7M14 10v7" />
		</>
	),
	arrow: (
		<>
			<path d="M7 17 17 7M7 7h10v10" />
		</>
	),
	functions: (
		<>
			<path d="M8 4h10M8 12h8M5 21l4-17M12 21l7-9" />
		</>
	),
	check: <path d="m5 12 4 4L19 6" />,
	more: (
		<>
			<circle cx="5" cy="12" r="1" />
			<circle cx="12" cy="12" r="1" />
			<circle cx="19" cy="12" r="1" />
		</>
	),
	info: (
		<>
			<circle cx="12" cy="12" r="9" />
			<path d="M12 11v6M12 7h.01" />
		</>
	),
};
export type IconName = keyof typeof paths;
export function Icon({ name, size = 18 }: { name: IconName; size?: number }) {
	return (
		<svg
			width={size}
			height={size}
			viewBox="0 0 24 24"
			fill="none"
			stroke="currentColor"
			strokeWidth="1.6"
			strokeLinecap="round"
			strokeLinejoin="round"
			aria-hidden="true">
			{paths[name]}
		</svg>
	);
}
export function ErrorNotice({
	message,
	onRetry,
}: {
	message: string;
	onRetry?: () => void;
}) {
	return (
		<div className="error-notice" role="alert">
			<Icon name="info" />
			<span>{message}</span>
			{onRetry && (
				<button type="button" onClick={onRetry}>
					Повторить
				</button>
			)}
		</div>
	);
}
export function EmptyState({
	title = 'Ничего не найдено',
	children,
}: {
	title?: string;
	children?: ReactNode;
}) {
	return (
		<div className="empty-state">
			<span className="empty-icon">
				<Icon name="grid" size={26} />
			</span>
			<h3>{title}</h3>
			{children}
		</div>
	);
}
export function Field({
	label,
	error,
	required,
	children,
	hint,
}: {
	label: string;
	error?: string;
	required?: boolean;
	children: ReactNode;
	hint?: string;
}) {
	return (
		<label className={`field ${error ? 'field-error' : ''}`}>
			<span>
				{label}
				{required && <b className="required"> *</b>}
			</span>
			{children}
			{error ? (
				<small role="alert">{error}</small>
			) : (
				hint && <small>{hint}</small>
			)}
		</label>
	);
}
export function Modal({
	title,
	children,
	onClose,
	wide = false,
	drawer = false,
	busy = false,
}: {
	title: string;
	children: ReactNode;
	onClose: () => void;
	wide?: boolean;
	drawer?: boolean;
	busy?: boolean;
}) {
	const ref = useRef<HTMLDialogElement>(null);
	const titleId = useId();
	useEffect(() => {
		const dialog = ref.current!;
		const previouslyFocused =
			document.activeElement instanceof HTMLElement
				? document.activeElement
				: null;
		dialog.showModal();
		return () => {
			dialog.close();
			previouslyFocused?.focus();
		};
	}, []);
	return (
		<dialog
			ref={ref}
			className={`modal ${wide ? 'modal-wide' : ''} ${drawer ? 'drawer' : ''}`}
			aria-labelledby={titleId}
			onCancel={(event) => {
				event.preventDefault();
				if (!busy) onClose();
			}}
			onClick={(event) => {
				if (event.target === event.currentTarget && !busy) {
					const box = event.currentTarget.getBoundingClientRect();
					if (
						event.clientX < box.left ||
						event.clientX > box.right ||
						event.clientY < box.top ||
						event.clientY > box.bottom
					)
						onClose();
				}
			}}>
			<div className="modal-header">
				<h2 id={titleId}>{title}</h2>
				<button
					type="button"
					className="icon-button"
					aria-label="Закрыть"
					disabled={busy}
					onClick={onClose}>
					<Icon name="close" />
				</button>
			</div>
			{children}
		</dialog>
	);
}
