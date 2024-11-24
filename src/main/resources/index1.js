let contacts = [];
let currentContactId = null;

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    fetchContacts();
});

// 获取所有联系人
async function fetchContacts() {
    try {
        const response = await fetch('http://localhost:8080/api/contacts');
        contacts = await response.json();
        renderContacts(contacts);
    } catch (error) {
        alert('获取联系人失败');
    }
}

// 渲染联系人列表
function renderContacts(contactsToRender) {
    const contactsList = document.getElementById('contactsList');
    contactsList.innerHTML = contactsToRender.map(contact => `
        <div class="contact-card">
            <div class="contact-header">
                <div class="contact-names">${contact.names.join(', ')}</div>
                <div class="contact-actions">
                    <button onclick="toggleFavorite(${contact.id})" class="btn">
                        ${contact.favorite ? '★' : '☆'}
                    </button>
                    <button onclick="editContact(${contact.id})" class="btn">编辑</button>
                    <button onclick="deleteContact(${contact.id})" class="btn">删除</button>
                </div>
            </div>
            <div>电话：${contact.phoneNumbers.join(', ')}</div>
            <div>邮箱：${contact.email || '-'}</div>
            <div>地址：${contact.location || '-'}</div>
            <div>社交账号：${contact.mediaHandles || '-'}</div>
        </div>
    `).join('');
}

// 显示添加对话框
function showAddDialog() {
    currentContactId = null;
    document.getElementById('dialogTitle').textContent = '添加联系人';
    document.getElementById('contactForm').reset();
    document.getElementById('contactDialog').style.display = 'flex';
    // 重置名字和电话输入框
    document.getElementById('namesContainer').innerHTML = `
        <div class="input-group">
            <input type="text" class="name-input">
            <button type="button" onclick="removeName(this)">删除</button>
        </div>
    `;
    document.getElementById('phonesContainer').innerHTML = `
        <div class="input-group">
            <input type="text" class="phone-input">
            <button type="button" onclick="removePhone(this)">删除</button>
        </div>
    `;
}

// 关闭对话框
function closeDialog() {
    document.getElementById('contactDialog').style.display = 'none';
}

// 添加名字输入框
function addNameField() {
    const container = document.getElementById('namesContainer');
    const div = document.createElement('div');
    div.className = 'input-group';
    div.innerHTML = `
        <input type="text" class="name-input">
        <button type="button" onclick="removeName(this)">删除</button>
    `;
    container.appendChild(div);
}

// 添加电话输入框
function addPhoneField() {
    const container = document.getElementById('phonesContainer');
    const div = document.createElement('div');
    div.className = 'input-group';
    div.innerHTML = `
        <input type="text" class="phone-input">
        <button type="button" onclick="removePhone(this)">删除</button>
    `;
    container.appendChild(div);
}

// 删除名字输入框
function removeName(button) {
    const container = document.getElementById('namesContainer');
    if (container.children.length > 1) {
        button.parentElement.remove();
    }
}

// 删除电话输入框
function removePhone(button) {
    const container = document.getElementById('phonesContainer');
    if (container.children.length > 1) {
        button.parentElement.remove();
    }
}

// 保存联系人
async function saveContact() {
    const names = Array.from(document.getElementsByClassName('name-input'))
        .map(input => input.value.trim())
        .filter(name => name);

    const phoneNumbers = Array.from(document.getElementsByClassName('phone-input'))
        .map(input => input.value.trim())
        .filter(phone => phone);

    const contact = {
        names,
        phoneNumbers,
        email: document.getElementById('emailInput').value,
        location: document.getElementById('locationInput').value,
        mediaHandles: document.getElementById('mediaHandlesInput').value,
        favorite: document.getElementById('favoriteInput').checked
    };

    try {
        const url = currentContactId
            ? `http://localhost:8080/api/contacts/${currentContactId}`
            : 'http://localhost:8080/api/contacts';

        const response = await fetch(url, {
            method: currentContactId ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(contact)
        });

        if (response.ok) {
            closeDialog();
            fetchContacts();
        } else {
            alert('保存失败');
        }
    } catch (error) {
        alert('保存失败');
    }
}

// 编辑联系人
async function editContact(id) {
    currentContactId = id;
    const contact = contacts.find(c => c.id === id);
    if (!contact) return;

    document.getElementById('dialogTitle').textContent = '编辑联系人';

    // 设置名字
    document.getElementById('namesContainer').innerHTML = contact.names.map(name => `
        <div class="input-group">
            <input type="text" class="name-input" value="${name}">
            <button type="button" onclick="removeName(this)">删除</button>
        </div>
    `).join('');

    // 设置电话
    document.getElementById('phonesContainer').innerHTML = contact.phoneNumbers.map(phone => `
        <div class="input-group">
            <input type="text" class="phone-input" value="${phone}">
            <button type="button" onclick="removePhone(this)">删除</button>
        </div>
    `).join('');

    document.getElementById('emailInput').value = contact.email || '';
    document.getElementById('locationInput').value = contact.location || '';
    document.getElementById('mediaHandlesInput').value = contact.mediaHandles || '';
    document.getElementById('favoriteInput').checked = contact.favorite;

    document.getElementById('contactDialog').style.display = 'flex';
}

// 删除联系人
async function deleteContact(id) {
    if (!confirm('确定要删除这个联系人吗？')) return;

    try {
        const response = await fetch(`http://localhost:8080/api/contacts/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            fetchContacts();
        } else {
            alert('删除失败');
        }
    } catch (error) {
        alert('删除失败');
    }
}

// 切换收藏状态
async function toggleFavorite(id) {
    const contact = contacts.find(c => c.id === id);
    if (!contact) return;

    try {
        const response = await fetch(`http://localhost:8080/api/contacts/${id}/favorite?favorite=${!contact.favorite}`, {
            method: 'PUT'
        });

        if (response.ok) {
            contact.favorite = !contact.favorite;
            renderContacts(contacts);
        }
    } catch (error) {
        alert('操作失败');
    }
}

// 显示收藏联系人
function showFavorites() {
    const favorites = contacts.filter(contact => contact.favorite);
    renderContacts(favorites);
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
}

// 显示所有联系人
function showAllContacts() {
    renderContacts(contacts);
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
}

// 搜索联系人
function searchContacts() {
    const keyword = document.getElementById('searchInput').value.toLowerCase();
    const filtered = contacts.filter(contact =>
        contact.names.some(name => name.toLowerCase().includes(keyword)) ||
        contact.phoneNumbers.some(phone => phone.includes(keyword))
    );
    renderContacts(filtered);
}

// 导出联系人
async function exportContacts() {
    try {
        const response = await fetch('http://localhost:8080/api/contacts/export', {
            method: 'GET',
        });

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'contacts.xlsx';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    } catch (error) {
        alert('导出失败');
    }
}

// 导入联系人
async function importContacts(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('http://localhost:8080/api/contacts/import', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            alert('导入成功');
            fetchContacts();
        } else {
            alert('导入失败');
        }
    } catch (error) {
        alert('导入失败');
    }
}