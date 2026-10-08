import React, { useState } from 'react';
import { Routes, Route, NavLink } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { FaBrain, FaChartBar, FaSearch, FaHistory, FaCog, FaBars } from 'react-icons/fa';

import Dashboard from './components/Dashboard';
import AnalysisForm from './components/AnalysisForm';
import AnalysisResult from './components/AnalysisResult';
import HistoryTable from './components/HistoryTable';
import ConfigPanel from './components/ConfigPanel';

function App() {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const navItemClass = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
      isActive ? 'bg-indigo-600 text-white' : 'hover:bg-slate-800 text-slate-300'
    }`;

  return (
    <div className="flex h-screen bg-slate-900 overflow-hidden font-sans text-slate-300">
      {/* Sidebar */}
      <div
        className={`${
          sidebarOpen ? 'w-64' : 'w-20'
        } bg-slate-900 border-r border-slate-800 transition-all duration-300 flex flex-col`}
      >
        <div className="h-16 flex items-center justify-between px-4 border-b border-slate-800">
          <div className={`flex items-center gap-3 text-indigo-500 overflow-hidden ${!sidebarOpen && 'justify-center w-full'}`}>
            <FaBrain className="text-2xl flex-shrink-0" />
            <span className={`font-bold text-lg whitespace-nowrap transition-opacity ${sidebarOpen ? 'opacity-100' : 'opacity-0 hidden'}`}>
              AI Bug Resolver
            </span>
          </div>
        </div>

        <nav className="flex-1 py-6 px-3 flex flex-col gap-2">
          <NavLink to="/" className={navItemClass}>
            <FaChartBar className="text-xl flex-shrink-0" />
            <span className={`${sidebarOpen ? 'block' : 'hidden'}`}>Dashboard</span>
          </NavLink>
          <NavLink to="/new-analysis" className={navItemClass}>
            <FaSearch className="text-xl flex-shrink-0" />
            <span className={`${sidebarOpen ? 'block' : 'hidden'}`}>New Analysis</span>
          </NavLink>
          <NavLink to="/history" className={navItemClass}>
            <FaHistory className="text-xl flex-shrink-0" />
            <span className={`${sidebarOpen ? 'block' : 'hidden'}`}>History</span>
          </NavLink>
          <NavLink to="/config" className={navItemClass}>
            <FaCog className="text-xl flex-shrink-0" />
            <span className={`${sidebarOpen ? 'block' : 'hidden'}`}>Configuration</span>
          </NavLink>
        </nav>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col h-full overflow-hidden bg-slate-950">
        <header className="h-16 bg-slate-900 border-b border-slate-800 flex items-center px-6 gap-4">
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="text-slate-400 hover:text-white p-2 rounded-lg hover:bg-slate-800 transition-colors"
          >
            <FaBars />
          </button>
          <h1 className="text-xl font-semibold text-white">AI Bug Resolver Workspace</h1>
        </header>

        <main className="flex-1 overflow-auto p-6 scrollbar-thin">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/new-analysis" element={<AnalysisForm />} />
            <Route path="/analysis/:id" element={<AnalysisResult />} />
            <Route path="/history" element={<HistoryTable />} />
            <Route path="/config" element={<ConfigPanel />} />
          </Routes>
        </main>
      </div>

      <Toaster
        position="bottom-right"
        toastOptions={{
          style: {
            background: '#1e293b',
            color: '#f8fafc',
            border: '1px solid #334155',
          },
        }}
      />
    </div>
  );
}

export default App;
