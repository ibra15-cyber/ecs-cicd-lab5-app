// Configuration
// const API_BASE_URL = 'http://localhost:8080/api/photos';
const API_BASE_URL = '/api/photos';


// Global variables
let allPhotos = [];
let filteredPhotos = [];

// Initialize the app
document.addEventListener('DOMContentLoaded', function() {
    setupEventListeners();
    loadPhotos();
});

// Setup event listeners
function setupEventListeners() {
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', handleSearch);

    // File upload
    const fileInput = document.getElementById('fileInput');
    const fileDropZone = document.getElementById('fileDropZone');

    fileInput.addEventListener('change', handleFileSelect);

    // Drag and drop
    fileDropZone.addEventListener('dragover', handleDragOver);
    fileDropZone.addEventListener('dragleave', handleDragLeave);
    fileDropZone.addEventListener('drop', handleFileDrop);

    // Description character count
    const description = document.getElementById('description');
    description.addEventListener('input', updateCharCount);

    // Upload form
    const uploadForm = document.getElementById('uploadForm');
    uploadForm.addEventListener('submit', handleUpload);

    // Close modals on outside click
    document.getElementById('uploadModal').addEventListener('click', function(e) {
        if (e.target === this) closeUploadModal();
    });

    document.getElementById('photoModal').addEventListener('click', function(e) {
        if (e.target === this) closePhotoModal();
    });
}

// Load photos from API
async function loadPhotos() {
    try {
        showLoadingState();
        const response = await fetch(API_BASE_URL);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        allPhotos = await response.json();
        filteredPhotos = [...allPhotos];
        renderPhotos();

    } catch (error) {
        console.error('Error loading photos:', error);
        showNotification('Failed to load photos. Please try again.', 'error');
        showEmptyState();
    }
}

