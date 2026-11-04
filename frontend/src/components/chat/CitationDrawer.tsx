import React from 'react';
import { X, FileText, CheckCircle2, Bookmark, ExternalLink } from 'lucide-react';
import { Citation } from '../../types';

interface CitationDrawerProps {
  citation: Citation | null;
  onClose: () => void;
}

export const CitationDrawer: React.FC<CitationDrawerProps> = ({ citation, onClose }) => {
  if (!citation) return null;

  const scorePct = Math.round(citation.relevanceScore * 100);

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-sm transition-opacity">
      <div className="w-full max-w-md bg-slate-900 border-l border-slate-800 h-full p-6 flex flex-col shadow-2xl animate-in slide-in-from-right duration-300">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2 text-indigo-400 font-semibold text-sm">
            <Bookmark className="h-4 w-4" />
            <span>Precision Source Citation</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto py-6 space-y-6">
          {/* Document metadata card */}
          <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-4 space-y-3">
            <div className="flex items-start gap-3">
              <div className="p-2.5 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                <FileText className="h-5 w-5" />
              </div>
              <div className="flex-1 min-w-0">
                <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  Corporate SOP Document
                </span>
                <p className="text-sm font-medium text-slate-100 truncate mt-0.5">
                  {citation.sourceFilename}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-800/60 text-xs">
              <div>
                <span className="text-slate-400">Physical Page:</span>
                <p className="font-semibold text-indigo-300 mt-0.5 flex items-center gap-1">
                  Page {citation.pageNumber}
                </p>
              </div>
              <div>
                <span className="text-slate-400">Retrieval Confidence:</span>
                <p className="font-semibold text-emerald-400 mt-0.5 flex items-center gap-1">
                  <CheckCircle2 className="h-3.5 w-3.5" />
                  {scorePct}% Match
                </p>
              </div>
            </div>
          </div>

          {/* Relevance progress meter */}
          <div className="space-y-1.5">
            <div className="flex justify-between text-xs">
              <span className="text-slate-400">Cosine Similarity Score</span>
              <span className="font-mono text-slate-300 font-semibold">
                {citation.relevanceScore.toFixed(2)} / 1.00
              </span>
            </div>
            <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
              <div
                className="bg-gradient-to-r from-indigo-500 to-emerald-400 h-2 rounded-full transition-all duration-500"
                style={{ width: `${scorePct}%` }}
              />
            </div>
          </div>

          {/* Verifiable Excerpt */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Verifiable Grounding Excerpt
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-medium">
                Verified Grounding
              </span>
            </div>
            <div className="relative bg-slate-950 p-4 rounded-xl border border-slate-800 font-mono text-xs text-slate-300 leading-relaxed space-y-2">
              <p className="italic">
                &ldquo;{citation.excerpt}&rdquo;
              </p>
            </div>
            <p className="text-[11px] text-slate-500">
              * Extracted directly from Apache PDFBox page text chunks stored in Hibernate.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-4 border-t border-slate-800">
          <button
            onClick={onClose}
            className="w-full py-2.5 px-4 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-sm font-medium transition"
          >
            Close Citation Inspector
          </button>
        </div>
      </div>
    </div>
  );
};
