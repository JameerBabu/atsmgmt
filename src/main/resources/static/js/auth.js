function logout() {
    // Clear all auth data
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('lastLogin');
    
    // Redirect to login page
    window.location.href = '/login.html';
}

// Check if user is authenticated
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

// Add this to all protected pages
document.addEventListener('DOMContentLoaded', checkAuth); 