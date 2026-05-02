import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import './App.css';

const API_URL = 'http://localhost:8081/api';

function Counter({ value }) {
  const [display, setDisplay] = useState(0);
  useEffect(() => {
    let start = 0;
    const step = Math.ceil(value / 20);
    const timer = setInterval(() => {
      start += step;
      if (start >= value) { setDisplay(value); clearInterval(timer); }
      else setDisplay(start);
    }, 30);
    return () => clearInterval(timer);
  }, [value]);
  return <span>{display}</span>;
}

export default function App() {
  const [groupedDocs, setGroupedDocs] = useState({});
  const [loadingDocs, setLoadingDocs] = useState(true);
  const [expandedFolders, setExpandedFolders] = useState({});
  const [folderFiles, setFolderFiles] = useState([]);
  const [organizing, setOrganizing] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [activeTab, setActiveTab] = useState('library');
  const [notification, setNotification] = useState(null);
  const [dragOver, setDragOver] = useState(false);

  // Move modal state
  const [moveModal, setMoveModal] = useState(null); // {docId, currentCategory}
  const [moveTarget, setMoveTarget] = useState('');

  const fileInputRef = useRef();

  useEffect(() => { loadGroupedDocuments(); }, []);

  const showNotification = (msg, type = 'success') => {
    setNotification({ msg, type });
    setTimeout(() => setNotification(null), 3500);
  };

  const loadGroupedDocuments = async () => {
    setLoadingDocs(true);
    try {
      const res = await axios.get(`${API_URL}/documents/grouped`);
      setGroupedDocs(res.data);
    } catch (e) {
      showNotification('Failed to load documents', 'error');
    } finally {
      setLoadingDocs(false);
    }
  };

  const toggleFolder = (cat) =>
    setExpandedFolders(p => ({ ...p, [cat]: !p[cat] }));

  const openPdf = (id) =>
    window.open(`${API_URL}/documents/${id}/view`, '_blank');

  // Delete a document
  const handleDelete = async (e, docId, filename) => {
    e.stopPropagation(); // Prevent opening PDF
    if (!window.confirm(`Delete "${filename}"?`)) return;
    try {
      await axios.delete(`${API_URL}/documents/${docId}`);
      showNotification(`✓ "${filename}" deleted`);
      await loadGroupedDocuments(); // Reload folders
    } catch (err) {
      showNotification('Delete failed!', 'error');
    }
  };

  // Open move modal
  const handleMoveClick = (e, docId, currentCategory) => {
    e.stopPropagation(); // Prevent opening PDF
    setMoveModal({ docId, currentCategory });
    setMoveTarget('');
  };

  // Confirm move
  const handleMoveConfirm = async () => {
    if (!moveTarget.trim()) return showNotification('Enter a category name!', 'error');
    try {
      await axios.put(`${API_URL}/documents/${moveModal.docId}/move`, null, {
        params: { category: moveTarget.trim() }
      });
      showNotification(`✓ Moved to "${moveTarget.trim()}"`);
      setMoveModal(null);
      await loadGroupedDocuments(); // Reload folders
    } catch (err) {
      showNotification('Move failed!', 'error');
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    const files = Array.from(e.dataTransfer.files).filter(f => f.name.endsWith('.pdf'));
    setFolderFiles(files);
  };

  const handleOrganize = async () => {
    if (!folderFiles.length) return showNotification('No PDFs selected!', 'error');
    setOrganizing(true);
    setUploadProgress(0);
    const formData = new FormData();
    folderFiles.forEach(f => formData.append('files', f));
    try {
      const res = await axios.post(`${API_URL}/upload-bulk`, formData, {
        onUploadProgress: (e) => {
          setUploadProgress(Math.round((e.loaded * 100) / e.total));
        }
      });

      // Check for duplicates in response
      const duplicates = res.data.filter(r => r.duplicateWarning);
      if (duplicates.length > 0) {
        showNotification(`⚠️ ${duplicates.length} duplicate(s) detected!`, 'error');
      } else {
        showNotification(`✓ ${folderFiles.length} files organized successfully!`);
      }

      await loadGroupedDocuments();
      setFolderFiles([]);
      setActiveTab('library');
    } catch (e) {
      showNotification('Upload failed. Check servers.', 'error');
    } finally {
      setOrganizing(false);
      setUploadProgress(0);
    }
  };

  const handleSearch = async () => {
    if (!query.trim()) return;
    setSearching(true);
    setSearchResults([]);
    try {
      const res = await axios.get(`${API_URL}/search`, { params: { query } });
      setSearchResults(res.data);
    } catch (e) {
      showNotification('Search failed', 'error');
    } finally {
      setSearching(false);
    }
  };

  const totalDocs = Object.values(groupedDocs).reduce((a, b) => a + b.length, 0);
  const totalCategories = Object.keys(groupedDocs).length;
  const allCategories = Object.keys(groupedDocs);

  const categoryColors = [
    '#00d4ff', '#ff6b6b', '#51cf66', '#fcc419',
    '#cc5de8', '#ff922b', '#20c997', '#f06595'
  ];

  return (
    <div className="root">
      {/* Notification */}
      {notification && (
        <div className={`toast ${notification.type}`}>
          {notification.msg}
        </div>
      )}

      {/* Move Modal */}
      {moveModal && (
        <div className="modal-overlay" onClick={() => setMoveModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h3 className="modal-title">✂️ Move to Folder</h3>
            <p className="modal-sub">
              Current: <strong>{moveModal.currentCategory}</strong>
            </p>

            {/* Quick select existing categories */}
            <div className="modal-categories">
              {allCategories
                .filter(c => c !== moveModal.currentCategory)
                .map(cat => (
                  <button
                    key={cat}
                    className={`cat-chip ${moveTarget === cat ? 'selected' : ''}`}
                    onClick={() => setMoveTarget(cat)}
                  >
                    📁 {cat}
                  </button>
                ))}
            </div>

            {/* Or type a new category */}
            <input
              className="modal-input"
              type="text"
              placeholder="Or type a new category name..."
              value={moveTarget}
              onChange={e => setMoveTarget(e.target.value)}
            />

            <div className="modal-actions">
              <button className="modal-cancel" onClick={() => setMoveModal(null)}>
                Cancel
              </button>
              <button className="modal-confirm" onClick={handleMoveConfirm}>
                Move →
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Sidebar */}
      <aside className="sidebar">
        <div className="logo">
          <div className="logo-icon">⬡</div>
          <div>
            <div className="logo-name">DocuSense</div>
            <div className="logo-tag">AI Document OS</div>
          </div>
        </div>

        <nav className="nav">
          {[
            { id: 'library', icon: '▤', label: 'Library' },
            { id: 'upload', icon: '↑', label: 'Upload' },
            { id: 'search', icon: '⌕', label: 'Search' },
          ].map(tab => (
            <button
              key={tab.id}
              className={`nav-btn ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              <span className="nav-icon">{tab.icon}</span>
              <span>{tab.label}</span>
            </button>
          ))}
        </nav>

        <div className="stats">
          <div className="stat">
            <div className="stat-value"><Counter value={totalDocs} /></div>
            <div className="stat-label">Documents</div>
          </div>
          <div className="stat">
            <div className="stat-value"><Counter value={totalCategories} /></div>
            <div className="stat-label">Categories</div>
          </div>
        </div>

        <div className="sidebar-footer">
          <div className="ai-badge">⬡ Powered by Llama 3</div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main">

        {/* Library Tab */}
        {activeTab === 'library' && (
          <div className="panel">
            <div className="panel-header">
              <div>
                <h1 className="panel-title">Document Library</h1>
                <p className="panel-sub">AI-organized folders — click to explore</p>
              </div>
              <button className="refresh-btn" onClick={loadGroupedDocuments}>↻ Refresh</button>
            </div>

            {loadingDocs && (
              <div className="loading-state">
                <div className="spinner" />
                <p>Loading your documents...</p>
              </div>
            )}

            {!loadingDocs && totalDocs === 0 && (
              <div className="empty-state">
                <div className="empty-icon">📂</div>
                <h3>No documents yet</h3>
                <p>Upload a folder to get started</p>
                <button className="cta-btn" onClick={() => setActiveTab('upload')}>
                  Upload Now →
                </button>
              </div>
            )}

            <div className="folders-grid">
              {Object.entries(groupedDocs).map(([cat, docs], i) => (
                <div key={cat} className="folder-card"
                  style={{ '--accent': categoryColors[i % categoryColors.length] }}>

                  <div className="folder-card-header" onClick={() => toggleFolder(cat)}>
                    <div className="folder-card-icon">
                      {expandedFolders[cat] ? '📂' : '📁'}
                    </div>
                    <div className="folder-card-info">
                      <div className="folder-card-name">{cat}</div>
                      <div className="folder-card-count">
                        {docs.length} file{docs.length !== 1 ? 's' : ''}
                      </div>
                    </div>
                    <div className="folder-card-arrow">
                      {expandedFolders[cat] ? '▾' : '▸'}
                    </div>
                  </div>

                  {expandedFolders[cat] && (
                    <div className="folder-card-files">
                      {docs.map(doc => (
                        <div key={doc.id} className="file-row">

                          {/* File name - click to open */}
                          <span className="file-icon">⬜</span>
                          <span
                            className="file-name"
                            onClick={() => openPdf(doc.id)}
                          >
                            {doc.filename}
                          </span>
                            {/* Show duplicate badge if document is a duplicate */}
  {doc.duplicate && (
    <span className="duplicate-badge">⚠️ Duplicate</span>
  )}

                          {/* Action buttons */}
                          <div className="file-actions">
                            <button
                              className="action-btn move-btn"
                              onClick={(e) => handleMoveClick(e, doc.id, cat)}
                              title="Move to another folder"
                            >
                              ✂️
                            </button>
                            <button
                              className="action-btn delete-btn"
                              onClick={(e) => handleDelete(e, doc.id, doc.filename)}
                              title="Delete file"
                            >
                              🗑️
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Upload Tab */}
        {activeTab === 'upload' && (
          <div className="panel">
            <div className="panel-header">
              <div>
                <h1 className="panel-title">Upload & Organize</h1>
                <p className="panel-sub">Drop a folder — AI sorts everything automatically</p>
              </div>
            </div>

            <div
              className={`dropzone ${dragOver ? 'dragover' : ''}`}
              onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
              onDragLeave={() => setDragOver(false)}
              onDrop={handleDrop}
              onClick={() => fileInputRef.current.click()}
            >
              <input
                ref={fileInputRef}
                type="file"
                accept=".pdf"
                multiple
                webkitdirectory=""
                style={{ display: 'none' }}
                onChange={(e) => {
                  const files = Array.from(e.target.files).filter(f => f.name.endsWith('.pdf'));
                  setFolderFiles(files);
                }}
              />
              <div className="dropzone-icon">⬡</div>
              <div className="dropzone-text">
                {folderFiles.length > 0
                  ? `${folderFiles.length} PDF(s) ready to organize`
                  : 'Drop folder here or click to browse'}
              </div>
              <div className="dropzone-sub">Supports PDF files only</div>
            </div>

            {folderFiles.length > 0 && (
              <div className="file-preview">
                <div className="file-preview-header">
                  Files to organize ({folderFiles.length})
                </div>
                {folderFiles.slice(0, 5).map((f, i) => (
                  <div key={i} className="file-preview-item">
                    <span>⬜ {f.name}</span>
                    <span className="file-size">{(f.size / 1024).toFixed(1)} KB</span>
                  </div>
                ))}
                {folderFiles.length > 5 && (
                  <div className="file-preview-more">
                    +{folderFiles.length - 5} more files...
                  </div>
                )}
              </div>
            )}

            {organizing && (
              <div className="progress-container">
                <div className="progress-label">
                  🤖 AI is reading and categorizing your documents...
                </div>
                <div className="progress-bar">
                  <div className="progress-fill" style={{ width: `${uploadProgress}%` }} />
                </div>
                <div className="progress-pct">{uploadProgress}%</div>
              </div>
            )}

            <button
              className="organize-btn"
              onClick={handleOrganize}
              disabled={organizing || folderFiles.length === 0}
            >
              {organizing ? '⬡ Organizing with AI...' : '✨ Organize with AI'}
            </button>
          </div>
        )}

        {/* Search Tab */}
        {activeTab === 'search' && (
          <div className="panel">
            <div className="panel-header">
              <div>
                <h1 className="panel-title">Semantic Search</h1>
                <p className="panel-sub">Search by meaning — not just keywords</p>
              </div>
            </div>

            <div className="search-box">
              <span className="search-icon">⌕</span>
              <input
                className="search-input"
                type="text"
                placeholder="e.g. community events, payment records..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
              <button className="search-btn" onClick={handleSearch} disabled={searching}>
                {searching ? '...' : 'Search'}
              </button>
            </div>

            {searching && (
              <div className="loading-state">
                <div className="spinner" />
                <p>Searching through documents...</p>
              </div>
            )}

            {searchResults.length > 0 && (
              <div className="search-results">
                <div className="results-header">{searchResults.length} results found</div>
                {searchResults.map((r, i) => (
                  <div key={i} className="result-card" onClick={() => openPdf(r.document.id)}>
                    <div className="result-rank">#{i + 1}</div>
                    <div className="result-info">
                      <div className="result-filename">{r.document.filename}</div>
                      <div className="result-category">📁 {r.document.category}</div>
                    </div>
                    <div className="result-score">
                      <div className="score-bar">
                        <div className="score-fill"
                          style={{ width: `${(r.similarity * 100).toFixed(0)}%` }} />
                      </div>
                      <div className="score-pct">{(r.similarity * 100).toFixed(1)}%</div>
                    </div>
                    <div className="result-open">↗</div>
                  </div>
                ))}
              </div>
            )}

            {!searching && searchResults.length === 0 && query && (
              <div className="empty-state">
                <div className="empty-icon">⌕</div>
                <h3>No results found</h3>
                <p>Try different search terms</p>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}