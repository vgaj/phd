import { useState, useEffect } from 'react';
import { fetchHistory } from '../api/client';

interface Props {
  source: string;
  destination: string;
  onBack: () => void;
}

export default function HostHistory({ source, destination, onBack }: Props) {
  const [lines, setLines] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory(source, destination)
      .then(data => setLines(data.history))
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, [source, destination]);

  return (
    <div>
      <button onClick={onBack}>Back</button>
      <h2>Observations: {source} → {destination}</h2>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <ul>
        {lines.map((l, i) => <li key={i}>{l}</li>)}
      </ul>
    </div>
  );
}
