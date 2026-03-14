import { useState, useMemo, useEffect } from 'react';
import { SummaryRow } from '../api/client';
import { SortState } from './useSorting';

export type SortColumn =
  | 'source'
  | 'destination'
  | 'lastSeen'
  | 'isCurrent'
  | 'scoreNumeric'
  | 'totalBytes'
  | 'totalTimes';

export interface FilterState {
  textSearch: string;
  currentOnly: boolean;
  scoreRange: [number, number];
  bytesRange: [number, number];
  timesRange: [number, number];
}

export interface Bounds {
  score: [number, number];
  bytes: [number, number];
  times: [number, number];
}

export interface UseFiltersResult {
  filters: FilterState;
  setFilters: React.Dispatch<React.SetStateAction<FilterState>>;
  filtered: SummaryRow[];
  bounds: Bounds;
}

function comparator(a: SummaryRow, b: SummaryRow, sort: SortState<SortColumn>): number {
  if (!sort.column) return 0;
  const col = sort.column;
  let cmp = 0;

  if (col === 'scoreNumeric' || col === 'totalBytes' || col === 'totalTimes') {
    cmp = (a[col] as number) - (b[col] as number);
  } else if (col === 'lastSeen') {
    cmp = new Date(a.lastSeen).getTime() - new Date(b.lastSeen).getTime();
  } else {
    cmp = String(a[col]).localeCompare(String(b[col]));
  }

  return sort.direction === 'asc' ? cmp : -cmp;
}

export function useFilters(rows: SummaryRow[], sort: SortState<SortColumn>): UseFiltersResult {
  const bounds = useMemo<Bounds>(() => {
    if (rows.length === 0) {
      return { score: [0, 0], bytes: [0, 0], times: [0, 0] };
    }
    let minScore = Infinity, maxScore = -Infinity;
    let minBytes = Infinity, maxBytes = -Infinity;
    let minTimes = Infinity, maxTimes = -Infinity;
    for (const r of rows) {
      if (r.scoreNumeric < minScore) minScore = r.scoreNumeric;
      if (r.scoreNumeric > maxScore) maxScore = r.scoreNumeric;
      if (r.totalBytes < minBytes) minBytes = r.totalBytes;
      if (r.totalBytes > maxBytes) maxBytes = r.totalBytes;
      if (r.totalTimes < minTimes) minTimes = r.totalTimes;
      if (r.totalTimes > maxTimes) maxTimes = r.totalTimes;
    }
    return {
      score: [minScore, maxScore],
      bytes: [minBytes, maxBytes],
      times: [minTimes, maxTimes],
    };
  }, [rows]);

  const [filters, setFilters] = useState<FilterState>({
    textSearch: '',
    currentOnly: false,
    scoreRange: [0, 0],
    bytesRange: [0, 0],
    timesRange: [0, 0],
  });

  // Sync filter ranges to bounds after first data load
  useEffect(() => {
    if (rows.length === 0) return;
    setFilters(prev => ({
      ...prev,
      scoreRange: bounds.score,
      bytesRange: bounds.bytes,
      timesRange: bounds.times,
    }));
  }, [bounds, rows.length]);

  const filtered = useMemo<SummaryRow[]>(() => {
    let result = rows;

    if (filters.textSearch.trim()) {
      const q = filters.textSearch.toLowerCase();
      result = result.filter(
        r =>
          r.source.toLowerCase().includes(q) ||
          r.destination.toLowerCase().includes(q)
      );
    }

    if (filters.currentOnly) {
      result = result.filter(r => r.isCurrent === 'Yes');
    }

    const [minScore, maxScore] = filters.scoreRange;
    const [minBytes, maxBytes] = filters.bytesRange;
    const [minTimes, maxTimes] = filters.timesRange;

    result = result.filter(
      r =>
        r.scoreNumeric >= minScore &&
        r.scoreNumeric <= maxScore &&
        r.totalBytes >= minBytes &&
        r.totalBytes <= maxBytes &&
        r.totalTimes >= minTimes &&
        r.totalTimes <= maxTimes
    );

    if (sort.column) {
      result = [...result].sort((a, b) => comparator(a, b, sort));
    }

    return result;
  }, [rows, filters, sort]);

  return { filters, setFilters, filtered, bounds };
}