// Render photos in the gallery
function renderPhotos() {
    const container = document.getElementById('galleryContainer');
    const emptyState = document.getElementById('emptyState');

    if (filteredPhotos.length === 0) {
        container.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    container.innerHTML = filteredPhotos.map(photo => `
                <div class="photo-card fade-in">
                    <img class="photo-image"
                         src="${photo.presignedUrl}"
                         alt="${photo.description}"
                         onclick="openPhotoModal(${photo.id})"
                         onerror="handleImageError(this)">
                    <div class="photo-info">
                        <div class="photo-description">${photo.description}</div>
                        <div class="photo-meta">
                            <span>${photo.timeAgo}</span>
                            <span>${photo.fileSizeFormatted}</span>
                        </div>
                        <div class="photo-actions">
                            <button class="btn btn-secondary btn-small" onclick="editPhoto(${photo.id})">
                                ✏️ Edit
                            </button>
                            <button class="btn btn-secondary btn-small" onclick="deletePhoto(${photo.id})">
                                🗑️ Delete
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
}

// Handle search
function handleSearch(event) {
    const query = event.target.value.toLowerCase().trim();

    if (!query) {
        filteredPhotos = [...allPhotos];
    } else {
        filteredPhotos = allPhotos.filter(photo =>
            photo.description.toLowerCase().includes(query) ||
            (photo.tags && photo.tags.toLowerCase().includes(query)) ||
            (photo.location && photo.location.toLowerCase().includes(query)) ||
            (photo.category && photo.category.toLowerCase().includes(query))
        );
    }

    renderPhotos();
}

// File handling
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        displayFilePreview(file);
    }
}

function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

function handleDragLeave(event) {
    event.currentTarget.classList.remove('dragover');
}

function handleFileDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');

    const files = event.dataTransfer.files;
    if (files.length > 0) {
        const file = files[0];
        if (file.type.startsWith('image/')) {
            document.getElementById('fileInput').files = files;
            displayFilePreview(file);
        } else {
            showNotification('Please select an image file.', 'error');
        }
    }
}

function displayFilePreview(file) {
    const preview = document.getElementById('filePreview');
    const previewImage = document.getElementById('previewImage');
    const fileInfo = document.getElementById('fileInfo');

    const reader = new FileReader();
    reader.onload = function(e) {
        previewImage.src = e.target.result;
        fileInfo.textContent = `${file.name} (${formatFileSize(file.size)})`;
        preview.style.display = 'block';
    };
    reader.readAsDataURL(file);
}

// Upload handling
async function handleUpload(event) {
    event.preventDefault();

    const formData = new FormData();
    const fileInput = document.getElementById('fileInput');
    const descriptionEl = document.getElementById('description');
    const tagsEl = document.getElementById('tags');
    const locationEl = document.getElementById('location');
    const categoryEl = document.getElementById('category');

    // Debug: Check if elements exist
    console.log('Form elements:', {
        fileInput: fileInput,
        description: descriptionEl,
        tags: tagsEl,
        location: locationEl,
        category: categoryEl
    });

    if (!descriptionEl) {
        showNotification('Form error: Description field not found', 'error');
        return;
    }

    const description = descriptionEl.value.trim();
    const tags = tagsEl ? tagsEl.value.trim() : '';
    const location = locationEl ? locationEl.value.trim() : '';
    const category = categoryEl ? categoryEl.value.trim() : '';

    if (!fileInput.files[0]) {
        showNotification('Please select a file to upload.', 'error');
        return;
    }

    if (!description) {
        showNotification('Please provide a description.', 'error');
        return;
    }

    formData.append('file', fileInput.files[0]);
    formData.append('description', description);
    if (tags) formData.append('tags', tags);
    if (location) formData.append('location', location);
    if (category) formData.append('category', category);

    const uploadBtn = document.getElementById('uploadBtn');
    const uploadBtnText = document.getElementById('uploadBtnText');

    try {
        uploadBtn.disabled = true;
        uploadBtnText.innerHTML = '<span class="loading"><span class="spinner"></span>Uploading...</span>';

        const response = await fetch(`${API_BASE_URL}/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! status: ${response.status}`);
        }

        const newPhoto = await response.json();
        showNotification('Photo uploaded successfully!', 'success');
        closeUploadModal();
        resetUploadForm();

        // Add new photo to the beginning of the array
        allPhotos.unshift(newPhoto);
        filteredPhotos = [...allPhotos];
        renderPhotos();

    } catch (error) {
        console.error('Error uploading photo:', error);
        showNotification('Failed to upload photo. Please try again.', 'error');
    } finally {
        uploadBtn.disabled = false;
        uploadBtnText.textContent = 'Upload Photo';
    }
}

// Photo management
async function deletePhoto(id) {
    if (!confirm('Are you sure you want to delete this photo? This action cannot be undone.')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // Remove photo from arrays
        allPhotos = allPhotos.filter(photo => photo.id !== id);
        filteredPhotos = filteredPhotos.filter(photo => photo.id !== id);
        renderPhotos();

        showNotification('Photo deleted successfully!', 'success');

    } catch (error) {
        console.error('Error deleting photo:', error);
        showNotification('Failed to delete photo. Please try again.', 'error');
    }
}

async function editPhoto(id) {
    const photo = allPhotos.find(p => p.id === id);
    if (!photo) return;

    const newDescription = prompt('Edit description:', photo.description);
    if (newDescription === null || newDescription.trim() === photo.description) {
        return;
    }

    if (!newDescription.trim()) {
        showNotification('Description cannot be empty.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${id}/description`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ description: newDescription.trim() })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const updatedPhoto = await response.json();

        // Update photo in arrays
        const updatePhotoInArray = (photos) => {
            const index = photos.findIndex(p => p.id === id);
            if (index !== -1) {
                photos[index] = updatedPhoto;
            }
        };

        updatePhotoInArray(allPhotos);
        updatePhotoInArray(filteredPhotos);
        renderPhotos();

        showNotification('Photo updated successfully!', 'success');

    } catch (error) {
        console.error('Error updating photo:', error);
        showNotification('Failed to update photo. Please try again.', 'error');
    }
}

