import React from 'react';
import { FaTimes } from 'react-icons/fa';

const Modal = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-slate-800 rounded-xl shadow-xl w-full max-w-2xl flex flex-col max-h-[90vh] border border-slate-700 animate-in fade-in zoom-in duration-200">
        <div className="flex items-center justify-between p-4 border-b border-slate-700">
          <h2 className="text-xl font-semibold text-white">{title}</h2>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-700 transition-colors"
          >
            <FaTimes />
          </button>
        </div>
        <div className="p-6 overflow-y-auto scrollbar-thin">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal;
