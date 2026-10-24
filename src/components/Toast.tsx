import React from 'react';
import { useApp } from '../context/AppContext';
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react';

export const Toast: React.FC = () => {
  const { toast, hideToast } = useApp();

  if (!toast) return null;

  const getIcon = () => {
    switch (toast.type) {
      case 'success':
        return <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />;
      case 'error':
        return <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />;
      case 'warning':
        return <AlertCircle className="w-5 h-5 text-amber-400 shrink-0" />;
      default:
        return <Info className="w-5 h-5 text-teal-400 shrink-0" />;
    }
  };

  return (
    <div className="fixed bottom-20 left-1/2 -translate-x-1/2 z-50 w-full max-w-sm px-4 animate-in fade-in slide-in-from-bottom-4 duration-200">
      <div className="bg-[#19332D] dark:bg-[#0D2623] text-white px-4 py-3 rounded-2xl shadow-2xl border border-teal-900/50 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5 min-w-0">
          {getIcon()}
          <span className="text-sm font-medium leading-snug line-clamp-2">{toast.message}</span>
        </div>

        <div className="flex items-center gap-2 shrink-0">
          {toast.action && (
            <button
              onClick={() => {
                toast.action?.onPress();
                hideToast();
              }}
              className="text-xs font-bold text-[#C4D639] hover:underline bg-white/10 px-2.5 py-1 rounded-lg"
            >
              {toast.action.label}
            </button>
          )}
          <button
            onClick={hideToast}
            className="p-1 text-white/60 hover:text-white rounded-lg transition-colors"
            aria-label="Close"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
