import React, { useState, useRef, useEffect } from 'react';
import {
  Send,
  Sparkles,
  ShieldAlert,
  ShieldCheck,
  Clock,
  Bookmark,
  FileText,
  AlertTriangle,
  RotateCcw,
} from 'lucide-react';
import { Citation, QueryResponse } from '../../types';
import { queryApi } from '../../services/api';
import { CitationDrawer } from './CitationDrawer';

interface ChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  response?: QueryResponse;
  timestamp: string;
}

const PRESET_QUESTIONS = [
  { label: '🩺 What is sick leave ?', query: 'What is sick leave ?' },
  { label: '💰 How can I work on customer refund ?', query: 'How can i work on customer refund ?' },
  { label: '🔒 What are the IT VPN limits ?', query: 'What are the remote access VPN session and inactivity limits?' },
  { label: '🛡️ Test Guardrail (Out of Domain)', query: 'How do I bake chocolate chip cookies with sourdough?' },
];

export const ChatView: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'assistant',
      text: 'Hello! I am **OpsMind AI**, your corporate knowledge assistant bot. You can ask me natural questions like *"What is sick leave ?"* or *"How can i work on customer refund ?"*. I will formulate an informative, structured answer based on our official corporate SOPs with exact page citations. If a question is not covered in our documentation, I will let you know: *"I am not able to answer this based on the provided corporate SOPs."*',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    },
  ]);

  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedCitation, setSelectedCitation] = useState<Citation | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSend = async (queryText?: string) => {
    const question = (queryText || input).trim();
    if (!question || loading) return;

    const userMessage: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'user',
      text: question,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const res = await queryApi.askQuestion(question);
      const assistantMessage: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'assistant',
        text: res.answer,
        response: res,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err: any) {
      const errorMessage: ChatMessage = {
        id: `err-${Date.now()}`,
        sender: 'assistant',
        text: `⚠️ **Query Error**: ${err.message || 'Unable to communicate with the neural backend.'}`,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
    }
  };

  const clearChat = () => {
    setMessages([
      {
        id: 'welcome',
        sender: 'assistant',
        text: 'Chat history cleared. How can I assist you with corporate SOPs today?',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      },
    ]);
  };

  return (
    <div className="flex flex-col h-[calc(100vh-4rem)] max-w-5xl mx-auto px-4 py-4 sm:py-6">
      {/* Preset Query Chips */}
      <div className="mb-4">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
            <Sparkles className="h-3.5 w-3.5 text-indigo-400" />
            Quick Enterprise Inquiries
          </span>
          <button
            onClick={clearChat}
            className="text-xs text-slate-400 hover:text-slate-200 flex items-center gap-1 transition"
          >
            <RotateCcw className="h-3 w-3" />
            Reset Chat
          </button>
        </div>
        <div className="flex flex-wrap gap-2">
          {PRESET_QUESTIONS.map((item, idx) => (
            <button
              key={idx}
              onClick={() => handleSend(item.query)}
              disabled={loading}
              className="px-3 py-1.5 rounded-lg bg-slate-900/90 border border-slate-800 text-xs text-slate-300 hover:text-white hover:bg-indigo-600/20 hover:border-indigo-500/40 transition disabled:opacity-50 text-left"
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {/* Messages Feed */}
      <div className="flex-1 overflow-y-auto space-y-4 pr-1 sm:pr-2">
        {messages.map((msg) => {
          const isUser = msg.sender === 'user';
          const res = msg.response;
          const isGuardrail = res?.guardrailTriggered;

          return (
            <div
              key={msg.id}
              className={`flex flex-col ${isUser ? 'items-end' : 'items-start'}`}
            >
              <div
                className={`max-w-2xl rounded-2xl p-4 sm:p-5 shadow-sm ${
                  isUser
                    ? 'bg-indigo-600 text-white rounded-br-none'
                    : isGuardrail
                    ? 'bg-amber-950/40 border border-amber-800/60 text-amber-100 rounded-bl-none'
                    : 'bg-slate-900 border border-slate-800 text-slate-200 rounded-bl-none'
                }`}
              >
                {/* Header for AI response */}
                {!isUser && (
                  <div className="flex items-center justify-between gap-4 mb-3 pb-2 border-b border-slate-800/80 text-xs">
                    <div className="flex items-center gap-1.5 font-semibold">
                      {isGuardrail ? (
                        <span className="flex items-center gap-1 text-amber-400">
                          <ShieldAlert className="h-4 w-4" />
                          Hallucination Guardrail Active
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-indigo-400">
                          <ShieldCheck className="h-4 w-4" />
                          OpsMind Grounded Synthesis
                        </span>
                      )}
                    </div>
                    {res && (
                      <div className="flex items-center gap-3 text-slate-400 text-[11px] font-mono">
                        <span className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          {res.latencyMs}ms
                        </span>
                        <span>•</span>
                        <span className={res.confidenceScore >= 0.6 ? 'text-emerald-400 font-semibold' : 'text-amber-400 font-semibold'}>
                          {Math.round(res.confidenceScore * 100)}% Confidence
                        </span>
                      </div>
                    )}
                  </div>
                )}

                {/* Body Message */}
                <div className="text-sm leading-relaxed whitespace-pre-wrap">
                  {msg.text}
                </div>

                {/* Guardrail Disclaimer */}
                {isGuardrail && (
                  <div className="mt-3 pt-3 border-t border-amber-800/40 text-xs text-amber-300/90 flex items-start gap-2">
                    <AlertTriangle className="h-4 w-4 text-amber-400 shrink-0 mt-0.5" />
                    <span>
                      Confidence score fell below the <strong>0.60</strong> threshold. The question was audited and logged as a <strong>Knowledge Gap</strong> for administrative review.
                    </span>
                  </div>
                )}

                {/* Precision Citations Carousel / Cards */}
                {res && !isGuardrail && res.citations && res.citations.length > 0 && (
                  <div className="mt-4 pt-3 border-t border-slate-800/80">
                    <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-400 mb-2">
                      <Bookmark className="h-3.5 w-3.5 text-indigo-400" />
                      <span>Verified Sources & Exact Citations ({res.citations.length})</span>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                      {res.citations.map((c, idx) => (
                        <button
                          key={idx}
                          onClick={() => setSelectedCitation(c)}
                          className="flex flex-col text-left p-2.5 rounded-xl bg-slate-950/70 border border-slate-800 hover:border-indigo-500/50 hover:bg-indigo-950/20 transition group"
                        >
                          <div className="flex items-center justify-between text-xs font-medium text-slate-300 group-hover:text-indigo-300">
                            <span className="truncate max-w-[170px] flex items-center gap-1">
                              <FileText className="h-3 w-3 text-indigo-400 shrink-0" />
                              {c.sourceFilename}
                            </span>
                            <span className="text-[10px] px-1.5 py-0.5 rounded bg-indigo-500/10 text-indigo-400 font-mono">
                              p.{c.pageNumber}
                            </span>
                          </div>
                          <p className="text-[11px] text-slate-400 line-clamp-2 mt-1 italic">
                            &ldquo;{c.excerpt}&rdquo;
                          </p>
                          <div className="mt-1.5 flex items-center justify-between text-[10px] text-slate-500">
                            <span>Score: {c.relevanceScore.toFixed(2)}</span>
                            <span className="text-indigo-400 font-medium group-hover:underline">
                              Inspect source →
                            </span>
                          </div>
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
              <span className="text-[10px] text-slate-500 mt-1 px-1">
                {msg.timestamp}
              </span>
            </div>
          );
        })}

        {loading && (
          <div className="flex items-center gap-3 p-4 rounded-2xl bg-slate-900 border border-slate-800 text-slate-400 text-sm max-w-md animate-pulse">
            <div className="h-4 w-4 rounded-full border-2 border-indigo-500 border-t-transparent animate-spin" />
            <span>Scanning Vector Space & applying Hallucination Guardrail...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Query Input Box */}
      <div className="mt-4 relative">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSend();
          }}
          className="flex items-center gap-2 bg-slate-900 border border-slate-800 rounded-2xl p-1.5 focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition shadow-xl"
        >
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask a question about corporate SOPs (e.g. refunds, sick leave, VPN)..."
            disabled={loading}
            className="flex-1 bg-transparent px-4 py-3 text-sm text-slate-100 placeholder-slate-500 focus:outline-none"
          />
          <button
            type="submit"
            disabled={!input.trim() || loading}
            className="p-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-medium disabled:opacity-40 disabled:cursor-not-allowed transition shadow-md shadow-indigo-600/30"
          >
            <Send className="h-4 w-4" />
          </button>
        </form>
        <p className="text-[11px] text-slate-500 text-center mt-2">
          Responses are strictly restricted to indexed corporate SOPs with 0.60 confidence thresholding.
        </p>
      </div>

      {/* Citation Detail Drawer */}
      <CitationDrawer
        citation={selectedCitation}
        onClose={() => setSelectedCitation(null)}
      />
    </div>
  );
};
