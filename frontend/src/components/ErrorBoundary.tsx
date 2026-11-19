import { Component, type ErrorInfo, type ReactNode } from 'react';

export class ErrorBoundary extends Component<
	{ children: ReactNode },
	{ failed: boolean }
> {
	state = { failed: false };
	static getDerivedStateFromError() {
		return { failed: true };
	}
	componentDidCatch(error: Error, info: ErrorInfo) {
		console.error('Ошибка интерфейса', error, info.componentStack);
	}
	render() {
		if (!this.state.failed) return this.props.children;
		return (
			<main className="recovery-screen" role="alert">
				<h1>Не удалось отобразить страницу</h1>
				<p>Обновите страницу, чтобы восстановить работу приложения.</p>
				<button
					className="button primary"
					type="button"
					onClick={() => window.location.reload()}
				>
					Обновить страницу
				</button>
			</main>
		);
	}
}
