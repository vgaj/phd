import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Switch from '@mui/material/Switch';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Slider from '@mui/material/Slider';
import Chip from '@mui/material/Chip';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SearchOffIcon from '@mui/icons-material/SearchOff';
import { FilterState, Bounds } from '../hooks/useFilters';

interface Props {
  filters: FilterState;
  setFilters: React.Dispatch<React.SetStateAction<FilterState>>;
  bounds: Bounds;
  resultCount: number;
  totalCount: number;
}

export default function FilterBar({ filters, setFilters, bounds, resultCount, totalCount }: Props) {
  const resetFilters = () => {
    setFilters(prev => ({
      ...prev,
      textSearch: '',
      currentOnly: false,
      scoreRange: bounds.score,
      bytesRange: bounds.bytes,
      timesRange: bounds.times,
    }));
  };

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'center' }}>
        <TextField
          size="small"
          label="Search source / destination"
          value={filters.textSearch}
          onChange={e => setFilters(prev => ({ ...prev, textSearch: e.target.value }))}
          sx={{ minWidth: 240 }}
        />

        <FormControlLabel
          control={
            <Switch
              checked={filters.currentOnly}
              onChange={e => setFilters(prev => ({ ...prev, currentOnly: e.target.checked }))}
            />
          }
          label="Active only"
        />

        <RangeFilter
          label="Score range"
          value={filters.scoreRange}
          min={bounds.score[0]}
          max={bounds.score[1]}
          onChange={v => setFilters(prev => ({ ...prev, scoreRange: v as [number, number] }))}
        />

        <RangeFilter
          label="Bytes range"
          value={filters.bytesRange}
          min={bounds.bytes[0]}
          max={bounds.bytes[1]}
          onChange={v => setFilters(prev => ({ ...prev, bytesRange: v as [number, number] }))}
        />

        <RangeFilter
          label="Times range"
          value={filters.timesRange}
          min={bounds.times[0]}
          max={bounds.times[1]}
          onChange={v => setFilters(prev => ({ ...prev, timesRange: v as [number, number] }))}
        />

        <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
          <Chip
            icon={resultCount === 0 ? <SearchOffIcon /> : undefined}
            label={`${resultCount} / ${totalCount} shown`}
            color={resultCount === 0 ? 'default' : 'primary'}
            size="small"
          />
          <Button size="small" variant="outlined" onClick={resetFilters}>
            Clear filters
          </Button>
        </Box>
      </Box>
    </Paper>
  );
}

interface RangeFilterProps {
  label: string;
  value: [number, number];
  min: number;
  max: number;
  onChange: (v: number | number[]) => void;
}

function RangeFilter({ label, value, min, max, onChange }: RangeFilterProps) {
  if (min === max) {
    return (
      <Chip
        label={`${label}: ${min}`}
        size="small"
        variant="outlined"
      />
    );
  }

  return (
    <Accordion disableGutters sx={{ minWidth: 200, flex: '0 0 auto' }}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography variant="body2">{label}</Typography>
      </AccordionSummary>
      <AccordionDetails>
        <Box sx={{ px: 1 }}>
          <Slider
            value={value}
            min={min}
            max={max}
            onChange={(_, v) => onChange(v)}
            valueLabelDisplay="auto"
            size="small"
          />
          <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="caption">{min}</Typography>
            <Typography variant="caption">{max}</Typography>
          </Box>
        </Box>
      </AccordionDetails>
    </Accordion>
  );
}
