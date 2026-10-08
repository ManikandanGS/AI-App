import React from 'react';

const colorMap = {
  COMPLETED: 'bg-green-500/10 text-green-500 border-green-500/20',
  PENDING: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20',
  FAILED: 'bg-red-500/10 text-red-500 border-red-500/20',
  CRITICAL: 'bg-red-500/10 text-red-500 border-red-500/20',
  HIGH: 'bg-orange-500/10 text-orange-500 border-orange-500/20',
  MEDIUM: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20',
  LOW: 'bg-green-500/10 text-green-500 border-green-500/20',
  DEFAULT: 'bg-slate-500/10 text-slate-300 border-slate-500/20',
};

const Badge = ({ type, text }) => {
  const colorClass = colorMap[type?.toUpperCase()] || colorMap.DEFAULT;
  const displayValue = text || type || 'UNKNOWN';

  return (
    <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium border ${colorClass}`}>
      {displayValue}
    </span>
  );
};

export default Badge;
