import React from 'react';
import { FaSpinner } from 'react-icons/fa';

const LoadingSpinner = ({ text = 'Loading...' }) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 gap-4 text-slate-400">
      <FaSpinner className="animate-spin text-3xl text-indigo-500" />
      <span>{text}</span>
    </div>
  );
};

export default LoadingSpinner;
