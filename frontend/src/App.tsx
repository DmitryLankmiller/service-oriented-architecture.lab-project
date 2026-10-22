import { useState } from 'react';
import './App.css';

function App() {
	const [count, setCount] = useState(0);
	const [greeting, setGreeting] = useState('');
	const [sent, setSent] = useState(false);
	const [name, setName] = useState('');

	return (
		<>
			<div>
				<h1>Hello, World!</h1>
			</div>
			<div>
				<button type="button" onClick={() => setCount((count) => count + 1)}>
					Count is {count}
				</button>
			</div>
			<br />
			<div>
				<input
					value={name}
					onChange={(e) => setName(e.target.value)}
					type="text"
				/>
				<button
					type="button"
					onClick={async () => {
						const params = new URLSearchParams({ name });
						const res = await fetch(`/~s373305/soa/api/greeting?${params}`);
						setSent(true);
						setGreeting((await res.json())['content']);
					}}>
					Send Greeting
				</button>
			</div>
			<br />
			<div>{sent ? greeting || 'Loading...' : ''}</div>
		</>
	);
}

export default App;
