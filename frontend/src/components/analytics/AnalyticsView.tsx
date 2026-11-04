import React, { useState, useEffect } from 'react';
import {
  BarChart3,
  ShieldAlert,
  CheckCircle2,
  Clock,
  FileText,
  AlertTriangle,
  FolderPlus,
  RefreshCw,
  Search,
  Sparkles,
} from 'lucide-react';
import { AnalyticsOverview, TopDocument, KnowledgeGap, TopicMetric } from '../../types';
import { analyticsApi } from '../../services/api';

export const AnalyticsView: React.FC = () => {
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [topDocs, setTopDocs] = useState<TopDocument[]>([]);
  const [knowledgeGaps, setKnowledgeGaps] = useState<KnowledgeGap[]>([]);
  const [topics, setTopics] = useState<TopicMetric[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterDept, setFilterDept] = useState<string>('ALL');

  const loadAllAnalytics = async () => {
    try {
      setLoading(true);
      const [ov, docs, gaps, top] = await Promise.all([
        analyticsApi.getOverview(),
        analyticsApi.getTopDocuments(),
        analyticsApi.getKnowledgeGaps(50),
        analyticsApi.getTopics(),
      ]);
      setOverview(ov);
      setTopDocs(docs);
      setKnowledgeGaps(gaps);
      setTopics(top);
    } catch (err: any) {
      console.error('Failed to load analytics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllAnalytics();
  }, []);

  const departments = ['ALL', ...Array.from(new Set(knowledgeGaps.map((g) => g.suggestedDepartment)))];

  const filteredGaps = filterDept === 'ALL'
    ? knowledgeGaps
    : knowledgeGaps.filter((g) => g.suggestedDepartment === filterDept);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <BarChart3 className="h-5 w-5 text-indigo-400" />
            Executive Analytics & Knowledge Gap Oversight
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Real-time compliance monitoring, hallucination guardrail interception rates, and missing policy gap analysis.
          </p>
        </div>
        <button
          onClick={loadAllAnalytics}
          disabled={loading}
          className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium transition self-start sm:self-auto"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Metrics</span>
        </button>
      </div>

      {/* KPI Overview Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Total Queries</span>
          <p className="text-2xl font-bold text-white mt-1">
            {overview?.totalQueries ?? 0}
          </p>
          <span className="text-[10px] text-slate-500 mt-0.5 block">Audit log count</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Grounded Answers</span>
          <p className="text-2xl font-bold text-emerald-400 mt-1">
            {overview?.answeredQueries ?? 0}
          </p>
          <span className="text-[10px] text-emerald-500/80 mt-0.5 block">With verified citations</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Guardrail Rate</span>
          <p className="text-2xl font-bold text-amber-400 mt-1">
            {overview?.guardrailTriggerRate ?? 0}%
          </p>
          <span className="text-[10px] text-amber-500/80 mt-0.5 block">Out-of-domain queries</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Knowledge Gaps</span>
          <p className="text-2xl font-bold text-rose-400 mt-1">
            {overview?.knowledgeGapCount ?? 0}
          </p>
          <span className="text-[10px] text-rose-500/80 mt-0.5 block">Actionable policy voids</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Avg Confidence</span>
          <p className="text-2xl font-bold text-indigo-400 mt-1">
            {Math.round((overview?.averageConfidenceScore ?? 0) * 100)}%
          </p>
          <span className="text-[10px] text-slate-500 mt-0.5 block">Cosine threshold 0.60</span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
          <span className="text-[11px] text-slate-400 uppercase font-semibold">Avg Latency</span>
          <p className="text-2xl font-bold text-cyan-400 mt-1">
            {overview?.averageLatencyMs ?? 0}ms
          </p>
          <span className="text-[10px] text-slate-500 mt-0.5 block">Vector retrieval time</span>
        </div>
      </div>

      {/* Two Column Section: Top Cited Documents & Topic Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Cited Documents */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <FileText className="h-4 w-4 text-indigo-400" />
              Frequently Accessed Corporate SOPs
            </h3>
            <span className="text-xs text-slate-400">By Citation Count</span>
          </div>

          {topDocs.length === 0 ? (
            <p className="text-xs text-slate-500 italic py-6 text-center">
              No citations logged yet. Ask questions in the Neural Chat to record source citations!
            </p>
          ) : (
            <div className="space-y-3">
              {topDocs.map((doc, idx) => (
                <div key={idx} className="space-y-1.5">
                  <div className="flex justify-between items-center text-xs">
                    <span className="font-medium text-slate-200 truncate max-w-[280px]">
                      {doc.filename}
                    </span>
                    <span className="font-mono text-indigo-400 font-semibold">
                      {doc.citationCount} citations ({doc.citationPercentage}%)
                    </span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden">
                    <div
                      className="bg-indigo-500 h-2 rounded-full transition-all duration-500"
                      style={{ width: `${Math.min(100, doc.citationPercentage)}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Topic Breakdown */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-emerald-400" />
              Topic Query Traffic Breakdown
            </h3>
            <span className="text-xs text-slate-400">{topics.length} Canonical Topics</span>
          </div>

          <div className="space-y-2 max-h-64 overflow-y-auto pr-1">
            {topics.map((t, idx) => (
              <div
                key={idx}
                className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950 border border-slate-800/80 text-xs"
              >
                <div>
                  <p className="font-medium text-slate-200">{t.topicName}</p>
                  <span className="text-[10px] text-slate-400">{t.category}</span>
                </div>
                <div className="text-right">
                  <span className="font-bold text-indigo-400">{t.queryCount} queries</span>
                  <span className="text-[10px] text-slate-500 block">
                    {t.documentCount} SOPs covered
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Knowledge Gap Audit Log */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4 shadow-xl">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <ShieldAlert className="h-4 w-4 text-amber-400" />
              <h3 className="text-sm font-bold text-white">
                Audited Knowledge Gaps (Unanswered Employee Inquiries)
              </h3>
              <span className="text-xs px-2 py-0.5 rounded-full bg-rose-500/10 text-rose-400 font-semibold border border-rose-500/20">
                {knowledgeGaps.length} Gaps Logged
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Questions intercepted by the Hallucination Guardrail because no matching corporate SOP exists. Author new SOPs to close these gaps!
            </p>
          </div>

          {/* Department Filter */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400">Department:</span>
            <select
              value={filterDept}
              onChange={(e) => setFilterDept(e.target.value)}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1 text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
            >
              {departments.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
          </div>
        </div>

        {filteredGaps.length === 0 ? (
          <div className="py-8 text-center text-slate-500 text-xs">
            <CheckCircle2 className="h-6 w-6 text-emerald-500/50 mx-auto mb-2" />
            No knowledge gaps logged for this filter. All queries were grounded against current SOPs!
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-semibold uppercase tracking-wider text-[10px] border-b border-slate-800">
                <tr>
                  <th className="py-2.5 px-3">Logged Timestamp</th>
                  <th className="py-2.5 px-3">Unanswered Employee Query</th>
                  <th className="py-2.5 px-3">Suggested Policy Owner</th>
                  <th className="py-2.5 px-3 text-right">Confidence Score</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {filteredGaps.map((gap) => (
                  <tr key={gap.id} className="hover:bg-slate-950/40 transition">
                    <td className="py-2.5 px-3 font-mono text-slate-400 whitespace-nowrap">
                      {new Date(gap.createdAt).toLocaleString()}
                    </td>
                    <td className="py-2.5 px-3 text-slate-200 font-medium max-w-md truncate">
                      &ldquo;{gap.question}&rdquo;
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-500/10 text-amber-300 border border-amber-500/20 whitespace-nowrap">
                        {gap.suggestedDepartment}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-right font-mono text-rose-400 font-semibold">
                      {Math.round(gap.confidenceScore * 100)}% (&lt; 60%)
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
