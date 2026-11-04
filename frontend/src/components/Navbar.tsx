import React from 'react';
import { MessageSquare, Network, BarChart3, FileText, Brain, ShieldCheck } from 'lucide-react';

export type ActiveTab = 'chat' | 'graph' | 'analytics' | 'documents';

interface NavbarProps {
  activeTab: ActiveTab;
  onTabChange: (tab: ActiveTab) => void;
  documentCount?: number;
}

export const Navbar: React.FC<NavbarProps> = ({ activeTab, onTabChange, documentCount = 3 }) => {
  const tabs = [
    { id: 'chat' as ActiveTab, label: 'Neural Chat', icon: MessageSquare },
    { id: 'graph' as ActiveTab, label: 'Knowledge Graph', icon: Network },
    { id: 'analytics' as ActiveTab, label: 'Analytics & Gaps', icon: BarChart3 },
    { id: 'documents' as ActiveTab, label: 'SOP Manager', icon: FileText, badge: documentCount },
  ];

  return (
    <header className="sticky top-0 z-40 bg-slate-900/80 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo & Title */}
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-indigo-500 via-indigo-600 to-violet-700 flex items-center justify-center shadow-lg shadow-indigo-500/20 ring-1 ring-white/10">
              <Brain className="h-6 w-6 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-white via-slate-100 to-indigo-200 bg-clip-text text-transparent">
                  OpsMind AI
                </span>
                <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-medium">
                  v1.0 • Neural Brain
                </span>
              </div>
              <p className="text-xs text-slate-400 hidden sm:block">
                Context-Aware Enterprise SOP & Knowledge Assistant
              </p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="flex items-center gap-1 sm:gap-2">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => onTabChange(tab.id)}
                  className={`flex items-center gap-2 px-3 sm:px-4 py-2 rounded-lg text-xs sm:text-sm font-medium transition-all duration-200 ${
                    isActive
                      ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className="h-4 w-4" />
                  <span>{tab.label}</span>
                  {tab.badge !== undefined && (
                    <span
                      className={`text-[10px] px-1.5 py-0.5 rounded-full font-semibold ${
                        isActive
                          ? 'bg-indigo-500 text-white'
                          : 'bg-slate-800 text-slate-300'
                      }`}
                    >
                      {tab.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>

          {/* Status Badge */}
          <div className="hidden md:flex items-center gap-2 text-xs text-emerald-400 bg-emerald-950/40 border border-emerald-800/40 px-3 py-1.5 rounded-full">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <ShieldCheck className="h-3.5 w-3.5" />
            <span>Hibernate Guardrail Active</span>
          </div>
        </div>
      </div>
    </header>
  );
};
