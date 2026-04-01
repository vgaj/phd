import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import InputLabel from '@mui/material/InputLabel';
import FormControl from '@mui/material/FormControl';
import SearchOffIcon from '@mui/icons-material/SearchOff';
import RefreshIcon from '@mui/icons-material/Refresh';
import CircularProgress from '@mui/material/CircularProgress';
import { FilterState, LastSeenWindow } from '../hooks/useFilters';

interface Props {
  filters: FilterState;
  setFilters: React.Dispatch<React.SetStateAction<FilterState>>;
  sourceOptions: string[];
  destinationOptions: string[];
  resultCount: number;
  totalCount: number;
  onRefresh: () => void;
  refreshing: boolean;
}

export default function FilterBar({ filters, setFilters, sourceOptions, destinationOptions, resultCount, totalCount, onRefresh, refreshing }: Props) {
  const resetFilters = () => {
    setFilters(prev => ({
      ...prev,
      source: '',
      destination: '',
      currentOnly: false,
      minScore: 0,
      lastSeenWindow: 'all',
    }));
  };

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>

        {/* Row 1: Source + Destination side by side */}
        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
          <FormControl size="small" fullWidth>
            <InputLabel>Source</InputLabel>
            <Select
              label="Source"
              value={filters.source}
              onChange={e => setFilters(prev => ({ ...prev, source: e.target.value }))}
            >
              {sourceOptions.map(opt => (
                <MenuItem key={opt} value={opt}>{opt === '' ? 'All' : opt}</MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl size="small" fullWidth>
            <InputLabel>Destination</InputLabel>
            <Select
              label="Destination"
              value={filters.destination}
              onChange={e => setFilters(prev => ({ ...prev, destination: e.target.value }))}
            >
              {destinationOptions.map(opt => (
                <MenuItem key={opt} value={opt}>{opt === '' ? 'All' : opt}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>

        {/* Row 2: remaining filters + result count + clear */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'center' }}>
          <FormControlLabel
            control={
              <Switch
                checked={filters.currentOnly}
                onChange={e => setFilters(prev => ({ ...prev, currentOnly: e.target.checked }))}
              />
            }
            label="Active only"
          />

          <FormControl size="small" sx={{ minWidth: 130 }}>
            <InputLabel>Min score</InputLabel>
            <Select
              label="Min score"
              value={filters.minScore}
              onChange={e => setFilters(prev => ({ ...prev, minScore: e.target.value as number }))}
            >
              <MenuItem value={0}>Any</MenuItem>
              {[9, 8, 7, 6, 5, 4, 3, 2, 1].map(n => (
                <MenuItem key={n} value={n}>{n}</MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 130 }}>
            <InputLabel>Last seen</InputLabel>
            <Select
              label="Last seen"
              value={filters.lastSeenWindow}
              onChange={e => setFilters(prev => ({ ...prev, lastSeenWindow: e.target.value as LastSeenWindow }))}
            >
              <MenuItem value="hour">Last hour</MenuItem>
              <MenuItem value="day">Last day</MenuItem>
              <MenuItem value="week">Last week</MenuItem>
              <MenuItem value="all">All</MenuItem>
            </Select>
          </FormControl>

          <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            {resultCount < totalCount && (
              <Chip
                icon={resultCount === 0 ? <SearchOffIcon /> : undefined}
                label={`${resultCount} / ${totalCount} shown`}
                color={resultCount === 0 ? 'default' : 'primary'}
                size="small"
              />
            )}
            <Button size="small" variant="outlined" onClick={resetFilters}>
              Clear filters
            </Button>
            <Button
              size="small"
              variant="outlined"
              onClick={onRefresh}
              disabled={refreshing}
              startIcon={refreshing ? <CircularProgress size={14} /> : <RefreshIcon />}
            >
              Refresh
            </Button>
          </Box>
        </Box>

      </Box>
    </Paper>
  );
}
