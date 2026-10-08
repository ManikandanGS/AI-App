import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FaBug, FaStar, FaCheckCircle, FaComments } from 'react-icons/fa';
import { getAnalyses, getFeedbackStats } from '../services/api';
import Badge from './common/Badge';
import LoadingSpinner from './common/LoadingSpinner';

const StatCard = ({ icon: Icon, title, value, colorClass }) => (
  <div className="bg-slate-800 p-6 rounded-xl border border-slate-700 shadow-sm">
    <div className="flex items-center gap-4">
      <div className={`p-3 rounded-lg ${colorClass}`}>
        <Icon className="text-2xl text-white" />
      </div>
      <div>
        <p className="text-slate-400 text-sm font-medium">{title}</p>
        <p className="text-2xl font-bold text-white">{value}</p>
      </div>
    </div>
  </div>
);

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [analyses, setAnalyses] = useState([]);
  const [stats, setStats] = useState({
    totalAnalyses: 0,
    avgRating: 0,
    successRate: 0,
    totalFeedback: 0,
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [analysesData, statsData] = await Promise.all([
          getAnalyses(),
          getFeedbackStats().catch(() => ({ totalFeedbacks: 0, averageRating: 0, percentApplied: 0 }))
        ]);
        setAnalyses(analysesData || []);
        setStats({
          totalAnalyses: analysesData?.length || 0,
          avgRating: statsData.averageRating || 0,
          successRate: statsData.percentApplied || 0,
          totalFeedback: statsData.totalFeedbacks || 0,
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (loading) return <LoadingSpinner text="Loading Dashboard..." />;

  const recentAnalyses = [...analyses].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 10);

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-bold text-white">Dashboard Overview</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard icon={FaBug} title="Total Analyses" value={stats.totalAnalyses} colorClass="bg-blue-500" />
        <StatCard icon={FaStar} title="Average Rating" value={stats.avgRating.toFixed(1)} colorClass="bg-yellow-500" />
        <StatCard icon={FaCheckCircle} title="Success Rate" value={`${stats.successRate.toFixed(1)}%`} colorClass="bg-green-500" />
        <StatCard icon={FaComments} title="Total Feedback" value={stats.totalFeedback} colorClass="bg-indigo-500" />
      </div>

      <div className="bg-slate-800 rounded-xl border border-slate-700 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-700 flex justify-between items-center">
          <h3 className="text-lg font-semibold text-white">Recent Analyses</h3>
          <Link to="/history" className="text-indigo-400 hover:text-indigo-300 text-sm font-medium">View All</Link>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-slate-900/50 text-slate-400 text-sm">
              <tr>
                <th className="px-6 py-3 font-medium">Ticket ID</th>
                <th className="px-6 py-3 font-medium">Status</th>
                <th className="px-6 py-3 font-medium">Severity</th>
                <th className="px-6 py-3 font-medium">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-700/50">
              {recentAnalyses.length === 0 ? (
                <tr>
                  <td colSpan="4" className="px-6 py-8 text-center text-slate-500">No analyses yet.</td>
                </tr>
              ) : (
                recentAnalyses.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-700/30 transition-colors">
                    <td className="px-6 py-4">
                      <Link to={`/analysis/${item.id}`} className="font-medium text-indigo-400 hover:text-indigo-300">
                        {item.jiraTicketId}
                      </Link>
                    </td>
                    <td className="px-6 py-4"><Badge type={item.status} /></td>
                    <td className="px-6 py-4"><Badge type={item.severity} /></td>
                    <td className="px-6 py-4 text-sm text-slate-400">
                      {new Date(item.createdAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