// Modal functions
function openUploadModal() {
    document.getElementById('uploadModal').classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeUploadModal() {
    document.getElementById('uploadModal').classList.remove('show');
    document.body.style.overflow = 'auto';
}

function openPhotoModal(id) {
    const photo = allPhotos.find(p => p.id === id);
    if (!photo) return;

    const modalImage = document.getElementById('modalImage');
    const modalInfo = document.getElementById('modalInfo');

    modalImage.src = photo.presignedUrl;
    modalImage.alt = photo.description;

    modalInfo.innerHTML = `
                <h3 style="color: var(--text-primary); margin-bottom: 0.5rem;">${photo.description}</h3>
                <div style="margin-bottom: 0.5rem;">
                    <strong>Uploaded:</strong> ${photo.timeAgo} |
                    <strong>Size:</strong> ${photo.fileSizeFormatted}
                </div>
                ${photo.tags ? `<div style="margin-bottom: 0.5rem;"><strong>Tags:</strong> ${photo.tags}</div>` : ''}
                ${photo.location ? `<div style="margin-bottom: 0.5rem;"><strong>Location:</strong> ${photo.location}</div>` : ''}
                ${photo.category ? `<div><strong>Category:</strong> ${photo.category}</div>` : ''}
            `;

    document.getElementById('photoModal').classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closePhotoModal() {
    document.getElementById('photoModal').classList.remove('show');
    document.body.style.overflow = 'auto';
}

// Utility functions
function refreshGallery() {
    const refreshBtn = document.getElementById('refreshBtn');
    const originalText = refreshBtn.innerHTML;

    refreshBtn.innerHTML = '<span class="loading"><span class="spinner"></span>Refreshing...</span>';
    refreshBtn.disabled = true;

    loadPhotos().finally(() => {
        refreshBtn.innerHTML = originalText;
        refreshBtn.disabled = false;
    });
}

function resetUploadForm() {
    document.getElementById('uploadForm').reset();
    document.getElementById('filePreview').style.display = 'none';
    updateCharCount();
}

function updateCharCount() {
    const description = document.getElementById('description');
    const charCount = document.getElementById('charCount');
    charCount.textContent = description.value.length;
}

function showLoadingState() {
    document.getElementById('galleryContainer').innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 4rem;">
                    <div class="loading" style="justify-content: center; font-size: 1.125rem;">
                        <div class="spinner" style="width: 24px; height: 24px;"></div>
                        Loading photos...
                    </div>
                </div>
            `;
}

function showEmptyState() {
    document.getElementById('galleryContainer').innerHTML = '';
    document.getElementById('emptyState').style.display = 'block';
}

function handleImageError(img) {
    img.style.display = 'none';
    const card = img.closest('.photo-card');
    if (card) {
        const placeholder = document.createElement('div');
        placeholder.className = 'photo-image';
        placeholder.style.cssText = `
                    display: flex; align-items: center; justify-content: center;
                    background: var(--surface-color); color: var(--text-muted);
                    font-size: 3rem; cursor: pointer;
                `;
        placeholder.textContent = '🖼️';
        placeholder.title = 'Image failed to load';
        img.parentNode.insertBefore(placeholder, img);
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function showNotification(message, type = 'info') {
    const notification = document.getElementById('notification');
    notification.textContent = message;
    notification.className = `notification ${type}`;
    notification.classList.add('show');

    setTimeout(() => {
        notification.classList.remove('show');
    }, 4000);
}

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // ESC key to close modals
    if (e.key === 'Escape') {
        if (document.getElementById('uploadModal').classList.contains('show')) {
            closeUploadModal();
        }
        if (document.getElementById('photoModal').classList.contains('show')) {
            closePhotoModal();
        }
    }

    // Ctrl/Cmd + U to open upload modal
    if ((e.ctrlKey || e.metaKey) && e.key === 'u') {
        e.preventDefault();
        openUploadModal();
    }

    // Ctrl/Cmd + R to refresh (prevent default and use custom refresh)
    if ((e.ctrlKey || e.metaKey) && e.key === 'r') {
        e.preventDefault();
        refreshGallery();
    }
});

// Handle browser back/forward buttons
window.addEventListener('popstate', function(e) {
    // Close any open modals when user navigates back
    closeUploadModal();
    closePhotoModal();
});

// Auto-refresh gallery every 5 minutes
setInterval(() => {
    if (document.visibilityState === 'visible') {
        loadPhotos();
    }
}, 5 * 60 * 1000);

// Refresh when tab becomes visible
document.addEventListener('visibilitychange', function() {
    if (document.visibilityState === 'visible') {
        loadPhotos();
    }
});