export interface Citation {
  sourceFilename: string;
  pageNumber: number;
  excerpt: string;
  relevanceScore: number;
}

export interface QueryResponse {
  id?: string;
  question: string;
  answer: string;
  confidenceScore: number;
  guardrailTriggered: boolean;
  latencyMs: number;
  citations: Citation[];
  createdAt?: string;
}

export interface DocumentChunk {
  id: string;
  pageNumber: number;
  chunkIndex: number;
  content: string;
  startChar: number;
  endChar: number;
}

export interface DocumentSummary {
  id: string;
  filename: string;
  fileType: string;
  fileSizeBytes: number;
  totalPages: number;
  totalChunks: number;
  status: string;
  uploadedAt: string;
  summary?: string;
  chunks?: DocumentChunk[];
}

export interface GraphNode {
  id: string;
  name: string;
  category: string;
  queryFrequency: number;
  associatedDocumentCount: number;
  importance: number;
  x?: number;
  y?: number;
  vx?: number;
  vy?: number;
}

export interface GraphEdge {
  id: string;
  sourceId: string;
  sourceName: string;
  targetId: string;
  targetName: string;
  weight: number;
  relationshipType: string;
}

export interface KnowledgeGraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
  totalNodes: number;
  totalEdges: number;
  topCategory: string;
  totalQueriesProcessed: number;
}

export interface AnalyticsOverview {
  totalQueries: number;
  answeredQueries: number;
  guardrailTriggeredCount: number;
  guardrailTriggerRate: number;
  knowledgeGapCount: number;
  averageConfidenceScore: number;
  averageLatencyMs: number;
  totalDocuments: number;
  totalChunks: number;
}

export interface TopDocument {
  filename: string;
  citationCount: number;
  citationPercentage: number;
}

export interface KnowledgeGap {
  id: string;
  question: string;
  confidenceScore: number;
  createdAt: string;
  suggestedDepartment: string;
}

export interface TopicMetric {
  topicName: string;
  category: string;
  queryCount: number;
  documentCount: number;
  percentage: number;
}
