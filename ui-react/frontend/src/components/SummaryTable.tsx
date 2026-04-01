import { useState, useEffect, useCallback } from 'react';
import Paper from '@mui/material/Paper';
import TableContainer from '@mui/material/TableContainer';
import Table from '@mui/material/Table';
import TableHead from '@mui/material/TableHead';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import TablePagination from '@mui/material/TablePagination';
import IconButton from '@mui/material/IconButton';
import Chip from '@mui/material/Chip';
import Collapse from '@mui/material/Collapse';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
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
  const [refreshing, setRefreshing] = useState(false);
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set());
  const [obsData, setObsData] = useState<Record<string, string[] | null>>({});
  const [activeTab, setActiveTab] = useState<Record<string, number>>({});
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);

  const { sort, toggleSort } = useSorting<SortColumn>('scoreNumeric');
  const { filters, setFilters, filtered, sourceOptions, destinationOptions } = useFilters(rows, sort);

  const loadData = useCallback((initial: boolean) => {
    if (initial) setLoading(true); else setRefreshing(true);
    fetchSummary()
      .then(data => {
        const mapped: SummaryRow[] = data.results.map(r => ({
          ...r,
          scoreNumeric: parseFloat(r.score) || 0,
        }));
        setRows(mapped);
        setError(null);
      })
      .catch(e => setError(String(e)))
      .finally(() => { setLoading(false); setRefreshing(false); });
  }, []);

  useEffect(() => { loadData(true); }, [loadData]);

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

  const handleTabChange = (key: string, row: SummaryRow, tab: number) => {
    setActiveTab(prev => ({ ...prev, [key]: tab }));
    if (tab === 1 && !(key in obsData)) {
      setObsData(d => ({ ...d, [key]: null }));
      fetchHistory(row.sourceIp, row.destinationIp)
        .then(res => setObsData(d => ({ ...d, [key]: res.history })))
        .catch(() => setObsData(d => ({ ...d, [key]: ['Error loading history'] })));
    }
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
          onRefresh={() => loadData(false)}
          refreshing={refreshing}
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
              </TableRow>
            </TableHead>
            <TableBody>
              {pageRows.map(row => {
                const key = rowKey(row);
                const expanded = expandedRows.has(key);
                const tab = activeTab[key] ?? 0;
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
                        <IconButton size="small" onClick={() => toggleExpand(key)}>
                          {expanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
                        </IconButton>
                      </TableCell>
                    </TableRow>
                    <TableRow key={`${key}-detail`}>
                      <TableCell colSpan={6} sx={{ py: 0, border: 0 }}>
                        <Collapse in={expanded} unmountOnExit>
                          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                            <Tabs
                              value={tab}
                              onChange={(_, v) => handleTabChange(key, row, v)}
                              textColor="inherit"
                              indicatorColor="primary"
                            >
                              <Tab label="Patterns" sx={{ textTransform: 'none', minHeight: 36 }} />
                              <Tab label="Data" sx={{ textTransform: 'none', minHeight: 36 }} />
                            </Tabs>
                          </Box>
                          {tab === 0 && (
                            <List dense sx={{ pl: 2 }}>
                              {row.details.map((d, i) => {
                                const isSub = d.startsWith('- ');
                                return (
                                  <ListItem key={i} sx={{ py: 0, pl: isSub ? 4 : 0 }}>
                                    <ListItemText primary={isSub ? d.slice(2) : d} />
                                  </ListItem>
                                );
                              })}
                            </List>
                          )}
                          {tab === 1 && (
                            !obs ? (
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
                            )
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
