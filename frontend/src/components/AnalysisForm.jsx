import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSearch, FaBrain } from 'react-icons/fa';
import toast from 'react-hot-toast';
import { submitAnalysis, getConfigs } from '../services/api';
import LoadingSpinner from './common/LoadingSpinner';

const AnalysisForm = () => {
  const [configs, setConfigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    jiraTicketId: '',
    configId: '',
  });

  useEffect(() => {
    getConfigs()
      .then(data => {
        setConfigs(data || []);
        if (data && data.length > 0) {
          setFormData(prev => ({ ...prev, configId: data[0].id }));
        }
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load configurations');
      })
      .finally(() => setLoading(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.jiraTicketId || !formData.configId) {
      toast.error('Please fill in all fields');
      return;
    }

    setSubmitting(true);
    try {
      const result = await submitAnalysis(formData);
      toast.success('Analysis started successfully!');
      navigate(`/analysis/${result.id}`);
    } catch (error) {
      console.error(error);
      toast.error(error.response?.data?.message || 'Failed to submit analysis');
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading configurations..." />;

  return (
    <div className="max-w-2xl mx-auto mt-10">
      <div className="bg-slate-800 p-8 rounded-2xl border border-slate-700 shadow-lg relative overflow-hidden">
        {submitting && (
          <div className="absolute inset-0 bg-slate-900/80 backdrop-blur-sm z-10 flex flex-col items-center justify-center">
            <FaBrain className="text-6xl text-indigo-500 animate-pulse mb-4" />
            <p className="text-xl font-semibold text-white animate-pulse">AI is analyzing...</p>
            <p className="text-sm text-indigo-300 mt-2">Fetching code, reading ticket, finding root cause</p>
          </div>
        )}

        <div className="flex items-center gap-4 mb-8">
          <div className="p-3 bg-indigo-500/20 rounded-xl">
            <FaSearch className="text-2xl text-indigo-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-white">New Bug Analysis</h2>
            <p className="text-slate-400 mt-1">Provide a Jira ticket ID to let AI find the root cause and suggest a fix.</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Project Configuration</label>
            {configs.length === 0 ? (
              <div className="text-yellow-500 bg-yellow-500/10 p-3 rounded-lg border border-yellow-500/20 text-sm">
                No configurations found. Please create one in the Configuration tab first.
              </div>
            ) : (
              <select
                value={formData.configId}
                onChange={(e) => setFormData({ ...formData, configId: e.target.value })}
                className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg px-4 py-3 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                disabled={submitting}
              >
                {configs.map(c => (
                  <option key={c.id} value={c.id}>{c.projectKey} ({c.jiraBaseUrl})</option>
                ))}
              </select>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-2">Jira Ticket ID</label>
            <input
              type="text"
              placeholder="e.g. PROJ-123"
              value={formData.jiraTicketId}
              onChange={(e) => setFormData({ ...formData, jiraTicketId: e.target.value.toUpperCase() })}
              className="w-full bg-slate-900 border border-slate-700 text-white rounded-lg px-4 py-3 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors uppercase"
              disabled={submitting}
            />
          </div>

          <button
            type="submit"
            disabled={submitting || configs.length === 0}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-3 rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 focus:ring-offset-slate-900 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Start Analysis
          </button>
        </form>
      </div>
    </div>
  );
};

export default AnalysisForm;
