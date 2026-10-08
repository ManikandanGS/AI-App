import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaSearch, FaEye } from 'react-icons/fa';
import { getAnalyses } from '../services/api';
import Badge from './common/Badge';
import LoadingSpinner from './common/LoadingSpinner';

const HistoryTable = () => {
  const [analyses, setAnalyses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  useEffect(() => {
    getAnalyses()
      .then(data => setAnalyses(data || []))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner text="Loading History..." />;

  const filteredData = analyses
    .filter(item => 
      item.jiraTicketId?.toLowerCase().includes(searchTerm.toLowerCase()) || 
      item.ticketSummary?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  const totalPages = Math.ceil(filteredData.length / itemsPerPage);
  const currentData = filteredData.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-2xl font-bold text-white">Analysis History</h2>
        
        <div className="relative w-full sm:w-64">
          <FaSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search tickets..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
            className="w-full bg-slate-800 border border-slate-700 text-white rounded-lg pl-10 pr-4 py-2 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          />
        </div>
      </div>

      <div className="bg-slate-800 rounded-xl border border-slate-700 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-900/50 text-slate-400">
              <tr>
                <th className="px-6 py-4 font-medium">Ticket ID</th>
                <th className="px-6 py-4 font-medium hidden md:table-cell">Summary</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium">Severity</th>
                <th className="px-6 py-4 font-medium hidden sm:table-cell">Confidence</th>
                <th className="px-6 py-4 font-medium">Date</th>
                <th className="px-6 py-4 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50 text-slate-300">
              {currentData.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center text-slate-500">
                    No matching analyses found.
                  </td>
                </tr>
              ) : (
                currentData.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-700/30 transition-colors">
                    <td className="px-6 py-4 font-bold text-indigo-400">{item.jiraTicketId}</td>
                    <td className="px-6 py-4 hidden md:table-cell max-w-xs truncate" title={item.ticketSummary}>
                      {item.ticketSummary || '-'}
                    </td>
                    <td className="px-6 py-4"><Badge type={item.status} /></td>
                    <td className="px-6 py-4"><Badge type={item.severity} /></td>
                    <td className="px-6 py-4 hidden sm:table-cell">
                      {item.confidenceScore ? `${item.confidenceScore}%` : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-slate-400">
                      {new Date(item.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link 
                        to={`/analysis/${item.id}`}
                        className="inline-flex items-center justify-center p-2 bg-slate-700 hover:bg-slate-600 text-white rounded-lg transition-colors"
                        title="View Details"
                      >
                        <FaEye />
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
        
        {totalPages > 1 && (
          <div className="p-4 border-t border-slate-700 flex items-center justify-between text-sm">
            <span className="text-slate-400">
              Showing {(currentPage - 1) * itemsPerPage + 1} to {Math.min(currentPage * itemsPerPage, filteredData.length)} of {filteredData.length} entries
            </span>
            <div className="flex gap-2">
              <button
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <button
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default HistoryTable;
