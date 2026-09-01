const API_BASE = '/api/v1/urls';
let allUrlsData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadUrls();
});

async function loadUrls() {
    const tableBody = document.getElementById('urlTableBody');
    try {
        const response = await fetch(API_BASE);
        if (!response.ok) throw new Error('Veriler alınamadı');
        
        allUrlsData = await response.json();
        renderTable(allUrlsData);
        updateOverallStats(allUrlsData);
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-state" style="color: #ef4444;">
                    <i class="fa-solid fa-triangle-exclamation"></i> Bağlantı hatası: Sunucu yanıt vermiyor.
                </td>
            </tr>`;
    }
}

function renderTable(urls) {
    const tableBody = document.getElementById('urlTableBody');
    if (!urls || urls.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="loading-state">
                    Henüz hiç kısa link oluşturulmadı. Yukarıdaki formdan ilk linkinizi kısaltın!
                </td>
            </tr>`;
        return;
    }

    tableBody.innerHTML = urls.map(item => `
        <tr>
            <td>
                <a href="${item.shortUrl}" target="_blank" class="link-short">${item.shortCode}</a>
            </td>
            <td>
                <span class="link-original" title="${item.originalUrl}">${item.originalUrl}</span>
            </td>
            <td>
                <span class="badge-clicks"><i class="fa-solid fa-mouse-pointer"></i> ${item.clickCount}</span>
            </td>
            <td>${formatDate(item.createdAt)}</td>
            <td>
                <span class="badge-active">Aktif</span>
            </td>
            <td>
                <div class="actions-cell">
                    <button onclick="copyText('${item.shortUrl}')" class="btn-icon" title="Kopyala">
                        <i class="fa-regular fa-copy"></i>
                    </button>
                    <button onclick="openAnalyticsModal('${item.shortCode}')" class="btn-icon" title="Analizler">
                        <i class="fa-solid fa-chart-simple"></i>
                    </button>
                    <button onclick="deleteUrl('${item.shortCode}')" class="btn-icon delete" title="Sil">
                        <i class="fa-regular fa-trash-can"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updateOverallStats(urls) {
    document.getElementById('statTotalLinks').innerText = urls.length;
    const totalClicks = urls.reduce((sum, u) => sum + (u.clickCount || 0), 0);
    document.getElementById('statTotalClicks').innerText = totalClicks;
}

async function handleShorten(e) {
    e.preventDefault();
    const originalUrl = document.getElementById('originalUrl').value;
    const customAlias = document.getElementById('customAlias').value;
    const expirationDays = document.getElementById('expirationDays').value;

    const btn = document.getElementById('shortenBtn');
    btn.disabled = true;
    btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> İşleniyor...`;

    try {
        const payload = {
            originalUrl: originalUrl,
            customAlias: customAlias ? customAlias : null,
            expirationDays: expirationDays ? parseInt(expirationDays) : null
        };

        const res = await fetch(`${API_BASE}/shorten`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Link kısaltma hatası');

        // Show Result
        document.getElementById('resultShortUrl').value = data.shortUrl;
        document.getElementById('resultOpenBtn').href = data.shortUrl;
        document.getElementById('resultCard').classList.remove('hidden');

        showToast('Link başarıyla kısaltıldı!');
        document.getElementById('shortenForm').reset();
        loadUrls();
    } catch (err) {
        showToast(err.message, true);
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<span>Link Kısalt</span> <i class="fa-solid fa-wand-magic-sparkles"></i>`;
    }
}

async function openAnalyticsModal(shortCode) {
    const modal = document.getElementById('analyticsModal');
    const tableBody = document.getElementById('analyticsTableBody');
    tableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;"><i class="fa-solid fa-spinner fa-spin"></i> Yükleniyor...</td></tr>`;
    modal.classList.remove('hidden');

    try {
        const res = await fetch(`${API_BASE}/analytics/${shortCode}`);
        if (!res.ok) throw new Error('Analitik bilgisi alınamadı');
        
        const data = await res.json();
        document.getElementById('modalShortCode').innerText = data.shortCode;
        document.getElementById('modalTotalClicks').innerText = data.totalClicks;
        document.getElementById('modalOriginalUrl').innerText = data.originalUrl;
        document.getElementById('modalOriginalUrl').href = data.originalUrl;

        if (!data.recentClicks || data.recentClicks.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="3" style="text-align:center; color:#64748b;">Henüz tıklama kaydı yok. Kısa linke tıklayarak test edin!</td></tr>`;
            return;
        }

        tableBody.innerHTML = data.recentClicks.map(c => `
            <tr>
                <td>${formatDate(c.clickedAt)}</td>
                <td><code>${c.ipAddress || '127.0.0.1'}</code></td>
                <td><small style="color:#94a3b8;">${c.userAgent ? c.userAgent.substring(0, 45) + '...' : 'Bilinmiyor'}</small></td>
            </tr>
        `).join('');
    } catch (err) {
        showToast(err.message, true);
        closeModal();
    }
}

function closeModal() {
    document.getElementById('analyticsModal').classList.add('hidden');
}

async function deleteUrl(shortCode) {
    if (!confirm(`${shortCode} kısa kodlu linki silmek istediğinizden emin misiniz?`)) return;

    try {
        const res = await fetch(`${API_BASE}/${shortCode}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Silme işlemi başarısız');
        showToast('Link başarıyla silindi.');
        loadUrls();
    } catch (err) {
        showToast(err.message, true);
    }
}

function filterUrls() {
    const query = document.getElementById('searchFilter').value.toLowerCase();
    const filtered = allUrlsData.filter(u => 
        u.shortCode.toLowerCase().includes(query) || 
        u.originalUrl.toLowerCase().includes(query)
    );
    renderTable(filtered);
}

function copyToClipboard(inputId) {
    const input = document.getElementById(inputId);
    input.select();
    navigator.clipboard.writeText(input.value);
    showToast('Kısa URL panoya kopyalandı! 📋');
}

function copyText(text) {
    navigator.clipboard.writeText(text);
    showToast('Kısa URL panoya kopyalandı! 📋');
}

function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.style.borderColor = isError ? '#ef4444' : '#6366f1';
    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('tr-TR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
