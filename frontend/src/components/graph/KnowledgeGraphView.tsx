import React, { useState, useEffect, useRef } from 'react';
import {
  Network,
  RefreshCw,
  Info,
  CheckCircle2,
  FileText,
  Activity,
  Layers,
  Sparkles,
} from 'lucide-react';
import { KnowledgeGraphData, GraphNode, GraphEdge } from '../../types';
import { graphApi } from '../../services/api';

const CATEGORY_COLORS: Record<string, { bg: string; border: string; text: string; fill: string }> = {
  'Finance & Billing': { bg: 'bg-indigo-950/60', border: 'border-indigo-500/50', text: 'text-indigo-400', fill: '#6366f1' },
  'Human Resources': { bg: 'bg-emerald-950/60', border: 'border-emerald-500/50', text: 'text-emerald-400', fill: '#10b981' },
  'Information Security': { bg: 'bg-cyan-950/60', border: 'border-cyan-500/50', text: 'text-cyan-400', fill: '#06b6d4' },
  'Risk & Compliance': { bg: 'bg-amber-950/60', border: 'border-amber-500/50', text: 'text-amber-400', fill: '#f59e0b' },
};

export const KnowledgeGraphView: React.FC = () => {
  const [graphData, setGraphData] = useState<KnowledgeGraphData | null>(null);
  const [loading, setLoading] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [selectedNode, setSelectedNode] = useState<GraphNode | null>(null);
  const [error, setError] = useState<string | null>(null);
  const svgRef = useRef<SVGSVGElement>(null);

  const loadGraph = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await graphApi.getGraph();
      if (!data || !data.nodes || data.nodes.length === 0) {
        // Automatically sync if empty
        const synced = await graphApi.syncGraph();
        setGraphData(synced);
        if (synced && synced.nodes && synced.nodes.length > 0) {
          setSelectedNode(synced.nodes[0]);
        }
      } else {
        setGraphData(data);
        if (data.nodes.length > 0) {
          setSelectedNode(data.nodes[0]);
        }
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load knowledge graph');
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async () => {
    try {
      setSyncing(true);
      setError(null);
      const updated = await graphApi.syncGraph();
      setGraphData(updated);
      if (updated && updated.nodes && updated.nodes.length > 0) {
        setSelectedNode(updated.nodes[0]);
      }
    } catch (err: any) {
      setError('Sync failed: ' + (err.message || 'Server error'));
    } finally {
      setSyncing(false);
    }
  };

  useEffect(() => {
    loadGraph();
  }, []);

  // Compute node 2D circular/clustered coordinates for SVG visualization
  const width = 760;
  const height = 480;
  const centerX = width / 2;
  const centerY = height / 2;

  const nodesWithCoords: GraphNode[] = (graphData?.nodes || []).map((node, idx, arr) => {
    const angle = (idx / arr.length) * 2 * Math.PI;
    const radius = 175;
    const x = centerX + radius * Math.cos(angle);
    const y = centerY + radius * Math.sin(angle);
    return { ...node, x, y };
  });

  const nodeMapById = new Map(nodesWithCoords.map((n) => [n.id, n]));
  const nodeMapByName = new Map(nodesWithCoords.map((n) => [n.name.toLowerCase().trim(), n]));

  const findNode = (id?: string, name?: string): GraphNode | undefined => {
    if (id && nodeMapById.has(id)) return nodeMapById.get(id);
    if (name && nodeMapByName.has(name.toLowerCase().trim())) return nodeMapByName.get(name.toLowerCase().trim());
    return undefined;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <Network className="h-5 w-5 text-indigo-400" />
            Corporate SOP Knowledge Graph Topology
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Visual neural network topology showing topic nodes, policy cross-references, and query traffic weights.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={handleSync}
            disabled={syncing || loading}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition disabled:opacity-50"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${syncing ? 'animate-spin' : ''}`} />
            <span>{syncing ? 'Re-syncing Topology...' : 'Re-sync Knowledge Graph'}</span>
          </button>
        </div>
      </div>

      {/* Summary KPI Badges */}
      {graphData && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
            <span className="text-[11px] text-slate-400 uppercase tracking-wider font-semibold">
              Total Topic Nodes
            </span>
            <p className="text-xl font-bold text-indigo-400 mt-0.5">
              {graphData.totalNodes}
            </p>
          </div>
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
            <span className="text-[11px] text-slate-400 uppercase tracking-wider font-semibold">
              Relational Edges
            </span>
            <p className="text-xl font-bold text-emerald-400 mt-0.5">
              {graphData.totalEdges}
            </p>
          </div>
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
            <span className="text-[11px] text-slate-400 uppercase tracking-wider font-semibold">
              Dominant Category
            </span>
            <p className="text-sm font-bold text-slate-200 mt-1 truncate">
              {graphData.topCategory}
            </p>
          </div>
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-3">
            <span className="text-[11px] text-slate-400 uppercase tracking-wider font-semibold">
              Query Hits Audited
            </span>
            <p className="text-xl font-bold text-violet-400 mt-0.5">
              {graphData.totalQueriesProcessed}
            </p>
          </div>
        </div>
      )}

      {/* Main Canvas & Detail Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* SVG Graph Visualizer */}
        <div className="lg:col-span-2 bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex flex-col items-center justify-center relative overflow-hidden shadow-xl">
          <div className="absolute top-4 left-4 z-10 flex items-center gap-2">
            <span className="flex h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <span className="text-xs text-slate-400 font-mono">Interactive Topology View</span>
          </div>

          {loading ? (
            <div className="h-96 flex flex-col items-center justify-center gap-3 text-slate-400">
              <RefreshCw className="h-8 w-8 animate-spin text-indigo-400" />
              <p className="text-sm">Building Knowledge Graph from Hibernate entities...</p>
            </div>
          ) : error ? (
            <div className="h-96 flex flex-col items-center justify-center gap-3 text-slate-300 text-sm">
              <p className="text-rose-400 font-medium">⚠️ {error}</p>
              <button
                onClick={handleSync}
                className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition"
              >
                Initialize & Re-sync Knowledge Graph
              </button>
            </div>
          ) : nodesWithCoords.length === 0 ? (
            <div className="h-96 flex flex-col items-center justify-center gap-3 text-slate-400 text-sm">
              <p>No topic nodes found in database.</p>
              <button
                onClick={handleSync}
                className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold shadow-md shadow-indigo-600/30 transition"
              >
                Build Knowledge Graph Topology
              </button>
            </div>
          ) : (
            <svg
              ref={svgRef}
              viewBox={`0 0 ${width} ${height}`}
              className="w-full h-[460px] select-none"
            >
              <defs>
                <radialGradient id="nodeGlow" cx="50%" cy="50%" r="50%">
                  <stop offset="0%" stopColor="#6366f1" stopOpacity="0.3" />
                  <stop offset="100%" stopColor="#6366f1" stopOpacity="0" />
                </radialGradient>
              </defs>

              {/* Central Neural Hub Indicator */}
              <circle cx={centerX} cy={centerY} r="60" fill="url(#nodeGlow)" />
              <circle cx={centerX} cy={centerY} r="36" fill="#0f172a" stroke="#334155" strokeWidth="1.5" />
              <text
                x={centerX}
                y={centerY - 4}
                textAnchor="middle"
                fill="#818cf8"
                fontSize="11"
                fontWeight="bold"
              >
                OpsMind
              </text>
              <text
                x={centerX}
                y={centerY + 12}
                textAnchor="middle"
                fill="#64748b"
                fontSize="9"
              >
                Neural Brain
              </text>

              {/* Render Connecting Edges */}
              {(graphData?.edges || []).map((edge, idx) => {
                const s = findNode(edge.sourceId, edge.sourceName);
                const t = findNode(edge.targetId, edge.targetName);
                if (!s || !t || s.x === undefined || s.y === undefined || t.x === undefined || t.y === undefined) {
                  return null;
                }

                const isConnectedToSelected =
                  selectedNode && (selectedNode.name === s.name || selectedNode.name === t.name);

                return (
                  <line
                    key={idx}
                    x1={s.x}
                    y1={s.y}
                    x2={t.x}
                    y2={t.y}
                    stroke={isConnectedToSelected ? '#818cf8' : '#334155'}
                    strokeWidth={isConnectedToSelected ? Math.max(2, edge.weight) : Math.max(1, edge.weight * 0.7)}
                    strokeOpacity={isConnectedToSelected ? 0.9 : 0.4}
                    strokeDasharray={edge.relationshipType === 'SECURITY_PREREQUISITE' ? '4 2' : undefined}
                  />
                );
              })}

              {/* Render Topic Nodes */}
              {nodesWithCoords.map((node) => {
                const isSelected = selectedNode?.id === node.id;
                const colors = CATEGORY_COLORS[node.category] || CATEGORY_COLORS['Finance & Billing'];
                const radius = Math.min(26, Math.max(16, 16 + (node.queryFrequency || 0) * 2));

                return (
                  <g
                    key={node.id}
                    onClick={() => setSelectedNode(node)}
                    className="cursor-pointer transition-transform duration-200 hover:scale-110"
                  >
                    {isSelected && (
                      <circle
                        cx={node.x}
                        cy={node.y}
                        r={radius + 6}
                        fill="none"
                        stroke="#a5b4fc"
                        strokeWidth="2"
                        className="animate-ping opacity-30"
                      />
                    )}
                    <circle
                      cx={node.x}
                      cy={node.y}
                      r={radius}
                      fill="#0f172a"
                      stroke={isSelected ? '#c7d2fe' : colors.fill}
                      strokeWidth={isSelected ? '2.5' : '1.8'}
                    />
                    <circle
                      cx={node.x}
                      cy={node.y}
                      r={radius - 4}
                      fill={colors.fill}
                      fillOpacity={isSelected ? 0.35 : 0.15}
                    />
                    <text
                      x={node.x}
                      y={(node.y || 0) + radius + 14}
                      textAnchor="middle"
                      fill={isSelected ? '#ffffff' : '#cbd5e1'}
                      fontSize="10"
                      fontWeight={isSelected ? 'bold' : 'normal'}
                      className="pointer-events-none drop-shadow"
                    >
                      {node.name}
                    </text>
                  </g>
                );
              })}
            </svg>
          )}

          {/* Category Legend */}
          <div className="flex flex-wrap items-center justify-center gap-4 mt-2 pt-3 border-t border-slate-800/80 text-[11px]">
            {Object.entries(CATEGORY_COLORS).map(([cat, style]) => (
              <div key={cat} className="flex items-center gap-1.5 text-slate-400">
                <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: style.fill }} />
                <span>{cat}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Node Inspector Panel */}
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-5 space-y-5">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-800 text-sm font-semibold text-slate-200">
            <Info className="h-4 w-4 text-indigo-400" />
            <span>Topic Node Inspector</span>
          </div>

          {selectedNode ? (
            <div className="space-y-4">
              <div>
                <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                  Selected Concept
                </span>
                <h3 className="text-lg font-bold text-white mt-0.5">
                  {selectedNode.name}
                </h3>
                <span
                  className={`inline-block mt-1 text-xs px-2.5 py-0.5 rounded-full font-medium border ${
                    CATEGORY_COLORS[selectedNode.category]?.border || 'border-slate-700'
                  } ${CATEGORY_COLORS[selectedNode.category]?.bg || 'bg-slate-800'} ${
                    CATEGORY_COLORS[selectedNode.category]?.text || 'text-slate-300'
                  }`}
                >
                  {selectedNode.category}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="bg-slate-950/70 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 flex items-center gap-1">
                    <Activity className="h-3.5 w-3.5 text-indigo-400" />
                    Query Hits
                  </span>
                  <p className="text-base font-bold text-slate-100 mt-1">
                    {selectedNode.queryFrequency} inquiries
                  </p>
                </div>
                <div className="bg-slate-950/70 p-3 rounded-xl border border-slate-800">
                  <span className="text-slate-400 flex items-center gap-1">
                    <FileText className="h-3.5 w-3.5 text-emerald-400" />
                    SOP Coverage
                  </span>
                  <p className="text-base font-bold text-slate-100 mt-1">
                    {selectedNode.associatedDocumentCount} documents
                  </p>
                </div>
              </div>

              <div className="bg-slate-950/70 p-3 rounded-xl border border-slate-800 text-xs">
                <div className="flex justify-between items-center mb-1">
                  <span className="text-slate-400">Calculated Importance Index</span>
                  <span className="font-mono font-semibold text-indigo-400">
                    {selectedNode.importance}
                  </span>
                </div>
                <p className="text-[11px] text-slate-500">
                  Weighted combination of corporate document occurrences and real-time employee inquiry volume.
                </p>
              </div>

              {/* Connected edges for selected node */}
              <div>
                <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider block mb-2">
                  Connected Relational Edges
                </span>
                <div className="space-y-1.5 max-h-48 overflow-y-auto">
                  {(graphData?.edges || [])
                    .filter((e) => e.sourceName === selectedNode.name || e.targetName === selectedNode.name)
                    .map((edge) => {
                      const peerName = edge.sourceName === selectedNode.name ? edge.targetName : edge.sourceName;
                      return (
                        <div
                          key={edge.id}
                          className="flex items-center justify-between p-2 rounded-lg bg-slate-950 border border-slate-800/80 text-xs"
                        >
                          <div className="flex items-center gap-2 truncate">
                            <span className="text-indigo-400 font-medium truncate">
                              ↔ {peerName}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 shrink-0">
                            <span className="text-[10px] text-slate-400 font-mono">
                              w: {edge.weight}
                            </span>
                            <span className="text-[9px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">
                              {edge.relationshipType}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                </div>
              </div>
            </div>
          ) : (
            <p className="text-xs text-slate-500 italic">
              Click any node on the graph to inspect its properties and relationships.
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
