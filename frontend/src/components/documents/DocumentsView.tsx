import React, { useState, useEffect } from 'react';
import {
  FileText,
  Upload,
  Trash2,
  Eye,
  RefreshCw,
  Sparkles,
  CheckCircle2,
  X,
  Layers,
  Database,
} from 'lucide-react';
import { DocumentSummary, DocumentChunk } from '../../types';
import { documentsApi } from '../../services/api';

export const DocumentsView: React.FC = () => {
  const [documents, setDocuments] = useState<DocumentSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [seeding, setSeeding] = useState(false);
  const [selectedDoc, setSelectedDoc] = useState<DocumentSummary | null>(null);
  const [inspectingDoc, setInspectingDoc] = useState<DocumentSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadDocuments = async () => {
    try {
      setLoading(true);
      setError(null);
      const docs = await documentsApi.getAll();
      setDocuments(docs);
    } catch (err: any) {
      setError(err.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDocuments();
  }, []);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.pdf')) {
      alert('Only PDF documents are supported for corporate SOP indexing.');
      return;
    }

    try {
      setUploading(true);
      await documentsApi.uploadPdf(file);
      await loadDocuments();
    } catch (err: any) {
      alert('Upload failed: ' + err.message);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleSeedSamples = async () => {
    try {
      setSeeding(true);
      await documentsApi.seedSamples();
      await loadDocuments();
    } catch (err: any) {
      alert('Failed to seed sample SOPs: ' + err.message);
    } finally {
      setSeeding(false);
    }
  };

  const handleDelete = async (id: string, filename: string) => {
    if (!window.confirm(`Are you sure you want to delete '${filename}'? This will cascade-delete all chunks and citations from Hibernate.`)) {
      return;
    }

    try {
      await documentsApi.delete(id);
      await loadDocuments();
      if (inspectingDoc?.id === id) {
        setInspectingDoc(null);
      }
    } catch (err: any) {
      alert('Failed to delete document: ' + err.message);
    }
  };

  const handleInspect = async (doc: DocumentSummary) => {
    try {
      const detailed = await documentsApi.getById(doc.id);
      setInspectingDoc(detailed);
    } catch (err: any) {
      alert('Failed to load chunks for document: ' + err.message);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      {/* Header & Quick Action Buttons */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <FileText className="h-5 w-5 text-indigo-400" />
            SOP Knowledge Base & Ingestion Pipeline
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Manage corporate PDF policies parsed via Apache PDFBox 3, sliding-window chunked, and indexed in Hibernate.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleSeedSamples}
            disabled={seeding || loading}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold border border-slate-700 transition disabled:opacity-50"
          >
            <Sparkles className={`h-3.5 w-3.5 text-amber-400 ${seeding ? 'animate-spin' : ''}`} />
            <span>{seeding ? 'Seeding SOPs...' : 'Re-seed Sample SOPs'}</span>
          </button>
        </div>
      </div>

      {/* Drag & Drop Upload Zone */}
      <div className="border-2 border-dashed border-slate-800 hover:border-indigo-500/50 bg-slate-900/60 rounded-2xl p-6 text-center transition group">
        <input
          type="file"
          id="pdf-upload"
          accept="application/pdf"
          onChange={handleFileUpload}
          disabled={uploading}
          className="hidden"
        />
        <label
          htmlFor="pdf-upload"
          className="cursor-pointer flex flex-col items-center justify-center gap-2"
        >
          <div className="p-3 rounded-full bg-indigo-500/10 text-indigo-400 group-hover:scale-110 transition duration-200 border border-indigo-500/20">
            {uploading ? (
              <RefreshCw className="h-6 w-6 animate-spin text-indigo-400" />
            ) : (
              <Upload className="h-6 w-6" />
            )}
          </div>
          <span className="text-sm font-semibold text-slate-200 group-hover:text-indigo-300">
            {uploading ? 'Parsing PDF pages and generating sliding-window chunks...' : 'Click or drag PDF SOP to upload & index'}
          </span>
          <span className="text-xs text-slate-500">
            Automatic physical page extraction (PDFBox 3.0.3) • 500-char chunks • 100-char overlap
          </span>
        </label>
      </div>

      {/* Documents List Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <Database className="h-4 w-4 text-emerald-400" />
            Active Indexed Corporate SOPs ({documents.length})
          </h3>
          <button
            onClick={loadDocuments}
            disabled={loading}
            className="text-xs text-slate-400 hover:text-slate-200 flex items-center gap-1 transition"
          >
            <RefreshCw className={`h-3 w-3 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>

        {loading ? (
          <div className="py-12 text-center text-slate-400 text-xs flex flex-col items-center justify-center gap-2">
            <RefreshCw className="h-6 w-6 animate-spin text-indigo-400" />
            Loading documents from database...
          </div>
        ) : documents.length === 0 ? (
          <div className="py-12 text-center text-slate-500 text-xs">
            No SOP documents found in database. Click &ldquo;Re-seed Sample SOPs&rdquo; above to load sample policies!
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-semibold uppercase tracking-wider text-[10px] border-b border-slate-800">
                <tr>
                  <th className="py-2.5 px-3">Document Filename</th>
                  <th className="py-2.5 px-3">Pages</th>
                  <th className="py-2.5 px-3">Chunks</th>
                  <th className="py-2.5 px-3">File Size</th>
                  <th className="py-2.5 px-3">Status</th>
                  <th className="py-2.5 px-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {documents.map((doc) => (
                  <tr key={doc.id} className="hover:bg-slate-950/40 transition">
                    <td className="py-3 px-3 font-medium text-slate-200 flex items-center gap-2">
                      <FileText className="h-4 w-4 text-indigo-400 shrink-0" />
                      <span className="truncate max-w-xs">{doc.filename}</span>
                    </td>
                    <td className="py-3 px-3 font-mono text-slate-300">
                      {doc.totalPages} pages
                    </td>
                    <td className="py-3 px-3 font-mono text-slate-300">
                      {doc.totalChunks} chunks
                    </td>
                    <td className="py-3 px-3 font-mono text-slate-400">
                      {Math.round(doc.fileSizeBytes / 1024)} KB
                    </td>
                    <td className="py-3 px-3">
                      <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {doc.status}
                      </span>
                    </td>
                    <td className="py-3 px-3 text-right space-x-2">
                      <button
                        onClick={() => handleInspect(doc)}
                        className="px-2.5 py-1 rounded-lg bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-400 border border-indigo-500/20 font-medium transition inline-flex items-center gap-1"
                      >
                        <Eye className="h-3 w-3" />
                        Inspect Chunks
                      </button>
                      <button
                        onClick={() => handleDelete(doc.id, doc.filename)}
                        className="px-2.5 py-1 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 font-medium transition inline-flex items-center gap-1"
                      >
                        <Trash2 className="h-3 w-3" />
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Chunk Inspector Modal */}
      {inspectingDoc && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm">
          <div className="w-full max-w-3xl bg-slate-900 border border-slate-800 rounded-2xl max-h-[85vh] flex flex-col shadow-2xl animate-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between p-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Layers className="h-5 w-5 text-indigo-400" />
                <div>
                  <h4 className="text-sm font-bold text-white">
                    {inspectingDoc.filename}
                  </h4>
                  <p className="text-xs text-slate-400">
                    {inspectingDoc.totalPages} Pages • {inspectingDoc.chunks?.length || 0} Overlapping Chunks
                  </p>
                </div>
              </div>
              <button
                onClick={() => setInspectingDoc(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            {/* Chunks List */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {(inspectingDoc.chunks || []).map((chunk) => (
                <div
                  key={chunk.id}
                  className="bg-slate-950 p-3.5 rounded-xl border border-slate-800/80 space-y-1.5"
                >
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-indigo-400">
                      Chunk #{chunk.chunkIndex + 1}
                    </span>
                    <div className="flex items-center gap-2 font-mono text-[11px] text-slate-400">
                      <span className="px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">
                        Page {chunk.pageNumber}
                      </span>
                      <span>
                        Chars: {chunk.startChar} - {chunk.endChar}
                      </span>
                    </div>
                  </div>
                  <p className="text-xs text-slate-300 font-mono leading-relaxed bg-slate-900/60 p-2.5 rounded-lg border border-slate-800/40">
                    {chunk.content}
                  </p>
                </div>
              ))}
            </div>

            {/* Modal Footer */}
            <div className="p-3 border-t border-slate-800 text-right">
              <button
                onClick={() => setInspectingDoc(null)}
                className="px-4 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold transition"
              >
                Close Inspector
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
