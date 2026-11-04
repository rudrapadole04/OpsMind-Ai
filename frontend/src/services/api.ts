import {
  QueryResponse,
  KnowledgeGraphData,
  AnalyticsOverview,
  TopDocument,
  KnowledgeGap,
  TopicMetric,
  DocumentSummary,
} from '../types';

const API_BASE = '/api';

export const queryApi = {
  async askQuestion(question: string): Promise<QueryResponse> {
    const res = await fetch(`${API_BASE}/query`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question }),
    });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || 'Failed to process query');
    }
    return res.json();
  },

  async getHistory(): Promise<QueryResponse[]> {
    const res = await fetch(`${API_BASE}/query/history`);
    if (!res.ok) throw new Error('Failed to fetch query history');
    return res.json();
  },

  async getKnowledgeGaps(): Promise<QueryResponse[]> {
    const res = await fetch(`${API_BASE}/query/knowledge-gaps`);
    if (!res.ok) throw new Error('Failed to fetch knowledge gaps');
    return res.json();
  },
};

export const graphApi = {
  async getGraph(): Promise<KnowledgeGraphData> {
    const res = await fetch(`${API_BASE}/graph`);
    if (!res.ok) throw new Error('Failed to load knowledge graph');
    return res.json();
  },

  async syncGraph(): Promise<KnowledgeGraphData> {
    const res = await fetch(`${API_BASE}/graph/sync`, { method: 'POST' });
    if (!res.ok) throw new Error('Failed to sync knowledge graph');
    return res.json();
  },

  async getTopTopics(limit = 5): Promise<any[]> {
    const res = await fetch(`${API_BASE}/graph/top-topics?limit=${limit}`);
    if (!res.ok) throw new Error('Failed to load top topics');
    return res.json();
  },
};

export const analyticsApi = {
  async getOverview(): Promise<AnalyticsOverview> {
    const res = await fetch(`${API_BASE}/analytics/overview`);
    if (!res.ok) throw new Error('Failed to load analytics overview');
    return res.json();
  },

  async getTopDocuments(): Promise<TopDocument[]> {
    const res = await fetch(`${API_BASE}/analytics/top-documents`);
    if (!res.ok) throw new Error('Failed to load top documents');
    return res.json();
  },

  async getKnowledgeGaps(limit = 20): Promise<KnowledgeGap[]> {
    const res = await fetch(`${API_BASE}/analytics/knowledge-gaps?limit=${limit}`);
    if (!res.ok) throw new Error('Failed to load knowledge gaps');
    return res.json();
  },

  async getTopics(): Promise<TopicMetric[]> {
    const res = await fetch(`${API_BASE}/analytics/topics`);
    if (!res.ok) throw new Error('Failed to load topic analytics');
    return res.json();
  },
};

export const documentsApi = {
  async getAll(): Promise<DocumentSummary[]> {
    const res = await fetch(`${API_BASE}/documents`);
    if (!res.ok) throw new Error('Failed to load documents');
    return res.json();
  },

  async getById(id: string): Promise<DocumentSummary> {
    const res = await fetch(`${API_BASE}/documents/${id}`);
    if (!res.ok) throw new Error('Failed to load document details');
    return res.json();
  },

  async uploadPdf(file: File): Promise<DocumentSummary> {
    const formData = new FormData();
    formData.append('file', file);
    const res = await fetch(`${API_BASE}/documents/upload`, {
      method: 'POST',
      body: formData,
    });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(err || 'Failed to upload document');
    }
    return res.json();
  },

  async delete(id: string): Promise<boolean> {
    const res = await fetch(`${API_BASE}/documents/${id}`, { method: 'DELETE' });
    return res.ok;
  },

  async seedSamples(): Promise<DocumentSummary[]> {
    const res = await fetch(`${API_BASE}/documents/seed-samples`, { method: 'POST' });
    if (!res.ok) throw new Error('Failed to seed sample SOPs');
    return res.json();
  },
};
