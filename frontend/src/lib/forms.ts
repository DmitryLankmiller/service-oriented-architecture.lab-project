export function isXmlText(value: string): boolean {
	for (const character of value) {
		const code = character.codePointAt(0)!;
		if (
			!(
				code === 9 ||
				code === 10 ||
				code === 13 ||
				(code >= 32 && code <= 0xd7ff) ||
				(code >= 0xe000 && code <= 0xfffd) ||
				(code >= 0x10000 && code <= 0x10ffff)
			)
		)
			return false;
	}
	return true;
}

export function xmlTextError(value: string): string | undefined {
	return isXmlText(value)
		? undefined
		: 'Текст содержит недопустимые управляющие символы. Удалите их и повторите ввод.';
}

export function focusInvalid(form: HTMLFormElement) {
	requestAnimationFrame(() =>
		form.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus(),
	);
}
