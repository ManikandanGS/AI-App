import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FaArrowLeft, FaCheckCircle, FaExclamationTriangle, FaRobot, FaCode, FaFileAlt } from 'react-icons/fa';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import toast from 'react-hot-toast';
import { getAnalysis } from '../services/api';
import Badge from './common/Badge';
import LoadingSpinner from './common/LoadingSpinner';
import FeedbackPanel from './FeedbackPanel';

const Section = ({ title, icon: Icon, children }) => (
  <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden mb-6">
    <div className="bg-slate-900/50 px-6 py-4 border-b border-slate-700 flex items-center gap-3">
      <Icon className="text-indigo-400 text-lg" />
      <h3 className="text-lg font-semibold text-white">{title}</h3>
    </div>
    <div className="p-6">
      {children}
    </div>
  </div>
);

const AnalysisResult = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAnalysis(id)
      .then(data => setAnalysis(data))
      .catch(err => {
        console.error(err);
        toast.error('Failed to load analysis details');
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <LoadingSpinner text="Loading Analysis Details..." />;
  if (!analysis) return <div className="text-center text-slate-400 mt-10">Analysis not found</div>;

  return (
    <div className="max-w-5xl mx-auto pb-10">
      {/* Header */}
      <button 
        onClick={() => navigate('/history')}
        className="flex items-center gap-2 text-slate-400 hover:text-white mb-6 transition-colors"
      >
        <FaArrowLeft /> Back to History
      </button>

      <div className="bg-slate-800 rounded-xl border border-slate-700 p-6 mb-6 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-3xl font-bold text-white">{analysis.jiraTicketId}</h1>
              <Badge type={analysis.status} />
              <Badge type={analysis.severity} />
            </div>
            <p className="text-xl text-slate-300">{analysis.ticketSummary || 'No summary available'}</p>
          </div>
          
          {analysis.confidenceScore && (
            <div className="bg-slate-900 p-4 rounded-lg border border-slate-700 min-w-[200px]">
              <div className="flex justify-between text-sm mb-2">
                <span className="text-slate-400">AI Confidence</span>
                <span className="text-white font-bold">{analysis.confidenceScore}%</span>
              </div>
              <div className="h-2 w-full bg-slate-800 rounded-full overflow-hidden">
                <div 
                  className={`h-full rounded-full ${analysis.confidenceScore > 80 ? 'bg-green-500' : analysis.confidenceScore > 50 ? 'bg-yellow-500' : 'bg-red-500'}`}
                  style={{ width: `${analysis.confidenceScore}%` }}
                ></div>
              </div>
              <div className="text-xs text-slate-500 mt-2 flex items-center justify-between">
                <span>Model: {analysis.aiModelUsed || 'Unknown'}</span>
                <span>{new Date(analysis.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {analysis.status === 'FAILED' ? (
        <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-6 rounded-xl flex items-start gap-4 mb-6">
          <FaExclamationTriangle className="text-2xl mt-1 flex-shrink-0" />
          <div>
            <h3 className="font-bold text-lg mb-1">Analysis Failed</h3>
            <p>The AI could not complete the analysis. Please check the backend logs for details.</p>
          </div>
        </div>
      ) : (
        <>
          <Section title="Root Cause Analysis" icon={FaRobot}>
            <div className="prose prose-invert max-w-none text-slate-300 whitespace-pre-wrap">
              {analysis.rootCause || 'No root cause identified.'}
            </div>
          </Section>

          <Section title="Suggested Fix" icon={FaCheckCircle}>
            <div className="prose prose-invert max-w-none text-slate-300 whitespace-pre-wrap">
              {analysis.suggestedFix || 'No fix suggested.'}
            </div>
          </Section>

          {analysis.filesAffected && analysis.filesAffected.length > 0 && (
            <Section title="Files Affected" icon={FaFileAlt}>
              <ul className="list-disc list-inside space-y-1 text-slate-300 font-mono text-sm">
                {analysis.filesAffected.map((file, i) => (
                  <li key={i}>{file}</li>
                ))}
              </ul>
            </Section>
          )}

          {analysis.codeDiff && (
            <Section title="Code Diff" icon={FaCode}>
              <div className="text-sm rounded-lg overflow-hidden border border-slate-700">
                <SyntaxHighlighter
                  language="diff"
                  style={vscDarkPlus}
                  customStyle={{ margin: 0, padding: '1.5rem', background: '#0f172a' }}
                >
                  {analysis.codeDiff}
                </SyntaxHighlighter>
              </div>
            </Section>
          )}

          {analysis.status === 'COMPLETED' && (
             <div className="mt-8">
               <FeedbackPanel resolutionId={analysis.resolutionId} />
             </div>
          )}
        </>
      )}
    </div>
  );
};

export default AnalysisResult;
