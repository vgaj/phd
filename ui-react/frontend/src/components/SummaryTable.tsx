import { useState, useEffect } from 'react';
import Paper from '@mui/material/Paper';
import TableContainer from '@mui/material/TableContainer';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import TablePagination from '@mui/material/TablePagination';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Collapse from '@mui/material/Collapse';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';

import { fetchSummary, fetchHistory, SummaryRow } from '../api/client';
import { useSorting } from '../hooks/useSorting';
import { useFilters, SortColumn } from '../hooks/useFilters';
import SortableHeader from './SortableHeader';
import FilterBar from './FilterBar';

function rowKey(row: SummaryRow): string {
  return `${row.sourceIp}|${row.destinationIp}`;
}

export default function SummaryTable() {
  const [rows, setRows] = useState<SummaryRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set());
  const [expandedObs, setExpandedObs] = useState<Set<string>>(new Set());
  const [obsData, setObsData] = useState<Record<string, string[] | null>>({});
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  const { sort, toggleSort } = useSorting<SortColumn>('scoreNumeric');
  const { filters, setFilters, filtered, sourceOptions, destinationOptions } = useFilters(rows, sort);

  useEffect(() => {
    fetchSummary()
      .then(data => {
        const mapped: SummaryRow[] = data.results.map(r => ({
          ...r,
          scoreNumeric: parseFloat(r.score) || 0,
        }));
        setRows(mapped);
      })
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, []);

  // Reset page when filters or sort change
  useEffect(() => {
    setPage(0);
  }, [filters, sort]);

  const toggleExpand = (key: string) => {
    setExpandedRows(prev => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key); else next.add(key);
      return next;
    });
  };

  const toggleObs = (row: SummaryRow) => {
    const key = rowKey(row);
    setExpandedObs(prev => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
        if (!(key in obsData)) {
          setObsData(d => ({ ...d, [key]: null }));
          fetchHistory(row.sourceIp, row.destinationIp)
            .then(res => setObsData(d => ({ ...d, [key]: res.history })))
            .catch(() => setObsData(d => ({ ...d, [key]: ['Error loading history'] })));
        }
      }
      return next;
    });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  const pageRows = filtered.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

  return (
    <Box>
      <Paper>
        <FilterBar
          filters={filters}
          setFilters={setFilters}
          sourceOptions={sourceOptions}
          destinationOptions={destinationOptions}
          resultCount={filtered.length}
          totalCount={rows.length}
        />

        <TableContainer>
          <Table stickyHeader size="small">
            <TableHead>
              <TableRow>
                <SortableHeader column="source" label="Source" sort={sort} onSort={toggleSort} />
                <SortableHeader column="destination" label="Destination" sort={sort} onSort={toggleSort} />
                <SortableHeader column="lastSeen" label="Last Seen" sort={sort} onSort={toggleSort} />
                <SortableHeader column="isCurrent" label="Current" sort={sort} onSort={toggleSort} />
                <SortableHeader column="scoreNumeric" label="Score" sort={sort} onSort={toggleSort} />
                <TableCell />
                <TableCell />
              </TableRow>
            </TableHead>
            <TableBody>
              {pageRows.map(row => {
                const key = rowKey(row);
                const expanded = expandedRows.has(key);
                const obsExpanded = expandedObs.has(key);
                const obs = obsData[key];
                return (
                  <>
                    <TableRow key={key} hover>
                      <TableCell>{row.source}</TableCell>
                      <TableCell>{row.destination}</TableCell>
                      <TableCell>{row.lastSeen}</TableCell>
                      <TableCell>
                        <Chip
                          label={row.isCurrent}
                          color={row.isCurrent === 'Yes' ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{row.score}</TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="text"
                          endIcon={expanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                          onClick={() => toggleExpand(key)}
                          sx={{ textTransform: 'none' }}
                        >
                          patterns
                        </Button>
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="text"
                          endIcon={obsExpanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                          onClick={() => toggleObs(row)}
                          sx={{ textTransform: 'none' }}
                        >
                          data
                        </Button>
                      </TableCell>
                    </TableRow>
                    <TableRow key={`${key}-expand`}>
                      <TableCell colSpan={7} sx={{ py: 0, border: 0 }}>
                        <Collapse in={expanded} unmountOnExit>
                          <List dense sx={{ pl: 2 }}>
                            {row.details.map((d, i) => {
                              const isSub = d.startsWith('- ');
                              return (
                                <ListItem key={i} sx={{ py: 0, pl: isSub ? 4 : 0 }}>
                                  <ListItemText primary={isSub ? d.slice(2) : `• ${d}`} />
                                </ListItem>
                              );
                            })}
                          </List>
                        </Collapse>
                      </TableCell>
                    </TableRow>
                    <TableRow key={`${key}-obs`}>
                      <TableCell colSpan={7} sx={{ py: 0, border: 0 }}>
                        <Collapse in={obsExpanded} unmountOnExit>
                          {!obs ? (
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, p: 1, pl: 2 }}>
                              <CircularProgress size={16} />
                              <Typography variant="body2">Loading...</Typography>
                            </Box>
                          ) : (
                            <List dense sx={{ pl: 2 }}>
                              {obs.map((line, i) => (
                                <ListItem key={i} sx={{ py: 0 }}>
                                  <ListItemText primary={line} />
                                </ListItem>
                              ))}
                            </List>
                          )}
                        </Collapse>
                      </TableCell>
                    </TableRow>
                  </>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>

        <TablePagination
          rowsPerPageOptions={[10, 25, 50, 100]}
          component="div"
          count={filtered.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={e => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
        />
      </Paper>
    </Box>
  );
}
