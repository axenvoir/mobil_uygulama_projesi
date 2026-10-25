import React, { Component, ErrorInfo, ReactNode } from 'react';
import { AlertTriangle, RefreshCw, Trash2 } from 'lucide-react';

interface Props {
  children: ReactNode;
  fallbackTitle?: string;
  onReset?: () => void;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error, errorInfo: null };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[Expira ErrorBoundary] Uncaught runtime error:', error);
    console.error('[Expira ErrorBoundary] Component stack trace:', errorInfo.componentStack);
    this.setState({ errorInfo });
  }

  private handleRetry = () => {
    if (this.props.onReset) {
      this.props.onReset();
    }
    this.setState({ hasError: false, error: null, errorInfo: null });
  };

  private handleReload = () => {
    window.location.reload();
  };

  private handleClearStorageAndReload = () => {
    try {
      localStorage.clear();
      sessionStorage.clear();
    } catch (e) {
      console.warn('Could not clear storage:', e);
    }
    window.location.reload();
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-[#F5F7F4] dark:bg-[#101C19] flex items-center justify-center p-4">
          <div className="w-full max-w-md bg-white dark:bg-[#1A2B26] border border-[#E1E8E3] dark:border-[#344A40] rounded-3xl p-6 shadow-xl text-center">
            <div className="w-16 h-16 rounded-2xl bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 mx-auto flex items-center justify-center mb-4">
              <AlertTriangle className="w-8 h-8" />
            </div>

            <h2 className="text-xl font-bold text-[#19332D] dark:text-[#F0F4EC] mb-2">
              {this.props.fallbackTitle || 'Bir Hata Oluştu / An Error Occurred'}
            </h2>

            <p className="text-sm text-[#5A6B63] dark:text-[#B5C5BC] mb-5 leading-relaxed">
              Uygulama çalışırken beklenmeyen bir hata yakalandı. Verilerinizi korumak için uygulama güvenli duruma getirildi.
            </p>

            {this.state.error && (
              <div className="mb-5 p-3 rounded-xl bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-900/50 text-left overflow-auto max-h-36">
                <p className="text-xs font-mono text-red-700 dark:text-red-300 break-words">
                  {this.state.error.name}: {this.state.error.message}
                </p>
                {this.state.errorInfo?.componentStack && (
                  <pre className="text-[10px] font-mono text-red-600/80 dark:text-red-400/80 mt-2 whitespace-pre-wrap">
                    {this.state.errorInfo.componentStack.slice(0, 300)}
                  </pre>
                )}
              </div>
            )}

            <div className="space-y-2">
              <button
                onClick={this.handleRetry}
                className="w-full py-3 px-4 bg-[#267968] hover:bg-[#153F38] active:scale-[0.98] transition-all text-white font-medium rounded-xl flex items-center justify-center gap-2 shadow-sm"
              >
                <RefreshCw className="w-4 h-4" />
                <span>Yeniden Dene (Try Again)</span>
              </button>

              <button
                onClick={this.handleReload}
                className="w-full py-2.5 px-4 bg-gray-100 dark:bg-[#253A33] hover:bg-gray-200 dark:hover:bg-[#2e4940] active:scale-[0.98] transition-all text-[#19332D] dark:text-[#F0F4EC] font-medium rounded-xl text-sm"
              >
                Sayfayı Yenile (Reload Page)
              </button>

              <button
                onClick={this.handleClearStorageAndReload}
                className="w-full py-2 px-3 text-red-600 hover:text-red-700 dark:text-red-400 text-xs flex items-center justify-center gap-1.5 transition-colors"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Önbelleği Temizle & Yeniden Başlat (Clear Cache)</span>
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
