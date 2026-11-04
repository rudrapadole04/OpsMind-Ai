import React, { useState, useEffect } from 'react';
import { Navbar, ActiveTab } from './components/Navbar';
import { ChatView } from './components/chat/ChatView';
import { KnowledgeGraphView } from './components/graph/KnowledgeGraphView';
import { AnalyticsView } from './components/analytics/AnalyticsView';
import { DocumentsView } from './components/documents/DocumentsView';
import { documentsApi } from './services/api';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ActiveTab>('chat');
  const [documentCount, setDocumentCount] = useState<number>(3);

  const fetchDocCount = async () => {
    try {
      const docs = await documentsApi.getAll();
      setDocumentCount(docs.length);
    } catch {
      // Backend might be offline during initial client boot
    }
  };

  useEffect(() => {
    fetchDocCount();
  }, [activeTab]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500 selection:text-white">
      {/* Top Navbar */}
      <Navbar
        activeTab={activeTab}
        onTabChange={setActiveTab}
        documentCount={documentCount}
      />

      {/* Main Tab Content */}
      <main className="flex-1">
        {activeTab === 'chat' && <ChatView />}
        {activeTab === 'graph' && <KnowledgeGraphView />}
        {activeTab === 'analytics' && <AnalyticsView />}
        {activeTab === 'documents' && <DocumentsView />}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950/90 py-3 text-center text-[11px] text-slate-500">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>
            OpsMind AI © 2026 — Enterprise SOP Neural Brain & Context-Aware Knowledge Assistant
          </span>
          <div className="flex items-center gap-3 font-mono text-[10px]">
            <span className="text-slate-400">Spring Boot 3.3</span>
            <span>•</span>
            <span className="text-indigo-400">Hibernate 6 JPA</span>
            <span>•</span>
            <span className="text-emerald-400">Apache PDFBox 3.0</span>
            <span>•</span>
            <span className="text-cyan-400">Pure Java VSM</span>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default App;
